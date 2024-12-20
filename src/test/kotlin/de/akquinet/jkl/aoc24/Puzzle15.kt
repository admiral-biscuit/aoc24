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

  fun thiccify(): ThiccWarehouse =
    ThiccWarehouse(
      dimension = Dimension(2 * dimension.width, dimension.height),
      robot = robot.point.thiccify().let { (p1, _) -> ThiccWarehouseRobot(p1) },
      walls = walls.map { it.point.thiccify().let { (p1, p2) -> ThiccWarehouseWall(p1, p2) } },
      boxes = boxes.map { it.point.thiccify().let { (p1, p2) -> ThiccWarehouseBox(p1, p2) } },
    )
}

private fun Point.thiccify(): Pair<Point, Point> = Point(2 * x, y) to Point(2 * x + 1, y)

private sealed interface ThiccWarehouseObject {
  val point1: Point
  val point2: Point
}

private data class ThiccWarehouseRobot(var point: Point) {
  private fun canMove(warehouse: ThiccWarehouse, direction: Direction): Boolean {
    val nextPoint = point.move(direction)

    val canMove =
      when (val warehouseObject = warehouse.getObjectAt(nextPoint)) {
        null -> true
        is ThiccWarehouseWall -> false
        is ThiccWarehouseBox -> warehouseObject.canMove(warehouse, direction)
      }

    return canMove
  }

  fun move(warehouse: ThiccWarehouse, direction: Direction) {
    if (!canMove(warehouse, direction)) return

    val nextPoint = point.move(direction)

    val warehouseObject = warehouse.getObjectAt(nextPoint)
    if (warehouseObject is ThiccWarehouseBox) {
      warehouseObject.move(warehouse, direction)
    }

    point = nextPoint
  }
}

private data class ThiccWarehouseWall(override val point1: Point, override val point2: Point) :
  ThiccWarehouseObject

private data class ThiccWarehouseBox(override var point1: Point, override var point2: Point) :
  ThiccWarehouseObject {

  private fun nextPointsToCheck(
    nextPoint1: Point,
    nextPoint2: Point,
    direction: Direction,
  ): List<Point> =
    when (direction) {
      Direction.NORTH,
      Direction.SOUTH -> listOf(nextPoint1, nextPoint2)
      Direction.WEST -> listOf(nextPoint1)
      Direction.EAST -> listOf(nextPoint2)
    }

  fun canMove(warehouse: ThiccWarehouse, direction: Direction): Boolean {
    val canMove: Boolean

    val nextPoint1 = point1.move(direction)
    val nextPoint2 = point2.move(direction)

    val nextPointsToCheck = nextPointsToCheck(nextPoint1, nextPoint2, direction)

    canMove =
      nextPointsToCheck
        .map { point ->
          when (val warehouseObject = warehouse.getObjectAt(point)) {
            null -> true
            is ThiccWarehouseWall -> false
            is ThiccWarehouseBox -> warehouseObject.canMove(warehouse, direction)
          }
        }
        .all { it }

    return canMove
  }

  fun move(warehouse: ThiccWarehouse, direction: Direction) {
    if (!canMove(warehouse, direction)) return

    val nextPoint1 = point1.move(direction)
    val nextPoint2 = point2.move(direction)

    val nextPointsToCheck = nextPointsToCheck(nextPoint1, nextPoint2, direction)

    nextPointsToCheck.forEach { point ->
      val warehouseObject = warehouse.getObjectAt(point)
      if (warehouseObject is ThiccWarehouseBox) {
        warehouseObject.move(warehouse, direction)
      }
    }

    point1 = nextPoint1
    point2 = nextPoint2
  }

  fun gps(): Int = abs(point1.x) + 100 * abs(point1.y)
}

private data class ThiccWarehouse(
  val dimension: Dimension,
  val robot: ThiccWarehouseRobot,
  val walls: List<ThiccWarehouseWall>,
  val boxes: List<ThiccWarehouseBox>,
) {
  fun getObjectAt(point: Point): ThiccWarehouseObject? {
    val wall = walls.firstOrNull { it.point1 == point || it.point2 == point }
    if (wall != null) return wall

    val box = boxes.firstOrNull { it.point1 == point || it.point2 == point }
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
          in walls.map { it.point1 } -> "#"
          in walls.map { it.point2 } -> "#"
          in boxes.map { it.point1 } -> "["
          in boxes.map { it.point2 } -> "]"
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
            '@' -> robot = WarehouseMovable(point)
            'O' -> boxes.add(WarehouseMovable(point))
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

      fun newWarehouse(): Warehouse =
        Warehouse(dimension, robot!!.copy(), walls.toList(), boxes.toList().map { it.copy() })

      test("part one") {
        val warehouse = newWarehouse()
        directions.forEach { direction -> warehouse.robot.move(warehouse, direction) }

        val solution1 = warehouse.boxes.sumOf { box -> box.gps() }
        solution1 shouldBe 1514353
      }

      test("part two") {
        val warehouse = newWarehouse().thiccify()
        directions.forEach { direction -> warehouse.robot.move(warehouse, direction) }

        val solution2 = warehouse.boxes.sumOf { box -> box.gps() }
        solution2 shouldBe 1533076
      }
    },
  )
