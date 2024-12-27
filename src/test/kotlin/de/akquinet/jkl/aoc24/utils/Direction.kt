package de.akquinet.jkl.aoc24.utils

enum class Direction(val vector: Point) {
  NORTH(Point(0, -1)),
  EAST(Point(1, 0)),
  SOUTH(Point(0, 1)),
  WEST(Point(-1, 0));

  infix fun dot(other: Direction): Int {
    val (v1, v2) = this.vector
    val (w1, w2) = other.vector
    return v1 * w1 + v2 * w2
  }

  fun opposite(): Direction =
    when (this) {
      NORTH -> SOUTH
      EAST -> WEST
      SOUTH -> NORTH
      WEST -> EAST
    }

  fun toChar(): Char =
    when (this) {
      NORTH -> '^'
      EAST -> '>'
      SOUTH -> 'v'
      WEST -> '<'
    }

  companion object {
    fun getByVector(vector: Point): Direction = entries.first { it.vector == vector }
  }
}

infix fun Point.move(direction: Direction): Point = this plus direction.vector
