package rendering

import com.huskerdev.openglfx.OpenGLCanvas
import com.huskerdev.openglfx.events.GLRenderEvent
import com.huskerdev.openglfx.events.GLReshapeEvent
import org.lwjgl.opengl.GL30.*



class ExampleRenderer {
    companion object {
        var animation = 0.0

        fun reshape(canvas: OpenGLCanvas, event: GLReshapeEvent){
            glMatrixMode(GL_PROJECTION)
            glLoadIdentity()

            val aspect = canvas.height / canvas.width
            glFrustum(-1.0, 1.0, -aspect, aspect, 5.0, 60.0)
            glMatrixMode(GL_MODELVIEW)
            glLoadIdentity()

            glTranslatef(0.0f, 0.0f, -40.0f);
            glEnable(GL_DEPTH_TEST)
        }

        fun render(canvas: OpenGLCanvas, event: GLRenderEvent){
            animation += event.delta * 100

            val width = canvas.width
            val height = canvas.height

            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            glBegin(GL_QUADS)
            glColor3f(1.0f, 0.5f, 0.0f)
            glVertex2d(-10.0, 0.0)
            glVertex2d(width, 0.0)
            glVertex2d(width, height / 2)
            glVertex2d(-10.0, height / 2)
            glEnd()

            // Moving rectangle
            val rectSize = 2f

            glPushMatrix()
            glRotatef(animation.toFloat(), 1f, 0f, 0f)
            glRotatef(animation.toFloat(), 0f, 1f, 0f)

            glBegin(GL_QUADS)
            // top
            glColor3f(1.0f, 0.0f, 0.0f)
            glNormal3f(0.0f, 1.0f, 0.0f)
            glVertex3f(-rectSize, rectSize, rectSize)
            glVertex3f(rectSize, rectSize, rectSize)
            glVertex3f(rectSize, rectSize, -rectSize)
            glVertex3f(-rectSize, rectSize, -rectSize)

            // front
            glColor3f(0.0f, 1.0f, 0.0f)
            glNormal3f(0.0f, 0.0f, 1.0f)
            glVertex3f(rectSize, -rectSize, rectSize)
            glVertex3f(rectSize, rectSize, rectSize)
            glVertex3f(-rectSize, rectSize, rectSize)
            glVertex3f(-rectSize, -rectSize, rectSize)

            // right
            glColor3f(0.0f, 0.0f, 1.0f)
            glNormal3f(1.0f, 0.0f, 0.0f)
            glVertex3f(rectSize, rectSize, -rectSize)
            glVertex3f(rectSize, rectSize, rectSize)
            glVertex3f(rectSize, -rectSize, rectSize)
            glVertex3f(rectSize, -rectSize, -rectSize)

            // left
            glColor3f(0.0f, 0.0f, 0.5f)
            glNormal3f(1.0f, 0.0f, 0.0f)
            glVertex3f(-rectSize, -rectSize, rectSize)
            glVertex3f(-rectSize, rectSize, rectSize)
            glVertex3f(-rectSize, rectSize, -rectSize)
            glVertex3f(-rectSize, -rectSize, -rectSize)

            // bottom
            glColor3f(0.5f, 0.0f, 0.0f)
            glNormal3f(0.0f, 1.0f, 0.0f)
            glVertex3f(rectSize, -rectSize, rectSize)
            glVertex3f(-rectSize, -rectSize, rectSize)
            glVertex3f(-rectSize, -rectSize, -rectSize)
            glVertex3f(rectSize, -rectSize, -rectSize)

            // back
            glColor3f(0.0f, 0.5f, 0.0f)
            glNormal3f(0.0f, 0.0f, 1.0f)
            glVertex3f(rectSize, rectSize, -rectSize)
            glVertex3f(rectSize, -rectSize, -rectSize)
            glVertex3f(-rectSize, -rectSize, -rectSize)
            glVertex3f(-rectSize, rectSize, -rectSize)

            glEnd()

            glPopMatrix()
        }
    }
}

