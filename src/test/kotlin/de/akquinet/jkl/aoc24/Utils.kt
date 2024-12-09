package de.akquinet.jkl.aoc24

@Suppress("ControlFlowWithEmptyBody")
fun whileTrue(action: () -> Boolean) {
  while (action.invoke()) {}
}

typealias IntPair = Pair<Int, Int>

fun <A, B> combineAll(someList: Iterable<A>, otherList: Iterable<B>): List<Pair<A, B>> =
  someList.flatMap { a -> otherList.map { b -> a to b } }

fun <A> List<A>.allPairs(): List<Pair<A, A>> =
  flatMapIndexed { i, first -> mapIndexed { j, second -> if (i < j) first to second else null } }
    .filterNotNull()

data class Point(val x: Int, val y: Int) {
  infix fun plus(other: Point): Point = Point(x + other.x, y + other.y)

  infix fun minus(other: Point): Point = Point(x - other.x, y - other.y)

  fun scaleBy(t: Int): Point = Point(t * x, t * y)
}

data class Dimension(val width: Int, val height: Int) {
  infix fun containsPoint(point: Point): Boolean = point.x in 0..<width && point.y in 0..<height
}
