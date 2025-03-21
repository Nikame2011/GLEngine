package com.nikame.glengine.Entity

import com.nikame.glengine.gObjects.AbstractGObject

class Camera {

    /**
     * Если [targetObject] не задан, этот параметр определит точку, в которую будет направлена камера,
     * а если [targetObject] задан, этот параметр определит смещение от координат объекта.
     */
    var targetCoord = XYZpoint(0f, 0f, 0f)

    /**
     * Целевой объект. Если не равен null, камера будет следовать за этим объектом по следующим правилам:
     * - [targetCoord] будет определять, на сколько направление камеры смещено от координат объекта: [targetObject] + [targetCoord]
     * - [distanceTarget] будет задавать положение самой камеры, как [targetObject] + [distanceTarget]
     */
    var targetObject: AbstractGObject? = null

    /**
     * Параметр задаёт точку расположения камеры, как
     * [targetObject] + [distanceTarget] или [targetCoord] + [distanceTarget], в зависимости от того, что задано
     */
    var distanceTarget = XYZpoint(0f, 0f, 2f)

    var vectorCoord = XYZpoint(0f, 1f, 0f)


//    /**
//     * Rotate camera around current target position to input angles.
//     *
//     * This fun override your camera position
//     */
//    fun spinAroundTarget(angles: XYZpoint) {
//        var rotor = positionCoord.sub(targetCoord)
//        rotor = rotor.rotateTo(angles)
//        positionCoord = rotor.add(targetCoord)
//    }

    fun getPosition(stoppedTarget: AbstractGObject?): XYZpoint {
        stoppedTarget?.let {
            lastPosition = it.position.add(it.center).add(distanceTarget)
            return lastPosition
        }
        lastPosition = targetCoord.add(distanceTarget)
        return lastPosition
    }

    var lastPosition = XYZpoint(0f, 0f, 0f)

    fun getTarget(stoppedTarget: AbstractGObject?): XYZpoint {
        stoppedTarget?.let {
            return it.position.add(it.center).add(targetCoord)
        }
        return targetCoord
    }

    fun getVector(stoppedTarget: AbstractGObject?): XYZpoint {
        stoppedTarget?.let {
            return vectorCoord.rotateTo(it.rotation)
        }
        return vectorCoord
    }

//    /**
//     * Rotate camera around transmitted position to transmitted angles.
//     *
//     * This fun override your camera position and target. NOT WORK!
//     */
//    fun spinAroundPosition(angles: XYZpoint, position: XYZpoint) {
////                var rotor = position.sub(target)
////                rotor = rotor.rotateTo(angles)
////                position = rotor.add(target)
//    }

//    /**
//     * Rotate camera around self to input angles.
//     *
//     * This fun override your target position
//     */
//    fun spin(angles: XYZpoint) {
//        var rotor = targetCoord.sub(positionCoord)
//        rotor = rotor.rotateTo(angles)
//        targetCoord = rotor.add(positionCoord)
//    }
//
//    fun move(shift: XYZpoint) {
//        positionCoord = positionCoord.add(shift)
//        targetCoord = targetCoord.add(shift)
//    }
}