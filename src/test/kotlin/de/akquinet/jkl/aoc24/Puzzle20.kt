package de.akquinet.jkl.aoc24

import arrow.core.raise.nullable
import de.akquinet.jkl.aoc24.utils.Dimension
import de.akquinet.jkl.aoc24.utils.ImplementsDijkstra
import de.akquinet.jkl.aoc24.utils.Point
import io.kotest.matchers.shouldBe

private data class Racetrack(
  val dimension: Dimension,
  val start: Point,
  val end: Point,
  val walls: List<Point>,
  val cheatAt: Point? = null,
) : ImplementsDijkstra<Point> {
  override fun distanceBetweenNeighbours(node1: Point, node2: Point): Int = 1

  override fun neighbours(node: Point): List<Point> =
    node.neighbours(dimension).filter { point -> point !in walls }
}

class Puzzle20 :
  PuzzleSpec(
    20,
    {
      val lines = readInputAsLines()
      val dimension = Dimension(lines.first().length, lines.size)

      var start: Point? = null
      var end: Point? = null
      val walls = mutableListOf<Point>()

      lines.forEachIndexed { y, row ->
        row.forEachIndexed { x, char ->
          val point = Point(x, y)
          when (char) {
            'S' -> start = point
            'E' -> end = point
            '#' -> walls.add(point)
            else -> Unit
          }
        }
      }

      val racetrack = Racetrack(dimension, start!!, end!!, walls.toList())

      val normalPath = racetrack.dijkstraAllPaths(racetrack.start, racetrack.end).single()
      val normalTime = normalPath.size - 1

      val distancesFromEnd = racetrack.dijkstra(racetrack.end)

      fun solve(n: Int) =
        normalPath
          .withIndex()
          .flatMap { (distanceFromStart, cheatAt) ->
            cheatAt
              .square(n)
              .filter { neighbour ->
                cheatAt manhattanDistance neighbour in 1..n && dimension containsPoint neighbour
              }
              .mapNotNull { neighbour ->
                val skip = neighbour manhattanDistance cheatAt
                nullable {
                  val cheatedTime = distanceFromStart + skip + distancesFromEnd[neighbour].bind()
                  normalTime - cheatedTime
                }
              }
          }
          .count { it >= 100 }

      test("part one") {
        val solution1 = solve(2)
        solution1 shouldBe 1355
      }

      test("part two") {
        val solution2 = solve(20)
        solution2 shouldBe 1007335
      }
    },
  )
