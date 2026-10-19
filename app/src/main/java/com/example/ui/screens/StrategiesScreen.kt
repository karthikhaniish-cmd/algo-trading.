package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SignalType
import com.example.model.StrategyConfig
import com.example.model.StrategyType
import com.example.ui.components.SignalBadge
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.RubyRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.TradingUiState
import java.util.Locale

@Composable
fun StrategiesScreen(
    state: TradingUiState,
    onUpdateStrategy: (StrategyConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val currentConfig = state.strategyConfig

    var selectedType by remember(currentConfig) { mutableStateOf(currentConfig.strategyType) }
    var fastPeriod by remember(currentConfig) { mutableFloatStateOf(currentConfig.fastPeriod.toFloat()) }
    var slowPeriod by remember(currentConfig) { mutableFloatStateOf(currentConfig.slowPeriod.toFloat()) }
    var rsiPeriod by remember(currentConfig) { mutableFloatStateOf(currentConfig.rsiPeriod.toFloat()) }
    var rsiOversold by remember(currentConfig) { mutableFloatStateOf(currentConfig.rsiOversold.toFloat()) }
    var rsiOverbought by remember(currentConfig) { mutableFloatStateOf(currentConfig.rsiOverbought.toFloat()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "ALGORITHMIC TRADING STRATEGIES",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = CyanAccent
        )
        Text(
            text = "Select an indicator model, adjust lookback parameters, and review mathematical signal generation rules.",
            fontSize = 12.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Strategy Selector Chips
        Text(
            text = "ACTIVE STRATEGY",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(6.dp))

        StrategyType.values().forEach { type ->
            val isSelected = selectedType == type
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) NavySurfaceVariant else NavySurface
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .border(
                        1.dp,
                        if (isSelected) CyanAccent else NavyBorder,
                        RoundedCornerShape(10.dp)
                    )
                    .clickable { selectedType = type }
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = type.displayName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) CyanAccent else TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = type.shortDesc, fontSize = 11.sp, color = TextSecondary)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Parameter Tuning Card
        Card(
            colors = CardDefaults.cardColors(containerColor = NavySurface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, NavyBorder, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Tune, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "PARAMETER CONFIGURATION",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanAccent,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                when (selectedType) {
                    StrategyType.SMA_CROSSOVER, StrategyType.EMA_CROSSOVER -> {
                        Text(
                            text = "Fast Period: ${fastPeriod.toInt()} bars",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Slider(
                            value = fastPeriod,
                            onValueChange = { fastPeriod = it },
                            valueRange = 3f..50f,
                            steps = 46,
                            colors = sliderColors()
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Slow Period: ${slowPeriod.toInt()} bars",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Slider(
                            value = slowPeriod,
                            onValueChange = { slowPeriod = it },
                            valueRange = 10f..200f,
                            steps = 189,
                            colors = sliderColors()
                        )
                    }
                    StrategyType.RSI_MEAN_REVERSION -> {
                        Text(
                            text = "RSI Calculation Period: ${rsiPeriod.toInt()} bars",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Slider(
                            value = rsiPeriod,
                            onValueChange = { rsiPeriod = it },
                            valueRange = 5f..30f,
                            steps = 24,
                            colors = sliderColors()
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Oversold Threshold (Buy): ${rsiOversold.toInt()}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = EmeraldGreen
                        )
                        Slider(
                            value = rsiOversold,
                            onValueChange = { rsiOversold = it },
                            valueRange = 15f..45f,
                            steps = 29,
                            colors = sliderColors()
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Overbought Threshold (Sell): ${rsiOverbought.toInt()}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = RubyRed
                        )
                        Slider(
                            value = rsiOverbought,
                            onValueChange = { rsiOverbought = it },
                            valueRange = 55f..85f,
                            steps = 29,
                            colors = sliderColors()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        onUpdateStrategy(
                            StrategyConfig(
                                strategyType = selectedType,
                                fastPeriod = fastPeriod.toInt(),
                                slowPeriod = slowPeriod.toInt(),
                                rsiPeriod = rsiPeriod.toInt(),
                                rsiOversold = rsiOversold.toDouble(),
                                rsiOverbought = rsiOverbought.toDouble()
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color(0xFF070C18)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("apply_strategy_button")
                ) {
                    Text("Apply & Re-Calculate Signals", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Signal History Log on Active Dataset
        val signals = state.indicatorSeries?.signals ?: emptyList()
        Text(
            text = "RECENT GENERATED SIGNALS (${signals.size} bars processed)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(NavySurface)
                .border(1.dp, NavyBorder, RoundedCornerShape(8.dp))
                .padding(10.dp)
        ) {
            Column {
                val nonHoldSignals = signals.filter { it.signal != SignalType.HOLD }.takeLast(6)
                if (nonHoldSignals.isEmpty()) {
                    Text(
                        text = "No BUY or SELL crossover events yet in the latest window. All bars currently HOLD.",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                } else {
                    nonHoldSignals.reversed().forEach { sig ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${sig.date}  •  Price: $${String.format(Locale.US, "%.2f", sig.price)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = sig.reason,
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    maxLines = 2
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            SignalBadge(signal = sig.signal)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun sliderColors() = SliderDefaults.colors(
    thumbColor = CyanAccent,
    activeTrackColor = CyanAccent,
    inactiveTrackColor = NavySurfaceVariant
)
