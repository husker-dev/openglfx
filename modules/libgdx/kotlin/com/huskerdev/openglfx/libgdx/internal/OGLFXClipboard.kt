package com.huskerdev.openglfx.libgdx.internal

import com.badlogic.gdx.utils.Clipboard
import javafx.application.Platform
import javafx.scene.input.DataFormat
import java.util.concurrent.CompletableFuture
import java.util.function.Supplier

class OGLFXClipboard: Clipboard {

    private fun <T> runAndWaitFX(action: Supplier<T>): T {
        if (Platform.isFxApplicationThread())
            return action.get()

        val future = CompletableFuture<T>()
        Platform.runLater {
            future.complete(action.get())
        }
        return future.get()
    }

    override fun hasContents(): Boolean =
        getContents().isNotEmpty()

    override fun getContents(): String = runAndWaitFX {
        javafx.scene.input.Clipboard.getSystemClipboard().string
    }

    override fun setContents(content: String): Unit = runAndWaitFX {
        javafx.scene.input.Clipboard.getSystemClipboard().setContent(
            mapOf(DataFormat.PLAIN_TEXT to content)
        )
    }

}