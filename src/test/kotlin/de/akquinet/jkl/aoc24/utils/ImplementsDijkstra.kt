package de.akquinet.jkl.aoc24.utils

import java.util.*

interface ImplementsDijkstra<T> {
  fun distanceBetweenNeighbours(node1: T, node2: T): Int

  fun neighbours(node: T): List<T>

  private fun reconstructPath(current: T, cameFrom: Map<T, T>): List<T> =
    generateSequence(current) { node -> cameFrom[node] }.toList().reversed()

  fun dijkstra(start: T): Map<T, Int> {
    val priorityQueue = PriorityQueue(compareBy<Pair<T, Int>> { it.second })
    priorityQueue.add(start to 0)

    val distance = mutableMapOf(start to 0)

    while (priorityQueue.isNotEmpty()) {
      val (current, currentDistance) = priorityQueue.poll()

      if (currentDistance > distance.getOrDefault(current, Int.MAX_VALUE)) continue

      for (neighbor in neighbours(current)) {
        val newDistance = currentDistance + distanceBetweenNeighbours(current, neighbor)
        if (newDistance < distance.getOrDefault(neighbor, Int.MAX_VALUE)) {
          distance[neighbor] = newDistance
          priorityQueue.add(neighbor to newDistance)
        }
      }
    }

    return distance.toMap()
  }
}
