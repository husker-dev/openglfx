
import javafx.application.Application

fun main(){
    System.setProperty("prism.order", "sw")
    System.setProperty("prism.vsync", "false")

    Application.launch(ExampleApp::class.java)
}