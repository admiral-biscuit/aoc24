package de.akquinet.jkl.aoc24

import io.kotest.matchers.shouldBe

private data class Antenna(val point: Point, val char: Char)

private data class CityMap(val dimension: Dimension, val data: List<Antenna>) {
  val antinodes = mutableSetOf<Point>()
}

private fun Pair<Point, Point>.antinodes(dimension: Dimension, tRange: Iterable<Int>): List<Point> {
  val (node1, node2) = this
  val delta = node1 minus node2

  val result = mutableListOf<Point>()
  val iterator = tRange.iterator()

  while (iterator.hasNext()) {
    val t = iterator.next()

    val antinode1 = node1.plus(delta.scaleBy(t))
    val antinode2 = node2.minus(delta.scaleBy(t))

    val antinodes = listOf(antinode1, antinode2).filter { point -> dimension containsPoint point }

    if (antinodes.isEmpty()) break

    result.addAll(antinodes)
  }

  return result
}

class Puzzle8 :
  PuzzleSpec(
    8,
    {
      val lines = readInputAsLines()
      val dimension = Dimension(width = lines.first().length, height = lines.size)

      val cityMap =
        CityMap(
          dimension = dimension,
          data =
            lines
              .flatMapIndexed { i, row ->
                row.mapIndexed { j, char ->
                  val point = Point(i, j)
                  Antenna(point, char).takeIf { char != '.' }
                }
              }
              .filterNotNull(),
        )

      fun solvePuzzleFor(tRange: Iterable<Int>): Int {
        with(cityMap.copy()) {
          data.forEach { antenna ->
            val pointsForChar = data.filter { it.char == antenna.char }.map { it.point }
            val pointPairs = pointsForChar.allPairs()
            pointPairs.forEach { pointPair ->
              antinodes.addAll(pointPair.antinodes(dimension, tRange))
            }
          }
          return antinodes.size
        }
      }

      test(PART_ONE) {
        val solution1 = solvePuzzleFor(tRange = listOf(1))
        solution1 shouldBe 247
      }

      test(PART_TWO) {
        val solution2 = solvePuzzleFor(tRange = 0..<Int.MAX_VALUE)
        solution2 shouldBe 861
      }
    },
  )
