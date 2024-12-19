package de.akquinet.jkl.aoc24

import arrow.fx.coroutines.parMap
import de.akquinet.jkl.aoc24.utils.Dimension
import de.akquinet.jkl.aoc24.utils.ImplementsDijkstra
import de.akquinet.jkl.aoc24.utils.Point
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers

private data class MemorySpace(val dimension: Dimension, val corruptedPoints: List<Point>) :
  ImplementsDijkstra<Point> {
  override fun distanceBetweenNeighbours(node1: Point, node2: Point): Int = 1

  override fun neighbours(node: Point): List<Point> =
    node.neighbours(dimension).filter { it !in corruptedPoints }

  // debug
  @Suppress("UNUSED")
  fun render(path: List<Point> = emptyList()): String =
    (0..<dimension.height).joinToString("\n") { y ->
      (0..<dimension.width).joinToString("") { x ->
        when (Point(x, y)) {
          in corruptedPoints -> "#"
          in path -> "O"
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
        val solution1 = memorySpace.dijkstra(start)[end]
        solution1 shouldBe 320
      }

      test("part two") {
        val firstBlockingPoint =
          (1025..corruptedPoints.size)
            .parMap(Dispatchers.IO) { numberOfCorruptedPoints ->
              val memorySpace =
                MemorySpace(dimension, corruptedPoints.take(numberOfCorruptedPoints))
              numberOfCorruptedPoints to memorySpace.dijkstra(start)[end]
            }
            .first { (_, distance) -> distance == null }
            .first

        val solution2 = corruptedPoints[firstBlockingPoint - 1].let { (x, y) -> "$x,$y" }

        solution2 shouldBe "34,40"
      }
    },
  )
