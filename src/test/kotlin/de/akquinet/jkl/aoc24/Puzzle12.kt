package de.akquinet.jkl.aoc24

import de.akquinet.jkl.aoc24.utils.*
import io.kotest.matchers.shouldBe
import kotlin.math.abs

private data class PlantMap(val dimension: Dimension, val data: Map<Point, Char>) {
  fun buildAllConnectedRegions(): List<Region> {
    val visited = mutableListOf<Point>()
    val regions = mutableListOf<Region>()
    data.keys.forEach { start -> buildConnectedRegion(start, regions, visited) }
    return regions.toList()
  }

  private fun buildConnectedRegion(
    start: Point,
    regions: MutableList<Region>,
    visited: MutableList<Point>,
  ) {
    if (start !in visited) {
      val char = data[start]!!
      val region = mutableListOf<Point>()
      addToConnectedRegion(char, start, region, visited)
      regions.add(Region(region.toList()))
    }
  }

  private fun addToConnectedRegion(
    char: Char,
    point: Point,
    region: MutableList<Point>,
    visited: MutableList<Point>,
  ) {
    if (point !in visited && data[point] == char) {
      visited.add(point)
      region.add(point)
      point.neighbours(dimension).forEach { neighbour ->
        addToConnectedRegion(char, neighbour, region, visited)
      }
    }
  }
}

@JvmInline
private value class Region(val value: List<Point>) {
  companion object {
    private val OFFSETS = listOf(Point(0, 0), Point(0, 1), Point(1, 0), Point(1, 1))
  }

  fun area(): Int = value.size

  fun perimeter(): Int {
    val region = value.toSet()
    return region.sumOf { point ->
      val neighbours = point.neighbours().toSet()
      val diff = neighbours subtract region
      diff.size
    }
  }

  fun countCorners(): Int {
    val corners = mutableMapOf<Point, Set<Point>>()

    value.forEach { point ->
      OFFSETS.forEach { offset ->
        corners[point plus offset] =
          corners.getOrDefault(point plus offset, setOf()) union setOf(point)
      }
    }

    val externalCorners = corners.values.count { it.size == 1 }
    val internalCorners = corners.values.count { it.size == 3 }
    val touchingCorners =
      corners.values
        .filter { it.size == 2 }
        .count {
          val points = it.toList()
          val delta = points[0] minus points[1]
          abs(delta.x) == 1 && abs(delta.y) == 1
        }

    return externalCorners + internalCorners + 2 * touchingCorners
  }
}

class Puzzle12 :
  PuzzleSpec(
    12,
    {
      val lines = readInputAsLines()
      val dimension = Dimension(width = lines.first().length, height = lines.size)

      val plantMap =
        PlantMap(
          dimension = dimension,
          data =
            lines
              .flatMapIndexed { i, row ->
                row.mapIndexed { j, char ->
                  val point = Point(i, j)
                  point to char
                }
              }
              .toMap(),
        )

      val regions = plantMap.buildAllConnectedRegions()

      test("part one") {
        val solution1 = regions.sumOf { region -> region.area() * region.perimeter() }
        solution1 shouldBe 1424006
      }

      test("part two") {
        val solution2 = regions.sumOf { region -> region.area() * region.countCorners() }
        solution2 shouldBe 858684
      }
    },
  )
