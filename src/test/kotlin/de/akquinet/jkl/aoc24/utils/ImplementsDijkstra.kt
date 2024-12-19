package de.akquinet.jkl.aoc24.utils

import java.util.*

interface ImplementsDijkstra<T> {
  fun distanceBetweenNeighbours(node1: T, node2: T): Int

  fun neighbours(node: T): List<T>

  fun dijkstra(start: T): Map<T, Int> {
    val priorityQueue = PriorityQueue(compareBy<Pair<T, Int>> { it.second })
    priorityQueue.add(start to 0)

    val distance = mutableMapOf(start to 0)

    while (priorityQueue.isNotEmpty()) {
      val (current, currentDistance) = priorityQueue.poll()

      if (currentDistance > distance.getOrDefault(current, Int.MAX_VALUE)) continue

      for (neighbour in neighbours(current)) {
        val newDistance = currentDistance + distanceBetweenNeighbours(current, neighbour)
        if (newDistance < distance.getOrDefault(neighbour, Int.MAX_VALUE)) {
          distance[neighbour] = newDistance
          priorityQueue.add(neighbour to newDistance)
        }
      }
    }

    return distance.toMap()
  }

  fun dijkstraAllPaths(start: T, end: T): List<List<T>> {
    val priorityQueue = PriorityQueue(compareBy<Pair<T, Int>> { it.second })
    priorityQueue.add(start to 0)

    val distance = mutableMapOf(start to 0)
    val paths = mutableMapOf(start to listOf(listOf(start)))

    while (priorityQueue.isNotEmpty()) {
      val (current, currentDistance) = priorityQueue.poll()

      if (current == end) {
        return paths[end] ?: emptyList()
      }

      if (currentDistance > distance.getOrDefault(current, Int.MAX_VALUE)) continue

      for (neighbour in neighbours(current)) {
        val newDistance = currentDistance + distanceBetweenNeighbours(current, neighbour)
        when {
          newDistance < distance.getOrDefault(neighbour, Int.MAX_VALUE) -> {
            distance[neighbour] = newDistance
            paths[neighbour] = paths[current]!!.map { it + neighbour }
            priorityQueue.add(neighbour to newDistance)
          }

          newDistance == distance[neighbour] -> {
            val existingPaths = paths[neighbour] ?: emptyList()
            paths[neighbour] = existingPaths + paths[current]!!.map { it + neighbour }
          }
        }
      }
    }

    return emptyList()
  }
}
