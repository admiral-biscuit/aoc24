package de.akquinet.jkl.aoc24

import io.kotest.matchers.shouldBe

private data class Computer(val id: String) {
  private val connections = mutableSetOf<Computer>()

  fun getConnections(): Set<Computer> = connections.toSet()

  fun connectWith(other: Computer) {
    connections.add(other)
    other.connections.add(this)
  }

  fun idStartsWithT(): Boolean = id.startsWith("t")
}

private data class ComputerPool(val computers: List<Computer>) {
  fun getComputerById(id: String): Computer = computers.single { it.id == id }

  // shamelessly copied from Wikipedia
  fun bronKerbosch(
    all: Set<Computer> = emptySet(),
    some: Set<Computer> = computers.toSet(),
    none: Set<Computer> = emptySet(),
  ): Set<Computer> =
    if (some.isEmpty() && none.isEmpty()) {
      all
    } else {
      val withMostNeighbors = (some + none).maxBy { computer -> computer.getConnections().size }
      some
        .minus(withMostNeighbors.getConnections())
        .map { computer ->
          val connections = computer.getConnections()
          bronKerbosch(all + computer, some intersect connections, none intersect connections)
        }
        .maxBy { connections -> connections.size }
    }
}

private fun Set<Computer>.password(): String =
  map { computer -> computer.id }.sorted().joinToString(",")

class Puzzle23 :
  PuzzleSpec(
    23,
    {
      val idPairs = readInputAsLines().map { line -> line.split("-") }

      val pool = idPairs.flatten().toSet().map { id -> Computer(id) }.let { ComputerPool(it) }

      val computerPairs =
        idPairs.map { pair ->
          val computer1 = pool.getComputerById(pair[0])
          val computer2 = pool.getComputerById(pair[1])
          computer1 to computer2
        }

      computerPairs.forEach { (computer1, computer2) -> computer1.connectWith(computer2) }

      test("part one") {
        val solution1 =
          computerPairs
            .filter { (computer1, computer2) ->
              computer1.idStartsWithT() || computer2.idStartsWithT()
            }
            .map { (computer1, computer2) ->
              val connections1 = computer1.getConnections()
              val connections2 = computer2.getConnections()
              (computer1 to computer2) to (connections1 intersect connections2)
            }
            .filter { (_, intersection) -> intersection.isNotEmpty() }
            .flatMap { (pair, intersection) ->
              val (computer1, computer2) = pair
              intersection.map { setOf(computer1, computer2, it) }
            }
            .toSet()
            .size

        solution1 shouldBe 1200
      }

      test("part two") {
        val solution2 = pool.bronKerbosch().password()
        solution2 shouldBe "ag,gh,hh,iv,jx,nq,oc,qm,rb,sm,vm,wu,zr"
      }
    },
  )
