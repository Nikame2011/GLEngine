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
import com.nikame.glengine.Entity.Camera
import com.nikame.glengine.Entity.XYZpoint
import com.nikame.glengine.R
import com.nikame.glengine.gObjects.AbstractGObject
import com.nikame.glengine.gObjects.FLAG_MOVABLE
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.future
import java.lang.Math.sqrt
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

    lateinit var gObjects: ArrayList<AbstractGObject>
    lateinit var static: ArrayList<AbstractGObject>
    lateinit var movable: ArrayList<AbstractGObject>

    private lateinit var params: Params

    constructor(params: Params, gObjects: ArrayList<AbstractGObject>) : this() {
        this.gObjects = gObjects
        this.static = gObjects.filter { it.flags and FLAG_MOVABLE == 0 } as ArrayList
        this.movable = gObjects.filter { it.flags and FLAG_MOVABLE > 0 } as ArrayList
        this.params = params
    }

    fun getSize(objects: ArrayList<AbstractGObject>): Int {
        var size = objects.size
        for (gObject in objects) {
            size += getSize(gObject.childsBack)
            size += getSize(gObject.childsTop)
        }
        return size
    }

    fun putVertices(
        vertices: FloatArray,
        currentPosition: Int,
        objects: ArrayList<AbstractGObject>
    ): Int {
        var position = currentPosition
        for (gObject in objects) {
            var j = 0
            for (vert in gObject.prepareVertexes(params.context!!, position * 4)) {
                vertices[position * STRIDE + j++] = vert
            }
            position++
            position = putVertices(vertices, position, gObject.childsBack)
            position = putVertices(vertices, position, gObject.childsTop)
        }
        return position
    }

    private fun prepareData() {
        val vertices = FloatArray(getSize(gObjects) * STRIDE)
        putVertices(vertices, 0, gObjects)
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
        //todo цвет должен задаваться через параметры сцены
        glClearColor(0f, 0f, 0f, 1f) //glClearColor(0f, 0.9f, 1f, 1f)

        //включает альфа канал для прозрачных частей текстур
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

//        //включает буффер глубины, для определения, какие точки нужно рисовать
//        glEnable(GL_DEPTH_TEST)
//
//        //включает тестирование альфа-канала, все пиксели с альфа меньше 0.1f будут проигнорированы
//        glEnable (GL_ALPHA_TEST)
//        glAlphaFunc (GLES10.GL_EQUAL, 1f)

        //todo сделать два варианта отрисовки кадров: с полу-прозрачными изображениями и с дискретной
        // прозрачностью (только ПРОЗРАЧНО или НЕ ПРОЗРАЧНО)
        // для первого варианта использовать ручную сортировку объектов по z-координате перед отображением
        // для второго использовать glEnable(GL_DEPTH_TEST), glEnable (GL_ALPHA_TEST) и glAlphaFunc (GL_GREATER, 0.1)
        // связано с тем, что в случае частичной прозрачности важен порядок отрисовки объектов и частично прозрачные
        // должны рисоваться позже непрозрачных и так же иметь порядок отрисовки между собой
        // в случае же дискретности можно полностью отбросить прозрачные пиксели а для оставшихся непрозрачных использовать буффер глубины
        // https://www.reddit.com/r/opengl/comments/8tes3h/opengl_depth_testing_and_alpha_transparency/
        // UPD. решение с дискретной альфой и буффером глубины почему-то не работает. оставим вариант с сортировкой при рисовании.
        // UPD2. попробовать в фрагментном шейдере такой код: должен работать вместе с буффером
        // vec4 color = texture2D(texture, outTexCoords) * outColor;
        // if(color.a < 0.5)
        // {
        //   discard;
        // }
        // gl_FragColor = vec4(color.r,color.g,color.b,1);

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

//    var lastCamXYZ = XYZpoint(0f, 0f, 0f)
//    var lastTarXYZ = XYZpoint(0f, 0f, 0f)

    private fun createViewMatrix(targetObject: AbstractGObject?) {
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

//        lastCamXYZ = targetObject!!.position
        // точка положения камеры
        val position = params.camera.getPosition(targetObject)
        val eyeX = position.x
        val eyeY = position.y
        val eyeZ = position.z

        // точка направления камеры
        val target = params.camera.getTarget(targetObject)
        val centerX = target.x
        val centerY = target.y
        val centerZ = target.z

        // up-вектор
        val vector = params.camera.getVector(targetObject)
        val upX = vector.x
        val upY = vector.y
        val upZ = vector.z
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
    override fun onDrawFrame(gl: GL10?) {

        val lastPositionsMove = movable.map { it.clone() }
        bufferMatrix = null

        val lastPositions = ArrayList<AbstractGObject>(static)//gObjects//.map { it.clone() }
        lastPositions.addAll(lastPositionsMove)
        //todo разделить передвигаемые и статичные объекты. статичные объекты рисовать без всяких копирований итд
        // для передвигаемых объектов ввести параметр "последняя дата обновления" (один на все объекты) и отрисовывать
        // их только когда он отличается от последнего нарисованного

        lastPositions.let {
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
            val cDate = Date()
            if (startDate != null) {
                if (cDate.time - startDate!!.time < 2000)
                    frames++
                else {
//                    val fr = frames / 2
                    frames = 0
                    startDate = cDate
//                    Log.e("frame", "on draw ${fr}")
//                    CoroutineScope(Dispatchers.Main).launch {
//                        Toast.makeText(params.context, (fr ).toString(), Toast.LENGTH_SHORT)
//                            .show()
//                    }
//                    }
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
            val pos = it.indexOf(params.camera.targetObject)
            //Создаём матрицу для камеры
            createViewMatrix(if (pos == -1) null else it.get(pos))

            bindAndDraw(it, null)

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
    }

    fun bindAndDraw(gObjecto: List<AbstractGObject>, parentMatrix: FloatArray?) {

        var binding = 0
        var saved = 0

        for (gObject in gObjecto) {
//            if(gObject.equals(params.camera.targetObject)){
//                lastTarXYZ=gObject.position
//                if(lastCamXYZ.x!=lastTarXYZ.x|| lastCamXYZ.y!=lastTarXYZ.y||lastCamXYZ.z!=lastTarXYZ.z)
//                    Log.e("Render","Wrong camera position: ${lastCamXYZ.x} /${lastTarXYZ.x} //${lastCamXYZ.y} /${lastTarXYZ.y} //${lastCamXYZ.z} /${lastTarXYZ.z}")
//            }

            if (parentMatrix == null) {
                if (gObject.position.sub(params.camera.lastPosition)
                        .lengthPow() > params.scene.visibleRadiusPow
//                        .length() > params.scene.visibleRadius
                )
                    continue
            }

            val objectMatrix = getModelMatrix(gObject, parentMatrix?.clone())

            if (gObject.childsBack.size > 0) {
                bindAndDraw(gObject.childsBack, objectMatrix)
            }


            if (gObject.flags and FLAG_MOVABLE == 0 && gObject.bindedMatrix != null) {
                glUniformMatrix4fv(uMatrixLocation, 1, false, gObject.bindedMatrix, 0)
                saved++
            } else {
                bindMatrix(gObject, objectMatrix)
                binding++
            }

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, gObject.textureId)
            glDrawArrays(GLES20.GL_TRIANGLE_STRIP, gObject.startIndex, 4)

            if (gObject.childsTop.size > 0) {
                bindAndDraw(gObject.childsTop, objectMatrix)
            }
        }
//        Log.e("bindingCount","binding ${binding}, saved ${saved}")
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

        val updatedHeight = (top - params.scene.start.y).toInt()
        val updatedWidth = (params.scene.start.x - right).toInt()
        params.scene.visibleRadiusPow =
            ((updatedHeight * updatedHeight + updatedWidth * updatedWidth) / 3.24).toFloat()
        params.scene.visibleRadius =
            (sqrt((updatedHeight * updatedHeight + updatedWidth * updatedWidth).toDouble()) / 1.8f).toFloat()

        /* TODO вместо  Matrix.frustumM(... попробовать Matrix.orthoM(... для отключения 3d перспективы*/
        Matrix.orthoM(mProjectionMatrix, 0, left, right, bottom, top, near, far)
    }

    var bufferMatrix: FloatArray? = null

    private fun bindMatrix(gObject: AbstractGObject, mModelMatrix: FloatArray?) {
        if (mModelMatrix != null) {
            if (bufferMatrix == null) {
                bufferMatrix = FloatArray(16)
                Matrix.multiplyMM(bufferMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0)
            }
            Matrix.multiplyMM(mMatrix, 0, bufferMatrix, 0, mModelMatrix, 0)

//            Matrix.multiplyMM(mMatrix, 0, mViewMatrix, 0, mModelMatrix, 0)
//            Matrix.multiplyMM(mMatrix, 0, mProjectionMatrix, 0, mMatrix, 0)
        } else {
            Matrix.multiplyMM(mMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0)
        }
//        gObject.bindedMatrix = mMatrix.clone()
//        val index=gObjects.indexOf(gObject)
//        if(index>-1)
//            gObjects.get(index).bindedMatrix = mMatrix.clone()
        glUniformMatrix4fv(uMatrixLocation, 1, false, mMatrix, 0)
    }

//    private fun setModelMatrix() {
//        Matrix.setIdentityM(mModelMatrix, 0)
//        Matrix.rotateM(mModelMatrix, 0, 45f, 0f, 0f, 1f)
//    }

    private fun getModelMatrix(gObject: AbstractGObject, parentMatrix: FloatArray?): FloatArray {

        var mModelMatrix = FloatArray(16)
        Matrix.setIdentityM(mModelMatrix, 0)

        if (parentMatrix != null) {
            mModelMatrix = parentMatrix
        } else if (gObject.flags and FLAG_MOVABLE == 0 && gObject.mModelMatrix != null) {
            return gObject.mModelMatrix!!
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
        //gObject.mModelMatrix = mModelMatrix
        val index = gObjects.indexOf(gObject)
        if (index > -1)
            gObjects.get(index).mModelMatrix = mModelMatrix

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
            var visibleRadius: Float = 0f
            var visibleRadiusPow: Float = 0f

        }
    }
}