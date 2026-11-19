package com.example.kidsbudget.data

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

object Money {
  fun parseToCents(input: String): Long? {
    val cleaned =
      input
        .trim()
        .replace(Regex("[^0-9,.-]"), "")
        .let { value ->
          when {
            value.count { it == '.' } == 1 && value.count { it == ',' } >= 1 ->
              value.replace(",", "")
            value.count { it == ',' } == 1 && !value.contains('.') -> value.replace(',', '.')
            else -> value.replace(",", "")
          }
        }
    if (cleaned.isEmpty() || cleaned == "-" || cleaned == ".") return null
    val decimal = cleaned.toBigDecimalOrNull() ?: return null
    return decimal.movePointRight(2).setScale(0, RoundingMode.HALF_UP).toLong()
  }

  fun format(cents: Long, locale: Locale = Locale.getDefault()): String {
    val amount = BigDecimal(cents).movePointLeft(2)
    return currencyFormat(locale).format(amount)
  }

  fun formatSigned(cents: Long, locale: Locale = Locale.getDefault()): String {
    val formatted = format(kotlin.math.abs(cents), locale)
    return when {
      cents > 0 -> "+$formatted"
      cents < 0 -> "-$formatted"
      else -> formatted
    }
  }

  private fun currencyFormat(locale: Locale): NumberFormat =
    NumberFormat.getCurrencyInstance(locale).apply {
      minimumFractionDigits = 2
      maximumFractionDigits = 2
    }
}
