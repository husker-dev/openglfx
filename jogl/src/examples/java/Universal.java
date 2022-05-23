import com.huskerdev.openglfx.GLCanvasAnimator;
import com.huskerdev.openglfx.OpenGLCanvas;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.stage.Stage;
import rendering.ExampleJavaRenderer;

import static com.huskerdev.openglfx.jogl.JOGLFXExecutorKt.JOGL_MODULE;


public class Universal extends Application {

    public static void main(String[] args) {
        System.setProperty("prism.vsync", "false");
        OpenGLCanvas.forceUniversal = true;

        Application.launch(Universal.class);
    }

    public void start(Stage stage) {
        stage.setTitle("Java \"Universal\" example");
        stage.setWidth(400);
        stage.setHeight(400);

        stage.setScene(new Scene(
            new SplitPane(createGL(), createGL())
        ));

        stage.show();
    }

    private OpenGLCanvas createGL() {
        OpenGLCanvas canvas = OpenGLCanvas.create(JOGL_MODULE);
        canvas.setAnimator(new GLCanvasAnimator(60.0));

        canvas.addOnReshapeEvent(ExampleJavaRenderer::reshape);
        canvas.addOnRenderEvent(ExampleJavaRenderer::render);

        return canvas;
    }
}

