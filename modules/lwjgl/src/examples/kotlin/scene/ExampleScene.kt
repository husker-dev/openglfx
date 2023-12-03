package scene

import com.huskerdev.openglfx.canvas.events.GLInitializeEvent
import com.huskerdev.openglfx.canvas.events.GLRenderEvent
import com.huskerdev.openglfx.canvas.events.GLReshapeEvent
import javafx.scene.paint.Color
import org.lwjgl.opengl.GL30.*
import scene.graphics.Mesh
import scene.graphics.Shader
import scene.graphics.shapes.Cube
import scene.graphics.shapes.Plane
import scene.math.Matrix4
import scene.math.Vec3
import java.lang.Math.random
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin


class ExampleScene {
    private var time = 0f

    private var transformLocation = 0
    private var viewLocation = 0
    private var projectionLocation = 0

    private val meshes = arrayListOf<Mesh>()

    fun init(event: GLInitializeEvent){
        val shader = Shader(
            """
                #version 330 core
                layout (location = 0) in vec3 aPos;
                layout (location = 1) in vec4 aColor;
                out vec4 color;
                
                uniform float time;
                uniform mat4 transform;
                uniform mat4 view;
                uniform mat4 projection;
                
                void main() {
                    gl_Position = projection * view * transform * vec4(aPos, 1.0);
                    color = aColor;
                }
            """.trimIndent(),
            """
                #version 330 core
                out vec4 FragColor;
                in vec4 color;
                
                void main() {
                    FragColor = vec4(color);
                }
            """.trimIndent()
        )

        glUseProgram(shader.program)
        transformLocation = glGetUniformLocation(shader.program, "transform")
        viewLocation = glGetUniformLocation(shader.program, "view")
        projectionLocation = glGetUniformLocation(shader.program, "projection")

        for(x in -10..10 step 2){
            for(z in -10..10 step 2) {
                if(random() > 0.7)
                    continue
                val rotate = random() * PI * 4

                meshes.add(
                    Cube(Color.color((x+10)/20.0, (z+10)/20.0, 1.0),
                    Matrix4.rotationX(rotate.toFloat()) *
                        Matrix4.rotationY(rotate.toFloat()) *
                        Matrix4.translate(x.toFloat(), 0f, z.toFloat()))
                )
            }
        }
        meshes.add(Plane(15f, 15f, color = Color.color(0.0, 0.0, 0.0, 0.6)))

        glEnable(GL_DEPTH_TEST)
        glEnable(GL_BLEND)
        glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE)
    }

    fun reshape(event: GLReshapeEvent){
        glUniformMatrix4fv(projectionLocation, true,
            Matrix4.perspective(
                Math.toRadians(90.0).toFloat(),
                event.width.toFloat() / event.height.toFloat(),
                0.001f, 100f
            ).toByteBuffer())
    }

    fun render(event: GLRenderEvent){
        glClearColor(0f, 0f, 0f, 0f)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        glUniformMatrix4fv(transformLocation, true, Matrix4.identity.toByteBuffer())
        glUniformMatrix4fv(viewLocation, true,(
                Matrix4.lookAt(
                    Vec3(
                        cos(time / 10) * (cos(time / 10) * 5 + 7),
                        cos(time / 10) * 2f + 4f,
                        sin(time / 10) * (cos(time / 10) * 5 + 7),
                    ),
                    Vec3(0f, 0f, 0f)
                )
            ).toByteBuffer())

        for(mesh in meshes)
            mesh.render(transformLocation)

        time += event.delta.toFloat() * 2f
    }
}

