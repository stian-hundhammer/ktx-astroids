package no.knowledge.ktxastroid

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import ktx.app.KtxApplicationAdapter
import ktx.app.clearScreen
import ktx.graphics.use

/**
 * Setting window size x
 */
const val windowWidth = 1280f

/**
 * Setting window size y
 */
const val windowHeight = 960f

/**
 * Main function. Start the game
 */
fun main() {
    LwjglApplication(
        KtxAstroidGame(),
        LwjglApplicationConfiguration().apply {
            width = windowWidth.toInt()
            height = windowHeight.toInt()
        }
    )
}

/**
 * Main game class
 */
class KtxAstroidGame : KtxApplicationAdapter {

    private lateinit var renderer: ShapeRenderer
    private var spaceShip = SpaceShip(Physics(Point(windowWidth / 2, windowHeight / 2), Speed.STILL))
    private val astroids = mutableListOf<Astroid>()
    private val bullets = mutableListOf<Bullet>()

    // will be moved into a Level object later
    private var levelSpeedRange = (-5..5)

    override fun create() {
        renderer = ShapeRenderer()
        (1..5).forEach {
            astroids.add(
                Astroid(
                    40f,
                    Physics(
                        Point(
                            (50..windowWidth.toInt()).random().toFloat(),
                            (0..windowHeight.toInt()).random().toFloat()
                        ),
                        Speed(
                            levelSpeedRange.random().toFloat(),
                            levelSpeedRange.random().toFloat()
                        )
                    )
                )
            )
        }
    }

    override fun render() {
        handleInput()
        logic()
        draw()
    }

    private fun handleInput() {
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            spaceShip.turnRight()
        }

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            spaceShip.turnLeft()
        }

        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            spaceShip.speedUp()
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.D)) {
            printDebug()
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            if (bullets.size < 6) {
                bullets.add(spaceShip.shoot())
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
            System.exit(0)
        }
    }

    private fun printDebug() {
        println("spaceship: angle=${spaceShip.angle} ${spaceShip.physics}")
        println("bullets: $bullets")
    }

    private fun logic() {

        // rewrite - not very functional..
        var hits = mutableListOf<Pair<Astroid, Bullet>>()
        for (b in bullets) {
            for (a in astroids) {
                if (a.isHitByBullet(b)) {
                    hits.add(Pair(a, b))
                }
            }
        }

        hits.forEach { pair ->
            val astroid = pair.first
            val bullet = pair.second

            bullets.remove(bullet)
            astroids.remove(astroid)
            // explode!!!
            if (astroid.size > 10) {
                // spawn two smaller astroids
                astroids.add(createSmallerAstroid(astroid))
                astroids.add(createSmallerAstroid(astroid))
            }
        }

        astroids.forEach {
            it.logic()
        }

        // update bullets and remove those
        // out of range
        val bulletsToRemove = bullets.map {
            it.logic()
            if (it.outOfRange()) {
                it
            } else null
        }.filterNotNull()

        bullets.removeAll(bulletsToRemove)

        spaceShip.logic()

        if (astroids.isEmpty()) {
            println("New Level!")
        }
    }

    /**
     * Just create a smaller astroid based on the one
     * given. The size of the new one is half the one given.
     *
     * @return new and smaller astroid
     */
    private fun createSmallerAstroid(astroid: Astroid): Astroid {
        return Astroid(
            astroid.size / 2,
            Physics(
                astroid.physics.location,
                Speed(
                    (-6..6).random().toFloat(),
                    (-6..6).random().toFloat()
                )
            )
        )
    }

    private fun draw() {
        clearScreen(0f, 0f, 0f, 0f)
        spaceShip.draw(renderer)
        bullets.forEach { it.draw(renderer) }
        astroids.forEach { it.draw(renderer) }
    }
}

interface CanDraw {
    fun draw(renderer: ShapeRenderer)
}

/**
 * An astroid
 */
class Astroid(val size: Float, val physics: Physics) : CanDraw {
    override fun draw(renderer: ShapeRenderer) =
        renderer.use(ShapeRenderer.ShapeType.Line) {
            renderer.color = Color.WHITE
            renderer.circle(physics.location.x, physics.location.y, size)
        }

    fun logic() {
        physics.moveWithSpeed()
    }

    fun isHitByBullet(bullet: Bullet): Boolean =
        bullet.physics.location.x > physics.location.x - size &&
            bullet.physics.location.x < physics.location.x + size &&
            bullet.physics.location.y > physics.location.y - size &&
            bullet.physics.location.y < physics.location.y + size
}

/**
 * The space ship
 */
data class SpaceShip(val physics: Physics) : CanDraw {

    var angle = 0f

    override fun draw(renderer: ShapeRenderer) {
        renderer.use(ShapeRenderer.ShapeType.Point) {
            renderer.color = Color.WHITE
            renderer.point(physics.location.x, physics.location.y, 0f)
        }

        renderer.use(ShapeRenderer.ShapeType.Line) {
            renderer.color = Color.WHITE
            renderer.polyline(vertices())
        }

        // draw circle at point of ship during development
        /*
        val pointOfShip = Point(physics.location.x, physics.location.y + 15).rotate(physics.location, angle)
        renderer.use(ShapeRenderer.ShapeType.Line) {
            renderer.circle(pointOfShip.x, pointOfShip.y, 5f)
        }
         */
    }

    /**
     * pair of x and y's for the spaceship
     */
    private fun vertices(): FloatArray {

        // on our way to transform x and y to Point objects
        // keep it for now, but will be rewritten
        val rotateAround = physics.location
        val one = Point(physics.location.x - 10, physics.location.y - 15)
        val two = Point(physics.location.x, physics.location.y + 15)
        val three = Point(physics.location.x + 10, physics.location.y - 15)

        val oneMark = one.rotate(rotateAround, angle)
        val twoMark = two.rotate(rotateAround, angle)
        val threeMark = three.rotate(rotateAround, angle)

        return floatArrayOf(
            oneMark.x,
            oneMark.y,
            twoMark.x,
            twoMark.y,
            threeMark.x,
            threeMark.y,
            oneMark.x,
            oneMark.y
        )
    }

    fun logic() {
        physics.moveWithSpeed()
    }

    fun turnRight() {
        angle -= 0.1f
        if (angle < 0) angle = 359.9f
    }

    fun turnLeft() {
        angle += 0.1f
        if (angle > 359.9f) angle = 0f
    }

    fun speedUp() {

        val pointOfShip = Point(physics.location.x, physics.location.y + 15).rotate(physics.location, angle)

        physics.speed.dx += (physics.location.x - pointOfShip.x) / 200f * -1f
        physics.speed.dy += (physics.location.y - pointOfShip.y) / 200f * -1f
    }

    fun shoot(): Bullet {

        // doing the same thing here as rotating, and finding
        // the diff between the rotated point of the space ship
        // and the center

        val pointOfShip = Point(physics.location.x, physics.location.y + 15).rotate(physics.location, angle)

        return Bullet(
            Physics(
                pointOfShip,
                Speed(
                    (physics.location.x - pointOfShip.x) * -1,
                    (physics.location.y - pointOfShip.y) * -1
                )
            )
        )
    }
}

data class Bullet(val physics: Physics) : CanDraw {

    private var ticksSinceFired: Int = 0

    override fun draw(renderer: ShapeRenderer) {
        renderer.use(ShapeRenderer.ShapeType.Filled) {
            renderer.color = Color.WHITE
            renderer.circle(physics.location.x, physics.location.y, 2f)
        }
    }

    fun logic() {
        ticksSinceFired += 1
        physics.moveWithSpeed()
    }

    fun outOfRange(): Boolean = ticksSinceFired > 35
}
