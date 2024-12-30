package de.akquinet.jkl.aoc24

import de.akquinet.jkl.aoc24.utils.mergeCounts
import io.kotest.matchers.shouldBe

data class SecretNumberGenerator(val seed: Long, val numberOfValues: Int) {
  private fun mix(secretNumber: Long, value: Long): Long = secretNumber xor value

  private fun prune(secretNumber: Long): Long = secretNumber.mod(16777216L)

  private fun next(secretNumber: Long): Long {
    var result = secretNumber

    result = prune(mix(result, 64L * result))
    result = prune(mix(result, result / 32L))
    result = prune(mix(result, 2048L * result))

    return result
  }

  val secretNumbers: List<Long> =
    generateSequence(seed) { next(it) }.take(numberOfValues + 1).toList()

  private val bananas: List<Int> = secretNumbers.map { it.mod(10L).toInt() }

  private fun changes(): List<Int> = bananas.zipWithNext { x, y -> y - x }

  fun changeWindows(): Map<List<Int>, Int> =
    changes()
      .windowed(4, partialWindows = false)
      .mapIndexed { i, window -> window to bananas[i + 4] }
      // if there are duplicate keys (i.e. windows with different banana counts), only the last
      // one gets added to the map, so we revert this behaviour
      .reversed()
      .toMap()
}

class Puzzle22 :
  PuzzleSpec(
    22,
    {
      val seeds = readInputAsLines().map { line -> line.toLong() }
      val sngs = seeds.map { seed -> SecretNumberGenerator(seed, 2000) }

      test("part one") {
        val solution1 = sngs.sumOf { sng -> sng.secretNumbers.last() }
        solution1 shouldBe 18261820068L
      }

      test("part two") {
        val solution2 =
          sngs
            .map { sng -> sng.changeWindows() }
            .reduce { counts1, counts2 -> counts1 mergeCounts counts2 }
            .maxOf { (_, count) -> count }

        solution2 shouldBe 2044
      }
    },
  )
