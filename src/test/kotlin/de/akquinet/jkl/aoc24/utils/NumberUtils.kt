package de.akquinet.jkl.aoc24.utils

infix fun Long.pow(exponent: Long): Long {
  var result = 1L
  (1..exponent).forEach { _ -> result *= this }
  return result
}
