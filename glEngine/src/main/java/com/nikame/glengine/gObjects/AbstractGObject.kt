package com.nikame.glengine.gObjects

import android.content.Context
import com.nikame.glengine.Entity.XYZpoint
import java.lang.Float.max
import java.util.UUID

abstract class AbstractGObject {

    var flags = 0

    constructor(drawableId: Int, width: Float, height: Float) {
        this.drawableId = drawableId
        this.height = height
        this.width = width
        this.baseCollisionRadius = max(height, width) / 2
    }

    constructor(drawableId: Int, width: Float, height: Float, depth: Float)
            : this(drawableId, width, height) {
        this.depth = depth
    }

    var uuid = UUID.randomUUID()
    var drawableId: Int = 0

    var mModelMatrix: FloatArray? = null
    var bindedMatrix: FloatArray? = null

    /**
     * размеры изображения
     */
    var height = 0f
    var width = 0f
    var depth = 0f
    var baseCollisionRadius = 0f

    /**
     *  ссылка на загруженную текстуру
     */
    var textureId: Int = 0

    /**
     * начальный индекс в буффере вертексов
     */
    var startIndex = 0


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

    val childsBack = ArrayList<AbstractGObject>()
    val childsTop = ArrayList<AbstractGObject>()

    var speed = XYZpoint(0f, 0f, 0f)

    var acceleration = XYZpoint(0f, 0f, 0f)

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


    //TODO есть ли сейчас смысл в этих абстрактных методах? может, стоит по умолчанию в них отдавать
    // значения, а оверрайд в конкретных классах по ним будет только при необходимости
    abstract fun getDrawPosition(): XYZpoint
    abstract fun getDrawRotation(): XYZpoint
    abstract fun getDrawRotationPos(): XYZpoint
    abstract fun onCollisionFinded(collised: AbstractGObject)

    fun addChild(child: AbstractGObject) {
        if (child.position.z > 0) {
            childsTop.add(child)
            childsTop.sortBy { o -> o.position.z }
        } else {
            childsBack.add(child)
            childsBack.sortBy { o -> o.position.z }
        }
    }

    override fun equals(other: Any?): Boolean {
        return other != null && other is AbstractGObject && uuid.equals(other.uuid)
    }

    fun update(timeCoeffitient: Float) {
        if (flags and FLAG_MOVABLE > 0) {
            speed = speed.add(acceleration.multiply(timeCoeffitient))
            position = position.add(speed.multiply(timeCoeffitient))
        }
    }

    abstract fun clone(): AbstractGObject

//    companion object {
//        interface DateAnimable {
//            var rotation: XYZpoint
//
//            var targetRotation: XYZpoint
//
//            var startDate: Date
//            var lastDate: Date
//
//
//            var length: Long
//
//            fun getCurrentRotation(): XYZpoint {
//                val currentDate = Date()
//                val currentShift = currentDate.time - startDate.time
//                if (currentShift > length) {
//                    //change current animation and set currentshift to zero
//                }
//
//                var sub = targetRotation.sub(rotation)
//
//                val coef =
//                    (currentDate.time - lastDate.time).toFloat() / (length - lastDate.time).toFloat()
//                rotation = rotation.add(sub.multiply(coef))
//                lastDate = currentDate
//                return rotation
//            }
//        }
//
//        interface Movable {
//
//        }
//    }


    var baseRadius: Float? = null
    var elasticity: Float = 1f
    var mass: Float = 1f

    fun collide(other: AbstractGObject): Boolean {
        if (position.add(center).sub(other.position.add(other.center))
                .length() < baseCollisionRadius + other.baseCollisionRadius
        ) {
            val speed1 = speed
            val speed2 = other.speed
            speed = speed2
            other.speed = speed1
            return true
        }
        return false
    }

}

val FLAG_MOVABLE = 0x000001
val FLAG_COLLISIBLE = 0x000010
