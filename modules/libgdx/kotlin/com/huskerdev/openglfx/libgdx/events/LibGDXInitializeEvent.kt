package com.huskerdev.openglfx.libgdx.events

import com.badlogic.gdx.Application
import com.huskerdev.openglfx.canvas.events.GLInitializeEvent

class LibGDXInitializeEvent(
    override val application: Application
): GLInitializeEvent(ANY), LibGDXEvent