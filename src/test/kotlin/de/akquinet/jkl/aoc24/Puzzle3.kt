package de.akquinet.jkl.aoc24

import io.kotest.matchers.shouldBe

class Puzzle3 :
  PuzzleSpec(
    3,
    {
      val input = readInputAsText()

      val mulRegex = Regex("mul\\(([0-9]+),([0-9]+)\\)")

      test(PART_ONE) {
        val solution1 =
          mulRegex.findAll(input).sumOf {
            val (a, b) = it.destructured
            a.toInt() * b.toInt()
          }

        solution1 shouldBe 161289189
      }

      test(PART_TWO) {
        val doRegex = Regex("do\\(\\)")
        val dontRegex = Regex("don't\\(\\)")

        val regex = Regex("${doRegex.pattern}|${dontRegex.pattern}|${mulRegex.pattern}")

        var enabled = true
        var result = 0

        regex.findAll(input).forEach { matchResult ->
          val capture = matchResult.groupValues.first()
          when {
            capture.matches(doRegex) -> enabled = true
            capture.matches(dontRegex) -> enabled = false
            else ->
              if (enabled) {
                val (a, b) = matchResult.destructured
                result += a.toInt() * b.toInt()
              }
          }
        }

        result shouldBe 83595109
      }
    },
  )
