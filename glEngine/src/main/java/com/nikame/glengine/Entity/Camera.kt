package com.nikame.glengine.Entity

class Camera {
    var position = XYZpoint(0f, 0f, 2f)
    var target = XYZpoint(0f, 0f, 0f)
    var vector = XYZpoint(0f, 1f, 0f)


    /**
     * Rotate camera around current target position to input angles.
     *
     * This fun override your camera position
     */
    fun spinAroundTarget(angles: XYZpoint) {
        var rotor = position.sub(target)
        rotor = rotor.rotateTo(angles)
        position = rotor.add(target)
    }

    /**
     * Rotate camera around transmitted position to transmitted angles.
     *
     * This fun override your camera position and target. NOT WORK!
     */
    fun spinAroundPosition(angles: XYZpoint, position: XYZpoint) {
//                var rotor = position.sub(target)
//                rotor = rotor.rotateTo(angles)
//                position = rotor.add(target)
    }

    /**
     * Rotate camera around self to input angles.
     *
     * This fun override your target position
     */
    fun spin(angles: XYZpoint) {
        var rotor = target.sub(position)
        rotor = rotor.rotateTo(angles)
        target = rotor.add(position)
    }

    fun move(shift: XYZpoint) {
        position = position.add(shift)
        target = target.add(shift)
    }
}