package com.reno.polaroidfilter

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class ShapeRenderer<T : Shape>(private val items: List<T>) : GLSurfaceView.Renderer {
    private val projectionMatrix: FloatArray = FloatArray(16)
    private val viewMatrix: FloatArray = FloatArray(16)
    private val vpMatrix: FloatArray = FloatArray(16)

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        items.forEach { it.onSurfaceCreated() }

        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
        GLES20.glClearDepthf(1.0f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glDepthFunc(GLES20.GL_LEQUAL)
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        val aspectRatio = width.toFloat() / height
        Matrix.frustumM(
            projectionMatrix, 0,
            -aspectRatio, aspectRatio,
            -1.0f, 1.0f,
            3.0f, 7.0f
        )
        Matrix.setLookAtM(
            viewMatrix, 0,
            0.0f, 0.0f, 3.0f,
            0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f
        )
        Matrix.multiplyMM(vpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

        items.forEach { it.onSurfaceChanged(width, height) }
    }

    override fun onDrawFrame(gl: GL10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        items.forEach { it.onDrawFrame(vpMatrix) }
    }

    fun onPause() {
        items.forEach { it.onPause() }
    }

    fun onResume() {
        items.forEach { it.onResume() }
    }

    fun onDestroy() {
        items.forEach { it.onDestroy() }
    }

    init {
        Matrix.setIdentityM(projectionMatrix, 0)
        Matrix.setIdentityM(viewMatrix, 0)
        Matrix.setIdentityM(vpMatrix, 0)
    }
}