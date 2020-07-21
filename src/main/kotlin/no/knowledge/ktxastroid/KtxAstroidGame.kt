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
import kotlin.math.sin

/**
 * Main function. Start the game
 */
fun main() {
    LwjglApplication(KtxAstroidGame(),
            LwjglApplicationConfiguration().apply {
                width = 1280
                height = 720
            })
}

/**
 * Main game class
 */
class KtxAstroidGame : KtxApplicationAdapter {

    private lateinit var renderer: ShapeRenderer
    private var spaceShip = SpaceShip(Physics(640f, 350f))
    private val astroids = mutableListOf<Astroid>()
    private val bullets = mutableListOf<Bullet>()

    // will be moved into a Level object later
    private var levelSpeedRange = (-5..5)

    override fun create() {
        renderer = ShapeRenderer()
        (1..5).forEach {
            astroids.add(
                    Astroid(40f, Physics(
                            (50..1200).random().toFloat(),
                            (0..700).random().toFloat(),
                            Speed(
                                levelSpeedRange.random().toFloat(),
                                levelSpeedRange.random().toFloat()
                            )
                    ))
            )
        }
    }

    override fun render() {
        handleInput()
        logic()
        draw()
    }

    private fun handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            spaceShip.turnRight()
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            spaceShip.turnLeft()
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
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

    }

    private fun printDebug() {
        println("spaceship: direction=${spaceShip.direction} ${spaceShip.physics}")
    }

    private fun logic() {


        // rewrite - not very functional..
        var hits = mutableListOf<Pair<Astroid,Bullet>>()
        for(b in bullets) {
            for(a in astroids) {
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
            if(astroid.size > 10) {
                // spawn two smaller astroids
                astroids.add(Astroid(astroid.size / 2, Physics(
                        astroid.physics.x, astroid.physics.y,
                        Speed(
                                (-6..6).random().toFloat(),
                                (-6..6).random().toFloat()
                        ))
                ))

                astroids.add(Astroid(astroid.size / 2, Physics(
                        astroid.physics.x, astroid.physics.y,
                        Speed(
                                (-6..6).random().toFloat(),
                                (-6..6).random().toFloat()
                        ))
                ))
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

        if(astroids.isEmpty()) {
            println("New Level!")
        }

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
                renderer.circle(physics.x, physics.y, size)
            }

    fun logic() {
        physics.moveWithSpeed()
    }

    fun isHitByBullet(bullet: Bullet): Boolean =
            bullet.physics.x > physics.x - size &&
                    bullet.physics.x < physics.x + size &&
                    bullet.physics.y > physics.y - size &&
                    bullet.physics.y < physics.y + size

}


/**
 * The space ship
 */
data class SpaceShip(val physics: Physics) : CanDraw {

    var direction = 0f

    override fun draw(renderer: ShapeRenderer) {
        renderer.use(ShapeRenderer.ShapeType.Point) {
            renderer.color = Color.WHITE
            renderer.point(physics.x, physics.y, 0f)
        }

        renderer.use(ShapeRenderer.ShapeType.Line) {
            renderer.color = Color.WHITE
            renderer.polyline(vertices())
        }
    }

    /**
     * pair of x and y's for the spaceship
     */
    private fun vertices(): FloatArray {

        // on our way to transform x and y to Point objects
        // keep it for now, but will be rewritten
        val rotateAround = Point(physics.x, physics.y)
        val one = Point(physics.x - 10, physics.y - 15)
        val two = Point(physics.x, physics.y + 15)
        val three = Point(physics.x + 10, physics.y - 15)

        val oneMark = one.rotate(rotateAround, direction)
        val twoMark = two.rotate(rotateAround, direction)
        val threeMark = three.rotate(rotateAround, direction)

        return floatArrayOf(
                oneMark.x, oneMark.y,
                twoMark.x, twoMark.y,
                threeMark.x, threeMark.y,
                oneMark.x, oneMark.y
        )
    }

    fun logic() {
        physics.moveWithSpeed()
    }

    fun turnRight() {
        direction += 1
        if (direction > 360) direction = 0f
    }

    fun turnLeft() {
        direction -= 1
        if (direction < 0) direction = 360f
    }

    fun speedUp() {
        physics.speed.dx += sin(direction)
        physics.speed.dy += sin(direction)
    }

    fun shoot(): Bullet =
            Bullet(Physics(
                    physics.x,
                    physics.y,
                    Speed(5f, 5f)))

}

data class Bullet(val physics: Physics) : CanDraw {

    private var ticksSinceFired: Int = 0

    override fun draw(renderer: ShapeRenderer) {
        renderer.use(ShapeRenderer.ShapeType.Point) {
            renderer.color = Color.WHITE
            renderer.point(physics.x, physics.y, 0f)
        }
    }

    fun logic() {
        ticksSinceFired += 1
        physics.moveWithSpeed()
    }

    fun outOfRange(): Boolean = ticksSinceFired > 60
}