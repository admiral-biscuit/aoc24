package de.akquinet.jkl.aoc24

import de.akquinet.jkl.aoc24.utils.combineAll
import io.kotest.matchers.shouldBe

private sealed interface Schematic {
  data class Lock(override val heights: List<Int>) : Schematic

  data class Key(override val heights: List<Int>) : Schematic {
    fun fitsInto(lock: Lock): Boolean =
      heights.zip(lock.heights).map { (h1, h2) -> h1 + h2 <= 5 }.all { it }
  }

  val heights: List<Int>

  companion object {
    fun fromString(input: String): Schematic {
      val lines = input.split("\n")

      val firstLine = lines.first()

      val heights =
        firstLine.indices.map { i ->
          lines.indices.map { j -> lines[j][i] }.count { it == '#' } - 1
        }

      return if (firstLine.first() == '.') {
        Lock(heights)
      } else {
        Key(heights)
      }
    }
  }
}

class Puzzle25 :
  PuzzleSpec(
    25,
    {
      val schematics = readInputAsText().split("\n\n").map { Schematic.fromString(it) }

      test("part one") {
        val keys = schematics.filterIsInstance<Schematic.Key>()
        val locks = schematics.filterIsInstance<Schematic.Lock>()

        val solution1 = combineAll(keys, locks).count { (key, lock) -> key.fitsInto(lock) }
        solution1 shouldBe 3
      }
    },
  )
