package de.akquinet.jkl.aoc24

import de.akquinet.jkl.aoc24.utils.Direction
import de.akquinet.jkl.aoc24.utils.ImplementsDijkstra
import de.akquinet.jkl.aoc24.utils.Point
import de.akquinet.jkl.aoc24.utils.combineAll
import io.kotest.matchers.shouldBe

private data class KeypadButton(val label: String, val coords: Point)

private data class Keypad(val buttons: List<KeypadButton>) : ImplementsDijkstra<KeypadButton> {
  fun getButtonByLabel(label: String) = buttons.single { it.label == label }

  override fun distanceBetweenNeighbours(node1: KeypadButton, node2: KeypadButton): Int = 1

  override fun neighbours(node: KeypadButton): List<KeypadButton> =
    buttons.filter { it.coords manhattanDistance node.coords == 1 }

  companion object {
    val NUMERIC: Keypad =
      listOf(
          "7" to (0 to 0),
          "8" to (1 to 0),
          "9" to (2 to 0),
          "4" to (0 to 1),
          "5" to (1 to 1),
          "6" to (2 to 1),
          "1" to (0 to 2),
          "2" to (1 to 2),
          "3" to (2 to 2),
          "0" to (1 to 3),
          "A" to (2 to 3),
        )
        .map { (label, coords) -> KeypadButton(label, Point(coords.first, coords.second)) }
        .let { Keypad(it) }

    val ARROWS: Keypad =
      listOf("^" to (1 to 0), "A" to (2 to 0), "<" to (0 to 1), "v" to (1 to 1), ">" to (2 to 1))
        .map { (label, coords) -> KeypadButton(label, Point(coords.first, coords.second)) }
        .let { Keypad(it) }
  }

  fun segmentToSymbols(segment: List<KeypadButton>): String {
    val symbols =
      segment.zipWithNext().map { (node1, node2) ->
        val delta = node2.coords minus node1.coords
        val direction = Direction.getByVector(delta)
        direction.toSymbol()
      }

    return symbols.joinToString("") + "A"
  }

  fun allShortestSegments(input: String): List<List<String>> {
    val modifiedInput = "A$input"
    val buttons = modifiedInput.toList().map { getButtonByLabel("$it") }
    return buttons.zipWithNext().map { (start, end) ->
      val allSegments = dijkstraAllPaths(start, end)
      allSegments.map { segmentToSymbols(it) }
    }
  }

  fun allShortestSequences(input: String): List<String> {
    var paths = listOf("")
    val segments = allShortestSegments(input)
    segments.forEach { segment ->
      val combinations = combineAll(paths, segment)
      paths = combinations.map { (path, segment) -> path + segment }
    }
    return paths
  }
}

private fun calculateShortestSequence(code: String) =
  Keypad.NUMERIC.allShortestSequences(code)
    .flatMap { Keypad.ARROWS.allShortestSequences(it) }
    .flatMap { Keypad.ARROWS.allShortestSequences(it) }
    .minBy { it.length }

private fun calculateComplexity(code: String, sequence: String): Int =
  sequence.length * code.replace("A", "").toInt()

class Puzzle21 :
  PuzzleSpec(
    21,
    {
      val codes = readInputAsLines()

      test("part one") {
        val solution1 =
          codes.sumOf { code ->
            val sequence = calculateShortestSequence(code)
            calculateComplexity(code, sequence)
          }

        solution1 shouldBe 138764
      }
    },
  )
