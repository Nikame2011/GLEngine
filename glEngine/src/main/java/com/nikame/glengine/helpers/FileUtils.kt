package com.nikame.glengine.helpers

import android.content.Context
import android.content.res.Resources
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.Exception

class FileUtils {
    companion object {
        fun readTextFromRaw(context: Context, resource: Int): String {
            val stringBuilder = StringBuilder()
            try {
                var bufferedReader: BufferedReader? = null
                try {
                    val inputStream = context.resources.openRawResource(resource)
                    bufferedReader = BufferedReader(InputStreamReader(inputStream))
                    var line: String? = bufferedReader.readLine()
                    while (line != null) {
                        stringBuilder.append(line).append("\r\n")
                        line = bufferedReader.readLine()
                    }
                } finally {
                    if (bufferedReader != null) {
                        bufferedReader.close()
                    }
                }
            } catch (ioex: IOException) {
                logException(ioex)
            } catch (nfex: Resources.NotFoundException) {
                logException(nfex)
            }
            return stringBuilder.toString()
        }

        fun logException(exception: Exception) {
            exception.printStackTrace()
        }
    }
}