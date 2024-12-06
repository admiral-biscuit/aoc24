package de.akquinet.jkl.aoc24

typealias IntPair = Pair<Int, Int>

fun <A, B> combineAll(someList: Iterable<A>, otherList: Iterable<B>): List<Pair<A, B>> =
  someList.flatMap { a -> otherList.map { b -> a to b } }
