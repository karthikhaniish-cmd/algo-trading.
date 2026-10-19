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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import com.example.model.SignalType
import com.example.ui.components.DatasetTypeBadge
import com.example.ui.components.FinancialCandlestickChart
import com.example.ui.components.PaperTradingBanner
import com.example.ui.components.PnlText
import com.example.ui.components.SignalBadge
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
fun DashboardScreen(
    state: TradingUiState,
    onSelectDataset: (String) -> Unit,
    onNavigateToBacktest: () -> Unit,
    onNavigateToPaperTrading: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showRsi by remember { mutableStateOf(false) }
    var dropdownExpanded by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    val currentDataset = state.selectedDataset
    val latestBar = state.bars.lastOrNull()
    val latestSignal = state.latestSignal

    val positionsValue = state.paperPositions.sumOf { it.quantity * it.currentPrice }
    val totalEquity = state.paperAccount.cash + positionsValue
    val totalPnl = totalEquity - state.paperAccount.startingCash

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Paper Trading Banner
        PaperTradingBanner(modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(14.dp))

        // Asset Selector & Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { dropdownExpanded = true }
                        .padding(vertical = 4.dp, horizontal = 6.dp)
                ) {
                    Text(
                        text = currentDataset?.symbol ?: "SELECT SYMBOL",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CyanAccent
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Select Symbol",
                        tint = CyanAccent,
                        modifier = Modifier.size(28.dp)
                    )
                }

                DropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false },
                    modifier = Modifier.background(NavySurface)
                ) {
                    state.datasets.forEach { ds ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text("${ds.symbol} - ${ds.name}", color = TextPrimary, fontWeight = FontWeight.Bold)
                                    Text(ds.type.name, color = TextMuted, fontSize = 11.sp)
                                }
                            },
                            onClick = {
                                onSelectDataset(ds.id)
                                dropdownExpanded = false
                            }
                        )
                    }
                }

                Text(
                    text = currentDataset?.name ?: "No dataset selected",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            if (currentDataset != null) {
                DatasetTypeBadge(type = currentDataset.type)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Latest Price & Daily Change Row
        if (latestBar != null) {
            val change = latestBar.close - latestBar.open
            val changePct = if (latestBar.open > 0) (change / latestBar.open) * 100.0 else 0.0
            val isBullish = change >= 0

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = String.format(Locale.US, "$%.2f", latestBar.close),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PnlText(amount = change, percent = changePct, fontSize = 14)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "As of ${latestBar.date}",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }

                if (latestSignal != null) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "STRATEGY SIGNAL",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        SignalBadge(signal = latestSignal.signal)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Chart Sub-controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${state.strategyConfig.strategyType.displayName} Chart",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )

            Row {
                OutlinedButton(
                    onClick = { showRsi = !showRsi },
                    modifier = Modifier.testTag("toggle_rsi_button")
                ) {
                    Text(
                        text = if (showRsi) "Hide RSI Pane" else "Show RSI Pane",
                        fontSize = 11.sp,
                        color = if (showRsi) CyanAccent else TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Interactive Candlestick Chart
        FinancialCandlestickChart(
            bars = state.bars,
            fastOverlay = state.indicatorSeries?.fastValues ?: emptyList(),
            slowOverlay = state.indicatorSeries?.slowValues ?: emptyList(),
            rsiOverlay = state.indicatorSeries?.rsiValues ?: emptyList(),
            showRsiPane = showRsi
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Strategy Explainability Card
        if (latestSignal != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = NavySurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NavyBorder, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "ALGORITHMIC SIGNAL ANALYSIS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanAccent,
                            letterSpacing = 0.5.sp
                        )
                        SignalBadge(signal = latestSignal.signal)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = latestSignal.reason,
                        fontSize = 13.sp,
                        color = TextPrimary,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Rule: Zero look-ahead bias enforced. Orders trigger on next bar's open price.",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Portfolio Summary Cards
        Text(
            text = "VIRTUAL PORTFOLIO SUMMARY",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard(
                title = "Total Net Worth",
                value = String.format(Locale.US, "$%.2f", totalEquity),
                subValue = String.format(Locale.US, "Cash: $%.2f", state.paperAccount.cash),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Total P&L",
                value = String.format(Locale.US, "%s$%.2f", if (totalPnl >= 0) "+" else "", totalPnl),
                subValue = String.format(Locale.US, "%s%.2f%% Return", if (totalPnl >= 0) "+" else "", (totalPnl / state.paperAccount.startingCash) * 100.0),
                isPositive = totalPnl >= 0,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Navigation Buttons
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = onNavigateToBacktest,
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color(0xFF070C18)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("nav_backtest_btn")
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Run Backtest", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onNavigateToPaperTrading,
                colors = ButtonDefaults.buttonColors(containerColor = NavySurfaceVariant, contentColor = TextPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("nav_paper_trade_btn")
            ) {
                Icon(imageVector = Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Paper Trading", fontWeight = FontWeight.Bold)
            }
        }
    }
}
