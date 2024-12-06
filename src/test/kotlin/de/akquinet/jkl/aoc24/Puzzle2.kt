package de.akquinet.jkl.aoc24

import io.kotest.matchers.shouldBe
import kotlin.math.abs

private fun List<Pair<Int, Int>>.isIncreasing(): Boolean = map { (x, y) -> y - x }.all { it > 0 }

private fun List<Pair<Int, Int>>.isDecreasing(): Boolean = map { (x, y) -> y - x }.all { it < 0 }

private fun List<Pair<Int, Int>>.hasOnlySmallDiffs(): Boolean =
  map { (x, y) -> abs(x - y) }.all { it in 1..3 }

private fun List<Int>.isSafe(): Boolean =
  zipWithNext().let { (it.isDecreasing() || it.isIncreasing()) && it.hasOnlySmallDiffs() }

private fun List<Int>.canBeMadeSafe(): Boolean {
  if (isSafe()) return true

  indices.forEach { index ->
    val subList = toMutableList().also { it.removeAt(index) }.toList()
    if (subList.isSafe()) return true
  }

  return false
}

class Puzzle2 :
  PuzzleSpec(
    2,
    {
      val numberLists = readInputAsLines().map { line -> line.split(" ").map { it.toInt() } }

      test(PART_ONE) {
        val solution1 = numberLists.count { it.isSafe() }
        solution1 shouldBe 432
      }

      test(PART_TWO) {
        val solution2 = numberLists.count { it.canBeMadeSafe() }
        solution2 shouldBe 488
      }
    },
  )
