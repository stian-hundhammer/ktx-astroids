package no.knowledge.ktxastroid

import kotlin.math.cos
import kotlin.math.sin


/**
 * Position and speed
 */
data class Physics(
        var location: Point = Point(0f, 0f),
        val speed: Speed = Speed()) {

    fun moveWithSpeed() {
        location = Point(
                swapIfOutside(location.x + speed.dx, 1200f),
                swapIfOutside(location.y + speed.dy, 720f)
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
data class Speed(var dx: Float = 0f, var dy: Float = 0f)

/**
 * Hold a point x, y
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