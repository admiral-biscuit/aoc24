package de.akquinet.jkl.aoc24

import arrow.fx.coroutines.parMap
import de.akquinet.jkl.aoc24.utils.Dimension
import de.akquinet.jkl.aoc24.utils.Point
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlin.math.abs
import kotlinx.coroutines.Dispatchers

private data class MemorySpace(val dimension: Dimension, val corruptedPoints: List<Point>) {
  private fun reconstructPath(current: Point, cameFrom: Map<Point, Point>): List<Point> =
    generateSequence(current) { point -> cameFrom[point] }.toList().reversed()

  // shamelessly copied from Wikipedia
  fun aStarPath(start: Point, end: Point): List<Point>? {
    val heuristic: (Point) -> Int = { point -> (end minus point).let { (x, y) -> abs(x) + abs(y) } }

    val discovered = mutableSetOf(start)
    val cameFrom = mutableMapOf<Point, Point>()

    val gMap = mutableMapOf(start to 0)
    val fMap = mutableMapOf(start to heuristic(start))

    val gScore: (Point) -> Int = { point -> gMap.getOrDefault(point, Int.MAX_VALUE) }
    val fScore: (Point) -> Int = { point -> fMap.getOrDefault(point, Int.MAX_VALUE) }

    while (discovered.isNotEmpty()) {
      val current = discovered.minBy(fScore)

      if (current == end) return reconstructPath(current, cameFrom)

      discovered.remove(current)
      val neighbours = current.neighbours(dimension).filter { point -> point !in corruptedPoints }
      neighbours.forEach { neighbour ->
        val tentativeGScore = gScore(current) + 1
        if (tentativeGScore < gScore(neighbour)) {
          cameFrom[neighbour] = current
          gMap[neighbour] = tentativeGScore
          fMap[neighbour] = tentativeGScore + heuristic(neighbour)
          if (neighbour !in discovered) {
            discovered.add(neighbour)
          }
        }
      }
    }

    return null
  }

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

        val shortestPath = memorySpace.aStarPath(start, end).shouldNotBeNull()
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
            .first { (_, path) -> path == null }
            .first

        val solution2 = corruptedPoints[firstBlockingPoint - 1].let { (x, y) -> "$x,$y" }

        solution2 shouldBe "34,40"
      }
    },
  )
