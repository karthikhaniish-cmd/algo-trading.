package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.model.BacktestConfig
import com.example.ui.components.EquityCurveChart
import com.example.ui.components.PnlText
import com.example.ui.components.StatCard
import com.example.ui.theme.AmberWarning
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
fun BacktestingScreen(
    state: TradingUiState,
    onRunBacktest: () -> Unit,
    onUpdateConfig: (BacktestConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val config = state.backtestConfig

    var capitalText by remember(config) { mutableStateOf(config.initialCapital.toString()) }
    var feeText by remember(config) { mutableStateOf(config.feePercent.toString()) }
    var slippageText by remember(config) { mutableStateOf(config.slippagePercent.toString()) }
    var stopLossText by remember(config) { mutableStateOf(config.stopLossPct?.toString() ?: "3.0") }
    var takeProfitText by remember(config) { mutableStateOf(config.takeProfitPct?.toString() ?: "6.0") }

    val backtest = state.latestBacktestResult

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "STRATEGY BACKTESTING ENGINE",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = CyanAccent
        )
        Text(
            text = "Simulates historical performance with next-bar execution, realistic transaction fees, and slippage.",
            fontSize = 12.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Simulation Configuration Form
        Card(
            colors = CardDefaults.cardColors(containerColor = NavySurface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, NavyBorder, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "SIMULATION PARAMETERS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanAccent,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = capitalText,
                        onValueChange = { capitalText = it },
                        label = { Text("Starting Balance ($)", fontSize = 11.sp) },
                        colors = textFieldColors(),
                        modifier = Modifier.weight(1.2f)
                    )
                    OutlinedTextField(
                        value = feeText,
                        onValueChange = { feeText = it },
                        label = { Text("Fee (%)", fontSize = 11.sp) },
                        colors = textFieldColors(),
                        modifier = Modifier.weight(0.9f)
                    )
                    OutlinedTextField(
                        value = slippageText,
                        onValueChange = { slippageText = it },
                        label = { Text("Slippage (%)", fontSize = 11.sp) },
                        colors = textFieldColors(),
                        modifier = Modifier.weight(0.9f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = stopLossText,
                        onValueChange = { stopLossText = it },
                        label = { Text("Stop Loss (%)", fontSize = 11.sp) },
                        colors = textFieldColors(),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = takeProfitText,
                        onValueChange = { takeProfitText = it },
                        label = { Text("Take Profit (%)", fontSize = 11.sp) },
                        colors = textFieldColors(),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val cap = capitalText.toDoubleOrNull() ?: 10000.0
                        val fee = feeText.toDoubleOrNull() ?: 0.1
                        val slip = slippageText.toDoubleOrNull() ?: 0.05
                        val sl = stopLossText.toDoubleOrNull()
                        val tp = takeProfitText.toDoubleOrNull()
                        onUpdateConfig(
                            config.copy(
                                initialCapital = cap,
                                feePercent = fee,
                                slippagePercent = slip,
                                stopLossPct = sl,
                                takeProfitPct = tp
                            )
                        )
                        onRunBacktest()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color(0xFF070C18)),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !state.isBacktesting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("run_backtest_button")
                ) {
                    if (state.isBacktesting) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color(0xFF070C18))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Simulating Bars...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Execute Backtest Simulation", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Results Section
        if (backtest != null) {
            val m = backtest.metrics

            Text(
                text = "PERFORMANCE SCORECARD (${backtest.strategyName} on ${backtest.symbol})",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = CyanAccent,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Scorecard grid
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard(
                    title = "Strategy Return",
                    value = String.format(Locale.US, "%s%.2f%%", if (m.totalReturnPct >= 0) "+" else "", m.totalReturnPct),
                    subValue = String.format(Locale.US, "Final: $%.2f", m.finalCapital),
                    isPositive = m.totalReturnPct >= 0,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Buy & Hold Benchmark",
                    value = String.format(Locale.US, "%s%.2f%%", if (m.buyAndHoldReturnPct >= 0) "+" else "", m.buyAndHoldReturnPct),
                    subValue = "100% Equity Buy & Hold",
                    isPositive = m.buyAndHoldReturnPct >= 0,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard(
                    title = "Win Rate",
                    value = String.format(Locale.US, "%.1f%%", m.winRatePct),
                    subValue = "${m.winningTrades} wins / ${m.losingTrades} losses",
                    isPositive = m.winRatePct >= 50.0,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Profit Factor",
                    value = String.format(Locale.US, "%.2f", m.profitFactor),
                    subValue = "${m.totalTrades} total trades",
                    isPositive = m.profitFactor >= 1.0,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard(
                    title = "Max Drawdown",
                    value = String.format(Locale.US, "-%.2f%%", m.maxDrawdownPct),
                    subValue = "Peak to trough drop",
                    isPositive = m.maxDrawdownPct < 15.0,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Sharpe Ratio",
                    value = String.format(Locale.US, "%.2f", m.sharpeRatio),
                    subValue = "Annualized (252 bars)",
                    isPositive = m.sharpeRatio >= 1.0,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Equity Curve Chart
            EquityCurveChart(equityPoints = backtest.equityCurve)

            Spacer(modifier = Modifier.height(16.dp))

            // Trade Log Table
            Text(
                text = "EXECUTION TRADE LOG (${backtest.trades.size} TRADES)",
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
                    if (backtest.trades.isEmpty()) {
                        Text(
                            text = "No trades executed during this backtest window.",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    } else {
                        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
                            Text("Entry", modifier = Modifier.weight(1f), fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                            Text("Exit", modifier = Modifier.weight(1f), fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                            Text("P&L ($)", modifier = Modifier.weight(1.2f), fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                            Text("Reason", modifier = Modifier.weight(1.2f), fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                        }
                        backtest.trades.takeLast(10).reversed().forEach { trade ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(trade.entryDate, fontSize = 10.sp, color = TextPrimary)
                                    Text(String.format(Locale.US, "$%.2f", trade.entryPrice), fontSize = 9.sp, color = TextSecondary)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(trade.exitDate, fontSize = 10.sp, color = TextPrimary)
                                    Text(String.format(Locale.US, "$%.2f", trade.exitPrice), fontSize = 9.sp, color = TextSecondary)
                                }
                                Column(modifier = Modifier.weight(1.2f)) {
                                    PnlText(amount = trade.pnlDollar, percent = trade.pnlPercent, fontSize = 11)
                                }
                                Text(
                                    text = trade.exitReason,
                                    fontSize = 10.sp,
                                    color = TextSecondary,
                                    modifier = Modifier.weight(1.2f),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Regulatory / Academic Disclaimer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0x18FFD600))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = AmberWarning,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "DISCLOSURE: Backtested historical performance does not guarantee future results. Simulated trading includes assumptions regarding liquidity and model execution.",
                    fontSize = 10.sp,
                    color = AmberWarning,
                    lineHeight = 14.sp
                )
            }
        }
    }
}

@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = CyanAccent,
    unfocusedBorderColor = NavyBorder,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedContainerColor = NavySurface,
    unfocusedContainerColor = NavySurface
)
