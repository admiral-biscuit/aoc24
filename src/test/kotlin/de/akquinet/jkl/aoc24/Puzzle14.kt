package de.akquinet.jkl.aoc24

import arrow.fx.coroutines.parMap
import de.akquinet.jkl.aoc24.utils.Point
import de.akquinet.jkl.aoc24.utils.combineAll
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers

private data class Robot(val position: Point, val velocity: Point) {
  fun move(t: Int = 1): Robot = copy(position = position plus velocity.scaleBy(t))

  fun modPosition(width: Int, height: Int): Robot {
    val xMod = position.x.mod(width)
    val yMod = position.y.mod(height)
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
        val interestingIndex =
          (0..<10304)
            .parMap(Dispatchers.IO) { t ->
              val robots =
                initialRobots.map { robot -> robot.move(t).modPosition(mapWidth, mapHeight) }
              val render = robots.render(mapWidth, mapHeight)
              render.mightBeInteresting()
            }
            .indexOfFirst { it }

        interestingIndex shouldBe 8087
      }

      // The number of all possible map states is 101 * 103 = 10403.
      // This follows from Lagrange's theorem.
      test("all possible map states") {
        val mapStates = mutableListOf<String>()
        var robots = initialRobots

        while (true) {
          val render = robots.render(mapWidth, mapHeight)

          if (render in mapStates) break
          mapStates.add(render)

          robots = robots.map { robot -> robot.move().modPosition(mapWidth, mapHeight) }
        }

        mapStates.size shouldBe 10403
      }
    },
  )
