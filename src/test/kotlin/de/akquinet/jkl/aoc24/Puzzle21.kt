package de.akquinet.jkl.aoc24

import de.akquinet.jkl.aoc24.utils.*
import io.kotest.matchers.shouldBe

private data class KeypadButton(val label: Char, val coords: Point)

private data class Keypad(val buttons: List<KeypadButton>) : ImplementsDijkstra<KeypadButton> {
  override fun distanceBetweenNeighbours(node1: KeypadButton, node2: KeypadButton): Int = 1

  override fun neighbours(node: KeypadButton): List<KeypadButton> =
    buttons.filter { it.coords manhattanDistance node.coords == 1 }

  companion object {
    val NUMBERS: Keypad =
      listOf(
          '7' to (0 to 0),
          '8' to (1 to 0),
          '9' to (2 to 0),
          '4' to (0 to 1),
          '5' to (1 to 1),
          '6' to (2 to 1),
          '1' to (0 to 2),
          '2' to (1 to 2),
          '3' to (2 to 2),
          '0' to (1 to 3),
          'A' to (2 to 3),
        )
        .map { (label, coords) -> KeypadButton(label, Point(coords.first, coords.second)) }
        .let { Keypad(it) }

    val ARROWS: Keypad =
      listOf('^' to (1 to 0), 'A' to (2 to 0), '<' to (0 to 1), 'v' to (1 to 1), '>' to (2 to 1))
        .map { (label, coords) -> KeypadButton(label, Point(coords.first, coords.second)) }
        .let { Keypad(it) }
  }

  val allShortestPaths: Map<Pair<Char, Char>, List<String>> =
    combineAll(buttons, buttons).associate { (start, end) ->
      (start.label to end.label) to dijkstraAllPaths(start, end).map { pathToString(it) }
    }

  private fun pathToString(segment: List<KeypadButton>): String =
    segment.zipWithNext().joinToString("") { (button1, button2) ->
      val delta = button2.coords minus button1.coords
      val direction = Direction.getByVector(delta)
      "${direction.toChar()}"
    }
}

private fun String.calculateShortestPathLength(
  numberOfRobots: Int,
  iteration: Int = 0,
  cache: MutableMap<Pair<String, Int>, Long> = mutableMapOf(),
): Long =
  cache.getOrPut(this to iteration) {
    val allShortestPaths =
      when (iteration) {
        0 -> Keypad.NUMBERS.allShortestPaths
        else -> Keypad.ARROWS.allShortestPaths
      }

    when (iteration) {
      numberOfRobots + 1 -> length.toLong()
      else -> {
        "A$this".zipWithNext().sumOf { (start, end) ->
          allShortestPaths.getValue(start to end).minOf { path ->
            "${path}A".calculateShortestPathLength(numberOfRobots, iteration + 1, cache)
          }
        }
      }
    }
  }

private fun String.calculateComplexity(numberOfRobots: Int): Long =
  calculateShortestPathLength(numberOfRobots) * replace("A", "").toInt()

class Puzzle21 :
  PuzzleSpec(
    21,
    {
      val codes = readInputAsLines()

      test("part one") {
        val solution1 = codes.sumOf { code -> code.calculateComplexity(2) }
        solution1 shouldBe 138764L
      }

      test("part two") {
        val solution2 = codes.sumOf { code -> code.calculateComplexity(25) }
        solution2 shouldBe 169137886514152L
      }
    },
  )
