package com.nikame.glengine.gObjects

import android.content.Context
import com.nikame.glengine.Entity.XYZpoint
import java.util.Date

abstract class AbstractGObject {
    constructor(imgResId: Int, width: Float, height: Float) {
        this.drawableId = imgResId
        this.height = height
        this.width = width
    }

    constructor(imgResId: Int, width: Float, height: Float, depth: Float)
            : this(imgResId, width, height) {
        this.depth = depth
    }

    var parent: AbstractGObject? = null

    var drawableId: Int = 0

    /**
     *  ссылка на загруженную текстуру
     */
    var textureId: Int = 0

    /**
     * размеры изображения
     */
    var height = 0f
    var width = 0f
    var depth = 0f

    /**
     * начальный индекс в буффере вертексов
     */
    var startIndex = 0

    fun prepareVertexes(context: Context, startIndex: Int): FloatArray {
        textureId = com.nikame.glengine.graphics.TextureUtils.loadTexture(context, drawableId)
        this.startIndex = startIndex
        return floatArrayOf(
            0f, height, depth, 0f, 0f,
            0f, 0f, depth, 0f, 1f,
            width, height, depth, 1f, 0f,
            width, 0f, depth, 1f, 1f,
        )
    }

    /**
     * позиция объекта относительно нулевых координат или относительно позиции родительского объекта
     */
    var position = XYZpoint(0f, 0f, 0f)

    /**
     *
     */
    var rotation = XYZpoint(0f, 0f, 0f)


    /**
     * центр вращения
     */
    var center = XYZpoint(0f, 0f, 0f)

    abstract fun getDrawPosition(): XYZpoint
    abstract fun getDrawRotation(): XYZpoint
    abstract fun getDrawRotationPos(): XYZpoint
//
//    val childs=ArrayList<AbstractGObject>()
//
//    fun addChildOnTop(child:AbstractGObject){
//
//    }
//
//    fun addChildToBottom(child:AbstractGObject){
//
//    }

    companion object {
        interface DateAnimable {
            var rotation: XYZpoint

            var targetRotation: XYZpoint

            var startDate: Date
            var lastDate: Date


            var length: Long

            fun getCurrentRotation(): XYZpoint {
                val currentDate = Date()
                val currentShift = currentDate.time - startDate.time
                if (currentShift > length) {
                    //change current animation and set currentshift to zero
                }

                var sub = targetRotation.sub(rotation)

                val coef =
                    (currentDate.time - lastDate.time).toFloat() / (length - lastDate.time).toFloat()
                rotation = rotation.add(sub.multiply(coef))
                lastDate = currentDate
                return rotation
            }
        }

        interface Movable {

        }
    }
}