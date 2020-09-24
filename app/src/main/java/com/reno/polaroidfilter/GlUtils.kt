package com.reno.polaroidfilter

import android.opengl.GLES20
import android.opengl.GLES30
import android.opengl.Matrix
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * Some OpenGL utility functions.
 */
object GlUtils {
    val TAG = GlUtils::class.java.simpleName

    /** Identity matrix for general use.  Don't modify or life will get weird.  */
    val IDENTITY_MATRIX: FloatArray
    const val SIZEOF_FLOAT = 4

    /**
     * 버텍스 쉐이더와 프레그먼트 쉐이더로 새로운 프로그램을 만듭니다
     *
     * @return 프로그램을 아이디를 리턴합니다. 프로그램을 만드는데 실패할 경우 0을 리턴합니다.
     */
    fun createProgram(vertexSource: String?, fragmentSource: String?): Int {
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource)
        if (vertexShader == 0) {
            return 0
        }
        val pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource)
        if (pixelShader == 0) {
            return 0
        }
        var program = GLES20.glCreateProgram()
        checkGlError("glCreateProgram")
        if (program == 0) {
            Log.e(TAG, "Could not create program")
        }
        GLES20.glAttachShader(program, vertexShader)
        checkGlError("glAttachShader")
        GLES20.glAttachShader(program, pixelShader)
        checkGlError("glAttachShader")
        GLES20.glLinkProgram(program)
        val linkStatus = IntArray(1)
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] != GLES20.GL_TRUE) {
            Log.e(TAG, "Could not link program: ")
            Log.e(TAG, GLES20.glGetProgramInfoLog(program))
            GLES20.glDeleteProgram(program)
            program = 0
        }
        return program
    }

    /**
     * 제공된 쉐이더 소스를 컴파일 합니다.
     *
     * @return 쉐이더 아이디를 리턴합니다. 실패시 0을 리턴합니다.
     */
    fun loadShader(shaderType: Int, source: String?): Int {
        var shader = GLES20.glCreateShader(shaderType)
        checkGlError("glCreateShader type=$shaderType")
        GLES20.glShaderSource(shader, source)
        GLES20.glCompileShader(shader)
        val compiled = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
        if (compiled[0] == 0) {
            Log.e(TAG, "Could not compile shader $shaderType:")
            Log.e(TAG, " " + GLES20.glGetShaderInfoLog(shader))
            GLES20.glDeleteShader(shader)
            shader = 0
        }
        return shader
    }

    /**
     * GLError가 발생했는지 확인합니다.
     */
    fun checkGlError(op: String) {
        val error = GLES20.glGetError()
        if (error != GLES20.GL_NO_ERROR) {
            val msg = op + ": glError 0x" + Integer.toHexString(error)
            Log.e(TAG, msg)
            throw RuntimeException(msg)
        }
    }

    /**
     * Checks to see if the location we obtained is valid.  GLES returns -1 if a label
     * could not be found, but does not set the GL error.
     *
     *
     * Throws a RuntimeException if the location is invalid.
     */
    fun checkLocation(location: Int, label: String) {
        if (location < 0) {
            throw RuntimeException("Unable to locate '$label' in program")
        }
    }

    /**
     * Creates a texture from raw data.
     *
     * @param data Image data, in a "direct" ByteBuffer.
     * @param width Texture width, in pixels (not bytes).
     * @param height Texture height, in pixels.
     * @param format Image data format (use constant appropriate for glTexImage2D(), e.g. GL_RGBA).
     * @return Handle to texture.
     */
    fun createImageTexture(data: ByteBuffer?, width: Int, height: Int, format: Int): Int {
        val textureHandles = IntArray(1)
        val textureHandle: Int
        GLES20.glGenTextures(1, textureHandles, 0)
        textureHandle = textureHandles[0]
        checkGlError("glGenTextures")

        // Bind the texture handle to the 2D texture target.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle)

        // Configure min/mag filtering, i.e. what scaling method do we use if what we're rendering
        // is smaller or larger than the source image.
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_LINEAR
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_LINEAR
        )
        checkGlError("loadImageTexture")

        // Load the data from the buffer into the texture handle.
        GLES20.glTexImage2D(
            GLES20.GL_TEXTURE_2D,  /*level*/0, format,
            width, height,  /*border*/0, format, GLES20.GL_UNSIGNED_BYTE, data
        )
        checkGlError("loadImageTexture")
        return textureHandle
    }

    /**
     * C++레벨의 float 배열을 저장하기 위한 메모리를 생성합니다.
     */
    fun createFloatBuffer(coords: FloatArray): FloatBuffer {
        // Allocate a direct ByteBuffer, using 4 bytes per float, and copy coords into it.
        val bb = ByteBuffer.allocateDirect(coords.size * SIZEOF_FLOAT)
        bb.order(ByteOrder.nativeOrder())
        val fb = bb.asFloatBuffer()
        fb.put(coords)
        fb.position(0)
        return fb
    }

    /**
     * Writes GL version info to the log.
     */
    fun logVersionInfo() {
        Log.i(TAG, "vendor  : " + GLES20.glGetString(GLES20.GL_VENDOR))
        Log.i(TAG, "renderer: " + GLES20.glGetString(GLES20.GL_RENDERER))
        Log.i(TAG, "version : " + GLES20.glGetString(GLES20.GL_VERSION))
        if (false) {
            val values = IntArray(1)
            GLES30.glGetIntegerv(GLES30.GL_MAJOR_VERSION, values, 0)
            val majorVersion = values[0]
            GLES30.glGetIntegerv(GLES30.GL_MINOR_VERSION, values, 0)
            val minorVersion = values[0]
            if (GLES30.glGetError() == GLES30.GL_NO_ERROR) {
                Log.i(TAG, "iversion: $majorVersion.$minorVersion")
            }
        }
    }

    init {
        IDENTITY_MATRIX = FloatArray(16)
        Matrix.setIdentityM(IDENTITY_MATRIX, 0)
    }
}