package de.akquinet.jkl.aoc24

import de.akquinet.jkl.aoc24.utils.applyRepeatedly
import de.akquinet.jkl.aoc24.utils.countEachElement
import io.kotest.matchers.shouldBe

private fun replaceStone(number: Long): List<Long> {
  val ns = number.toString()

  return when {
    number == 0L -> listOf(1L)
    ns.length % 2 == 0 ->
      listOf(ns.substring(0, ns.length / 2), ns.substring(ns.length / 2)).map { it.toLong() }
    else -> listOf(2024L * number)
  }
}

class Puzzle11 :
  PuzzleSpec(
    11,
    {
      val stoneNumbers = readInputAsText().split(" ").map { it.toLong() }

      fun solvePuzzle(numberOfBlinks: Int): Long =
        stoneNumbers
          .countEachElement()
          .applyRepeatedly(numberOfBlinks) { oldStonesWithCount ->
            val newStonesWithCount = mutableMapOf<Long, Long>()
            oldStonesWithCount.forEach { (oldStone, oldCount) ->
              val newStones = replaceStone(oldStone)
              newStones.forEach { newStone ->
                newStonesWithCount[newStone] =
                  newStonesWithCount.getOrDefault(newStone, 0L) + oldCount
              }
            }
            newStonesWithCount.toMap()
          }
          .values
          .sum()

      test(PART_ONE) {
        val solution1 = solvePuzzle(25)
        solution1 shouldBe 186996L
      }

      test(PART_TWO) {
        val solution2 = solvePuzzle(75)
        solution2 shouldBe 221683913164898L
      }
    },
  )
