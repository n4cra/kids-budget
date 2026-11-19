package com.example.kidsbudget.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.kidsbudget.data.BalancePoint
import com.example.kidsbudget.data.Dates
import com.example.kidsbudget.data.Money
import kotlin.math.max

@Composable
fun BalanceChart(
  points: List<BalancePoint>,
  modifier: Modifier = Modifier,
) {
  if (points.isEmpty()) {
    Box(
      modifier = modifier.fillMaxWidth().height(180.dp),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = "Balance over time will appear after the first transaction.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
    return
  }

  val lineColor = MaterialTheme.colorScheme.primary
  val gridColor = MaterialTheme.colorScheme.outlineVariant
  val zeroColor = MaterialTheme.colorScheme.outline
  val fillColor = lineColor.copy(alpha = 0.18f)
  val onSurface = MaterialTheme.colorScheme.onSurfaceVariant
  val minY = minOf(0L, points.minOf { it.balanceCents })
  val maxY = maxOf(0L, points.maxOf { it.balanceCents })
  val yRange = max(1L, maxY - minY)
  val startLabel = Dates.formatShort(points.first().timestamp)
  val endLabel = Dates.formatShort(points.last().timestamp)
  val topLabel = Money.format(maxY)
  val bottomLabel = Money.format(minY)
  val description =
    "Balance went from ${Money.format(points.first().balanceCents)} to ${Money.format(points.last().balanceCents)}"

  Column(modifier = modifier.semantics { contentDescription = description }) {
    Row(modifier = Modifier.fillMaxWidth().height(200.dp)) {
      Column(
        modifier = Modifier.padding(end = 8.dp),
        verticalArrangement = Arrangement.SpaceBetween,
      ) {
        Text(text = topLabel, style = MaterialTheme.typography.labelSmall, color = onSurface)
        Spacer(Modifier.weight(1f))
        Text(text = bottomLabel, style = MaterialTheme.typography.labelSmall, color = onSurface)
      }
      Canvas(modifier = Modifier.weight(1f).fillMaxSize()) {
        val minX = points.first().timestamp.toFloat()
        val maxX = points.last().timestamp.toFloat()
        val xRange = max(1f, maxX - minX)
        val sameTime = maxX == minX

        fun xFor(index: Int, timestamp: Long): Float =
          when {
            points.size == 1 -> size.width
            sameTime -> size.width * index / (points.size - 1).toFloat()
            else -> ((timestamp - minX) / xRange) * size.width
          }

        fun yFor(cents: Long): Float {
          val t = (cents - minY).toFloat() / yRange.toFloat()
          return size.height - (t * size.height)
        }

        val dash = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
        drawLine(gridColor, Offset(0f, 0f), Offset(size.width, 0f), strokeWidth = 1f, pathEffect = dash)
        drawLine(
          gridColor,
          Offset(0f, size.height),
          Offset(size.width, size.height),
          strokeWidth = 1f,
          pathEffect = dash,
        )
        if (minY < 0 && maxY > 0) {
          val zeroY = yFor(0)
          drawLine(zeroColor, Offset(0f, zeroY), Offset(size.width, zeroY), strokeWidth = 1.5f)
        }

        val linePath = Path()
        val fillPath = Path()
        val mapped =
          points.mapIndexed { index, point -> Offset(xFor(index, point.timestamp), yFor(point.balanceCents)) }
        val first = mapped.first()
        val startX = if (points.size == 1) 0f else first.x
        val zeroY = yFor(0).coerceIn(0f, size.height)
        linePath.moveTo(startX, first.y)
        fillPath.moveTo(startX, zeroY)
        fillPath.lineTo(startX, first.y)
        if (startX != first.x) {
          linePath.lineTo(first.x, first.y)
          fillPath.lineTo(first.x, first.y)
        }
        mapped.drop(1).forEach { point ->
          linePath.lineTo(point.x, point.y)
          fillPath.lineTo(point.x, point.y)
        }
        fillPath.lineTo(mapped.last().x, zeroY)
        fillPath.close()

        drawPath(fillPath, fillColor)
        drawPath(linePath, lineColor, style = Stroke(width = 6f, cap = StrokeCap.Round))
        mapped.forEach { point -> drawCircle(lineColor, radius = 7f, center = point) }
      }
    }
    Row(
      modifier = Modifier.fillMaxWidth().padding(start = 72.dp, top = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Text(text = startLabel, style = MaterialTheme.typography.labelSmall, color = onSurface)
      Text(text = endLabel, style = MaterialTheme.typography.labelSmall, color = onSurface)
    }
  }
}
