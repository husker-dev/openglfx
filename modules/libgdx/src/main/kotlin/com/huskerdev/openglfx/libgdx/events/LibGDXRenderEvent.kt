package com.huskerdev.openglfx.libgdx.events

import com.badlogic.gdx.Application
import com.huskerdev.openglfx.canvas.events.GLRenderEvent

class LibGDXRenderEvent(
    override val application: Application,
    fps: Int,
    delta: Double,
    width: Int,
    height: Int,
    fbo: Int): GLRenderEvent(ANY, fps, delta, width, height, fbo), LibGDXEvent