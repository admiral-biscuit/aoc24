package de.akquinet.jkl.aoc24

import io.kotest.matchers.shouldBe

private typealias Operation = (Long, Long) -> Long

private fun List<Long>.applyOperations(ops: List<Operation>): Long {
  require(ops.size == size - 1)
  var result = first()
  ops.forEachIndexed { i, op -> result = op(result, this[i + 1]) }
  return result
}

private data class Equation(val lhs: Long, val rhs: List<Long>) {
  fun holdsForOperations(ops: List<Operation>): Boolean = lhs == rhs.applyOperations(ops)
}

private fun <T> generatePaths(values: Sequence<T>, length: Int): Sequence<List<T>> {
  if (length == 0) return sequenceOf(emptyList())

  return generatePaths(values, length - 1).flatMap { path ->
    values.map { value -> listOf(value) + path }
  }
}

class Puzzle7 :
  PuzzleSpec(
    7,
    {
      val equations =
        readInputAsLines().map { line ->
          val (leftString, rightString) = line.split(":")
          val lhs = leftString.toLong()
          val rhs = rightString.trim().split(" ").map { it.toLong() }
          Equation(lhs, rhs)
        }

      val plus: Operation = { x, y -> x + y }
      val times: Operation = { x, y -> x * y }

      fun solvePuzzleForBaseOps(baseOps: Sequence<Operation>) =
        equations.sumOf { equation ->
          val opsPath = generatePaths(baseOps, equation.rhs.size - 1)
          val equationHolds = opsPath.any { ops -> equation.holdsForOperations(ops) }
          if (equationHolds) equation.lhs else 0
        }

      test("part one") {
        val baseOps = sequenceOf(plus, times)
        val solution1 = solvePuzzleForBaseOps(baseOps)

        solution1 shouldBe 6392012777720
      }

      test("part two") {
        val concat: Operation = { x, y -> "$x$y".toLong() }

        val baseOps = sequenceOf(plus, times, concat)
        val solution2 = solvePuzzleForBaseOps(baseOps)

        solution2 shouldBe 61561126043536
      }
    },
  )
