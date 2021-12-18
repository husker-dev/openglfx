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

## Usage

  - Add dependency
    ```gradle
      repositories {
          // ...
          maven { url 'https://jitpack.io' }
      }
    
      dependencies {
          implementation 'com.github.husker-dev.openglfx:core:2.3'

          implementation 'com.github.husker-dev.openglfx:lwjgl:2.3' // For LWJGL
          implementation 'com.github.husker-dev.openglfx:jogl:2.3'  // For JOGL
      }
    ```

  - Create Node 
    ```kotlin
    val canvas = OpenGLCanvas.create(/* Module */);
    ```
    ```kotlin
    val canvas = OpenGLCanvas.create(/* Module */, /* DirectDrawPolicy */);
    ```
    
    #### Module
      - ```JOGL_MODULE``` - JOGL library
      - ```LWJGL_MODULE``` - LWJGL library
    
    #### DirectDrawPolicy
      - ```NEVER``` - Never use direct render
      - ```IF_AVAILABLE``` - Use direct render if available
      - ```ALWAYS``` - Use only direct render

  - Handle events
    ```kotlin
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
   


## Rendering types

  |                       |      Universal     |       Direct
  | --------------------- | :----------------: | :----------------: |
  | Performance           | :x:                | :heavy_check_mark:
  | Smooth resizing       | :x:                | :heavy_check_mark:
  | Separate GL context   | :heavy_check_mark: | :x:
  | **OpenGL** pipeline       | :heavy_check_mark: | :heavy_check_mark:
  | **DirectX** pipeline  | :heavy_check_mark: | :x:
  | **Software** pipeline | :heavy_check_mark: | :x:
  | Calls ```init``` once | :heavy_check_mark: | :x:

## Examples

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
      implementation 'com.github.husker-dev.openglfx:core:2.3'
      implementation 'com.github.husker-dev.openglfx:lwjgl:2.3'
    
      // LWJGL
      implementation "org.lwjgl:lwjgl"
      implementation "org.lwjgl:lwjgl-glfw"
      implementation "org.lwjgl:lwjgl-opengl"
      runtimeOnly "org.lwjgl:lwjgl::your-platform"
      runtimeOnly "org.lwjgl:lwjgl-glfw::your-platform"
      runtimeOnly "org.lwjgl:lwjgl-opengl::your-platform"
  
      // ...
  }
  ```
  
  ### Kotlin example
  ```kotlin
  val canvas = OpenGLCanvas.create(LWJGL_MODULE)
  // OpenGLCanvas.create(LWJGL_MODULE, DirectDrawPolicy.ALWAYS)
  // OpenGLCanvas.create(LWJGL_MODULE, DirectDrawPolicy.IF_AVAILABLE)
  // OpenGLCanvas.create(LWJGL_MODULE, DirectDrawPolicy.NEVER)
  
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
  [Direct example](https://github.com/husker-dev/openglfx/blob/master/lwjgl/src/examples/kotlin/Direct.kt)
  
  [Universal example](https://github.com/husker-dev/openglfx/blob/master/lwjgl/src/examples/kotlin/Universal.kt)
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
      implementation 'com.github.husker-dev.openglfx:core:2.3'
      implementation 'com.github.husker-dev.openglfx:jogl:2.3'
    
      // JOGL
      implementation 'org.jogamp.jogl:jogl-all-main:2.3.2'
      implementation 'org.jogamp.gluegen:gluegen-rt-main:2.3.2'
  
      // ...
  }
  ```
  
  ### Kotlin example
  ```kotlin
  val canvas = OpenGLCanvas.create(JOGL_MODULE)
  // OpenGLCanvas.create(JOGL_MODULE, DirectDrawPolicy.ALWAYS)
  // OpenGLCanvas.create(JOGL_MODULE, DirectDrawPolicy.IF_AVAILABLE)
  // OpenGLCanvas.create(JOGL_MODULE, DirectDrawPolicy.NEVER)
  
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
  
  [Direct example](https://github.com/husker-dev/openglfx/blob/master/jogl/src/examples/kotlin/Direct.kt)
  
  [Universal example](https://github.com/husker-dev/openglfx/blob/master/jogl/src/examples/kotlin/Universal.kt)
</details>


## Wiki
  Read [wiki articles](https://github.com/husker-dev/openglfx/wiki) for more information
