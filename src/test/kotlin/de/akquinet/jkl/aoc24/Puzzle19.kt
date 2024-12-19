package de.akquinet.jkl.aoc24

import io.kotest.matchers.shouldBe

@JvmInline
private value class TowelPattern(val value: String) {
  fun length(): Int = value.length

  fun tail(start: Int): TowelPattern = TowelPattern(value.substring(start))

  fun startsWith(pattern: TowelPattern): Boolean = value.startsWith(pattern.value)

  fun isEmpty(): Boolean = value.isEmpty()
}

private fun List<TowelPattern>.waysToMakeDesign(
  design: TowelPattern,
  cache: MutableMap<TowelPattern, Long> = mutableMapOf(),
): Long =
  if (design.isEmpty()) {
    1L
  } else {
    cache.getOrPut(design) {
      filter { pattern -> design.startsWith(pattern) }
        .sumOf { pattern -> waysToMakeDesign(design.tail(pattern.length()), cache) }
    }
  }

class Puzzle19 :
  PuzzleSpec(
    19,
    {
      val (block1, block2) = readInputAsText().split("\n\n")
      val patterns = block1.split(", ").map { TowelPattern(it) }
      val designs = block2.split("\n").map { TowelPattern(it) }

      test("part one") {
        val solution1 = designs.count { design -> patterns.waysToMakeDesign(design) > 0 }
        solution1 shouldBe 324
      }

      test("part two") {
        val solution2 = designs.sumOf { design -> patterns.waysToMakeDesign(design) }
        solution2 shouldBe 575227823167869L
      }
    },
  )
