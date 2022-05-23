package rendering;

import com.huskerdev.openglfx.events.*;
import com.huskerdev.openglfx.jogl.events.*;
import com.jogamp.opengl.GL2;

import static com.jogamp.opengl.GL.*;
import static com.jogamp.opengl.GL2ES3.GL_QUADS;
import static com.jogamp.opengl.fixedfunc.GLMatrixFunc.*;


public class ExampleJavaRenderer {

    static float animation = 0f;

    public static void reshape(GLReshapeEvent event){
        GL2 gl = ((JOGLReshapeEvent)event).getGl().getGL2();

        gl.glMatrixMode(GL_PROJECTION);
        gl.glLoadIdentity();

        double aspect = (double)event.height / event.width;
        gl.glFrustum(-1.0, 1.0, -aspect, aspect, 5.0, 60.0);
        gl.glMatrixMode(GL_MODELVIEW);
        gl.glLoadIdentity();

        gl.glTranslatef(0.0f, 0.0f, -40.0f);
        gl.glEnable(GL_DEPTH_TEST);
    }

    public static void render(GLRenderEvent event){
        GL2 gl = ((JOGLRenderEvent)event).getGl().getGL2();

        animation += event.delta * 100;

        double width = event.width;
        double height = event.height;

        gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        gl.glBegin(GL_QUADS);
        gl.glColor3f(1.0f, 0.5f, 0.0f);
        gl.glVertex2d(-10.0, 0.0);
        gl.glVertex2d(width, 0.0);
        gl.glVertex2d(width, height / 2);
        gl.glVertex2d(-10.0, height / 2);
        gl.glEnd();

        // Moving rectangle
        float rectSize = 2f;

        gl.glPushMatrix();
        gl.glRotatef(animation, 1f, 0f, 0f);
        gl.glRotatef(animation, 0f, 1f, 0f);

        gl.glBegin(GL_QUADS);
        // top
        gl.glColor3f(1.0f, 0.0f, 0.0f);
        gl.glNormal3f(0.0f, 1.0f, 0.0f);
        gl.glVertex3f(-rectSize, rectSize, rectSize);
        gl.glVertex3f(rectSize, rectSize, rectSize);
        gl.glVertex3f(rectSize, rectSize, -rectSize);
        gl.glVertex3f(-rectSize, rectSize, -rectSize);

        // front
        gl.glColor3f(0.0f, 1.0f, 0.0f);
        gl.glNormal3f(0.0f, 0.0f, 1.0f);
        gl.glVertex3f(rectSize, -rectSize, rectSize);
        gl.glVertex3f(rectSize, rectSize, rectSize);
        gl.glVertex3f(-rectSize, rectSize, rectSize);
        gl.glVertex3f(-rectSize, -rectSize, rectSize);

        // right
        gl.glColor3f(0.0f, 0.0f, 1.0f);
        gl.glNormal3f(1.0f, 0.0f, 0.0f);
        gl.glVertex3f(rectSize, rectSize, -rectSize);
        gl.glVertex3f(rectSize, rectSize, rectSize);
        gl.glVertex3f(rectSize, -rectSize, rectSize);
        gl.glVertex3f(rectSize, -rectSize, -rectSize);

        // left
        gl.glColor3f(0.0f, 0.0f, 0.5f);
        gl.glNormal3f(1.0f, 0.0f, 0.0f);
        gl.glVertex3f(-rectSize, -rectSize, rectSize);
        gl.glVertex3f(-rectSize, rectSize, rectSize);
        gl.glVertex3f(-rectSize, rectSize, -rectSize);
        gl.glVertex3f(-rectSize, -rectSize, -rectSize);

        // bottom
        gl.glColor3f(0.5f, 0.0f, 0.0f);
        gl.glNormal3f(0.0f, 1.0f, 0.0f);
        gl.glVertex3f(rectSize, -rectSize, rectSize);
        gl.glVertex3f(-rectSize, -rectSize, rectSize);
        gl.glVertex3f(-rectSize, -rectSize, -rectSize);
        gl.glVertex3f(rectSize, -rectSize, -rectSize);

        // back
        gl.glColor3f(0.0f, 0.5f, 0.0f);
        gl.glNormal3f(0.0f, 0.0f, 1.0f);
        gl.glVertex3f(rectSize, rectSize, -rectSize);
        gl.glVertex3f(rectSize, -rectSize, -rectSize);
        gl.glVertex3f(-rectSize, -rectSize, -rectSize);
        gl.glVertex3f(-rectSize, rectSize, -rectSize);

        gl.glEnd();

        gl.glPopMatrix();
    }
}

