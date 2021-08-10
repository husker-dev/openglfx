# OpenGLFX

OpenGL implementation for JavaFX, based on JOGL

## Features:
  - HiDPI support
  - One-line Node creation
  - Smooth resizing without slowing down the program
  - Just a simple Pane node
  
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
        implementation 'com.github.husker-dev:openglfx:0.5.2'
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
