package de.akquinet.jkl.aoc24.utils

@Suppress("ControlFlowWithEmptyBody")
fun whileTrue(action: () -> Boolean) {
  while (action.invoke()) {}
}

fun <T> T.applyRepeatedly(times: Int, f: (T) -> T): T {
  var result = this
  repeat(times) { result = f(result) }
  return result
}
