package evofunc.function

import evofunc.geometry.Point
import evofunc.random.Dice
import kotlin.math.pow

data class Parabola(
    private val a: Double = Dice.randomDouble(), private val b: Double = Dice.randomDouble(), private val c: Double = Dice.randomDouble(),
    private val d: Double = Dice.randomDouble(), private val e: Double = Dice.randomDouble(), private val f: Double = Dice.randomDouble()
) : PointFunction {
    override fun apply(p: Point) = Point(a * p.x.pow(b) + c, d * p.y.pow(e) + f)
    override fun applyInPlace(p: Point) {
        val x = a * p.x.pow(b) + c
        val y = d * p.y.pow(e) + f
        p.x = x
        p.y = y
    }

}