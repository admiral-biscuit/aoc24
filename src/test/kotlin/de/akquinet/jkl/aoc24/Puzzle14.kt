package de.akquinet.jkl.aoc24

import de.akquinet.jkl.aoc24.utils.Point
import de.akquinet.jkl.aoc24.utils.combineAll
import io.kotest.matchers.shouldBe

infix fun Int.positiveMod(other: Int): Int {
  val mod = this % other
  return if (mod < 0) {
    mod + other
  } else {
    mod
  }
}

private data class Robot(val position: Point, val velocity: Point) {
  fun move(t: Int = 1): Robot = copy(position = position plus velocity.scaleBy(t))

  fun modPosition(width: Int, height: Int): Robot {
    val xMod = position.x positiveMod width
    val yMod = position.y positiveMod height
    return copy(position = Point(xMod, yMod))
  }
}

private fun List<Robot>.byQuadrants(width: Int, height: Int): List<List<Robot>> =
  listOf(
      { p: Point -> p.x < width / 2 && p.y < height / 2 },
      { p: Point -> p.x < width / 2 && p.y > height / 2 },
      { p: Point -> p.x > width / 2 && p.y < height / 2 },
      { p: Point -> p.x > width / 2 && p.y > height / 2 },
    )
    .map { condition -> this.filter { robot -> condition(robot.position) } }

// find the pattern for part two
private fun List<Robot>.render(width: Int, height: Int): String {
  val positions = this.map { robot -> robot.modPosition(width, height).position }
  return combineAll(0..<height, 0..<width)
    .map { (y, x) ->
      if (Point(x, y) in positions) {
        "R"
      } else {
        "."
      }
    }
    .chunked(width)
    .joinToString("\n") { it.joinToString("") }
}

// purely heuristic
private fun String.mightBeInteresting(): Boolean =
  split("\n").count { line -> line.contains("RRRRR") } > 3

class Puzzle14 :
  PuzzleSpec(
    14,
    {
      val pvRegex = Regex("p=(\\d+),(\\d+) v=(-?\\d+),(-?\\d+)")

      val initialRobots =
        readInputAsLines().map { line ->
          val (p1, p2, v1, v2) = pvRegex.find(line)!!.destructured
          Robot(Point(p1.toInt(), p2.toInt()), Point(v1.toInt(), v2.toInt()))
        }

      val mapWidth = 101
      val mapHeight = 103

      test(PART_ONE) {
        val solution1 =
          initialRobots
            .map { robot -> robot.move(100).modPosition(mapWidth, mapHeight) }
            .byQuadrants(mapWidth, mapHeight)
            .map { it.count() }
            .reduce { a, b -> a * b }

        solution1 shouldBe 230172768
      }

      test(PART_TWO) {
        val interestingIndex: Int
        var index = 0
        var robots = initialRobots

        while (true) {
          val render = robots.render(mapWidth, mapHeight)

          if (render.mightBeInteresting()) {
            interestingIndex = index
            break
          }

          index += 1
          robots = robots.map { robot -> robot.move().modPosition(mapWidth, mapHeight) }
        }

        interestingIndex shouldBe 8087
      }
    },
  )
