package de.akquinet.jkl.aoc24.utils

fun <A, B> combineAll(someList: Iterable<A>, otherList: Iterable<B>): List<Pair<A, B>> =
  someList.flatMap { a -> otherList.map { b -> a to b } }

fun <A> List<A>.allPairs(): List<Pair<A, A>> =
  flatMapIndexed { i, first -> mapIndexed { j, second -> if (i < j) first to second else null } }
    .filterNotNull()

fun <T> List<T>.countEachElement(): Map<T, Long> =
  groupingBy { it }.eachCount().mapValues { (_, count) -> count.toLong() }

infix fun <T> Map<T, Int>.mergeCounts(other: Map<T, Int>): Map<T, Int> {
  val result = this.toMutableMap()

  other.forEach { (key, value) -> result[key] = result.getOrDefault(key, 0) + value }

  return result.toMap()
}
