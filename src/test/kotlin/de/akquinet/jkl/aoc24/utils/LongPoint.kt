package de.akquinet.jkl.aoc24.utils

data class LongPoint(val x: Long, val y: Long) {
  infix fun plus(other: LongPoint): LongPoint = LongPoint(x + other.x, y + other.y)

  infix fun minus(other: LongPoint): LongPoint = LongPoint(x - other.x, y - other.y)
}
