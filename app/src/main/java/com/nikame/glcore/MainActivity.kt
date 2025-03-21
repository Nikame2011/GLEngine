package com.nikame.glcore

import android.content.Context
import android.os.Bundle
import com.nikame.glengine.Entity.XYZpoint
import com.nikame.glengine.GraphicActivity
import com.nikame.glengine.gObjects.AbstractGObject
import com.nikame.glengine.graphics.MainRender
import kotlin.random.Random

/*
* создаём initActivity, в котором происходит полная активация ресурсов
* в ней: создаём все графические объекты, передавая в них ссылки на ресурсы,
* из этих ссылок должны получаться текстуры и набор точек, подобранных по размеру изображения, возможно также стоит использовать константу для масштабирования
* добавляем каждому объекту зависимости - к какому объекту привязан, насколько смещён, задаём порядок объектов
* создаём анимации - логику изменения связанных ГРУПП объектов
* анимация должна зависеть от времени, а не от шагов, текущие значения должны вычисляться через разницу между крайними
* грузим сохранения
*
* в мейн активити запускаем поток для просчёта изменений (он должен работать отдельно от визуалки)
* и включаем графику
* */

class MainActivity : GraphicActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun buildParams(): MainRender.Companion.Params {
        val par = MainRender.Companion.Params()
        par.context = applicationContext
        par.scene.start = XYZpoint(-350f, -350f, 5f)
        par.scene.width = 700
        par.scene.height = 700
        par.scene.depth = 10
        par.camera.distanceTarget = XYZpoint(0f, 0f, 5f)
        par.camera.targetCoord = XYZpoint(0f, 0f, 0f)
        return par
    }

    override fun buildGObjects(): ArrayList<AbstractGObject> {
        val gObjects = arrayListOf<Part>()
        val rand= Random(0)
        val x=rand.nextInt(1000)
        for(i in 0..2000+x){
            val size=0.5f+rand.nextFloat()*3.5f
            val star=Part(R.drawable.star,size,size,0f)
            star.position= XYZpoint(-1000f+rand.nextInt(2000),-1000f+rand.nextInt(2000),0f)
            gObjects.add(star)
        }


//        val x=rand.nextInt(500)
//        for(i in 0..500+x){
//            val size=100f+rand.nextFloat()*200f
//            val star=Part(R.drawable.stars,size,size,0f)
//            star.position= XYZpoint(-2000f+rand.nextInt(4000),-2000f+rand.nextInt(4000),0f)
//            gObjects.add(star)
//        }

        return gObjects as ArrayList<AbstractGObject>
    }

    override fun onFrameChanged() {

    }

    class Part : AbstractGObject {
        constructor(context: Context, imgResId: Int, width: Float, height: Float) : super(
            imgResId,
            width,
            height
        )

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
            copy.position = position
            copy.rotation = rotation
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

            copy.mModelMatrix=mModelMatrix
            copy.bindedMatrix=bindedMatrix
            return copy
        }
    }


}