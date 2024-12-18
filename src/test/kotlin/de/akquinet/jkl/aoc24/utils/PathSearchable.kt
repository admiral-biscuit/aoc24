package de.akquinet.jkl.aoc24.utils

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import java.util.*

interface PathSearchable<T> {
  companion object {
    data class NoPathBetween<T>(val start: T, val end: T)
  }

  fun distance(node1: T, node2: T): Int

  fun neighbours(node: T): List<T>

  private fun reconstructPath(current: T, cameFrom: Map<T, T>): List<T> =
    generateSequence(current) { node -> cameFrom[node] }.toList().reversed()

  // shamelessly copied from Wikipedia
  fun aStarPath(start: T, end: T): Either<NoPathBetween<T>, List<T>> {
    val heuristic: (T) -> Int = { node -> distance(node, end) }

    val cameFrom = mutableMapOf<T, T>()
    val gMap = mutableMapOf(start to 0)
    val fMap = mutableMapOf(start to heuristic(start))

    val discovered = PriorityQueue(compareBy<T> { fMap.getOrDefault(it, Int.MAX_VALUE) })
    discovered.add(start)

    while (discovered.isNotEmpty()) {
      val current = discovered.poll()

      if (current == end) return reconstructPath(current, cameFrom).right()

      neighbours(current).forEach { neighbour ->
        val tentativeGScore = gMap[current]!! + distance(current, neighbour)
        if (tentativeGScore < gMap.getOrDefault(neighbour, Int.MAX_VALUE)) {
          cameFrom[neighbour] = current
          gMap[neighbour] = tentativeGScore
          fMap[neighbour] = tentativeGScore + heuristic(neighbour)
          discovered.add(neighbour)
        }
      }
    }

    return NoPathBetween(start, end).left()
  }
}
