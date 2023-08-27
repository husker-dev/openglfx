# OpenGLFX
<a href="LICENSE"><img src="https://img.shields.io/github/license/husker-dev/openglfx?style=flat-square"></a>
<a href="https://github.com/husker-dev/openglfx/releases/latest"><img src="https://img.shields.io/github/v/release/husker-dev/openglfx?style=flat-square"></a>

Embedded OpenGL node for rendering using LWJGL and JOGL with the best performance available.

Written on Kotlin with Java compatibility.

## Screenshots

<p>
<img src="https://user-images.githubusercontent.com/31825139/129398976-f1317b23-5583-47e9-ab1c-d12eea54d4ab.gif" height="280"/>
<img src="https://user-images.githubusercontent.com/31825139/131416822-b90bb974-583c-48a2-ae47-8e0022fd5229.gif" height="280"/>
</p>

# Usage

> **NOTE:** All examples are written in Kotlin, Gradle and LWJGL. If you want to use JOGL/Java/Maven, you can use [example code generator](https://husker-dev.github.io/husker-dev/?page=tools/openglfx).

### Dependency
```groovy
repositories {
    // ...
    maven { url 'https://jitpack.io' }
}

dependencies {
    // ...JavaFX and LWJGL libraries...
    implementation 'com.github.husker-dev.openglfx:core:3.0.5'
    implementation 'com.github.husker-dev.openglfx:lwjgl:3.0.5'
}
```

### Creation

This library adds only one component - ```OpenGLCanvas```, that can be used like a regular element in JavaFX.

```kotlin
import com.huskerdev.openglfx.OpenGLCanvas
import com.huskerdev.openglfx.lwjgl.LWJGLExecutor.Companion.LWJGL_MODULE

val canvas = OpenGLCanvas.create(LWJGL_MODULE)
// or
val canvas = OpenGLCanvas.create(LWJGL_MODULE, CORE_PROFILE) // For Core OpenGL profile
```

### Rendering

```OpenGLCanvas``` uses a logic similar to JOGL. The component has events in which you need to render content.

```kotlin
// JOGL only: Use the following code in each event to get the GL object
// val gl = (event as JOGLEvent).gl

canvas.addOnInitEvent { event ->
    // Init some gl properties only at start
}

canvas.addOnRenderEvent { event ->
    val fps = event.fps
    val delta = event.delta
    val width = event.width
    val height = event.height
    // Render some content
}

canvas.addOnReshapeEvent { event ->
    val width = event.width
    val height = event.height
    // Changing viewport and matrices
}

canvas.addOnDisposeEvent { event ->
    // Clear some native data
}
```

### Auto repaint
If you need to update content with a certain FPS, then you should use ```GLCanvasAnimator```. Keep in mind that JavaFX can limits the refresh rate.

```kotlin
import com.huskerdev.openglfx.GLCanvasAnimator

canvas.animator = GLCanvasAnimator(60.0) 
canvas.animator = GLCanvasAnimator(GLCanvasAnimator.UNLIMITED_FPS) // For maximum available FPS
canvas.animator = null // To remove animator
```

Don't forget to disable VSync before JavaFX initialization if you want to get FPS more than monitor's frequency.
```kotlin
System.setProperty("prism.vsync", "false")
```

# Notes
- JOGL can't initialize on macOS ([#22](https://github.com/husker-dev/openglfx/issues/22))
> If you know how to fix that problem I would be very happy

### Reflections opens
```
--add-opens javafx.base/com.sun.javafx=ALL-UNNAMED
--add-opens javafx.graphics/com.sun.prism=ALL-UNNAMED
--add-opens javafx.graphics/com.sun.prism.d3d=ALL-UNNAMED
--add-opens javafx.graphics/com.sun.javafx.scene.layout=ALL-UNNAMED
--add-opens javafx.graphics/com.sun.javafx.scene=ALL-UNNAMED
--add-opens javafx.graphics/com.sun.javafx.sg.prism=ALL-UNNAMED
--add-opens javafx.graphics/com.sun.javafx.tk=ALL-UNNAMED
--add-opens javafx.graphics/com.sun.javafx.geom=ALL-UNNAMED
--add-opens javafx.graphics/javafx.scene.image=ALL-UNNAMED
```

# Under the hood

### Overview
  | Node events | Path |
  | ----------- | ---- |
  | created     | → ```New offscreen GL context``` → ```GLExecutor transforms``` → Init event |
  | repainted   | → ```GLExecutor transforms``` → Render event → ```Share textures``` → JavaFX paint |
  | resized     | → ```GLExecutor transforms``` → Reshape event |
  | disposed    | → ```GLExecutor transforms``` → Dispose event |

- ### Offscreen GL
  [husker-dev/offscreen-jgl](https://github.com/husker-dev/offscreen-jgl) is used to create offscreen thread-independent GL context on Windows, MacOS and Linux.

- ### GLExecutor
  Executors are the bridges from OpenGLFX inner logic to outer libraries like LWJGL or JOGL.
  
  |          | LWJGL  | JOGL |
  | -------- | ------ | ---- |
  |  Class   | [LWJGLExecutor.kt](https://github.com/husker-dev/openglfx/blob/master/lwjgl/src/main/kotlin/com/huskerdev/openglfx/lwjgl/LWJGLExecutor.kt)  | [JOGLFXExecutor.kt](https://github.com/husker-dev/openglfx/blob/master/jogl/src/main/kotlin/com/huskerdev/openglfx/jogl/JOGLFXExecutor.kt)  |
  | Instance | LWJGL_MODULE | JOGL_MODULE |

  If you want to add new OpenGL library, just create your implementation of [GLExecutor](https://github.com/husker-dev/openglfx/blob/master/core/src/main/kotlin/com/huskerdev/openglfx/core/GLExecutor.kt) and use it as existing one: ```OpenGLCanvas.create(YOUR_EXECUTOR_INSTANCE)```.

- ### Texture sharing

  To efficiently connect OpenGL and JavaFX, OpenGLFX uses some techniques based on OS.
  
  [InteropImpl]: https://github.com/husker-dev/openglfx/blob/master/core/src/main/kotlin/com/huskerdev/openglfx/core/implementations/InteropImpl.kt
  [SharedImpl]: https://github.com/husker-dev/openglfx/blob/master/core/src/main/kotlin/com/huskerdev/openglfx/core/implementations/SharedImpl.kt
  [UniversalImpl]: https://github.com/husker-dev/openglfx/blob/master/core/src/main/kotlin/com/huskerdev/openglfx/core/implementations/UniversalImpl.kt
  
  |             | Description | Implementation |
  | ----------- | ----------- | -------------- |
  | Windows     | [NV_DX_interop](https://www.khronos.org/registry/OpenGL/extensions/NV/WGL_NV_DX_interop.txt) is used to synchronize textures between DirectX from JavaFX and OpenGL. | [InteropImpl.kt][InteropImpl]
  | Linux/MacOS | Creates a new GL context, that is shared with the JavaFX's one. Then renders to a JavaFX texture. | [SharedImpl.kt][SharedImpl]
  | Other       | Copies PixelBuffer from ```glReadPixels``` to JavaFX Image | [UniversalImpl.kt][UniversalImpl]


# Thanks to

- [James H Ball](https://github.com/jameshball) - macOS tester
- [Andrew Hamilton](https://github.com/orange451) - macOS tester, suggested new additions
