package de.akquinet.jkl.aoc24

import io.kotest.matchers.shouldBe

private data class CityMap(val width: Int, val height: Int, val data: Map<IntPair, Char>) {
  val antennaLabels = data.values.toSet()
  val antiNodes = mutableSetOf<IntPair>()

  fun antennasByChar(char: Char): List<IntPair> =
    data.filterValues { it == char }.toList().map { it.first }

  fun isInBounds(position: IntPair): Boolean =
    position.first in 0..<height && position.second in 0..<width

  fun putAntinodeAt(position: IntPair) {
    antiNodes.add(position)
  }
}

private fun <A> List<A>.allPairs(): List<Pair<A, A>> =
  flatMapIndexed { i, first -> mapIndexed { j, second -> if (i < j) first to second else null } }
    .filterNotNull()

private fun Pair<IntPair, IntPair>.maxTwoAntinodes(cityMap: CityMap): List<IntPair> {
  val (node1, node2) = this

  val di = node1.first - node2.first
  val dj = node1.second - node2.second

  val antiNode1 = node1.first + di to node1.second + dj
  val antiNode2 = node2.first - di to node2.second - dj

  return listOf(antiNode1, antiNode2).filter { cityMap.isInBounds(it) }
}

private fun Pair<IntPair, IntPair>.allAntinodes(cityMap: CityMap): List<IntPair> {
  val (node1, node2) = this
  val di = node1.first - node2.first
  val dj = node1.second - node2.second

  val result = mutableListOf<IntPair>()
  var t = 0

  while (true) {
    val antiNode1 = node1.first + t * di to node1.second + t * dj
    val antiNode2 = node2.first - t * di to node2.second - t * dj

    val antiNodes = listOf(antiNode1, antiNode2).filter { cityMap.isInBounds(it) }

    if (antiNodes.isEmpty()) break

    result.addAll(antiNodes)
    t += 1
  }

  return result
}

class Puzzle8 :
  PuzzleSpec(
    8,
    {
      val lines = readInputAsLines()

      val cityMap =
        CityMap(
          width = lines.first().length,
          height = lines.size,
          data =
            lines
              .mapIndexed { i, row -> row.mapIndexed { j, char -> Pair(i, j) to char } }
              .flatten()
              .toMap()
              .filterValues { char -> char != '.' },
        )

      fun solvePuzzleFor(
        cityMap: CityMap,
        antiNodeStrategy: Pair<IntPair, IntPair>.(CityMap) -> List<IntPair>,
      ): Int {
        val cm = cityMap.copy()

        cm.antennaLabels.forEach { char ->
          val antennaPositions = cm.antennasByChar(char)
          val antennaPairs = antennaPositions.allPairs()
          antennaPairs.forEach { nodes ->
            val antiNodes = nodes.antiNodeStrategy(cm)
            antiNodes.toList().forEach { antiNode -> cm.putAntinodeAt(antiNode) }
          }
        }

        return cm.antiNodes.size
      }

      test(PART_ONE) {
        val solution1 = solvePuzzleFor(cityMap, Pair<IntPair, IntPair>::maxTwoAntinodes)
        solution1 shouldBe 247
      }

      test(PART_TWO) {
        val solution2 = solvePuzzleFor(cityMap, Pair<IntPair, IntPair>::allAntinodes)
        solution2 shouldBe 861
      }
    },
  )
