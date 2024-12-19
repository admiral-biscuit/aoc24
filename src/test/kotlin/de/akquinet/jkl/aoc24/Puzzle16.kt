package de.akquinet.jkl.aoc24

import de.akquinet.jkl.aoc24.utils.*
import io.kotest.matchers.shouldBe

private data class PointWithDirection(val coords: Point, val direction: Direction)

private fun turnDistance(dir1: Direction, dir2: Direction) =
  when (dir1 dot dir2) {
    1 -> 0
    0 -> 1000
    else -> throw IllegalStateException("not supposed to be called")
  }

private data class ReindeerMaze(
  val dimension: Dimension,
  val start: Point,
  val end: Point,
  val walls: List<Point>,
) : ImplementsDijkstra<PointWithDirection> {
  override fun distanceBetweenNeighbours(
    node1: PointWithDirection,
    node2: PointWithDirection,
  ): Int = 1 + turnDistance(node1.direction, node2.direction)

  override fun neighbours(node: PointWithDirection): List<PointWithDirection> =
    Direction.entries
      .map { direction ->
        val nextCoords = node.coords plus direction.vector
        PointWithDirection(nextCoords, direction)
      }
      .filter { (coords, direction) -> coords !in walls && direction != node.direction.opposite() }

  // debug
  @Suppress("UNUSED")
  fun render(path: List<Point> = emptyList()): String =
    (0..<dimension.height).joinToString("\n") { y ->
      (0..<dimension.width).joinToString("") { x ->
        when (Point(x, y)) {
          in walls -> "#"
          in path -> "O"
          else -> "."
        }
      }
    }
}

class Puzzle16 :
  PuzzleSpec(
    16,
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

      val maze = ReindeerMaze(dimension, start!!, end!!, walls)

      test("part one") {
        val startWithDirection = PointWithDirection(maze.start, Direction.EAST)
        // the end point is only reachable from the left
        val endWithDirection = PointWithDirection(maze.end, Direction.EAST)

        val solution1 = maze.dijkstra(startWithDirection)[endWithDirection]
        solution1 shouldBe 98520
      }
    },
  )
