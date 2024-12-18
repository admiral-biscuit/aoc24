package de.akquinet.jkl.aoc24

import de.akquinet.jkl.aoc24.utils.pow
import io.kotest.matchers.shouldBe

private enum class ThreeBitInteger(val value: Int) {
  ZERO(0),
  ONE(1),
  TWO(2),
  THREE(3),
  FOUR(4),
  FIVE(5),
  SIX(6),
  SEVEN(7);

  companion object {
    fun fromInt(int: Int): ThreeBitInteger = ThreeBitInteger.entries.first { it.value == int }

    fun fromString(string: String): ThreeBitInteger = fromInt(string.toInt())
  }
}

private fun List<ThreeBitInteger>.toOutputString(): String =
  joinToString(",") { it.value.toString() }

private class ProgramRunner(
  val program: List<ThreeBitInteger>,
  var regA: Long,
  var regB: Long,
  var regC: Long,
) {
  var instructionPointer = 0
  val out = mutableListOf<ThreeBitInteger>()

  fun runProgram(): String {
    while (instructionPointer < program.size - 1) {
      val instruction = getInstruction(program[instructionPointer])
      val input = program[instructionPointer + 1]
      instruction(input)
      instructionPointer += 2
    }

    return out.toOutputString()
  }

  private fun getInstruction(opCode: ThreeBitInteger): (ThreeBitInteger) -> Unit =
    when (opCode) {
      ThreeBitInteger.ZERO -> ::adv
      ThreeBitInteger.ONE -> ::bxl
      ThreeBitInteger.TWO -> ::bst
      ThreeBitInteger.THREE -> ::jnz
      ThreeBitInteger.FOUR -> ::bxc
      ThreeBitInteger.FIVE -> ::out
      ThreeBitInteger.SIX -> ::bdv
      ThreeBitInteger.SEVEN -> ::cdv
    }

  private fun adv(input: ThreeBitInteger) {
    regA /= 2L pow comboOperand(input)
  }

  private fun bxl(input: ThreeBitInteger) {
    regB = regB xor input.value.toLong()
  }

  private fun bst(input: ThreeBitInteger) {
    regB = comboOperand(input).mod(8L)
  }

  private fun jnz(input: ThreeBitInteger) {
    if (regA == 0L) return
    instructionPointer = input.value - 2
  }

  @Suppress("UNUSED_PARAMETER")
  private fun bxc(input: ThreeBitInteger) {
    regB = regB xor regC
  }

  private fun out(input: ThreeBitInteger) {
    out.add(ThreeBitInteger.fromInt(comboOperand(input).mod(8)))
  }

  private fun bdv(input: ThreeBitInteger) {
    regB = regA / (2L pow comboOperand(input))
  }

  private fun cdv(input: ThreeBitInteger) {
    regC = regA / (2L pow comboOperand(input))
  }

  private fun comboOperand(input: ThreeBitInteger): Long =
    when (input) {
      ThreeBitInteger.ZERO,
      ThreeBitInteger.ONE,
      ThreeBitInteger.TWO,
      ThreeBitInteger.THREE -> input.value.toLong()
      ThreeBitInteger.FOUR -> regA
      ThreeBitInteger.FIVE -> regB
      ThreeBitInteger.SIX -> regC
      ThreeBitInteger.SEVEN -> throw IllegalStateException("not a valid program")
    }
}

class Puzzle17 :
  PuzzleSpec(
    17,
    {
      val input = readInputAsLines()
      val regA = input[0].split(" ")[2].toLong()
      val regB = input[1].split(" ")[2].toLong()
      val regC = input[2].split(" ")[2].toLong()
      val program = input[4].split(" ")[1].split(",").map { s -> ThreeBitInteger.fromString(s) }

      test("part one") {
        val programRunner = ProgramRunner(program, regA, regB, regC)
        val solution1 = programRunner.runProgram()
        solution1 shouldBe "2,3,4,7,5,7,3,0,7"
      }

      test("part two") {
        // this is what the program actually does before jumping to the start again
        fun step(a: Long, out: MutableList<ThreeBitInteger>): Long {
          var b = a.mod(8L) xor 2L
          val c = a / (2L pow b)
          b = b xor c xor 7
          out.add(ThreeBitInteger.fromInt(b.mod(8L).toInt()))
          return a / 8L
        }

        // this is now the same as ProgramRunner(program, regA, regB, regC).runProgram()
        fun runProgram(aInit: Long): List<ThreeBitInteger> {
          val out = mutableListOf<ThreeBitInteger>()
          var a = aInit
          while (a != 0L) {
            a = step(a, out)
          }
          return out.toList()
        }

        runProgram(regA).toOutputString() shouldBe "2,3,4,7,5,7,3,0,7"

        // this is what we want as output
        val targetOutput =
          "2,4,1,2,7,5,4,3,0,3,1,7,5,5,3,0".split(",").map { ThreeBitInteger.fromString(it) }
        val targetOutputReversed = targetOutput.reversed()

        data class RegAValue(val value: Long, val previous: List<RegAValue>?) {
          fun ends(): List<RegAValue> = previous?.flatMap { it.ends() } ?: listOf(this)
        }

        // we will now revert the process described by "step" recursively
        fun possiblePreviousValues(a: Long, index: Int = 0): RegAValue {
          if (index > targetOutputReversed.size - 1) return RegAValue(a, null)

          val left = 8L * a
          val right = 8L * (a + 1)
          val previous =
            (left..<right).filter {
              it != 0L && runProgram(it).reversed()[index] == targetOutputReversed[index]
            }

          return RegAValue(a, previous.map { possiblePreviousValues(it, index + 1) })
        }

        // solve the puzzle
        val allPossibleValues = possiblePreviousValues(0L)
        val minRegA = allPossibleValues.ends().minOf { it.value }

        runProgram(minRegA) shouldBe targetOutput
        minRegA shouldBe 190384609508367L
      }
    },
  )
