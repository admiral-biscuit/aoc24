package de.akquinet.jkl.aoc24

import arrow.core.filterIsInstance
import de.akquinet.jkl.aoc24.utils.pow
import io.kotest.matchers.shouldBe

private sealed interface LazyWire {
  data class Evaluated(val value: Boolean) : LazyWire

  data class Waiting(
    val input1: String,
    val input2: String,
    val targetId: String,
    val operation: (Boolean, Boolean) -> Boolean,
  ) : LazyWire
}

@JvmInline
private value class Wires(private val lazyWires: MutableMap<String, LazyWire>) {
  fun put(id: String, lazyWire: LazyWire) {
    lazyWires[id] = lazyWire
  }

  fun evaluate(id: String): Boolean =
    when (val lazyWire = lazyWires.getValue(id)) {
      is LazyWire.Evaluated -> lazyWire.value
      is LazyWire.Waiting -> {
        val value1 = evaluate(lazyWire.input1)
        val value2 = evaluate(lazyWire.input2)
        lazyWire.operation(value1, value2).also { lazyWires[id] = LazyWire.Evaluated(it) }
      }
    }

  fun evaluateAll() {
    lazyWires.forEach { (id, _) -> evaluate(id) }
  }

  fun zValuesToInt(): Long =
    lazyWires
      .filterIsInstance<String, LazyWire.Evaluated>()
      .filter { (id, _) -> id.startsWith("z") }
      .toSortedMap()
      .map { (_, lazyWire) -> lazyWire.value.toInt() }
      .mapIndexed { i, value -> value * 2L.pow(i.toLong()) }
      .sum()
}

private fun Boolean.Companion.fromBinaryString(s: String): Boolean =
  when (s) {
    "1" -> true
    "0" -> false
    else -> throw IllegalArgumentException("expected 0 or 1 but got $s")
  }

private fun String.toBooleanOperation(): (Boolean, Boolean) -> Boolean =
  when (this) {
    "AND" -> { a, b -> a && b }
    "OR" -> { a, b -> a || b }
    "XOR" -> { a, b -> a xor b }
    else -> throw IllegalArgumentException("unexpected operation $this")
  }

private fun Boolean.toInt(): Int = if (this) 1 else 0

class Puzzle24 :
  PuzzleSpec(
    24,
    {
      val (block1, block2) = readInputAsText().split("\n\n").map { block -> block.split("\n") }

      val wires = Wires(mutableMapOf())

      block1.forEach { line ->
        val (id, value) = line.split(": ")
        wires.put(id, LazyWire.Evaluated(Boolean.fromBinaryString(value)))
      }

      block2.forEach { line ->
        val (id1, opString, id2, _, targetId) = line.split(" ")
        val operation = opString.toBooleanOperation()
        wires.put(targetId, LazyWire.Waiting(id1, id2, targetId, operation))
      }

      wires.evaluateAll()

      test("part one") {
        val solution1 = wires.zValuesToInt()
        solution1 shouldBe 51107420031718L
      }
    },
  )
