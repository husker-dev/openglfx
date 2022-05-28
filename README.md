# OpenGLFX
<a href="LICENSE"><img src="https://img.shields.io/github/license/husker-dev/openglfx?style=flat-square"></a>
<a href="https://jitpack.io/#husker-dev/openglfx"><img src="https://img.shields.io/jitpack/v/github/husker-dev/openglfx?style=flat-square"></a>
<a href="https://github.com/husker-dev/openglfx/releases/latest"><img src="https://img.shields.io/github/v/release/husker-dev/openglfx?style=flat-square"></a>

OpenGL implementation for JavaFX

## Features:
  - **JOGL** and **LWJGL** support
  - HiDPI support
  - Good performance

## Screenshots

<p>
<img src="https://user-images.githubusercontent.com/31825139/129398976-f1317b23-5583-47e9-ab1c-d12eea54d4ab.gif" height="280"/>
<img src="https://user-images.githubusercontent.com/31825139/131416822-b90bb974-583c-48a2-ae47-8e0022fd5229.gif" height="280"/>
</p>

## How it works

- ### Windows
  [NV_DX_interop](https://www.khronos.org/registry/OpenGL/extensions/NV/WGL_NV_DX_interop.txt) is used to synchronize textures between DirectX and OpenGL.

- ### Linux and macOS
  Creates a new GL context, that is shared with the JavaFX's one. 

  Then renders to a JavaFX texture.

- ### Other cases (Fallback)
  Copies PixelBuffer from ```glReadPixels``` to JavaFX Image

## Example code

You can use [example generator](https://huskerdev.com/?page=tools/openglfx) to mix **Java/Kotlin** and **Gradle/Maven/Sbt**.

Or expand one of the following examples:
<details><summary>Kotlin + LWJGL</summary>
  
#### Gradle
```groovy
plugins {
    id 'org.jetbrains.kotlin.jvm'
}
  
repositories {
    mavenCentral()
    maven { url 'https://jitpack.io' }
}

dependencies {
    def lwjglNatives = "win"
    def jfxNatives = "win"
  
    // Kotlin lib
    implementation "org.jetbrains.kotlin:kotlin-stdlib"
  
    // OpenGLFX
    implementation 'com.github.husker-dev.openglfx:core:3.0.2'
    implementation 'com.github.husker-dev.openglfx:lwjgl:3.0.2'

    // LWJGL
    implementation platform("org.lwjgl:lwjgl-bom:3.3.0")
    implementation "org.lwjgl:lwjgl"
    implementation "org.lwjgl:lwjgl-opengl"
  
    runtimeOnly "org.lwjgl:lwjgl::$lwjglNatives"
    runtimeOnly "org.lwjgl:lwjgl-opengl::$lwjglNatives"

    // JavaFX
    compileOnly "org.openjfx:javafx-base:18.0.1:$jfxNatives"
    compileOnly "org.openjfx:javafx-controls:18.0.1:$jfxNatives"
    compileOnly "org.openjfx:javafx-graphics:18.0.1:$jfxNatives"
}
```

#### Kotlin
```kotlin
import com.huskerdev.openglfx.GLCanvasAnimator
import com.huskerdev.openglfx.OpenGLCanvas
import com.huskerdev.openglfx.OpenGLCanvas.Companion.CORE_PROFILE
import com.huskerdev.openglfx.lwjgl.LWJGLExecutor.Companion.LWJGL_MODULE
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.SplitPane
import javafx.scene.layout.Region
import javafx.stage.Stage
import org.lwjgl.opengl.GL30.*
import kotlin.math.absoluteValue
import kotlin.math.sin

fun main() {
    System.setProperty("prism.vsync", "false")
    Application.launch(ExampleApp::class.java)
}

class ExampleApp: Application(){
  
    var animation = 0.0

    override fun start(stage: Stage?) {
        stage!!.title = "Kotlin \"LWJGL\" example"
        stage.width = 400.0
        stage.height = 400.0

        stage.scene = Scene(createGLCanvas()))
        stage.show()
    }

    private fun createGLCanvas(): Region {
        val canvas = OpenGLCanvas.create(LWJGL_MODULE, CORE_PROFILE)
        canvas.animator = GLCanvasAnimator(60.0)

        canvas.addOnRenderEvent {
            val alpha = sin(System.nanoTime().toDouble() / 1000000000.0).toFloat().absoluteValue
  
            glClearColor(alpha, 1 - alpha, 1 - alpha, 1f)
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        }

        return canvas
    }
}
```
  
</details>
  
## OpenGL profile choosing
Some people will want to change the OpenGL profile for some reason.

To do this, you need to specify a variable at the OpenGLCanvas creation:
```kotlin
OpenGLCanvas.create(LWJGL_MODULE, CORE_PROFILE)
```

Possible values:
- ```CORE_PROFILE```
- ```COMPATIBILITY_PROFILE``` (default)

## Animator

Animator allows to automatically repaint canvas with fixed FPS.

```kotlin
canvas.animator = GLCanvasAnimator(60.0) // FPS: 60
```
> Due to JavaFX limits, rendering method may be called less times than required. 
> 

To set unlimited FPS, replace number to ```GLCanvasAnimator.UNLIMITED_FPS```:
```kotlin
canvas.animator = GLCanvasAnimator(GLCanvasAnimator.UNLIMITED_FPS) // FPS: Unlimited
```

To stop repainting, just remove the animator:
```kotlin
canvas.animator = null
```

## Reflections opens
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

## Notes
- JOGL can't initialize on macOS
- Linux is supported only on x64 and x86 architectures
> If anyone knows how to fix any of these problems I would be very happy


## Example repo
If you still have a misunderstanding about the implementation, take a look at the example repository that uses OpenGLFX with Java and Maven 

https://github.com/orange451/OpenGLFX-LWJGL-Sample


## Thanks to

- [James H Ball](https://github.com/jameshball) - macOS tester
- [Andrew Hamilton](https://github.com/orange451) - macOS tester, suggested new additions

## Wiki
  Read [wiki articles](https://github.com/husker-dev/openglfx/wiki) for more information about JavaFX pipelines
