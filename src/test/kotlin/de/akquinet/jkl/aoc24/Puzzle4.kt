package de.akquinet.jkl.aoc24

import de.akquinet.jkl.aoc24.utils.IntPair
import de.akquinet.jkl.aoc24.utils.combineAll
import io.kotest.matchers.shouldBe

private fun fourRight(i: Int, j: Int): List<IntPair> =
  listOf(i to j, i to j + 1, i to j + 2, i to j + 3)

private fun fourBottom(i: Int, j: Int): List<IntPair> =
  listOf(i to j, i + 1 to j, i + 2 to j, i + 3 to j)

private fun fourBottomRight(i: Int, j: Int): List<IntPair> =
  listOf(i to j, i + 1 to j + 1, i + 2 to j + 2, i + 3 to j + 3)

private fun fourBottomLeft(i: Int, j: Int): List<IntPair> =
  listOf(i to j, i + 1 to j - 1, i + 2 to j - 2, i + 3 to j - 3)

private fun crossAt(i: Int, j: Int): List<IntPair> =
  listOf(i - 1 to j - 1, i - 1 to j + 1, i to j, i + 1 to j - 1, i + 1 to j + 1)

class Puzzle4 :
  PuzzleSpec(
    4,
    {
      val lines = readInputAsLines()
      val coordinates = combineAll(lines.indices, lines.first().indices)

      fun wordForShape(i: Int, j: Int, shape: (Int, Int) -> List<IntPair>): String? =
        shape(i, j)
          .takeIf { point -> coordinates.containsAll(point) }
          ?.joinToString("") { (ii, jj) -> lines[ii][jj].toString() }

      test("part one") {
        val solution1 =
          listOf(::fourRight, ::fourBottom, ::fourBottomRight, ::fourBottomLeft).sumOf { shape ->
            coordinates
              .mapNotNull { (i, j) -> wordForShape(i, j, shape) }
              .count { word -> word == "XMAS" || word == "SAMX" }
          }

        solution1 shouldBe 2336
      }

      test("part two") {
        val solution2 =
          coordinates
            .mapNotNull { (i, j) -> wordForShape(i, j, ::crossAt) }
            .count { word ->
              word == "MSAMS" || word == "SMASM" || word == "MMASS" || word == "SSAMM"
            }

        solution2 shouldBe 1831
      }
    },
  )
