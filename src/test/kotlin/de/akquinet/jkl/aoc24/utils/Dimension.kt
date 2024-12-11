package de.akquinet.jkl.aoc24.utils

data class Dimension(val width: Int, val height: Int) {
  infix fun containsPoint(point: Point): Boolean = point.x in 0..<width && point.y in 0..<height
}
