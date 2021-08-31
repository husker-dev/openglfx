# OpenGLFX
<a href="LICENSE"><img src="https://img.shields.io/github/license/husker-dev/openglfx?style=flat-square"></a>
<a href="https://jitpack.io/#husker-dev/openglfx"><img src="https://img.shields.io/jitpack/v/github/husker-dev/openglfx?style=flat-square"></a>
<a href="https://github.com/husker-dev/openglfx/releases/latest"><img src="https://img.shields.io/github/v/release/husker-dev/openglfx?style=flat-square"></a>

OpenGL implementation for JavaFX, based on JOGL

## Features:
  - HiDPI support
  - One-line Node creation
  - Smooth resizing without slowing down the program
  - Just a simple Pane node

## Examples

<p>
<img src="https://user-images.githubusercontent.com/31825139/129398976-f1317b23-5583-47e9-ab1c-d12eea54d4ab.gif" height="280"/>
<img src="https://user-images.githubusercontent.com/31825139/131416822-b90bb974-583c-48a2-ae47-8e0022fd5229.gif" height="280"/>
</p>

## Requirements
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
        implementation 'com.github.husker-dev:openglfx:1.0'
    }
    ```
  - Create node
    ```java
    Node glNode = OpenGLCanvas.create();
    ```
    ```java
    Node glNode = OpenGLCanvas.create(/* GLCapabilities */, /* FPS */);
    ```
    ```java
    Node glNode = OpenGLCanvas.create(/* GLCapabilities */, /* FPS */, /* requireDirectDraw */);
    ```


## Rendering types

  ### Universal
  
  - <p>
    :heavy_check_mark: Uses a separate OpenGL context. 

    :heavy_check_mark: Updates WritableImage using NIO pixel buffer.

    :x: Loads the processor at high output resolution.

    :x: Artifacts when resizing.
    </p>
  ### Direct GL
  
  - <p>
    :heavy_check_mark: Best available performance

    :heavy_check_mark: Processor load is independent of frame size

    :large_orange_diamond: Uses JavaFX GL context. 

    :x: Calls ```init```, ```reshape```, ```display``` every frame

    :x: Only on ```es2``` pipeline
    </p>
  
To disable ```Direct GL``` mode, set ```requireDirectDraw``` variable to **false** when creating the node. 

You can specify JavaFX rendering by setting following property before initializing:
```java
// es2 - OpenGL
// d3d - DirectX
// sw  - Software
System.setProperty("prism.order", "es2");
```

## How to use OpenGL on Windows
To use OpenGL rendering pipeline on Windows, you can use two ways:
1. Use compiled Windows OpenGL extension library [[Download]](https://github.com/husker-dev/openglfx/raw/master/libs/javafx.graphics.win-es2.jar) 

2. Compile OpenJFX sources by yourself
   - Configure sources using [official instruction](https://wiki.openjdk.java.net/display/OpenJFX/Building+OpenJFX)
   - Change line in ```build.gradle``` 

      From this:
      ```Groovy
      defineProperty("INCLUDE_ES2", IS_WINDOWS ? "false" : "true")
      ```
      To this
      ```Groovy
      defineProperty("INCLUDE_ES2", "true")
      ```
    - Compile
    - Add ```prism_es2.dll``` and ```javafx.graphics.jar``` to your project
  
