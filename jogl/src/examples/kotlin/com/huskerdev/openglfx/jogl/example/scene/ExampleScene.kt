package com.huskerdev.openglfx.jogl.example.scene

import com.huskerdev.openglfx.events.GLInitializeEvent
import com.huskerdev.openglfx.events.GLRenderEvent
import com.huskerdev.openglfx.events.GLReshapeEvent
import com.huskerdev.openglfx.jogl.events.JOGLEvent
import com.jogamp.opengl.GL.*
import javafx.scene.paint.Color
import com.huskerdev.openglfx.jogl.example.scene.graphics.Mesh
import com.huskerdev.openglfx.jogl.example.scene.graphics.Shader
import com.huskerdev.openglfx.jogl.example.scene.graphics.shapes.Cube
import com.huskerdev.openglfx.jogl.example.scene.graphics.shapes.Plane
import com.huskerdev.openglfx.jogl.example.scene.math.Matrix4
import com.huskerdev.openglfx.jogl.example.scene.math.Vec3
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
        val gl = (event as JOGLEvent).gl
        val shader = Shader(gl,
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

        gl.glUseProgram(shader.program)
        transformLocation = gl.glGetUniformLocation(shader.program, "transform")
        viewLocation = gl.glGetUniformLocation(shader.program, "view")
        projectionLocation = gl.glGetUniformLocation(shader.program, "projection")

        for(x in -10..10 step 2){
            for(z in -10..10 step 2) {
                if(random() > 0.7)
                    continue
                val rotate = random() * PI * 4

                meshes.add(
                    Cube(gl, Color.color((x+10)/20.0, (z+10)/20.0, 1.0),
                    Matrix4.rotationX(rotate.toFloat()) *
                        Matrix4.rotationY(rotate.toFloat()) *
                        Matrix4.translate(x.toFloat(), 0f, z.toFloat()))
                )
            }
        }
        meshes.add(Plane(gl, 15f, 15f, color = Color.color(0.0, 0.0, 0.0, 0.8)))

        gl.glEnable(GL_DEPTH_TEST)
        gl.glEnable(GL_BLEND)
        gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
    }

    fun reshape(event: GLReshapeEvent){
        val gl = (event as JOGLEvent).gl
        gl.glUniformMatrix4fv(projectionLocation, 1, true,
            Matrix4.perspective(
                Math.toRadians(90.0).toFloat(),
                event.width.toFloat() / event.height.toFloat(),
                0.001f, 100f
            ).toByteBuffer())
    }

    fun render(event: GLRenderEvent){
        val gl = (event as JOGLEvent).gl
        gl.glClearColor(0f, 0f, 0f, 0f)
        gl.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        gl.glUniformMatrix4fv(transformLocation, 1, true, Matrix4.identity.toByteBuffer())
        gl.glUniformMatrix4fv(viewLocation, 1, true,(
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
            mesh.render(gl, transformLocation)

        time += event.delta.toFloat() * 2f
    }
}

