package de.akquinet.jkl.aoc24

import io.kotest.matchers.shouldBe

private data class LinkedPoint(val coords: Point, val height: Int, val next: List<LinkedPoint>) {
  fun ends(): List<LinkedPoint> =
    if (next.isEmpty() && height == 9) {
      listOf(this)
    } else {
      next.flatMap { it.ends() }.toList()
    }
}

private data class TopoMap(val dimension: Dimension, val data: Map<Point, Int>) {
  fun ascendingPathsFrom(start: Point): LinkedPoint {
    val height = data[start]!!
    val neighbours = start.neighbours(dimension).filter { point -> data[point]!! - height == 1 }
    return LinkedPoint(start, height, neighbours.map { point -> ascendingPathsFrom(point) })
  }
}

class Puzzle10 :
  PuzzleSpec(
    10,
    {
      val lines = readInputAsLines()
      val dimension = Dimension(width = lines.first().length, height = lines.size)

      val topoMap =
        TopoMap(
          dimension = dimension,
          data =
            lines
              .flatMapIndexed { i, row ->
                row.mapIndexed { j, height ->
                  val point = Point(i, j)
                  point to height.toString().toInt()
                }
              }
              .toMap(),
        )

      val listOfEnds =
        with(topoMap) {
          data
            .filterValues { height -> height == 0 }
            .keys
            .map { point -> ascendingPathsFrom(point).ends() }
        }

      test(PART_ONE) {
        val solution1 = listOfEnds.sumOf { ends -> ends.toSet().count() }
        solution1 shouldBe 811
      }

      test(PART_TWO) {
        val solution2 = listOfEnds.sumOf { ends -> ends.count() }
        solution2 shouldBe 1794
      }
    },
  )
