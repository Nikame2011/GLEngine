package com.nikame.glengine.graphics

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLES20.GL_BLEND
import android.opengl.GLES20.GL_COLOR_BUFFER_BIT
import android.opengl.GLES20.GL_DEPTH_BUFFER_BIT
import android.opengl.GLES20.GL_FLOAT
import android.opengl.GLES20.GL_FRAGMENT_SHADER
import android.opengl.GLES20.GL_ONE_MINUS_SRC_ALPHA
import android.opengl.GLES20.GL_SRC_ALPHA
import android.opengl.GLES20.GL_VERTEX_SHADER
import android.opengl.GLES20.glBlendFunc
import android.opengl.GLES20.glClear
import android.opengl.GLES20.glClearColor
import android.opengl.GLES20.glDrawArrays
import android.opengl.GLES20.glEnable
import android.opengl.GLES20.glEnableVertexAttribArray
import android.opengl.GLES20.glGetAttribLocation
import android.opengl.GLES20.glGetUniformLocation
import android.opengl.GLES20.glUniformMatrix4fv
import android.opengl.GLES20.glUseProgram
import android.opengl.GLES20.glVertexAttribPointer
import android.opengl.GLES20.glViewport
import android.opengl.GLSurfaceView.Renderer
import android.opengl.Matrix
import android.widget.Toast
import com.nikame.glengine.Entity.Camera
import com.nikame.glengine.Entity.XYZpoint
import com.nikame.glengine.R
import com.nikame.glengine.gObjects.AbstractGObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.future
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.Date
import java.util.concurrent.CompletableFuture
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class MainRender() : Renderer {

    private val POSITION_COUNT = 3
    private val TEXTURE_COUNT = 2
    private val STRIDE = (POSITION_COUNT
            + TEXTURE_COUNT) * 4

    private var programId = 0
    private lateinit var vertexData: FloatBuffer

    private var aPositionLocation = 0
    private var aTextureLocation = 0
    private var uTextureUnitLocation = 0
    private var uMatrixLocation = 0

    //матрица проекции
    private val mProjectionMatrix = FloatArray(16)

    //матрица камеры
    private val mViewMatrix = FloatArray(16)

    //матрица объекта
//    private val mModelMatrix = FloatArray(16)

    //итоговая матрица
    private val mMatrix = FloatArray(16)

//    private var texture = 0
//    private var texture2 = 0

    private lateinit var gObjects: ArrayList<AbstractGObject>

    private lateinit var params: Params

    constructor(params: Params, gObjects: ArrayList<AbstractGObject>) : this() {
        this.gObjects = gObjects
        this.params = params
    }

    private fun prepareData() {
        val vertices = FloatArray(gObjects.size * STRIDE)
        for (i in 0..gObjects.size - 1) {
            val gObject = gObjects.get(i)
            var j = 0
            for (vert in gObject.prepareVertexes(params.context!!, i * 4)) {
                vertices[i * STRIDE + j++] = vert
            }
        }
//        val vertices = floatArrayOf(
//            -0.5f, 1.0f, -8f, 0f, 0f,
//            -0.5f, 0.0f, -8f, 0f, 1f,
//            0.5f, 1.0f, -8f, 1f, 0f,
//            0.5f, 0.0f, -8f, 1f, 1f,
//
//            -0.5f, 0.0f, -1f, 0f, 0f,
//            -0.5f, -1.0f, -1f, 0f, 1f,
//            0.5f, 0.0f, -1f, 1f, 0f,
//            0.5f, -1.0f, -1f, 1f, 1f
//        )

//            [0.0, 1.0, -8.0, 0.0, 0.0,
//            0.0, 0.0, -8.0, 0.0, 1.0,
//            1.0, 1.0, -8.0, 1.0, 0.0,
//            1.0, 0.0, -8.0, 1.0, 1.0,
//            0.0, 0.5, -1.0, 0.0, 0.0,
//            0.0, 0.0, -1.0, 0.0, 1.0,
//            0.5, 0.5, -1.0, 1.0, 0.0,
//            0.5, 0.0, -1.0, 1.0, 1.0]

        vertexData = ByteBuffer.allocateDirect(vertices.size * 4).order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        //vertices.size * 4 is size of float array to float size (4 bytes)
        vertexData.put(vertices)

//        texture = TextureUtils.loadTexture(params.context, R.drawable.h_ny_2022_new)
//        texture2 = TextureUtils.loadTexture(params.context, R.drawable.head_new)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        glClearColor(0f, 0.9f, 1f, 1f)

        //включае альфа канал для прозрачных частей текстур
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        //грузим шейдеры и создаём программу
        createProgram()

        //получаем ссылки на переменные в шейдерах
        getLocations()

        //готовим массив точек для отрисовки и текстуры
        prepareData()

        //какая-то херня, нужно разбираться
        bindData()
    }

    private fun createProgram() {
        val vertexShaderId =
            ShaderUtils.createShader(params.context!!, GL_VERTEX_SHADER, R.raw.vertex_shader)
        val fragmentShaderId =
            ShaderUtils.createShader(params.context!!, GL_FRAGMENT_SHADER, R.raw.fragment_shader)
        programId = ShaderUtils.createProgram(vertexShaderId, fragmentShaderId)
        glUseProgram(programId)
    }

    private fun getLocations() {
        uTextureUnitLocation = glGetUniformLocation(programId, "u_TextureUnit")
        uMatrixLocation = glGetUniformLocation(programId, "u_Matrix")
        aPositionLocation = glGetAttribLocation(programId, "a_Position")
        aTextureLocation = glGetAttribLocation(programId, "a_Texture")
    }

    private fun createViewMatrix() {
//        // точка положения камеры
//        val eyeX = 0f
//        val eyeY = 0f
//        val eyeZ = 0f
//
//        // точка направления камеры
//        val centerX = 0f
//        val centerY = 0f
//        val centerZ = -10f
//
//        // up-вектор
//        val upX = 0f
//        val upY = 1f
//        val upZ = 0f
        // точка положения камеры
        val eyeX = params.camera.position.x
        val eyeY = params.camera.position.y
        val eyeZ = params.camera.position.z

        // точка направления камеры
        val centerX = params.camera.target.x
        val centerY = params.camera.target.y
        val centerZ = params.camera.target.z

        // up-вектор
        val upX = params.camera.vector.x
        val upY = params.camera.vector.y
        val upZ = params.camera.vector.z
        Matrix.setLookAtM(
            mViewMatrix, 0, eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ
        )
    }

    private fun bindData() {
        // координаты вершин

        // координаты вершин
        vertexData.position(0)
        glVertexAttribPointer(
            aPositionLocation, POSITION_COUNT, GL_FLOAT,
            false, STRIDE, vertexData
        )
        glEnableVertexAttribArray(aPositionLocation)

        // координаты текстур

        // координаты текстур
        vertexData.position(POSITION_COUNT)
        glVertexAttribPointer(
            aTextureLocation, TEXTURE_COUNT, GL_FLOAT,
            false, STRIDE, vertexData
        )
        glEnableVertexAttribArray(aTextureLocation)

        // помещаем текстуру в target 2D юнита 0
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)

        // юнит текстуры
        GLES20.glUniform1i(uTextureUnitLocation, 0)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        glViewport(0, 0, width, height)

        //создаём матрицу проекции, которая описывает параметры видимой зоны
        createProjectionMatrix(width, height)
    }

    var frames = 0
    var startDate: Date? = null
    var angle = 0f

    override fun onDrawFrame(gl: GL10?) {
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        val cDate = Date()
        if (startDate != null) {
            if (cDate.time - startDate!!.time < 5000)
                frames++
            else {
                val fr = frames
                frames = 0
                startDate = cDate
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(params.context, (fr / 5).toString(), Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            startDate = cDate
        }
//        params.camera.target.y+=0.0001f
//        angle+=0.00001f
//        params.camera.spinAroundTarget(XYZpoint(angle,0f,0f))
//        gObjects.get(2).rotation.z +=0.1f
//        gObjects.get(3).rotation.z +=0.1f
//        gObjects.get(0).position.y -= 2
//        gObjects.get(2).position.y ++
//        gObjects.get(3).position.y ++
//        params.camera.position.y++
//        params.camera.target.y++

        //Создаём матрицу для камеры
        createViewMatrix()

        for (gObject in gObjects) {
            bindMatrix(getModelMatrix(gObject))
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, gObject.textureId)
            glDrawArrays(GLES20.GL_TRIANGLE_STRIP, gObject.startIndex, 4)
        }

//        bindMatrix(null)
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture)
//        glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
//
////        Matrix.setIdentityM(mModelMatrix, 0)
////        setModelMatrix()
//        bindMatrix(null)
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture2)
//        glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 4, 4)
    }

    private fun createProjectionMatrix(width: Int, height: Int) {
//        //размеры задней площадки
//        var left = -1f
//        var right = 1f
//        var bottom = -1f
//        var top = 1f
//
//        //расстояние от камеры до передней видимой границы
//        val near = 1f
//
//        //расстояние от камеры до задней видимой границы
//        val far = 8f
//
//        //пересчитываем размеры площадки так, чтобы изображение не искажалось
//        // на разных экранах и при повороте
//        if (width > height) {
//            val ratio = width.toFloat() / height
//            left *= ratio
//            right *= ratio
//        } else {
//            val ratio = height.toFloat() / width
//            bottom *= ratio
//            top *= ratio
//        }

        //размеры задней площадки
        var left = params.scene.start.x
        var right = params.scene.start.x + params.scene.width
        var bottom = params.scene.start.y
        var top = params.scene.start.y + params.scene.height

        //расстояние от камеры до передней видимой границы
        val near = params.scene.start.z

        //расстояние от камеры до задней видимой границы
        val far = params.scene.start.z + params.scene.depth

        //пересчитываем размеры площадки так, чтобы изображение не искажалось
        // на разных экранах и при повороте
        if (width > height) {
            //todo нужно отталкиваться только от ширины или только от высоты, а вторую координату считать
            val ratio = width.toFloat() / height
            left *= ratio
            right *= ratio

            //todo near && far && camera position xz camera target xz?
        } else {
            val ratio = height.toFloat() / width
            bottom *= ratio
            top *= ratio
            //todo camera target y camera position y?
        }

        /* TODO вместо  Matrix.frustumM(... попробовать Matrix.orthoM(... для отключения 3d перспективы*/
        Matrix.orthoM(mProjectionMatrix, 0, left, right, bottom, top, near, far)
    }

    private fun bindMatrix(mModelMatrix: FloatArray?) {
        if (mModelMatrix != null) {
            Matrix.multiplyMM(mMatrix, 0, mViewMatrix, 0, mModelMatrix, 0)
            Matrix.multiplyMM(mMatrix, 0, mProjectionMatrix, 0, mMatrix, 0)
        } else {
            Matrix.multiplyMM(mMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0)
        }
        glUniformMatrix4fv(uMatrixLocation, 1, false, mMatrix, 0)
    }

//    private fun setModelMatrix() {
//        Matrix.setIdentityM(mModelMatrix, 0)
//        Matrix.rotateM(mModelMatrix, 0, 45f, 0f, 0f, 1f)
//    }

    private fun getModelMatrix(gObject: AbstractGObject): FloatArray {


        var mModelMatrix = FloatArray(16)
        Matrix.setIdentityM(mModelMatrix, 0)
        if (gObject.parent != null) {

            mModelMatrix = getModelMatrix(gObject.parent!!)
        }


        val point = gObject.getDrawPosition().add(gObject.getDrawRotationPos())
        Matrix.translateM(
            mModelMatrix,
            0,
            point.x,
            point.y,
            point.z
        )

//        Matrix.translateM(
//            mModelMatrix,
//            0,
//            -gObject.getDrawRotationPos().x,
//            -gObject.getDrawRotationPos().y,
//            -gObject.getDrawRotationPos().z
//        )

//            Matrix.translateM(
//            mModelMatrix,
//            0,
//            gObject.getDrawPosition().x,
//            gObject.getDrawPosition().y,
//            gObject.getDrawPosition().z
//            )

        Matrix.rotateM(
            mModelMatrix,
            0,
            gObject.getDrawRotation().z,
            0f,
            0f,
            -1f,
        )

        Matrix.rotateM(
            mModelMatrix,
            0,
            gObject.getDrawRotation().x,
            1f,
            0f,
            0f,
        )

        Matrix.rotateM(
            mModelMatrix,
            0,
            gObject.getDrawRotation().y,
            0f,
            1f,
            0f,
        )


//        Matrix.translateM(
//            mModelMatrix,
//            0,
//            gObject.getDrawPosition().x,
//            gObject.getDrawPosition().y,
//            gObject.getDrawPosition().z
//        )

        Matrix.translateM(
            mModelMatrix,
            0,
            -gObject.getDrawRotationPos().x,
            -gObject.getDrawRotationPos().y,
            -gObject.getDrawRotationPos().z
        )

//        Matrix.rotateM(mModelMatrix, 0, 0f, 0f, 0f, 1f)
//        if (gObject.parent!=null){
//            val result = FloatArray(16)
//
//            Matrix.multiplyMV(result,0,getModelMatrix(gObject.parent!!),0,mModelMatrix,0)
//            return result
//        }

        return mModelMatrix
    }

    companion object {
        lateinit var defRender: Deferred<MainRender>
        private var render: MainRender? = null

        suspend fun getRender(): MainRender {
            return defRender.await()
        }

        fun getRenderJ(): CompletableFuture<MainRender> =
            GlobalScope.future(Dispatchers.Main) { getRender() }

        class Params {
            var context: Context? = null
            var scene = Scene()
            var camera = Camera()
        }

        class Scene {
            var start: XYZpoint = XYZpoint(0f, 0f, 0f)
            var height: Int = 0
            var width: Int = 0
            var depth: Int = 0
        }

    }

}