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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PriceBar
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanGlow
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
fun FinancialCandlestickChart(
    bars: List<PriceBar>,
    fastOverlay: List<Double?> = emptyList(),
    slowOverlay: List<Double?> = emptyList(),
    rsiOverlay: List<Double?> = emptyList(),
    showRsiPane: Boolean = false,
    modifier: Modifier = Modifier
) {
    if (bars.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(260.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(NavySurface)
                .border(1.dp, NavyBorder, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("No market data available for chart.", color = TextMuted, fontSize = 13.sp)
        }
        return
    }

    var selectedIndex by remember(bars) { mutableStateOf<Int?>(null) }
    val displayIndex = selectedIndex ?: (bars.size - 1)
    val activeBar = bars.getOrNull(displayIndex) ?: bars.last()
    val activeFast = fastOverlay.getOrNull(displayIndex)
    val activeSlow = slowOverlay.getOrNull(displayIndex)
    val activeRsi = rsiOverlay.getOrNull(displayIndex)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(NavySurface)
            .border(1.dp, NavyBorder, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        // Scrubbing Header / Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "${activeBar.date}  •  O: $${activeBar.open}  H: $${activeBar.high}  L: $${activeBar.low}  C: $${activeBar.close}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Row(modifier = Modifier.padding(top = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                    val change = activeBar.close - activeBar.open
                    val changePct = if (activeBar.open > 0) (change / activeBar.open) * 100.0 else 0.0
                    val changeColor = if (change >= 0) EmeraldGreen else RubyRed
                    val changeSign = if (change >= 0) "+" else ""

                    Text(
                        text = String.format(Locale.US, "%s$%.2f (%s%.2f%%)", changeSign, change, changeSign, changePct),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = changeColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Vol: ${formatVolume(activeBar.volume)}",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
            }

            // Indicator legends
            Column(horizontalAlignment = Alignment.End) {
                if (activeFast != null) {
                    Text(
                        text = String.format(Locale.US, "Fast MA: %.2f", activeFast),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = CyanAccent
                    )
                }
                if (activeSlow != null) {
                    Text(
                        text = String.format(Locale.US, "Slow MA: %.2f", activeSlow),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = AmberWarning
                    )
                }
                if (activeRsi != null && showRsiPane) {
                    val rsiColor = when {
                        activeRsi <= 30.0 -> EmeraldGreen
                        activeRsi >= 70.0 -> RubyRed
                        else -> TextSecondary
                    }
                    Text(
                        text = String.format(Locale.US, "RSI: %.1f", activeRsi),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = rsiColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Canvas Chart
        val chartHeight = if (showRsiPane) 240.dp else 190.dp

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight)
                .clip(RoundedCornerShape(8.dp))
                .background(NavySurfaceVariant.copy(alpha = 0.5f))
                .pointerInput(bars) {
                    detectDragGestures(
                        onDrag = { change, _ ->
                            val width = size.width.toFloat()
                            val barWidth = width / bars.size
                            val idx = (change.position.x / barWidth).toInt().coerceIn(0, bars.size - 1)
                            selectedIndex = idx
                        },
                        onDragEnd = { selectedIndex = null },
                        onDragCancel = { selectedIndex = null }
                    )
                }
                .pointerInput(bars) {
                    detectTapGestures(
                        onTap = { offset ->
                            val width = size.width.toFloat()
                            val barWidth = width / bars.size
                            val idx = (offset.x / barWidth).toInt().coerceIn(0, bars.size - 1)
                            selectedIndex = idx
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val totalWidth = size.width
                val totalHeight = size.height

                val rsiPaneHeight = if (showRsiPane) totalHeight * 0.28f else 0f
                val pricePaneHeight = totalHeight - rsiPaneHeight - (if (showRsiPane) 12f else 0f)
                val volumePaneHeight = pricePaneHeight * 0.22f
                val candlePaneHeight = pricePaneHeight - volumePaneHeight

                val count = bars.size
                if (count == 0) return@Canvas

                val barSlotWidth = totalWidth / count
                val candleWidth = max(2f, barSlotWidth * 0.65f)

                // Determine min/max price
                var minPrice = bars.minOf { it.low }
                var maxPrice = bars.maxOf { it.high }
                if (fastOverlay.isNotEmpty()) {
                    fastOverlay.filterNotNull().forEach {
                        minPrice = min(minPrice, it)
                        maxPrice = max(maxPrice, it)
                    }
                }
                if (slowOverlay.isNotEmpty()) {
                    slowOverlay.filterNotNull().forEach {
                        minPrice = min(minPrice, it)
                        maxPrice = max(maxPrice, it)
                    }
                }
                val pricePadding = (maxPrice - minPrice) * 0.08
                val plotMinPrice = minPrice - pricePadding
                val plotMaxPrice = maxPrice + pricePadding
                val priceRange = max(0.001, plotMaxPrice - plotMinPrice)

                val maxVolume = max(1L, bars.maxOf { it.volume })

                fun priceToY(price: Double): Float {
                    val normalized = (price - plotMinPrice) / priceRange
                    return (candlePaneHeight * (1.0 - normalized)).toFloat()
                }

                // Draw horizontal price grid lines (3 lines)
                for (step in 1..3) {
                    val y = candlePaneHeight * (step / 4f)
                    drawLine(
                        color = Color(0x18FFFFFF),
                        start = Offset(0f, y),
                        end = Offset(totalWidth, y),
                        strokeWidth = 1f
                    )
                }

                // Draw Candlesticks & Volumes
                for (i in 0 until count) {
                    val bar = bars[i]
                    val xCenter = (i + 0.5f) * barSlotWidth
                    val isBull = bar.close >= bar.open
                    val barColor = if (isBull) EmeraldGreen else RubyRed

                    // High-Low Wick
                    val yHigh = priceToY(bar.high)
                    val yLow = priceToY(bar.low)
                    drawLine(
                        color = barColor,
                        start = Offset(xCenter, yHigh),
                        end = Offset(xCenter, yLow),
                        strokeWidth = max(1f, candleWidth * 0.2f)
                    )

                    // Open-Close Body
                    val yOpen = priceToY(bar.open)
                    val yClose = priceToY(bar.close)
                    val topY = min(yOpen, yClose)
                    val bodyHeight = max(2f, kotlin.math.abs(yClose - yOpen))

                    drawRect(
                        color = barColor,
                        topLeft = Offset(xCenter - (candleWidth / 2f), topY),
                        size = Size(candleWidth, bodyHeight)
                    )

                    // Volume Bar (at bottom of price pane)
                    val volHeight = (bar.volume.toFloat() / maxVolume) * volumePaneHeight
                    val volTop = pricePaneHeight - volHeight
                    drawRect(
                        color = barColor.copy(alpha = 0.35f),
                        topLeft = Offset(xCenter - (candleWidth / 2f), volTop),
                        size = Size(candleWidth, volHeight)
                    )
                }

                // Draw Fast MA Line (Cyan)
                if (fastOverlay.size == count) {
                    val fastPath = Path()
                    var started = false
                    for (i in 0 until count) {
                        val v = fastOverlay[i]
                        if (v != null) {
                            val x = (i + 0.5f) * barSlotWidth
                            val y = priceToY(v)
                            if (!started) {
                                fastPath.moveTo(x, y)
                                started = true
                            } else {
                                fastPath.lineTo(x, y)
                            }
                        }
                    }
                    if (started) {
                        drawPath(fastPath, color = CyanAccent, style = Stroke(width = 2.2f))
                    }
                }

                // Draw Slow MA Line (Amber)
                if (slowOverlay.size == count) {
                    val slowPath = Path()
                    var started = false
                    for (i in 0 until count) {
                        val v = slowOverlay[i]
                        if (v != null) {
                            val x = (i + 0.5f) * barSlotWidth
                            val y = priceToY(v)
                            if (!started) {
                                slowPath.moveTo(x, y)
                                started = true
                            } else {
                                slowPath.lineTo(x, y)
                            }
                        }
                    }
                    if (started) {
                        drawPath(slowPath, color = AmberWarning, style = Stroke(width = 2.2f))
                    }
                }

                // Draw RSI Sub-pane if requested
                if (showRsiPane && rsiOverlay.size == count) {
                    val rsiTop = totalHeight - rsiPaneHeight

                    // RSI background divider
                    drawLine(
                        color = NavyBorder,
                        start = Offset(0f, rsiTop),
                        end = Offset(totalWidth, rsiTop),
                        strokeWidth = 1.5f
                    )

                    // Overbought (70) and Oversold (30) reference lines
                    val y70 = rsiTop + (rsiPaneHeight * (1f - 0.7f))
                    val y30 = rsiTop + (rsiPaneHeight * (1f - 0.3f))
                    val y50 = rsiTop + (rsiPaneHeight * 0.5f)

                    drawLine(color = RubyRed.copy(alpha = 0.4f), start = Offset(0f, y70), end = Offset(totalWidth, y70), strokeWidth = 1f)
                    drawLine(color = Color(0x22FFFFFF), start = Offset(0f, y50), end = Offset(totalWidth, y50), strokeWidth = 1f)
                    drawLine(color = EmeraldGreen.copy(alpha = 0.4f), start = Offset(0f, y30), end = Offset(totalWidth, y30), strokeWidth = 1f)

                    val rsiPath = Path()
                    var rsiStarted = false
                    for (i in 0 until count) {
                        val rsiVal = rsiOverlay[i]
                        if (rsiVal != null) {
                            val x = (i + 0.5f) * barSlotWidth
                            val clampedRsi = rsiVal.coerceIn(0.0, 100.0).toFloat()
                            val y = rsiTop + (rsiPaneHeight * (1f - (clampedRsi / 100f)))
                            if (!rsiStarted) {
                                rsiPath.moveTo(x, y)
                                rsiStarted = true
                            } else {
                                rsiPath.lineTo(x, y)
                            }
                        }
                    }
                    if (rsiStarted) {
                        drawPath(rsiPath, color = CyanGlow, style = Stroke(width = 2f))
                    }
                }

                // Draw Crosshair line if scrubbing
                if (selectedIndex != null) {
                    val scrubIdx = selectedIndex!!
                    val scrubX = (scrubIdx + 0.5f) * barSlotWidth
                    drawLine(
                        color = Color(0xAAFFFFFF),
                        start = Offset(scrubX, 0f),
                        end = Offset(scrubX, totalHeight),
                        strokeWidth = 1.2f
                    )
                }
            }
        }
    }
}

private fun formatVolume(volume: Long): String {
    return when {
        volume >= 1_000_000_000 -> String.format(Locale.US, "%.1fB", volume / 1_000_000_000.0)
        volume >= 1_000_000 -> String.format(Locale.US, "%.1fM", volume / 1_000_000.0)
        volume >= 1_000 -> String.format(Locale.US, "%.1fK", volume / 1_000.0)
        else -> volume.toString()
    }
}
