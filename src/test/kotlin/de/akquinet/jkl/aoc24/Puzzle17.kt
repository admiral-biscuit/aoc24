package de.akquinet.jkl.aoc24

import io.kotest.matchers.shouldBe

private infix fun Long.pow(exponent: Long): Long {
  var result = 1L
  (1..exponent).forEach { _ -> result *= this }
  return result
}

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
    fun fromString(string: String): ThreeBitInteger =
      ThreeBitInteger.entries.first { it.value == string.toInt() }
  }
}

private class ProgramRunner(
  val program: List<ThreeBitInteger>,
  var regA: Long,
  var regB: Long,
  var regC: Long,
) {
  var instructionPointer = 0
  val out = mutableListOf<Int>()

  fun runProgram(): String {
    while (instructionPointer < program.size - 1) {
      val instruction = getInstruction(program[instructionPointer])
      val input = program[instructionPointer + 1]
      instruction(input)
      instructionPointer += 2
    }

    return out.joinToString(",")
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
    out.add(comboOperand(input).mod(8))
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
    },
  )
