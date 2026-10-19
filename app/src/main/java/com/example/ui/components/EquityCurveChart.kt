package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EquityPoint
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.RubyRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

@Composable
fun EquityCurveChart(
    equityPoints: List<EquityPoint>,
    modifier: Modifier = Modifier
) {
    if (equityPoints.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(NavySurface)
                .border(1.dp, NavyBorder, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("No backtest equity data yet. Run a backtest to view performance.", color = TextMuted, fontSize = 12.sp)
        }
        return
    }

    var selectedIndex by remember(equityPoints) { mutableStateOf<Int?>(null) }
    val displayIndex = selectedIndex ?: (equityPoints.size - 1)
    val activePoint = equityPoints.getOrNull(displayIndex) ?: equityPoints.last()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(NavySurface)
            .border(1.dp, NavyBorder, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        // Legend & Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "EQUITY CURVE & BENCHMARK",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "${activePoint.date} • Strategy: $${String.format(Locale.US, "%.2f", activePoint.equity)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanAccent
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "B&H: $${String.format(Locale.US, "%.2f", activePoint.benchmarkEquity)}",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                Text(
                    text = "Drawdown: -${String.format(Locale.US, "%.2f", activePoint.drawdownPct)}%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (activePoint.drawdownPct > 5.0) RubyRed else TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(NavySurfaceVariant.copy(alpha = 0.4f))
                .pointerInput(equityPoints) {
                    detectDragGestures(
                        onDrag = { change, _ ->
                            val width = size.width.toFloat()
                            val slotWidth = width / equityPoints.size
                            val idx = (change.position.x / slotWidth).toInt().coerceIn(0, equityPoints.size - 1)
                            selectedIndex = idx
                        },
                        onDragEnd = { selectedIndex = null },
                        onDragCancel = { selectedIndex = null }
                    )
                }
                .pointerInput(equityPoints) {
                    detectTapGestures(
                        onTap = { offset ->
                            val width = size.width.toFloat()
                            val slotWidth = width / equityPoints.size
                            val idx = (offset.x / slotWidth).toInt().coerceIn(0, equityPoints.size - 1)
                            selectedIndex = idx
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val totalWidth = size.width
                val totalHeight = size.height

                val count = equityPoints.size
                if (count < 2) return@Canvas

                val minVal = min(
                    equityPoints.minOf { it.equity },
                    equityPoints.minOf { it.benchmarkEquity }
                )
                val maxVal = max(
                    equityPoints.maxOf { it.equity },
                    equityPoints.maxOf { it.benchmarkEquity }
                )

                val padding = max(1.0, (maxVal - minVal) * 0.08)
                val plotMin = minVal - padding
                val plotMax = maxVal + padding
                val range = max(0.001, plotMax - plotMin)

                fun toY(value: Double): Float {
                    val normalized = (value - plotMin) / range
                    return (totalHeight * (1.0 - normalized)).toFloat()
                }

                // Grid lines
                for (s in 1..3) {
                    val y = totalHeight * (s / 4f)
                    drawLine(
                        color = Color(0x15FFFFFF),
                        start = Offset(0f, y),
                        end = Offset(totalWidth, y),
                        strokeWidth = 1f
                    )
                }

                // Benchmark Line (Dotted / Dashed appearance or subtle secondary)
                val benchPath = Path()
                for (i in 0 until count) {
                    val x = (i.toFloat() / (count - 1)) * totalWidth
                    val y = toY(equityPoints[i].benchmarkEquity)
                    if (i == 0) benchPath.moveTo(x, y) else benchPath.lineTo(x, y)
                }
                drawPath(benchPath, color = TextSecondary.copy(alpha = 0.6f), style = Stroke(width = 1.8f))

                // Strategy Equity Line (Cyan with subtle gradient fill)
                val stratPath = Path()
                val fillPath = Path()
                for (i in 0 until count) {
                    val x = (i.toFloat() / (count - 1)) * totalWidth
                    val y = toY(equityPoints[i].equity)
                    if (i == 0) {
                        stratPath.moveTo(x, y)
                        fillPath.moveTo(x, totalHeight)
                        fillPath.lineTo(x, y)
                    } else {
                        stratPath.lineTo(x, y)
                        fillPath.lineTo(x, y)
                    }
                }
                fillPath.lineTo(totalWidth, totalHeight)
                fillPath.close()

                drawPath(fillPath, color = CyanAccent.copy(alpha = 0.08f))
                drawPath(stratPath, color = CyanAccent, style = Stroke(width = 2.6f))

                // Scrub line
                if (selectedIndex != null) {
                    val scrubX = (selectedIndex!!.toFloat() / (count - 1)) * totalWidth
                    drawLine(
                        color = Color(0xCCFFFFFF),
                        start = Offset(scrubX, 0f),
                        end = Offset(scrubX, totalHeight),
                        strokeWidth = 1.2f
                    )
                }
            }
        }
    }
}
