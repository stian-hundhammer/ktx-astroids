package no.knowledge.ktxastroid

import kotlin.math.cos
import kotlin.math.sin

/**
 * Position and speed
 */
data class Physics(
    var location: Point,
    val speed: Speed
) {

    fun moveWithSpeed() {
        location = Point(
            swapIfOutside(location.x + speed.dx, windowWidth),
            swapIfOutside(location.y + speed.dy, windowHeight)
        )
    }

    private fun swapIfOutside(value: Float, max: Float): Float =
        if (value > max) {
            0f
        } else if (value < 0) {
            max
        } else value
}

/**
 * Holding speed in each direction
 */
data class Speed(var dx: Float = 0f, var dy: Float = 0f) {
    companion object {
        val STILL = Speed(0f, 0f)
    }
}

/**
 * Hold a point x, y and operations on it
 */
data class Point(val x: Float, val y: Float) {
    fun rotate(rotateAround: Point, degree: Float): Point {
        val myX = x - rotateAround.x
        val myY = y - rotateAround.y

        return Point(
            (myX * cos(degree)) - (myY * sin(degree)) + rotateAround.x,
            (myX * sin(degree)) + (myY * cos(degree)) + rotateAround.y
        )
    }
}
