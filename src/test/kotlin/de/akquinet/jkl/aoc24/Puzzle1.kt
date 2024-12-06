package de.akquinet.jkl.aoc24

import io.kotest.matchers.shouldBe
import kotlin.math.abs

class Puzzle1 :
  PuzzleSpec(
    1,
    {
      val columns =
        readInputAsLines()
          .map {
            val ints = it.split("   ")
            Pair(ints[0].toInt(), ints[1].toInt())
          }
          .unzip()

      test(PART_ONE) {
        val firstColumnSorted = columns.first.sorted()
        val secondColumnSorted = columns.second.sorted()
        val solution1 = firstColumnSorted.zip(secondColumnSorted) { x, y -> abs(x - y) }.sum()

        solution1 shouldBe 3569916
      }

      test(PART_TWO) {
        val (firstColumnSorted, secondColumnSorted) = columns
        val solution2 =
          firstColumnSorted.sumOf { x -> x * secondColumnSorted.count { y -> x == y } }

        solution2 shouldBe 26407426
      }
    },
  )
