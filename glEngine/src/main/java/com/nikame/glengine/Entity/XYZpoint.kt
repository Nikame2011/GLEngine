package com.nikame.glengine.Entity

class XYZpoint(var x: Float, var y: Float) {
    var z = 0f
        get() = field
        set(value) {
            field = value
        }

    constructor(x: Float, y: Float, z: Float) : this(x, y) {
        this.z = z
    }

    fun add(point: XYZpoint): XYZpoint {
        return XYZpoint(x + point.x, y + point.y, z + point.z)
    }

    fun sub(point: XYZpoint): XYZpoint {
        return XYZpoint(this.x - point.x, this.y - point.y, this.z - point.z)
    }

    fun multiply(coeff: Float): XYZpoint {
        return XYZpoint(this.x * coeff, this.y * coeff, this.z * coeff)
    }

    fun rotateTo(angles: XYZpoint): XYZpoint {
        val cosZ = Math.cos(Math.toRadians(angles.z.toDouble()))
        val sinZ = Math.sin(Math.toRadians(angles.z.toDouble()))
        val cosX = Math.cos(Math.toRadians(angles.x.toDouble()))
        val sinX = Math.sin(Math.toRadians(angles.x.toDouble()))
        val cosY = Math.cos(Math.toRadians(angles.y.toDouble()))
        val sinY = Math.sin(Math.toRadians(angles.y.toDouble()))

        val xZ = x * cosZ - y * sinZ
        val yZ = y * cosZ + x * sinZ

        val yX = yZ * cosX + z * sinX
        val zX = z * cosX - yZ * sinX

        val xY = xZ * cosY + zX * sinY
        val zY = zX * cosY - xZ * sinY

        return XYZpoint(xY.toFloat(), yX.toFloat(), zY.toFloat())
    }
}