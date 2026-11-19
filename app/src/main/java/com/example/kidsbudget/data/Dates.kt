package com.example.kidsbudget.data

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

object Dates {
  private val mediumFormatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
  private val shortFormatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)

  fun todayMillis(zoneId: ZoneId = ZoneId.systemDefault()): Long = localDateToMillis(LocalDate.now(zoneId), zoneId)

  fun localDateToMillis(date: LocalDate, zoneId: ZoneId = ZoneId.systemDefault()): Long =
    date.atStartOfDay(zoneId).toInstant().toEpochMilli()

  fun millisToLocalDate(millis: Long, zoneId: ZoneId = ZoneId.systemDefault()): LocalDate =
    Instant.ofEpochMilli(millis).atZone(zoneId).toLocalDate()

  fun utcMillisToLocalDate(millis: Long): LocalDate =
    Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()

  fun localDateToUtcMillis(date: LocalDate): Long =
    date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

  fun formatMedium(millis: Long, zoneId: ZoneId = ZoneId.systemDefault()): String =
    mediumFormatter.format(millisToLocalDate(millis, zoneId))

  fun formatShort(millis: Long, zoneId: ZoneId = ZoneId.systemDefault()): String =
    shortFormatter.format(millisToLocalDate(millis, zoneId))
}
