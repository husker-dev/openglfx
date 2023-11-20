![Безымянный](https://github.com/husker-dev/openglfx/assets/31825139/3968c34e-ad83-4613-a5ce-1524aa79aa83)

### [Buy a coffe](https://www.buymeacoffee.com/huskerdev) for developer.

<a href="LICENSE"><img src="https://img.shields.io/github/license/husker-dev/openglfx?style=flat-square"></a>
<a href="https://github.com/husker-dev/openglfx/releases/latest"><img src="https://img.shields.io/github/v/release/husker-dev/openglfx?style=flat-square"></a>

# About

This library adds a new element to the JavaFX for rendering OpenGL graphics. It is optimized for each platform. Also, this library includes some auxiliary functions for working with OpenGL from JavaFX.

- [Dependency](#dependency)
- [Usage](#usage)
  - [Rendering events](#rendering-events)
  - [Antialiasing (MSAA)](#antialiasing-msaa)
  - [Async rendering](#async-rendering)
  - [Y-flipping](#y-flipping)
  - [OpenGL profile](#opengl-profile)
  - [Auto repaint](#auto-repaint)
  - [Image transfering](#image-transfering)
- [Notes](#notes)
  - [Reflection opens](#reflection-opens)
- [Under the hood](#under-the-hood)
- [Thanks to](#thanks-to)


> **NOTE:** All examples are written in Kotlin + Gradle + LWJGL. If you want to use Java/JOGL/Maven, you can use [example code generator](https://husker-dev.github.io/husker-dev/?page=tools/openglfx).

# Dependency
```groovy
dependencies {
    // ...JavaFX and LWJGL libraries...
    implementation 'com.huskerdev:openglfx:4.0'
    implementation 'com.huskerdev:openglfx-lwjgl:4.0'
}
```

# Usage

This library adds only one component - ```OpenGLCanvas```, that can be used like a regular element in JavaFX.

```kotlin
import com.huskerdev.openglfx.canvas.GLCanvas
import com.huskerdev.openglfx.lwjgl.LWJGLExecutor.Companion.LWJGL_MODULE

val canvas = GLCanvas.create(LWJGL_MODULE)
```

### Rendering events

```GLCanvas``` uses a logic similar to JOGL. The component has events where you can render the content.

```kotlin
canvas.addOnInitEvent { event ->
    // Init some gl properties only once
}

canvas.addOnRenderEvent { event ->
    // Render some content every frame
}

canvas.addOnReshapeEvent { event ->
    // Change viewport matrices at resizing
}

canvas.addOnDisposeEvent { event ->
    // Clear native data at disposing
}
```

### Antialiasing (MSAA)
```GLCanvas``` supports multi-sampling anti-aliasing. It can be enabled during creation.
For maximum possible MSAA level, specify -1.

```kotlin
GLCanvas.create(LWJGL_MODULE, msaa = 4)
```

### Async rendering
```GLCanvas``` can render graphics in a separated thread. This feature can either improve or decrease performance. Test it on your applications.

```kotlin
GLCanvas.create(LWJGL_MODULE, async = true)
```

### Y-flipping
By default, OpenGL draws the image upside down. ```GLCanvas``` has the ability to flip it without loss of performance. To do this, specify the parameter at startup.

```kotlin
GLCanvas.create(LWJGL_MODULE, flipY = true)
```

### OpenGL profile
OpenGL has two initialization profiles - ```Core``` and ```Compatibility```, you can read about them on the Internet. 

In GLCanvas you can specify the desired option. I advise you to always choose Core. 
To do this, you need to specify a parameter when creating.

```kotlin
GLCanvas.create(LWJGL_MODULE, profile = GLProfile.Core)
GLCanvas.create(LWJGL_MODULE, profile = GLProfile.Compatibility)
```

### Auto repaint
If you need to update content with a certain FPS, then you should use ```GLCanvasAnimator```. Keep in mind that JavaFX can limits the refresh rate.

```kotlin
import com.huskerdev.openglfx.canvas.GLCanvasAnimator

canvas.animator = GLCanvasAnimator(60.0) 
canvas.animator = GLCanvasAnimator(GLCanvasAnimator.UNLIMITED_FPS) // For maximum available FPS
canvas.animator = null // To remove animator
```

Don't forget to disable VSync before JavaFX initialization if you want to get FPS more than monitor's frequency.
```kotlin
System.setProperty("prism.vsync", "false")
```

### Image transfering
```openglfx``` has the ability to move images from JavaFX to OpenGL textures and vice versa. A special class is used for this:
```kotlin
val fbo = GLImageManager.toGL(image)

val image = GLImageManager.fromGL(fbo, width, height)
```

# Notes
- Async canvas blinks at resizing;
- JOGL can't initialize on macOS ([#22](https://github.com/husker-dev/openglfx/issues/22)).
> If you know how to fix that problem I would be very happy

### Reflection opens
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
  
  [BlitCanvasImpl]: https://github.com/husker-dev/openglfx/blob/master/core/src/main/kotlin/com/huskerdev/openglfx/canvas/implementations/BlitCanvasImpl.kt
  [SharedCanvasImpl]: https://github.com/husker-dev/openglfx/blob/master/core/src/main/kotlin/com/huskerdev/openglfx/canvas/implementations/SharedCanvasImpl.kt
  [NVDXInteropCanvasImpl]: https://github.com/husker-dev/openglfx/blob/master/core/src/main/kotlin/com/huskerdev/openglfx/canvas/implementations/NVDXInteropCanvasImpl.kt
  [IOSurfaceCanvasImpl]: https://github.com/husker-dev/openglfx/blob/master/core/src/main/kotlin/com/huskerdev/openglfx/canvas/implementations/IOSurfaceCanvasImpl.kt

  [AsyncBlitCanvasImpl]: https://github.com/husker-dev/openglfx/blob/master/core/src/main/kotlin/com/huskerdev/openglfx/canvas/implementations/async/AsyncBlitCanvasImpl.kt
  [AsyncSharedCanvasImpl]: https://github.com/husker-dev/openglfx/blob/master/core/src/main/kotlin/com/huskerdev/openglfx/canvas/implementations/async/AsyncSharedCanvasImpl.kt
  [AsyncNVDXInteropCanvasImpl]: https://github.com/husker-dev/openglfx/blob/master/core/src/main/kotlin/com/huskerdev/openglfx/canvas/implementations/async/AsyncNVDXInteropCanvasImpl.kt
  [AsyncIOSurfaceCanvasImpl]: https://github.com/husker-dev/openglfx/blob/master/core/src/main/kotlin/com/huskerdev/openglfx/canvas/implementations/async/AsyncIOSurfaceCanvasImpl.kt
  
  |             | Description | Sync implementation | Async implementation |
  | ----------- | ----------- | ------------------- | -------------------- |
  | Windows     | [NV_DX_interop](https://www.khronos.org/registry/OpenGL/extensions/NV/WGL_NV_DX_interop.txt) is used to synchronize textures between DirectX from JavaFX and OpenGL. | [NVDXInteropCanvasImpl.kt][NVDXInteropCanvasImpl] | [AsyncNVDXInteropCanvasImpl.kt][AsyncNVDXInteropCanvasImpl]
  | Linux       | Creates context that is shared with JavaFX's one. After rendering, shared texture is displayed in JavaFX frame. | [SharedCanvasImpl.kt][SharedCanvasImpl] | [AsyncSharedCanvasImpl.kt][AsyncSharedCanvasImpl]
  | macOS       | IOSurface is used to create memory block in VRAM that can be used in different OpenGL contexts. | [IOSurfaceCanvasImpl.kt][IOSurfaceCanvasImpl] | [AsyncIOSurfaceCanvasImpl.kt][AsyncIOSurfaceCanvasImpl]
  | Other       | Copies PixelBuffer from ```glReadPixels``` to JavaFX Image | [BlitCanvasImpl.kt][BlitCanvasImpl] | [AsyncBlitCanvasImpl.kt][AsyncBlitCanvasImpl]


# Thanks to

- [James H Ball](https://github.com/jameshball) - macOS tester
- [Andrew Hamilton](https://github.com/orange451) - macOS tester, suggested new additions
