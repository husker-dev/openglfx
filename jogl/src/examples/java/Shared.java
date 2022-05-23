import com.huskerdev.openglfx.GLCanvasAnimator;
import com.huskerdev.openglfx.OpenGLCanvas;
import com.huskerdev.openglfx.utils.OpenGLFXUtils;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.stage.Stage;
import rendering.ExampleJavaRenderer;

import static com.huskerdev.openglfx.jogl.JOGLFXExecutorKt.JOGL_MODULE;


public class Shared extends Application {

    public static void main(String[] args) {
        System.setProperty("prism.order", "es2");
        System.setProperty("prism.vsync", "false");

        Application.launch(Shared.class);
    }

    public void start(Stage stage) {
        stage.setTitle("Java \"Shared\" example");
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

