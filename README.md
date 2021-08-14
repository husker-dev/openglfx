# OpenGLFX
![GitHub](https://img.shields.io/github/license/husker-dev/openglfx?style=flat-square)
![JitPack](https://img.shields.io/jitpack/v/github/husker-dev/openglfx?style=flat-square)
![GitHub release (latest by date)](https://img.shields.io/github/v/release/husker-dev/openglfx?style=flat-square)

OpenGL implementation for JavaFX, based on JOGL

## Features:
  - HiDPI support
  - One-line Node creation
  - Smooth resizing without slowing down the program
  - Just a simple Pane node

## Examples

<details><summary>Show</summary>
<p>

<img src="https://user-images.githubusercontent.com/31825139/129398976-f1317b23-5583-47e9-ab1c-d12eea54d4ab.gif" height="280"/>

</p>
</details>


  
## Requirements
  - Kotlin
  - [jogl-all-main](https://mvnrepository.com/artifact/org.jogamp.jogl/jogl-all-main)
  - [gluegen-rt-main](https://mvnrepository.com/artifact/org.jogamp.gluegen/gluegen-rt-main)
  
## Usage

  - Add dependency 
  
    ```gradle
    repositories {
        // ...
        maven { url 'https://jitpack.io' }
    }
    
    dependencies {
        implementation 'com.github.husker-dev:openglfx:0.6.3'
    }
    ```
  - Create node
    ```java
    Node glNode0 = new OpenGLCanvas(/* GLEventListener */);
    ```
    ```java
    Node glNode1 = new OpenGLCanvas(/* GLCapabilities */, /* GLEventListener */);
    ```
    ```java
    Node glNode2 = new OpenGLCanvas(/* GLCapabilities */, /* GLEventListener */, /* FPS */);
    ```
