
import javafx.application.Application

fun main() {
    System.setProperty("prism.order", "d3d")
    System.setProperty("prism.vsync", "false")

    Application.launch(ExampleApp::class.java)
}