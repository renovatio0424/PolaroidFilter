package com.reno.polaroidfilter

import android.opengl.GLES20
import android.opengl.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.opengles.GL

abstract class TextureShape : Shape() {
    protected abstract val uVs: FloatArray
    protected abstract val uVHandleName: String?
    protected abstract fun bindTexture(textureId: Int)

    private var textures = IntArray(1)
    private var uvHandle = 0
    private lateinit var uvBuffer: FloatBuffer

    override fun onDrawFrame(matrix: FloatArray?) {
        GLES20.glUseProgram(program)
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(
            positionHandle,
            coordsPerVertex,
            GLES20.GL_FLOAT,
            false,
            vertexStride,
            vertexBuffer
        )
        GLES20.glEnableVertexAttribArray(uvHandle)
        GLES20.glVertexAttribPointer(uvHandle, 2, GLES20.GL_FLOAT, false, 0, uvBuffer)
        Matrix.multiplyMM(mvpMatrix, 0, matrix, 0, modelMatrix, 0)
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0])
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indices.size, GLES20.GL_UNSIGNED_BYTE, indexBuffer)
    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        super.onSurfaceChanged(width, height)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        GLES20.glEnable(GLES20.GL_BLEND)

        uvHandle = GLES20.glGetAttribLocation(program, uVHandleName)
        val bb = ByteBuffer.allocateDirect(uVs.size * SIZE_OF_FLOAT)
        bb.order(ByteOrder.nativeOrder())
        uvBuffer = bb.asFloatBuffer()
        uvBuffer.put(uVs)
        uvBuffer.position(0)
        GLES20.glGenTextures(1, textures, 0)
        bindTexture(textures[0])
    }
}
