# opengl-javafx

OpenGL implementation for JavaFX, based on JOGL

## Features:
  - HiDPI support
  - One-line Node creation
  - Smooth resizing without slowing down the program
  
## Flaws
  - Prints on top layer of the window
  
## Dependencies
  - JOGL ([2.4.0](https://jogamp.org/deployment/v2.4.0-rc-20200115/fat/jogamp-fat.jar) or later)
  
## Usage
  
  ```java
  Node glNode = new OpenGLCanvas(/* GLEventListener */);
  ```
  or
  ```java
  Node glNode = new OpenGLCanvas(/* GLCapabilities */, /* GLEventListener */);
  ```
