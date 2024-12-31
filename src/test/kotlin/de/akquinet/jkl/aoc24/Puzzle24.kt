package de.akquinet.jkl.aoc24

import arrow.core.filterIsInstance
import de.akquinet.jkl.aoc24.utils.pow
import io.kotest.matchers.shouldBe

private val INT_REGEX = Regex("\\d+")

private fun String.findFirstInt(): Int? = INT_REGEX.find(this)?.value?.toInt()

private fun Int.twoDigits(): String = toString().padStart(2, '0')

private data class WireData(
  val id1: String,
  val id2: String,
  val targetId: String,
  val opString: String,
) {
  companion object {
    fun fromLine(line: String): WireData {
      val (id1, opString, id2, _, targetId) = line.split(" ")
      return WireData(id1, id2, targetId, opString)
    }
  }

  override fun toString(): String = "$id1 $opString $id2 -> $targetId"

  fun sorted(): WireData {
    val (newId1, newId2) = listOf(id1, id2).sorted()
    return WireData(newId1, newId2, targetId, opString)
  }

  fun applyAliases(aliases: Map<String, String>): WireData {
    val newId1 = aliases[id1] ?: id1
    val newId2 = aliases[id2] ?: id2
    val newTargetId = aliases[targetId] ?: targetId
    return WireData(newId1, newId2, newTargetId, opString)
  }

  fun highestInt(): Int =
    listOf(id1, id2, targetId).mapNotNull { it.findFirstInt() }.maxOrNull() ?: -1
}

private sealed interface LazyWire {
  data class Evaluated(val value: Boolean) : LazyWire

  data class Waiting(val wireData: WireData) : LazyWire
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
        val value1 = evaluate(lazyWire.wireData.id1)
        val value2 = evaluate(lazyWire.wireData.id2)
        val operation = lazyWire.wireData.opString.toBooleanOperation()
        operation(value1, value2).also { lazyWires[id] = LazyWire.Evaluated(it) }
      }
    }

  fun evaluateAll() {
    lazyWires.forEach { (id, _) -> evaluate(id) }
  }

  fun zValuesToLong(): Long =
    lazyWires
      .filterIsInstance<String, LazyWire.Evaluated>()
      .filter { (id, _) -> id.startsWith("z") }
      .toSortedMap()
      .mapValues { (_, lazyWire) -> lazyWire.value }
      .map { (_, bool) -> bool.toInt() }
      .mapIndexed { i, value -> value.toLong() * 2L.pow(i.toLong()) }
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

private fun List<WireData>.generateAliases(
  generateAlias: (WireData) -> String?
): Map<String, String> {
  val aliases = mutableMapOf<String, String>()

  forEach { wireData ->
    val alias = generateAlias(wireData)
    if (alias != null) {
      aliases[wireData.targetId] = alias
    }
  }

  return aliases.toMap()
}

private fun List<WireData>.applyAliases(aliases: Map<String, String>): List<WireData> = map {
  it.applyAliases(aliases)
}

private fun List<WireData>.prettyPrint() =
  groupBy { it.highestInt() }
    .toSortedMap()
    .map { (_, wireDataList) ->
      wireDataList.map { it.sorted() }.sortedBy { it.toString() }.joinToString("\n")
    }
    .joinToString("\n\n")

private fun List<WireData>.swap(idA: String, idB: String): List<WireData> = map { wireData ->
  val newTargetId =
    when (wireData.targetId) {
      idA -> idB
      idB -> idA
      else -> wireData.targetId
    }

  wireData.copy(targetId = newTargetId)
}

class Puzzle24 :
  PuzzleSpec(
    24,
    {
      test("part one") {
        val (block1, block2) = readInputAsText().split("\n\n").map { block -> block.split("\n") }

        val wires = Wires(mutableMapOf())

        block1.forEach { line ->
          val (id, value) = line.split(": ")
          wires.put(id, LazyWire.Evaluated(Boolean.fromBinaryString(value)))
        }

        block2.forEach { line ->
          val data = WireData.fromLine(line)
          wires.put(data.targetId, LazyWire.Waiting(data))
        }

        wires.evaluateAll()

        val solution1 = wires.zValuesToLong()
        solution1 shouldBe 51107420031718L
      }

      test("part two") {
        // last swap: AND39 and XOR39 are switched
        val swaps = listOf("gpr" to "z10", "nks" to "z21", "ghp" to "z33", "cpm" to "krs")

        val solution2 = swaps.flatMap { pair -> pair.toList() }.sorted().joinToString(",")
        solution2 shouldBe "cpm,ghp,gpr,krs,nks,z10,z21,z33"

        // I "debugged" the puzzle visually -- how the output looks like
        var allWireData =
          readInputAsText().split("\n\n")[1].split("\n").map { line -> WireData.fromLine(line) }

        swaps.forEach { (idA, idB) -> allWireData = allWireData.swap(idA, idB) }

        val xorAndAliases =
          allWireData.generateAliases { wireData ->
            wireData.sorted().run {
              if (id1.startsWith("x") && id2.startsWith("y") && !targetId.startsWith("z")) {
                val number = INT_REGEX.find(wireData.id1)!!.value.toInt().twoDigits()
                "${wireData.opString}$number"
              } else {
                null
              }
            }
          }

        allWireData = allWireData.applyAliases(xorAndAliases)

        val aliasesA =
          allWireData.generateAliases { wireData ->
            wireData.sorted().run {
              if (id1.startsWith("AND") && opString == "OR" && !targetId.startsWith("z")) {
                val number = INT_REGEX.find(id1)!!.value.toInt().let { it + 1 }.twoDigits()
                "VA$number"
              } else {
                null
              }
            }
          }

        val aliasesB =
          allWireData.generateAliases { wireData ->
            wireData.sorted().run {
              if (id1.startsWith("XOR") && opString == "AND" && !targetId.startsWith("z")) {
                val number = INT_REGEX.find(id1)!!.value.toInt().twoDigits()
                "VB$number"
              } else {
                null
              }
            }
          }

        allWireData = allWireData.applyAliases(aliasesA).applyAliases(aliasesB)

        allWireData.prettyPrint() shouldBe
          """
          x00 AND y00 -> AND00
          x00 XOR y00 -> z00

          AND00 AND XOR01 -> hsv
          AND00 XOR XOR01 -> z01
          x01 AND y01 -> AND01
          x01 XOR y01 -> XOR01

          AND01 OR hsv -> VA02
          VA02 AND XOR02 -> VB02
          VA02 XOR XOR02 -> z02
          x02 AND y02 -> AND02
          x02 XOR y02 -> XOR02

          AND02 OR VB02 -> VA03
          VA03 AND XOR03 -> VB03
          VA03 XOR XOR03 -> z03
          x03 AND y03 -> AND03
          x03 XOR y03 -> XOR03

          AND03 OR VB03 -> VA04
          VA04 AND XOR04 -> VB04
          VA04 XOR XOR04 -> z04
          x04 AND y04 -> AND04
          x04 XOR y04 -> XOR04

          AND04 OR VB04 -> VA05
          VA05 AND XOR05 -> VB05
          VA05 XOR XOR05 -> z05
          x05 AND y05 -> AND05
          x05 XOR y05 -> XOR05

          AND05 OR VB05 -> VA06
          VA06 AND XOR06 -> VB06
          VA06 XOR XOR06 -> z06
          x06 AND y06 -> AND06
          x06 XOR y06 -> XOR06

          AND06 OR VB06 -> VA07
          VA07 AND XOR07 -> VB07
          VA07 XOR XOR07 -> z07
          x07 AND y07 -> AND07
          x07 XOR y07 -> XOR07

          AND07 OR VB07 -> VA08
          VA08 AND XOR08 -> VB08
          VA08 XOR XOR08 -> z08
          x08 AND y08 -> AND08
          x08 XOR y08 -> XOR08

          AND08 OR VB08 -> VA09
          VA09 AND XOR09 -> VB09
          VA09 XOR XOR09 -> z09
          x09 AND y09 -> AND09
          x09 XOR y09 -> XOR09

          AND09 OR VB09 -> VA10
          VA10 AND XOR10 -> VB10
          VA10 XOR XOR10 -> z10
          x10 AND y10 -> AND10
          x10 XOR y10 -> XOR10

          AND10 OR VB10 -> VA11
          VA11 AND XOR11 -> VB11
          VA11 XOR XOR11 -> z11
          x11 AND y11 -> AND11
          x11 XOR y11 -> XOR11

          AND11 OR VB11 -> VA12
          VA12 AND XOR12 -> VB12
          VA12 XOR XOR12 -> z12
          x12 AND y12 -> AND12
          x12 XOR y12 -> XOR12

          AND12 OR VB12 -> VA13
          VA13 AND XOR13 -> VB13
          VA13 XOR XOR13 -> z13
          x13 AND y13 -> AND13
          x13 XOR y13 -> XOR13

          AND13 OR VB13 -> VA14
          VA14 AND XOR14 -> VB14
          VA14 XOR XOR14 -> z14
          x14 AND y14 -> AND14
          x14 XOR y14 -> XOR14

          AND14 OR VB14 -> VA15
          VA15 AND XOR15 -> VB15
          VA15 XOR XOR15 -> z15
          x15 AND y15 -> AND15
          x15 XOR y15 -> XOR15

          AND15 OR VB15 -> VA16
          VA16 AND XOR16 -> VB16
          VA16 XOR XOR16 -> z16
          x16 AND y16 -> AND16
          x16 XOR y16 -> XOR16

          AND16 OR VB16 -> VA17
          VA17 AND XOR17 -> VB17
          VA17 XOR XOR17 -> z17
          x17 AND y17 -> AND17
          x17 XOR y17 -> XOR17

          AND17 OR VB17 -> VA18
          VA18 AND XOR18 -> VB18
          VA18 XOR XOR18 -> z18
          x18 AND y18 -> AND18
          x18 XOR y18 -> XOR18

          AND18 OR VB18 -> VA19
          VA19 AND XOR19 -> VB19
          VA19 XOR XOR19 -> z19
          x19 AND y19 -> AND19
          x19 XOR y19 -> XOR19

          AND19 OR VB19 -> VA20
          VA20 AND XOR20 -> VB20
          VA20 XOR XOR20 -> z20
          x20 AND y20 -> AND20
          x20 XOR y20 -> XOR20

          AND20 OR VB20 -> VA21
          VA21 AND XOR21 -> VB21
          VA21 XOR XOR21 -> z21
          x21 AND y21 -> AND21
          x21 XOR y21 -> XOR21

          AND21 OR VB21 -> VA22
          VA22 AND XOR22 -> VB22
          VA22 XOR XOR22 -> z22
          x22 AND y22 -> AND22
          x22 XOR y22 -> XOR22

          AND22 OR VB22 -> VA23
          VA23 AND XOR23 -> VB23
          VA23 XOR XOR23 -> z23
          x23 AND y23 -> AND23
          x23 XOR y23 -> XOR23

          AND23 OR VB23 -> VA24
          VA24 AND XOR24 -> VB24
          VA24 XOR XOR24 -> z24
          x24 AND y24 -> AND24
          x24 XOR y24 -> XOR24

          AND24 OR VB24 -> VA25
          VA25 AND XOR25 -> VB25
          VA25 XOR XOR25 -> z25
          x25 AND y25 -> AND25
          x25 XOR y25 -> XOR25

          AND25 OR VB25 -> VA26
          VA26 AND XOR26 -> VB26
          VA26 XOR XOR26 -> z26
          x26 AND y26 -> AND26
          x26 XOR y26 -> XOR26

          AND26 OR VB26 -> VA27
          VA27 AND XOR27 -> VB27
          VA27 XOR XOR27 -> z27
          x27 AND y27 -> AND27
          x27 XOR y27 -> XOR27

          AND27 OR VB27 -> VA28
          VA28 AND XOR28 -> VB28
          VA28 XOR XOR28 -> z28
          x28 AND y28 -> AND28
          x28 XOR y28 -> XOR28

          AND28 OR VB28 -> VA29
          VA29 AND XOR29 -> VB29
          VA29 XOR XOR29 -> z29
          x29 AND y29 -> AND29
          x29 XOR y29 -> XOR29

          AND29 OR VB29 -> VA30
          VA30 AND XOR30 -> VB30
          VA30 XOR XOR30 -> z30
          x30 AND y30 -> AND30
          x30 XOR y30 -> XOR30

          AND30 OR VB30 -> VA31
          VA31 AND XOR31 -> VB31
          VA31 XOR XOR31 -> z31
          x31 AND y31 -> AND31
          x31 XOR y31 -> XOR31

          AND31 OR VB31 -> VA32
          VA32 AND XOR32 -> VB32
          VA32 XOR XOR32 -> z32
          x32 AND y32 -> AND32
          x32 XOR y32 -> XOR32

          AND32 OR VB32 -> VA33
          VA33 AND XOR33 -> VB33
          VA33 XOR XOR33 -> z33
          x33 AND y33 -> AND33
          x33 XOR y33 -> XOR33

          AND33 OR VB33 -> VA34
          VA34 AND XOR34 -> VB34
          VA34 XOR XOR34 -> z34
          x34 AND y34 -> AND34
          x34 XOR y34 -> XOR34

          AND34 OR VB34 -> VA35
          VA35 AND XOR35 -> VB35
          VA35 XOR XOR35 -> z35
          x35 AND y35 -> AND35
          x35 XOR y35 -> XOR35

          AND35 OR VB35 -> VA36
          VA36 AND XOR36 -> VB36
          VA36 XOR XOR36 -> z36
          x36 AND y36 -> AND36
          x36 XOR y36 -> XOR36

          AND36 OR VB36 -> VA37
          VA37 AND XOR37 -> VB37
          VA37 XOR XOR37 -> z37
          x37 AND y37 -> AND37
          x37 XOR y37 -> XOR37

          AND37 OR VB37 -> VA38
          VA38 AND XOR38 -> VB38
          VA38 XOR XOR38 -> z38
          x38 AND y38 -> AND38
          x38 XOR y38 -> XOR38

          AND38 OR VB38 -> VA39
          VA39 AND XOR39 -> VB39
          VA39 XOR XOR39 -> z39
          x39 AND y39 -> AND39
          x39 XOR y39 -> XOR39

          AND39 OR VB39 -> VA40
          VA40 AND XOR40 -> VB40
          VA40 XOR XOR40 -> z40
          x40 AND y40 -> AND40
          x40 XOR y40 -> XOR40

          AND40 OR VB40 -> VA41
          VA41 AND XOR41 -> VB41
          VA41 XOR XOR41 -> z41
          x41 AND y41 -> AND41
          x41 XOR y41 -> XOR41

          AND41 OR VB41 -> VA42
          VA42 AND XOR42 -> VB42
          VA42 XOR XOR42 -> z42
          x42 AND y42 -> AND42
          x42 XOR y42 -> XOR42

          AND42 OR VB42 -> VA43
          VA43 AND XOR43 -> VB43
          VA43 XOR XOR43 -> z43
          x43 AND y43 -> AND43
          x43 XOR y43 -> XOR43

          AND43 OR VB43 -> VA44
          VA44 AND XOR44 -> VB44
          VA44 XOR XOR44 -> z44
          x44 AND y44 -> AND44
          x44 XOR y44 -> XOR44

          AND44 OR VB44 -> z45
        """
            .trimIndent()
      }
    },
  )
