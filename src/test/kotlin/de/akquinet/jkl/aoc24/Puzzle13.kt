package de.akquinet.jkl.aoc24

import de.akquinet.jkl.aoc24.utils.LongPoint
import io.kotest.matchers.shouldBe

private data class ClawMachine(
  val onA: LongPoint,
  val onB: LongPoint,
  val prizeLocation: LongPoint,
) {
  fun solve(): LongPoint? {
    val (a1, a2) = onA
    val (b1, b2) = onB
    val (c1, c2) = prizeLocation

    val det = a1 * b2 - b1 * a2

    if (det == 0L) return null

    val x1 = (c1 * b2 - b1 * c2).toDouble() / det
    val x2 = (a1 * c2 - c1 * a2).toDouble() / det

    return if (x1 % 1.0 == 0.0 && x2 % 1.0 == 0.0) {
      LongPoint(x1.toLong(), x2.toLong())
    } else {
      null
    }
  }
}

class Puzzle13 :
  PuzzleSpec(
    13,
    {
      val blocks = readInputAsText().split("\n\n").map { it.split("\n") }
      val xyRegex = Regex("X[+=](\\d+), Y[+=](\\d+)")

      val clawMachines =
        blocks.map { block ->
          val points =
            block.map { line ->
              val (x, y) = xyRegex.find(line)!!.destructured
              LongPoint(x.toLong(), y.toLong())
            }

          ClawMachine(points[0], points[1], points[2])
        }

      test(PART_ONE) {
        val solution1 = clawMachines.mapNotNull { it.solve() }.sumOf { (x, y) -> 3 * x + y }
        solution1 shouldBe 36954L
      }

      test(PART_TWO) {
        val solution2 =
          clawMachines
            .map {
              val newPrizeLocation = it.prizeLocation plus LongPoint(10000000000000, 10000000000000)
              it.copy(prizeLocation = newPrizeLocation)
            }
            .mapNotNull { it.solve() }
            .sumOf { (x, y) -> 3 * x + y }

        solution2 shouldBe 79352015273424L
      }
    },
  )
