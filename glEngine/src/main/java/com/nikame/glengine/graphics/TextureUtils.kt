package com.nikame.glengine.graphics

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLES20.glGenTextures
import android.opengl.GLUtils

class TextureUtils {
    companion object {
        fun loadTexture(context: Context, resourceId: Int): Int {
            val textureIds = IntArray(1)
            glGenTextures(1, textureIds, 0)
            if (textureIds[0] != 0) {

                // получение Bitmap
                val options = BitmapFactory.Options()
                options.inScaled = false

                val bitmap = BitmapFactory.decodeResource(
                    context.resources, resourceId, options
                )

                if (bitmap != null) {
                    GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIds[0])

                    GLES20.glTexParameteri(
                        GLES20.GL_TEXTURE_2D,
                        GLES20.GL_TEXTURE_MIN_FILTER,
                        GLES20.GL_LINEAR
                    )
                    GLES20.glTexParameteri(
                        GLES20.GL_TEXTURE_2D,
                        GLES20.GL_TEXTURE_MAG_FILTER,
                        GLES20.GL_LINEAR
                    )

                    GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)

                    bitmap.recycle()

                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)

                    return textureIds[0]
                }
                GLES20.glDeleteTextures(1, textureIds, 0)

            }
            return 0
        }
    }
}