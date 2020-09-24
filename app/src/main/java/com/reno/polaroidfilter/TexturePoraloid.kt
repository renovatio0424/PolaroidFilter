package com.reno.polaroidfilter

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLUtils

/**
 * VERTEX
 * (3)------(2)
 * ㅣ     / ㅣ
 * ㅣ   /   ㅣ
 * ㅣ /     ㅣ
 * (0)------(1)
 *
 * UV
 * (0,0)---(1,0)
 * ㅣ     / ㅣ
 * ㅣ   /   ㅣ
 * ㅣ /     ㅣ
 * (0,1)---(1,1)
 */

class TexturePoraloid(private val context: Context, private val bitmap: Bitmap) : TextureShape() {

    override fun bindTexture(textureId: Int) {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        if (!bitmap.isRecycled) {
            bitmap.recycle()
        }
    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        super.onSurfaceChanged(width, height)
    }

    override val uVs: FloatArray
        get() = floatArrayOf(
            0f, 1f,
            1f, 1f,
            1f, 0f,
            0f, 0f
        )
    override val vertexShader: String
        get() {
            return ShaderUtils.readShaderFile(context, R.raw.poraloid_vert)/*("uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "attribute vec2 a_texCoord;" +
                    "varying vec2 v_texCoord;" +
                    "void main() {" +
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "  v_texCoord = a_texCoord;" +
                    "}")*/
        }
    override val fragmentShader: String
        get() {
            return ShaderUtils.readShaderFile(context, R.raw.poraloid_frag)/*("precision mediump float;" +
                    "varying vec2 v_texCoord;" +
                    "uniform sampler2D s_texture;" +
                    "void main() {" +
                    "  gl_FragColor = texture2D(s_texture, v_texCoord);" +
                    "}")*/
        }

    //bottom left
    //bottom right
    // top right
    override val vertices: FloatArray
        get() {
            return floatArrayOf(
                -0.6f, -0.8f, -0.1f,  //bottom left
                0.6f, -0.8f, -0.1f,  //bottom right
                0.6f, 0.8f, -0.1f,  // top right
                -0.6f, 0.8f, -0.1f
            )
        }

    override val coordsPerVertex: Int
        get() {
            return 3
        }
    override val positionHandleName: String
        get() {
            return "vPosition"
        }
    override val uVHandleName: String
        get() {
            return "a_texCoord"
        }
    override val mVPMatHandleName: String
        get() {
            return "uMVPMatrix"
        }
    override val indices: ByteArray
        get() {
            return byteArrayOf(
                0, 1, 2,
                2, 3, 0
            )
        }

}
