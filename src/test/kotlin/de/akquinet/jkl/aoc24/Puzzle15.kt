package de.akquinet.jkl.aoc24

import de.akquinet.jkl.aoc24.utils.Dimension
import de.akquinet.jkl.aoc24.utils.Direction
import de.akquinet.jkl.aoc24.utils.Point
import de.akquinet.jkl.aoc24.utils.move
import io.kotest.matchers.shouldBe
import kotlin.math.abs

private sealed interface WarehouseObject {
  val point: Point
}

private data class WarehouseWall(override val point: Point) : WarehouseObject

private data class WarehouseMovable(override var point: Point) : WarehouseObject {
  fun move(warehouse: Warehouse, direction: Direction): Boolean {
    val moved: Boolean
    val nextPoint = point.move(direction)

    moved =
      when (val warehouseObject = warehouse.getObjectAt(nextPoint)) {
        null -> true
        is WarehouseWall -> false
        is WarehouseMovable -> warehouseObject.move(warehouse, direction)
      }

    if (moved) point = nextPoint

    return moved
  }

  fun gps(): Int = abs(point.x) + 100 * abs(point.y)
}

private data class Warehouse(
  val dimension: Dimension,
  val robot: WarehouseMovable,
  val walls: List<WarehouseWall>,
  val boxes: List<WarehouseMovable>,
) {
  fun getObjectAt(point: Point): WarehouseObject? {
    val wall = walls.firstOrNull { it.point == point }
    if (wall != null) return wall

    val box = boxes.firstOrNull { it.point == point }
    if (box != null) return box

    return null
  }

  // debug
  @Suppress("UNUSED")
  fun render(): String =
    (0..<dimension.height).joinToString("\n") { y ->
      (0..<dimension.width).joinToString("") { x ->
        when (Point(x, y)) {
          robot.point -> "@"
          in walls.map { it.point } -> "#"
          in boxes.map { it.point } -> "O"
          else -> "."
        }
      }
    }
}

class Puzzle15 :
  PuzzleSpec(
    15,
    {
      val (block1, block2) = readInputAsText().split("\n\n")

      val lines = block1.split("\n")

      val dimension = Dimension(lines.first().length, lines.size)
      var robot: WarehouseMovable? = null
      val walls = mutableListOf<WarehouseWall>()
      val boxes = mutableListOf<WarehouseMovable>()

      lines.forEachIndexed { y, row ->
        row.forEachIndexed { x, char ->
          val point = Point(x, y)
          when (char) {
            '@' -> robot = WarehouseMovable(point).copy()
            'O' -> boxes.add(WarehouseMovable(point).copy())
            '#' -> walls.add(WarehouseWall(point))
          }
        }
      }

      val directions =
        block2.replace("\n", "").map { char ->
          when (char) {
            '^' -> Direction.NORTH
            '>' -> Direction.EAST
            'v' -> Direction.SOUTH
            '<' -> Direction.WEST
            else -> throw IllegalStateException("illegal char $char")
          }
        }

      test("part one") {
        val warehouse = Warehouse(dimension, robot!!, walls, boxes)
        directions.forEach { direction -> warehouse.robot.move(warehouse, direction) }

        val solution1 = warehouse.boxes.sumOf { box -> box.gps() }
        solution1 shouldBe 1514353
      }
    },
  )
