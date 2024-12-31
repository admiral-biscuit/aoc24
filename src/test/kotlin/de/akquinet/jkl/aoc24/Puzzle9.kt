package de.akquinet.jkl.aoc24

import de.akquinet.jkl.aoc24.utils.whileTrue
import io.kotest.matchers.shouldBe

private fun <T> T.repeat(repetitions: Int): List<T> =
  generateSequence { this }.take(repetitions).toList()

private sealed interface DiskEntry {
  @JvmInline value class Number(val value: Int) : DiskEntry

  data object Empty : DiskEntry
}

private fun String.toDiskEntries(): List<DiskEntry> =
  toString().flatMapIndexed { index, char ->
    val digit = char.toString().toInt()
    if (index % 2 == 0) {
        DiskEntry.Number(index / 2)
      } else {
        DiskEntry.Empty
      }
      .repeat(digit)
  }

private data class Disk(val entries: MutableList<DiskEntry>) {
  val doNotMove = mutableSetOf<DiskEntry.Number>()

  fun checksum(): Long =
    entries
      .mapIndexed { index, entry ->
        if (entry is DiskEntry.Number) {
          index * entry.value.toLong()
        } else {
          0
        }
      }
      .sum()

  // puzzle 1
  fun moveLastEntryForward(): Boolean {
    val lastNumberIndex = entries.indexOfLast { it is DiskEntry.Number }
    val entry = entries[lastNumberIndex]

    val firstEmptyIndex = entries.take(lastNumberIndex).indexOfFirst { it is DiskEntry.Empty }

    if (firstEmptyIndex == -1) return false

    entries[lastNumberIndex] = DiskEntry.Empty
    entries[firstEmptyIndex] = entry

    return true
  }

  // puzzle 2
  fun getLastNumberBlock(): Pair<DiskEntry.Number, List<Int>>? {
    val lastNumber =
      entries.lastOrNull { entry -> entry is DiskEntry.Number && entry !in doNotMove }
    return lastNumber?.let {
      it as DiskEntry.Number to entries.indices.filter { index -> entries[index] == lastNumber }
    }
  }

  fun findFirstEmptyBlock(size: Int, before: Int): List<Int>? {
    for (i in 0..minOf(entries.size, before) - size) {
      if ((0..<size).all { entries[i + it] == DiskEntry.Empty }) {
        return (i..<i + size).toList()
      }
    }
    return null
  }

  fun tryMoveLastBlockForward(): Boolean {
    val lastNumberBlock = getLastNumberBlock() ?: return false

    val (lastNumber, lastNumberRange) = lastNumberBlock

    val firstEmptyBlock = findFirstEmptyBlock(lastNumberRange.size, lastNumberRange.first())

    if (firstEmptyBlock == null) {
      doNotMove.add(lastNumber)
    } else {
      lastNumberRange.forEachIndexed { i, j ->
        entries[j] = DiskEntry.Empty
        entries[firstEmptyBlock[i]] = lastNumber
      }
    }

    return true
  }

  // debug
  override fun toString(): String =
    entries.joinToString("") { entry ->
      when (entry) {
        is DiskEntry.Empty -> "."
        is DiskEntry.Number -> entry.value.toString()
      }
    }
}

class Puzzle9 :
  PuzzleSpec(
    9,
    {
      val diskEntries = readInputAsText().toDiskEntries()

      test("part one") {
        val disk = Disk(diskEntries.toMutableList())
        val solution1 = disk.apply { whileTrue { moveLastEntryForward() } }.checksum()

        solution1 shouldBe 6201130364722
      }

      test("part two") {
        val disk = Disk(diskEntries.toMutableList())
        val solution2 = disk.apply { whileTrue { tryMoveLastBlockForward() } }.checksum()

        solution2 shouldBe 6221662795602
      }
    },
  )
