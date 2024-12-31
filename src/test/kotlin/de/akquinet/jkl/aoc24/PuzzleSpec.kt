package de.akquinet.jkl.aoc24

import io.kotest.core.spec.style.FunSpec
import java.io.File

@Suppress("PropertyName")
abstract class PuzzleSpec(number: Int, body: PuzzleSpec.() -> Unit) : FunSpec() {
  private val inputFile = File(javaClass.getResource("/$number.txt")!!.toURI())

  fun readInputAsText(): String = inputFile.readText()

  fun readInputAsLines(): List<String> = inputFile.readLines()

  init {
    body()
  }
}
