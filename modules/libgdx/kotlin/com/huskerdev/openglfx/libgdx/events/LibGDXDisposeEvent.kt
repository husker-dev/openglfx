package com.huskerdev.openglfx.libgdx.events

import com.badlogic.gdx.Application
import com.huskerdev.openglfx.canvas.events.GLDisposeEvent

class LibGDXDisposeEvent(
    override val application: Application
): GLDisposeEvent(ANY), LibGDXEvent