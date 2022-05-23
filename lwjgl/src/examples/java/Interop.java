import com.huskerdev.openglfx.GLCanvasAnimator;
import com.huskerdev.openglfx.OpenGLCanvas;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.stage.Stage;
import rendering.ExampleJavaRenderer;

import static com.huskerdev.openglfx.lwjgl.LWJGLExecutor.LWJGL_MODULE;


public class Interop extends Application {

    public static void main(String[] args) {
        System.setProperty("prism.order", "d3d");
        System.setProperty("prism.vsync", "false");

        Application.launch(Interop.class);
    }

    public void start(Stage stage) {
        stage.setTitle("Java \"Interop\" example");
        stage.setWidth(400);
        stage.setHeight(400);

        stage.setScene(new Scene(
            new SplitPane(createGL(), createGL())
        ));

        stage.show();
    }

    private OpenGLCanvas createGL() {
        OpenGLCanvas canvas = OpenGLCanvas.create(LWJGL_MODULE);
        canvas.setAnimator(new GLCanvasAnimator(60.0));

        canvas.addOnReshapeEvent(ExampleJavaRenderer::reshape);
        canvas.addOnRenderEvent(ExampleJavaRenderer::render);

        return canvas;
    }
}

