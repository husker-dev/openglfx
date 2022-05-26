<script>
	function hasNavigation() { return false; }
</script>

# OpenGLFX
<a href="LICENSE"><img src="https://img.shields.io/github/license/husker-dev/openglfx?style=flat-square"></a>
<a href="https://jitpack.io/#husker-dev/openglfx"><img src="https://img.shields.io/jitpack/v/github/husker-dev/openglfx?style=flat-square"></a>
<a href="https://github.com/husker-dev/openglfx/releases/latest"><img src="https://img.shields.io/github/v/release/husker-dev/openglfx?style=flat-square"></a>

OpenGL implementation for JavaFX

## Features:
  - **JOGL** support
  - **LWJGL** support
  - One-line Node creation
  - Smooth resizing without slowing down the program
  - HiDPI support

## Examples

<p>
<img src="https://user-images.githubusercontent.com/31825139/129398976-f1317b23-5583-47e9-ab1c-d12eea54d4ab.gif" height="280"/>
<img src="https://user-images.githubusercontent.com/31825139/131416822-b90bb974-583c-48a2-ae47-8e0022fd5229.gif" height="280"/>
</p>

## How it works

There are two ways to render OpenGL content into JavaFX frame:
- The first method is to change the rendering function of the node, and call our OpenGL draw calls. 
  This method only works if ES2 is selected as the JavaFX rendering engine (pipeline). It is called ```Direct```
- The second way is to create a new OpenGL window where all the rendering takes place. Later, the entire pixel buffer is copied to the image on the JavaFX side. 
  This method is very slow, although it has an acceleration due to the use of IntBuffer from NIO. But, anyway it calculates on CPU. This method is called ```Universal```

There are also several ways to call OpenGL functions from Java code. The most preferred is ```LWJGL``` because it doesn't create unnecessary objects like ```JOGL``` does. All of them supported in this library.

## Example code

  You can use [example generator](https://huskerdev.com/?page=tools/openglfx), or choose one of these:

  <details><summary>LWJGL</summary>

  ### Gradle
  ```groovy
  repositories {
      mavenCentral()
      maven { url 'https://jitpack.io' }
  }
  
  // ...
  
  dependencies {
      // OpenGLFX
      implementation 'com.github.husker-dev.openglfx:core:2.6'
      implementation 'com.github.husker-dev.openglfx:lwjgl:2.6'
    
      // LWJGL
      implementation "org.lwjgl:lwjgl"
      implementation "org.lwjgl:lwjgl-glfw"
      implementation "org.lwjgl:lwjgl-opengl"
      runtimeOnly "org.lwjgl:lwjgl::your-platform"
      runtimeOnly "org.lwjgl:lwjgl-glfw::your-platform"
      runtimeOnly "org.lwjgl:lwjgl-opengl::your-platform"
  
      // Kotlin
      implementation "org.jetbrains.kotlin:kotlin-stdlib"
  
      // ...
  }
  ```
  
  ### Kotlin
  ```kotlin
  val canvas = OpenGLCanvas.create(LWJGL_MODULE, DirectDrawPolicy.NEVER)
  // DirectDrawPolicy.NEVER         - Never use direct render (default)
  // DirectDrawPolicy.IF_AVAILABLE  - Use direct render if available
  // DirectDrawPolicy.ALWAYS        - Use only direct render
  
  canvas.onInitialize {
      // ...
  }
  canvas.onRender {
      // ...
  }
  canvas.onUpdate {
      // ...
  }
  canvas.onReshape {
      // ...
  }
  canvas.onDispose {
      // ...
  }
  ```
  [Direct LWJGL example](https://github.com/husker-dev/openglfx/blob/master/lwjgl/src/examples/kotlin/Direct.kt)
  
  [Universal LWJGL example](https://github.com/husker-dev/openglfx/blob/master/lwjgl/src/examples/kotlin/Universal.kt)
  
  ---
</details>


<details><summary>JOGL</summary>

  ### Gradle
  ```groovy
  repositories {
      mavenCentral()
      maven { url 'https://jitpack.io' }
  }
  
  // ...
  
  dependencies {
      // OpenGLFX
      implementation 'com.github.husker-dev.openglfx:core:2.6'
      implementation 'com.github.husker-dev.openglfx:jogl:2.6'
    
      // JOGL
      implementation 'org.jogamp.jogl:jogl-all-main:2.3.2'
      implementation 'org.jogamp.gluegen:gluegen-rt-main:2.3.2'
  
      // Kotlin
      implementation "org.jetbrains.kotlin:kotlin-stdlib"
  
      // ...
  }
  ```
  
  ### Kotlin
  ```kotlin
  val canvas = OpenGLCanvas.create(JOGL_MODULE, DirectDrawPolicy.NEVER)
  // DirectDrawPolicy.NEVER         - Never use direct render (default)
  // DirectDrawPolicy.IF_AVAILABLE  - Use direct render if available
  // DirectDrawPolicy.ALWAYS        - Use only direct render
  
  canvas.onInitialize {
      val gl = (canvas as JOGLFXCanvas).gl
      // ...
  }
  canvas.onRender {
      val gl = (canvas as JOGLFXCanvas).gl
      // ...
  }
  canvas.onUpdate {
      // ...
  }
  canvas.onReshape {
      val gl = (canvas as JOGLFXCanvas).gl
      // ...
  }
  canvas.onDispose {
      val gl = (canvas as JOGLFXCanvas).gl
      // ...
  }
  ```
  
  [Direct JOGL example](https://github.com/husker-dev/openglfx/blob/master/jogl/src/examples/kotlin/Direct.kt)
  
  [Universal JOGL example](https://github.com/husker-dev/openglfx/blob/master/jogl/src/examples/kotlin/Universal.kt)
  
  ---
</details>

> Don't use JOGL if you want to run application on MacOS!

## Timer

Repaint timer allows you to automatically repaint canvas with fixed FPS.

Due to JavaFX limits, ```onRender``` method may be called more or less times than required. 
To solve this problem, there is a method ```onUpdate```. It invokes every frame, so you can do all the calculations there.

```kotlin
canvas.createTimer(60.0)  // FPS: 60
```

To manipulate timer lifecycle, you can do following:
```kotlin
val timer = canvas.createTimer(60.0)

timer.started = false
timer.fps = 200.0
```
   
## Rendering types comparison

- **Universal** - Uses separated window for OpenGL
- **Direct** - Uses JavaFX's OpenGL ([initially](https://github.com/husker-dev/openglfx/wiki/How-to-enable-OpenGL-pipeline-on-Windows) doesn't work on Windows)

  |                       |      Universal     |       Direct
  | --------------------- | :----------------: | :----------------: |
  | Performance           | :x:                | :heavy_check_mark:
  | Smooth resizing       | :x:                | :heavy_check_mark:
  | Separate GL context   | :heavy_check_mark: | :x:
  | Windows               | :heavy_check_mark: | :x:
  | Linux                 | :heavy_check_mark: | :heavy_check_mark:
  | MacOS                 | :heavy_check_mark: | :heavy_check_mark:
  | Calls ```onInit``` once | :heavy_check_mark: | :x:

## Wiki
  Read [wiki articles](https://github.com/husker-dev/openglfx/wiki) for more information
