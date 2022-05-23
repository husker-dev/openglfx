package rendering;


import com.huskerdev.openglfx.events.GLRenderEvent;
import com.huskerdev.openglfx.events.GLReshapeEvent;

import static org.lwjgl.opengl.GL11.*;

public class ExampleJavaRenderer {

    private static float animation = 0f;

    public static void reshape(GLReshapeEvent event){
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();

        double aspect = (double)event.height / event.width;
        glFrustum(-1.0, 1.0, -aspect, aspect, 5.0, 60.0);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        glTranslatef(0.0f, 0.0f, -40.0f);
    }

    public static void render(GLRenderEvent event){
        animation += event.delta * 100;

        double width = event.width;
        double height = event.height;

        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glBegin(GL_QUADS);
        glColor3f(1.0f, 0.5f, 0.0f);
        glVertex2d(-10.0, 0.0);
        glVertex2d(width, 0.0);
        glVertex2d(width, height / 2);
        glVertex2d(-10.0, height / 2);
        glEnd();

        // Moving rectangle
        float rectSize = 2f;

        glPushMatrix();
        glRotatef(animation, 1f, 0f, 0f);
        glRotatef(animation, 0f, 1f, 0f);

        glBegin(GL_QUADS);
        // top
        glColor3f(1.0f, 0.0f, 0.0f);
        glNormal3f(0.0f, 1.0f, 0.0f);
        glVertex3f(-rectSize, rectSize, rectSize);
        glVertex3f(rectSize, rectSize, rectSize);
        glVertex3f(rectSize, rectSize, -rectSize);
        glVertex3f(-rectSize, rectSize, -rectSize);

        // front
        glColor3f(0.0f, 1.0f, 0.0f);
        glNormal3f(0.0f, 0.0f, 1.0f);
        glVertex3f(rectSize, -rectSize, rectSize);
        glVertex3f(rectSize, rectSize, rectSize);
        glVertex3f(-rectSize, rectSize, rectSize);
        glVertex3f(-rectSize, -rectSize, rectSize);

        // right
        glColor3f(0.0f, 0.0f, 1.0f);
        glNormal3f(1.0f, 0.0f, 0.0f);
        glVertex3f(rectSize, rectSize, -rectSize);
        glVertex3f(rectSize, rectSize, rectSize);
        glVertex3f(rectSize, -rectSize, rectSize);
        glVertex3f(rectSize, -rectSize, -rectSize);

        // left
        glColor3f(0.0f, 0.0f, 0.5f);
        glNormal3f(1.0f, 0.0f, 0.0f);
        glVertex3f(-rectSize, -rectSize, rectSize);
        glVertex3f(-rectSize, rectSize, rectSize);
        glVertex3f(-rectSize, rectSize, -rectSize);
        glVertex3f(-rectSize, -rectSize, -rectSize);

        // bottom
        glColor3f(0.5f, 0.0f, 0.0f);
        glNormal3f(0.0f, 1.0f, 0.0f);
        glVertex3f(rectSize, -rectSize, rectSize);
        glVertex3f(-rectSize, -rectSize, rectSize);
        glVertex3f(-rectSize, -rectSize, -rectSize);
        glVertex3f(rectSize, -rectSize, -rectSize);

        // back
        glColor3f(0.0f, 0.5f, 0.0f);
        glNormal3f(0.0f, 0.0f, 1.0f);
        glVertex3f(rectSize, rectSize, -rectSize);
        glVertex3f(rectSize, -rectSize, -rectSize);
        glVertex3f(-rectSize, -rectSize, -rectSize);
        glVertex3f(-rectSize, rectSize, -rectSize);

        glEnd();

        glPopMatrix();
    }

}

