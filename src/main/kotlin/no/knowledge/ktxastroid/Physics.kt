package no.knowledge.ktxastroid


/**
 * Position and speed
 */
data class Physics(
        var x: Float = 0f,
        var y: Float = 0f,
        val speed: Speed = Speed()) {

    fun moveWithSpeed() {
        x = swapIfOutside(x + speed.x, 1200f)
        y = swapIfOutside(y + speed.y, 720f)
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
data class Speed(var x: Float = 0f, var y: Float = 0f)
