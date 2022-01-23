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

## Description 
JavaFX has very poor 3D functionality, so this library was created.


There are two ways to render OpenGL content into JavaFX frame:
- The first method is to change the rendering function of the node, and call our OpenGL draw calls. 
  This method only works if ES2 is selected as the JavaFX rendering engine (pipeline). It is called ```Direct```
- The second way is to create a new OpenGL window where all the rendering takes place. Later, the entire pixel buffer is copied to the image on the JavaFX side. 
  This method is very slow, although it has an acceleration due to the use of IntBuffer from NIO. But, anyway it calculates on CPU. This method is called ```Universal```

There are also several ways to call OpenGL functions from Java code. The most preferred is ```LWJGL``` because it doesn't create unnecessary objects like ```JOGL``` does. All of them supported in this library.

> JOGL also has problems on MacOS.

## Example code
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
      implementation 'com.github.husker-dev.openglfx:core:2.4'
      implementation 'com.github.husker-dev.openglfx:lwjgl:2.4'
    
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
      implementation 'com.github.husker-dev.openglfx:core:2.4'
      implementation 'com.github.husker-dev.openglfx:jogl:2.4'
    
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
   
## Rendering types comparison

  |                       |      Universal     |       Direct
  | --------------------- | :----------------: | :----------------: |
  | Performance           | :x:                | :heavy_check_mark:
  | Smooth resizing       | :x:                | :heavy_check_mark:
  | Separate GL context   | :heavy_check_mark: | :x:
  | **OpenGL** pipeline       | :heavy_check_mark: | :heavy_check_mark:
  | **DirectX** pipeline  | :heavy_check_mark: | :x:
  | **Software** pipeline | :heavy_check_mark: | :x:
  | Calls ```init``` once | :heavy_check_mark: | :x:

## Wiki
  Read [wiki articles](https://github.com/husker-dev/openglfx/wiki) for more information
