package de.akquinet.jkl.aoc24.utils

data class Point(val x: Int, val y: Int) {
  infix fun plus(other: Point): Point = Point(x + other.x, y + other.y)

  infix fun minus(other: Point): Point = Point(x - other.x, y - other.y)

  fun scaleBy(t: Int): Point = Point(t * x, t * y)

  fun neighbours(dimension: Dimension): List<Point> =
    listOf(Point(x, y + 1), Point(x + 1, y), Point(x, y - 1), Point(x - 1, y)).filter { point ->
      dimension containsPoint point
    }
}
