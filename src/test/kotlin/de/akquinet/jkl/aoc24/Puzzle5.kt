package de.akquinet.jkl.aoc24

import io.kotest.matchers.shouldBe

private data class PageOrderingRule(val first: Int, val second: Int)

private data class PageUpdate(val pages: List<Int>) {
  fun isCorrect(pageOrderingRules: List<PageOrderingRule>): Boolean {
    val zippedSubLists =
      (0..<pages.size - 1).flatMap { pages.takeLast(pages.size - it).zipWithNext() }

    return zippedSubLists
      .map { PageOrderingRule(it.first, it.second) }
      .all { it in pageOrderingRules }
  }

  fun getMiddleNumber(): Int = pages[pages.size / 2]

  fun sortBy(pageOrderingRules: List<PageOrderingRule>): PageUpdate {
    val sorted =
      pages.sortedWith { page1, page2 ->
        when {
          PageOrderingRule(page2, page1) in pageOrderingRules -> 1
          PageOrderingRule(page1, page2) in pageOrderingRules -> -1
          else -> 0
        }
      }

    return PageUpdate(sorted)
  }
}

class Puzzle5 :
  PuzzleSpec(
    5,
    {
      val (lines1, lines2) = readInputAsText().split("\n\n").map { it.split("\n") }

      val pageOrderingRules =
        lines1.map { line ->
          val (a, b) = line.split("|")
          PageOrderingRule(a.toInt(), b.toInt())
        }

      val pageUpdates =
        lines2.map { line -> PageUpdate(line.split(",").toList().map { it.toInt() }) }

      test(PART_ONE) {
        val solution1 =
          pageUpdates.filter { it.isCorrect(pageOrderingRules) }.sumOf { it.getMiddleNumber() }

        solution1 shouldBe 5948
      }

      test(PART_TWO) {
        val solution2 =
          pageUpdates
            .filter { !it.isCorrect(pageOrderingRules) }
            .sumOf { it.sortBy(pageOrderingRules).getMiddleNumber() }

        solution2 shouldBe 3062
      }
    },
  )
