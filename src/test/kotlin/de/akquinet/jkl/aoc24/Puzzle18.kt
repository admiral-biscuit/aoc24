package de.akquinet.jkl.aoc24

import arrow.fx.coroutines.parMap
import de.akquinet.jkl.aoc24.utils.Dimension
import de.akquinet.jkl.aoc24.utils.PathSearchable
import de.akquinet.jkl.aoc24.utils.Point
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.shouldBe
import kotlin.math.abs
import kotlinx.coroutines.Dispatchers

private data class MemorySpace(val dimension: Dimension, val corruptedPoints: List<Point>) :
  PathSearchable<Point> {
  override fun distance(node1: Point, node2: Point): Int =
    (node1 minus node2).let { (x, y) -> abs(x) + abs(y) }

  override fun neighbours(node: Point): List<Point> =
    node.neighbours(dimension).filter { it !in corruptedPoints }

  // debug
  @Suppress("UNUSED")
  fun render(visited: List<Point> = emptyList()): String =
    (0..<dimension.height).joinToString("\n") { y ->
      (0..<dimension.width).joinToString("") { x ->
        when (Point(x, y)) {
          in corruptedPoints -> "#"
          in visited -> "O"
          else -> "."
        }
      }
    }
}

class Puzzle18 :
  PuzzleSpec(
    18,
    {
      val dimension = Dimension(71, 71)

      val corruptedPoints =
        readInputAsLines().map { line ->
          val (x, y) = line.split(",")
          Point(x.toInt(), y.toInt())
        }

      val start = Point(0, 0)
      val end = dimension.let { (x, y) -> Point(x - 1, y - 1) }

      test("part one") {
        val memorySpace = MemorySpace(dimension, corruptedPoints.take(1024))

        val shortestPath = memorySpace.aStarPath(start, end).shouldBeRight()
        val solution1 = shortestPath.size - 1

        solution1 shouldBe 320
      }

      test("part two") {
        val firstBlockingPoint =
          (1025..corruptedPoints.size)
            .parMap(Dispatchers.IO) { numberOfCorruptedPoints ->
              val memorySpace =
                MemorySpace(dimension, corruptedPoints.take(numberOfCorruptedPoints))
              numberOfCorruptedPoints to memorySpace.aStarPath(start, end)
            }
            .first { (_, path) -> path.isLeft() }
            .first

        val solution2 = corruptedPoints[firstBlockingPoint - 1].let { (x, y) -> "$x,$y" }

        solution2 shouldBe "34,40"
      }
    },
  )
