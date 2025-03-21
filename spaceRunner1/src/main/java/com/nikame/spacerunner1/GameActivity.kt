package com.nikame.spacerunner1

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import com.nikame.glengine.Entity.XYZpoint
import com.nikame.glengine.GraphicActivity
import com.nikame.glengine.gObjects.AbstractGObject
import com.nikame.glengine.gObjects.FLAG_COLLISIBLE
import com.nikame.glengine.gObjects.FLAG_MOVABLE
import com.nikame.glengine.graphics.MainRender
import kotlin.random.Random

class GameActivity : GraphicActivity() {

    var dw = 0
    var dh = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dw = resources.displayMetrics.widthPixels //получаем ширину экрана

        dh = resources.displayMetrics.heightPixels //получаем ширину экрана

//        glSurfaceView.rootView.setOnTouchListener({ v, event -> onTouch(v, event) })

        onProgressChanged = { progress: Int, status: String ->

            if (progress == 100)
                glSurfaceView.rootView.setOnTouchListener({ v, event -> onTouch(v, event) })
        }
    }

    override fun buildParams(): MainRender.Companion.Params {
        val par = MainRender.Companion.Params()

        par.context = applicationContext

        //TODO разбираться, как правильно работать с камерой, прицел по координатам идёт не логично, или я что-то не понимаю
        par.scene.start = XYZpoint(-350f, -350f, 5f)
        par.scene.width = 700
        par.scene.height = 700
        par.scene.depth = 10
        par.camera.distanceTarget = XYZpoint(0f, 0f, 10f)
        par.camera.targetCoord = XYZpoint(0f, 0f, 0f)
        par.camera.targetObject = user


//        par.camera.distanceTarget = XYZpoint(0f, 0f, 5f)
//        par.camera.targetCoord = XYZpoint(0f, 0f, 0f)
        return par
    }

    lateinit var user: Ship

    val stars = ArrayList<Part>()

    override fun buildGObjects(): ArrayList<AbstractGObject> {
        val gObjects = arrayListOf<AbstractGObject>()
        val rand = Random(0)
        val x = rand.nextInt(1000)
        for (i in 0..2000 + x) {
            val size = 0.5f + rand.nextFloat() * 3.5f
            val star = Part(R.drawable.star, size, size, 0f)
            star.position = XYZpoint(-2000f + rand.nextInt(4000), -2000f + rand.nextInt(4000), 0f)
            stars.add(star)
        }

        gObjects.addAll(stars)

//        val fone = Part(R.drawable.ff2, 500f, 2500f, 0f)
//        fone.position = XYZpoint(100f, 3000f, 10f)
//        gObjects.add(fone)
//
//        val fone2 = Part(R.drawable.ff1, 700f, 3125f, 0f)
//        fone2.position = XYZpoint(0f, 0f, 2f)
//        gObjects.add(fone2)

        user = Ship()
        user.position = XYZpoint(350f, 350f, 5f)
        gObjects.add(user)

        val user2 = Ship()
        user2.position = XYZpoint(100f, 100f, 5f)
        gObjects.add(user2)


//        user = Part(R.drawable.body, 300f, 300f, 0f)
//        user.center = XYZpoint(150f, 150f, 2f)

//        val elevon = Part(R.drawable.elerons, 300f, 300f, 0f)
//        elevon.position = XYZpoint(0f, 0f, 1f)
//        elevon.center = XYZpoint(150f, 150f, 0f)

//        gObjects.add(Part(R.drawable.ff2, 500f, 2500f, 0f))
//        gObjects.add(Part(R.drawable.ff1, 700f, 3125f, 0f))

//       val elevon = Part(R.drawable.elerons, 300f, 300f, 0f)
//        elevon.position = XYZpoint(350f, 500f,5f)
//        elevon.center = XYZpoint(150f, 150f)
//        gObjects.add(elevon)
//
//
//       val user2 = Ship2()
//        user2.position = XYZpoint(350f, 150f, 5f)
//        gObjects.add(user2)


//        user.childs.add(elevon)
//        gObjects.get(0).position = XYZpoint(100f, 3000f, 10f)
//        gObjects.get(1).position = XYZpoint(0f, 0f, 10f)
//        gObjects.get(2).parent = gObjects.get(3)
        return gObjects as ArrayList<AbstractGObject>
    }

    class Part : AbstractGObject {

        constructor(imgResId: Int, width: Float, height: Float, depth: Float)
                : super(imgResId, width, height, depth)

        override fun getDrawPosition(): XYZpoint = position

        override fun getDrawRotation(): XYZpoint = rotation

        override fun getDrawRotationPos(): XYZpoint = center
        override fun onCollisionFinded(collised: AbstractGObject) {
        }

        override fun clone(): AbstractGObject {
            val copy = Part(drawableId, width, height, depth)

            copy.uuid = uuid
            copy.flags = flags
            copy.textureId = textureId
            copy.startIndex = startIndex
            copy.position = XYZpoint(position.x, position.y, position.z)
            copy.rotation = XYZpoint(rotation.x, rotation.y, rotation.z)
            copy.center = center

            copy.childsBack.clear()
            for (child in childsBack) {
                copy.childsBack.add(child.clone())
            }

            copy.childsTop.clear()
            for (child in childsTop) {
                copy.childsTop.add(child.clone())
            }

            copy.speed = speed
            copy.acceleration = acceleration

            copy.mModelMatrix = mModelMatrix
            return copy
        }
    }

    class Ship2 : AbstractGObject {
        lateinit var elevon: Part

        constructor() : super(
            R.drawable.body, 300f, 300f, 0f
        ) {
            center = XYZpoint(150f, 150f)

            val elevon = Part(R.drawable.elerons, 300f, 300f, 0.0f)
            elevon.position = XYZpoint(0f, 120f, 5f)
            elevon.center = XYZpoint(150f, 150f)
            //childs.add(elevon)
        }

        override fun getDrawPosition(): XYZpoint = position

        override fun getDrawRotation(): XYZpoint = rotation

        override fun getDrawRotationPos(): XYZpoint = center

        override fun onCollisionFinded(collised: AbstractGObject) {
        }

        override fun clone(): AbstractGObject {
            val copy = Ship2()
            copy.uuid = uuid
            copy.flags = flags
            copy.textureId = textureId
            copy.startIndex = startIndex
            copy.position = XYZpoint(position.x, position.y, position.z)
            copy.rotation = XYZpoint(rotation.x, rotation.y, rotation.z)
            copy.center = center

            copy.childsBack.clear()
            for (child in childsBack) {
                copy.childsBack.add(child.clone())
            }

            copy.childsTop.clear()
            for (child in childsTop) {
                copy.childsTop.add(child.clone())
            }

            copy.speed = speed
            copy.acceleration = acceleration

            copy.mModelMatrix = mModelMatrix
            return copy
        }
    }

    class Ship : AbstractGObject {
        lateinit var elevonR: Part
        lateinit var elevonL: Part
        lateinit var elevonC: Part
        lateinit var gondole: Part
        lateinit var engine: Part

        //300/854 =                    0,351288056206089
//body - source 854x854 - from 347x108, size 162x592 / game size (old) - 300x300
        //el l 299x477 - 111x318
        //el r 446x477 111x318
        //el c 421x476 - 14x320
        //gond 386x216 - 84x234
        constructor() : super(
            R.drawable.body_col, 56.9f, 207f, 0f
        ) {
            center = XYZpoint(28.45f, 95.9f)

            elevonL = Part(R.drawable.elerone_l_col, 38.99f, 111.71f, 0f)
            elevonL.position = XYZpoint(-17.19f, -33.37f, 0f)
            elevonL.center = XYZpoint(12f, 70f)
            childsBack.add(elevonL)

            elevonR = Part(R.drawable.elerone_r_col, 38.99f, 111.71f, 0f)
            elevonR.position = XYZpoint(35.1f, -33.37f, 0f)
            elevonR.center = XYZpoint(12f, 70f)
            childsBack.add(elevonR)
//
//
//            engine = Part(R.drawable.engine_col, 300f, 300f, 0f)
//            engine.position = XYZpoint(0f, 0f, 0f)
//            engine.center = XYZpoint(150f, 150f)
//            childsTop.add(engine)
//
            elevonC = Part(R.drawable.elerone_c_col, 4.91f, 111.71f, 0f)
            elevonC.position = XYZpoint(25.995f, -33.37f, 0f)
            elevonC.center = XYZpoint(2.455f, 700f)
            childsTop.add(elevonC)

            gondole = Part(R.drawable.gondole_col, 29.51f, 82.2f, 0f)
            gondole.position = XYZpoint(13.7f, 87.82f, 0f)
            gondole.center = XYZpoint(14.755f, 41.1f)
            childsTop.add(gondole)

            flags = FLAG_MOVABLE or FLAG_COLLISIBLE
        }

        override fun getDrawPosition(): XYZpoint = position

        override fun getDrawRotation(): XYZpoint = rotation

        override fun getDrawRotationPos(): XYZpoint = center

        override fun onCollisionFinded(collised: AbstractGObject) {
        }

        override fun clone(): AbstractGObject {
            val copy = Ship()
            copy.uuid = uuid
            copy.flags = flags
            copy.textureId = textureId
            copy.startIndex = startIndex
            copy.position = XYZpoint(position.x, position.y, position.z)
            copy.rotation = XYZpoint(rotation.x, rotation.y, rotation.z)
            copy.center = center

            copy.childsBack.clear()
            for (child in childsBack) {
                copy.childsBack.add(child.clone())
            }

            copy.childsTop.clear()
            for (child in childsTop) {
                copy.childsTop.add(child.clone())
            }

            copy.speed = speed
            copy.acceleration = acceleration

            copy.mModelMatrix = mModelMatrix
            return copy
        }
    }

    var startX = 0f
    var movedX = 0f
    var startY = 0f
    var movedY = 0f
    var moving = false
    var rotating = false
    var d = 0f

    override fun onFrameChanged() {
        rolling_inertial()
    }

    var rot = 0f

    var accelX = 0f
    var accelY = 0f
    var angle = 0f

    fun rolling_inertial() {
        if (rotating) {
            angle += rot / 10
            if (angle < 0) angle += 360f
            if (angle > 360) {
                angle -= 360f
            } else if (angle < -360) {
                angle += 360f
            }
        }

        if (moving) {
            if (Math.abs(movedX - startX) > dw / 8) accelX =
                ((movedX - startX) * 0.5f / dw) else accelX = 0f
            if (Math.abs(movedY - startY) > dw / 8) accelY =
                ((movedY - startY) * 0.5f / dw) else accelY = 0f
        } else {
            accelX = 0f
            accelY = 0f
        }

        user.rotation.z = angle
        user.acceleration = XYZpoint(accelX, -accelY, 0f).rotateTo(user.rotation)
//        user.rotation.z+=2
//        player.set_accel(x_ac, y_ac, angle)

//        camera.position=user.position.sub(XYZpoint(270f,310f,-5f))
//        camera.target=user.position.sub(XYZpoint(270f,310f,0f))

    }

    fun onTouch(button: View, motion: MotionEvent): Boolean {
        /* when (button.id) {
             R.id.Road_IV ->*/ when (motion.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                if (motion.pointerCount == 1) {
                    moving = true
                    rotating = false
                    rot = 0f
                    d = 0f
                }
                if (motion.pointerCount == 2) {
                    rotating = true
                    moving = false
                    startX = 0f
                    movedX = 0f
                    startY = 0f
                    movedY = 0f
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (motion.pointerCount == 1) {
                    moving = true
                    if (startX == 0f) startX = motion.x
                    movedX = motion.x
                    if (startY == 0f) startY = motion.y
                    movedY = motion.y
                    rotating = false
                    rot = 0f
                    d = 0f
                }
                if (motion.pointerCount == 2) {
                    rotating = true
                    if (d == 0f) d = Math.atan2(
                        (motion.getY(0) - motion.getY(1)).toDouble(),
                        (motion.getX(0) - motion.getX(1)).toDouble()
                    ).toFloat()
                    rot = Math.toDegrees(
                        Math.atan2(
                            (motion.getY(0) - motion.getY(1)).toDouble(),
                            (motion.getX(0) - motion.getX(1)).toDouble()
                        ) - d
                    ).toFloat()
                    if (rot > 200) rot -= 360f else if (rot < -200) rot += 360f
                    moving = false
                    startX = 0f
                    movedX = 0f
                    startY = 0f
                    movedY = 0f
                }
            }

            MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_UP -> {
                if (motion.pointerCount == 2) {
                    rotating = false
                    rot = 0f
                    d = 0f
                }
                if (motion.pointerCount == 1) {
                    rotating = false
                    rot = 0f
                    d = 0f
                    moving = false
                    startX = 0f
                    movedX = 0f
                    startY = 0f
                    movedY = 0f
                }
            }

            MotionEvent.ACTION_CANCEL -> {
                rotating = false
                rot = 0f
                d = 0f

                moving = false
                startX = 0f
                movedX = 0f
                startY = 0f
                movedY = 0f
            }
        }
//        Log.e("moving", "${startX}, ${movedX}, ${startY}, ${movedY}")
        //}
        return false
    }
}