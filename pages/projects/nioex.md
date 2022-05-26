# NioEx

Advanced I/O usage in Kotlin

# Features

- Speedometers for every operation
- Zipping file/folder
- Unzipping
- Copying
- Downloading from URL

## Examples

Here are some of examples, to see all of them, follow [this link](https://github.com/husker-dev/NioEx/tree/master/core/src/examples/kotlin)

- Zip file
  ```kotlin
  // Zip to archive
  fileToZip.zipTo("archive.zip")
  ```

- Zip several files
  ```kotlin
  // Zip file array
  fileToZip.children.zipTo("archive.zip")
  ```

- Zip file with speedometer
  ```kotlin
  fileToZip.zipTo("archive.zip", ZipSpeedometer().apply {
      updateDelay = 10
      speedUpdateDelay = 10
      
      onStarted {
          println("Zipping started!")
      }
      onCompleted {
          println("Zipping completed!")
      }
      onUpdate {
          println("-------------------------")
          println("Size: ${it.size}")
          println("Zipped bytes: ${it.current}")
          println("Zipping entry: ${it.entry}")
          println("Zipping file: ${it.file}")
          println("Speed: ${it.bytesPerSecond}")
      }
  })

  ```
