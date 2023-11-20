
import javafx.application.Application

fun main(){
    System.setProperty("prism.order", "es2")
    System.setProperty("prism.vsync", "false")

    Application.launch(ExampleApp::class.java)
}