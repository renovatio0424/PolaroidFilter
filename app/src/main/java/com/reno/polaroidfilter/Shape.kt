package com.reno.polaroidfilter

import android.opengl.GLES20
import android.opengl.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

abstract class Shape {
    protected abstract val vertexShader: String?
    protected abstract val fragmentShader: String?
    protected abstract val vertices: FloatArray
    protected abstract val coordsPerVertex: Int
    protected abstract val indices: ByteArray
    protected abstract val positionHandleName: String?
    protected abstract val mVPMatHandleName: String?

    abstract fun onDrawFrame(matrix: FloatArray?)
    protected var program = 0
    protected var vertexStride = coordsPerVertex * SIZE_OF_FLOAT
    protected var colorStride = VALUES_PER_COLOR * SIZE_OF_FLOAT
    protected val vertexBuffer: FloatBuffer
    protected val indexBuffer: ByteBuffer
    protected var positionHandle = 0
    protected var mvpMatrixHandle = 0
    protected val modelMatrix = FloatArray(16)
    protected val mvpMatrix = FloatArray(16)
    open fun onSurfaceChanged(width: Int, height: Int) {
        positionHandle = GLES20.glGetAttribLocation(program, positionHandleName)
        mvpMatrixHandle = GLES20.glGetUniformLocation(program, mVPMatHandleName)
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.setIdentityM(mvpMatrix, 0)
    }

    fun onSurfaceCreated() {
        program = GlUtils.createProgram(vertexShader, fragmentShader)
    }

    fun onResume() {}
    fun onPause() {}
    fun onDestroy() {}

    companion object {
        const val SIZE_OF_FLOAT = 4
        const val VALUES_PER_COLOR = 4
    }

    init {
        val byteBuffer = ByteBuffer.allocateDirect(vertices.size * SIZE_OF_FLOAT)
        byteBuffer.order(ByteOrder.nativeOrder())
        vertexBuffer = byteBuffer.asFloatBuffer()
        vertexBuffer.put(vertices)
        vertexBuffer.position(0)
        indexBuffer = ByteBuffer.allocateDirect(indices.size)
        indexBuffer.put(indices)
        indexBuffer.position(0)
    }
}