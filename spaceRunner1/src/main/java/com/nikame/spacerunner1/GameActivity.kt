package com.nikame.spacerunner1

import android.content.Context
import android.os.Bundle
import com.nikame.glengine.Entity.XYZpoint
import com.nikame.glengine.GraphicActivity
import com.nikame.glengine.gObjects.AbstractGObject
import com.nikame.glengine.graphics.MainRender

class GameActivity : GraphicActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onProgressChanged= { progress: Int, status: String ->

        }
    }
    override fun buildParams(): MainRender.Companion.Params {
        val par = MainRender.Companion.Params()
        par.context = applicationContext
        par.scene.start = XYZpoint(0f, 0f, 0f)
        par.scene.width = 700
        par.scene.height = 700
        par.scene.depth = 6
        par.camera.position = XYZpoint(0f, 0f, 5f)
        par.camera.target = XYZpoint(0f, 0f, 0f)
        return par
    }

    override fun buildGObjects(): ArrayList<AbstractGObject> {
        val gObjects = arrayListOf<Part>()
        //TODO depth не влияет на порядок отображения элементов. а должно. разобраться, почему. возможно, стоит задавать z=depth
        gObjects.add(Part(R.drawable.ff2, 500f, 2500f, 0f))
        gObjects.add(Part(R.drawable.ff1, 700f, 3125f, 5f))
        gObjects.add(Part(R.drawable.head_new, 151f, 95f, 0f))
        gObjects.add(Part(R.drawable.h_ny_2022_new, 89f, 101f, 2f))

        gObjects.get(0).position = XYZpoint(100f, 3000f, 0f)
        gObjects.get(1).position = XYZpoint(0f, 0f, 0f)
        gObjects.get(2).position = XYZpoint(270f, 310f, 0f)
        gObjects.get(2).center = XYZpoint(102f, 47f, 0f)
        gObjects.get(3).position = XYZpoint(70f, 37f, 0f)
        gObjects.get(3).center = XYZpoint(50f, 45f, 0f)
        gObjects.get(3).parent = gObjects.get(2)
        return gObjects as ArrayList<AbstractGObject>
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
    }
}