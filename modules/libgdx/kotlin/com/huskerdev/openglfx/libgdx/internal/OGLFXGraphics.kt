package com.huskerdev.openglfx.libgdx.internal

import com.badlogic.gdx.AbstractGraphics
import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Graphics
import com.badlogic.gdx.Graphics.DisplayMode
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3GL32
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.Cursor.SystemCursor.*
import com.badlogic.gdx.graphics.glutils.GLVersion
import com.huskerdev.openglfx.canvas.GLCanvas
import com.huskerdev.openglfx.canvas.GLCanvasAnimator
import javafx.scene.ImageCursor
import javafx.stage.Screen

class OGLFXGraphics(val canvas: GLCanvas): AbstractGraphics() {
    private var gl20: GL20? = null
    private var gl30: GL30? = null
    private var gl31: GL31? = null
    private var gl32: GL32? = null

    init {
        gl32 = Lwjgl3GL32()
        gl31 = gl32
        gl30 = gl32
        gl20 = gl32

        Gdx.gl32 = gl32
        Gdx.gl31 = gl31
        Gdx.gl30 = gl30
        Gdx.gl20 = gl20
        Gdx.gl = Gdx.gl32
    }

    override fun isGL30Available() = true
    override fun isGL31Available() = true
    override fun isGL32Available() = true

    override fun getGL20() = gl20
    override fun getGL30() = gl30
    override fun getGL31() = gl31
    override fun getGL32() = gl32

    override fun setGL20(gl20: GL20?) { this.gl20 = gl20 }
    override fun setGL30(gl30: GL30?) { this.gl30 = gl30 }
    override fun setGL31(gl31: GL31?) { this.gl31 = gl31 }
    override fun setGL32(gl32: GL32?) { this.gl32 = gl32 }

    override fun getWidth() = canvas.scaledWidth
    override fun getHeight() = canvas.scaledHeight
    override fun getBackBufferWidth() = canvas.scaledWidth
    override fun getBackBufferHeight() = canvas.scaledHeight

    override fun getSafeInsetLeft() = 0
    override fun getSafeInsetTop() = 0
    override fun getSafeInsetBottom() = 0
    override fun getSafeInsetRight() = 0

    override fun getFrameId() = canvas.fpsCounter.frameId
    override fun getDeltaTime() = canvas.fpsCounter.delta.toFloat()
    override fun getFramesPerSecond() = canvas.fpsCounter.currentFps

    override fun getType() = Graphics.GraphicsType.LWJGL3

    override fun getGLVersion() = GLVersion(Application.ApplicationType.Desktop, "3.0 GL",
        "openglfx", "openglfx")

    override fun getPpiX() = 96f
    override fun getPpiY() = 96f

    override fun getPpcX() = 96f
    override fun getPpcY() = 96f

    override fun supportsDisplayModeChange() = false

    override fun getPrimaryMonitor() = FXMonitorWrapper(Screen.getPrimary())

    override fun getMonitor(): Graphics.Monitor {
        val stage = canvas.scene.window
        return FXMonitorWrapper(Screen.getScreensForRectangle(stage.x, stage.y, stage.width, stage.height)[0])
    }

    override fun getMonitors(): Array<Graphics.Monitor> {
        val stage = canvas.scene.window
        return Screen.getScreensForRectangle(stage.x, stage.y, stage.width, stage.height)
            .map { FXMonitorWrapper(it) }.toTypedArray()
    }

    override fun getDisplayModes() = arrayOf(getDisplayMode())
    override fun getDisplayModes(monitor: Graphics.Monitor?) = arrayOf(getDisplayMode())
    override fun getDisplayMode() = object: DisplayMode(canvas.scaledWidth, canvas.scaledHeight, 0, 32) {}
    override fun getDisplayMode(monitor: Graphics.Monitor?) = getDisplayMode()

    override fun setFullscreenMode(displayMode: DisplayMode?) = false
    override fun setWindowedMode(width: Int, height: Int) = false
    override fun setTitle(title: String?) {}
    override fun setUndecorated(undecorated: Boolean) {}
    override fun setResizable(resizable: Boolean) {}
    override fun setVSync(vsync: Boolean) {}
    override fun setForegroundFPS(fps: Int) {}

    override fun getBufferFormat() =
        Graphics.BufferFormat(32, 32, 32, 32, 32, 32, 16, false)

    override fun supportsExtension(extension: String?) = true

    override fun setContinuousRendering(isContinuous: Boolean) {
        canvas.fps = 0
    }

    override fun isContinuousRendering() = canvas.fps == 0

    override fun requestRendering() {
        canvas.repaint()
    }

    override fun isFullscreen() = false

    override fun newCursor(pixmap: Pixmap, xHotspot: Int, yHotspot: Int) =
        FXCursorWrapper(pixmap, xHotspot, yHotspot)

    override fun setCursor(cursor: Cursor?) {
        canvas.cursor = if(cursor != null) (cursor as FXCursorWrapper).imageCursor else null
    }

    override fun setSystemCursor(systemCursor: Cursor.SystemCursor) {
        canvas.cursor = when(systemCursor) {
            Arrow, Ibeam, NotAllowed, None -> javafx.scene.Cursor.DEFAULT
            Crosshair -> javafx.scene.Cursor.CROSSHAIR
            Hand -> javafx.scene.Cursor.HAND
            HorizontalResize -> javafx.scene.Cursor.H_RESIZE
            VerticalResize -> javafx.scene.Cursor.V_RESIZE
            NWSEResize -> javafx.scene.Cursor.NW_RESIZE
            NESWResize -> javafx.scene.Cursor.NE_RESIZE
            AllResize -> javafx.scene.Cursor.MOVE
        }
    }

    class FXCursorWrapper(pixmap: Pixmap, xHotspot: Int, yHotspot: Int) : Cursor {
        val imageCursor = ImageCursor(FXPixmapImage(pixmap), xHotspot.toDouble(), yHotspot.toDouble())
        override fun dispose() {}
    }

    class FXMonitorWrapper internal constructor(screen: Screen) :
        Graphics.Monitor(screen.bounds.width.toInt(), screen.bounds.height.toInt(), "Unnamed screen")

}