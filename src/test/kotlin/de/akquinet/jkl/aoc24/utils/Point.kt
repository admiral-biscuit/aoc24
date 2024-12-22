package de.akquinet.jkl.aoc24.utils

import kotlin.math.abs

data class Point(val x: Int, val y: Int) {
  infix fun plus(other: Point): Point = Point(x + other.x, y + other.y)

  infix fun minus(other: Point): Point = Point(x - other.x, y - other.y)

  fun scaleBy(t: Int): Point = Point(t * x, t * y)

  fun square(t: Int): List<Point> {
    val range = -t..t
    val vectors = combineAll(range, range).map { (v, w) -> Point(v, w) }
    return vectors.map { this plus it }
  }

  fun neighbours(): List<Point> =
    listOf(Point(x, y + 1), Point(x + 1, y), Point(x, y - 1), Point(x - 1, y))

  fun neighbours(dimension: Dimension): List<Point> =
    neighbours().filter { point -> dimension containsPoint point }

  infix fun manhattanDistance(other: Point): Int =
    (this minus other).let { (x, y) -> abs(x) + abs(y) }
}
