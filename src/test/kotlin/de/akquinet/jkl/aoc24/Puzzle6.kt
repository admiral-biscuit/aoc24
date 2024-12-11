package de.akquinet.jkl.aoc24

import de.akquinet.jkl.aoc24.utils.IntPair
import io.kotest.matchers.shouldBe

private enum class Field(val char: Char) {
  EMPTY('.'),
  OBSTACLE('#');

  companion object {
    fun byChar(char: Char): Field = Field.entries.firstOrNull { it.char == char } ?: EMPTY
  }
}

private enum class Direction(val dr: Int, val dc: Int) {
  NORTH(-1, 0),
  EAST(0, 1),
  SOUTH(1, 0),
  WEST(0, -1);

  fun turnRight(): Direction =
    when (this) {
      NORTH -> EAST
      EAST -> SOUTH
      SOUTH -> WEST
      WEST -> NORTH
    }
}

private fun IntPair.plus(direction: Direction): IntPair =
  Pair(first + direction.dr, second + direction.dc)

private data class Room(val data: Map<IntPair, Field>) {
  fun getField(point: IntPair): Field? = data[point]

  fun withExtraObstacleAt(point: IntPair): Room {
    val newData = data.toMutableMap().also { it[point] = Field.OBSTACLE }.toMap()
    return copy(data = newData)
  }
}

private enum class GuardState {
  MOVING,
  OUT_OF_BOUNDS,
  TRAPPED_IN_CYCLE,
}

private data class Guard(
  var position: IntPair,
  var direction: Direction,
  var state: GuardState = GuardState.MOVING,
) {
  val visited = mutableSetOf(position)
  val record = mutableSetOf(position to direction)

  fun moveTo(newPosition: IntPair) {
    position = newPosition
    visited.add(newPosition)
    record.add(newPosition to direction)
  }
}

class Puzzle6 :
  PuzzleSpec(
    6,
    {
      var startingAt: IntPair = 0 to 0

      val room =
        Room(
          readInputAsLines()
            .mapIndexed { i, row ->
              row.mapIndexed { j, char ->
                val pair = i to j
                if (char == '^') startingAt = pair
                pair to Field.byChar(char)
              }
            }
            .flatten()
            .toMap()
        )

      // reuse for part 2
      var guardPath: Set<IntPair> = emptySet()

      fun doGuardWalk(start: IntPair, room: Room): Guard {
        val guard = Guard(start, Direction.NORTH)

        while (guard.state == GuardState.MOVING) {
          val nextPoint = guard.position.plus(guard.direction)

          if (guard.record.contains(nextPoint to guard.direction)) {
            guard.state = GuardState.TRAPPED_IN_CYCLE
            break
          }

          when (room.getField(nextPoint)) {
            Field.EMPTY -> guard.moveTo(nextPoint)
            Field.OBSTACLE -> guard.direction = guard.direction.turnRight()
            null -> guard.state = GuardState.OUT_OF_BOUNDS
          }
        }

        return guard
      }

      test(PART_ONE) {
        val guard = doGuardWalk(startingAt, room)

        // reuse for part 2
        guardPath = guard.visited

        guard.visited.size shouldBe 4826
      }

      test(PART_TWO) {
        val solution2 =
          guardPath
            .asSequence() // memory
            .filter { point -> point != startingAt }
            .map { point -> doGuardWalk(startingAt, room.withExtraObstacleAt(point)) }
            .count { guard -> guard.state == GuardState.TRAPPED_IN_CYCLE }

        solution2 shouldBe 1721
      }
    },
  )
