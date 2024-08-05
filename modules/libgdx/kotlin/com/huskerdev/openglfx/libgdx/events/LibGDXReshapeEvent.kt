package com.huskerdev.openglfx.libgdx.events

import com.badlogic.gdx.Application
import com.huskerdev.openglfx.canvas.events.GLReshapeEvent

class LibGDXReshapeEvent(
    override val application: Application,
    width: Int,
    height: Int
): GLReshapeEvent(ANY, width, height), LibGDXEvent