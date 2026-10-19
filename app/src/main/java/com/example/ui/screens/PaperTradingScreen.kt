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
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import com.example.model.OrderSide
import com.example.model.OrderStatus
import com.example.ui.components.PaperTradingBanner
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PaperTradingScreen(
    state: TradingUiState,
    onExecuteOrder: (symbol: String, side: OrderSide, quantity: Double, price: Double) -> Unit,
    onStepSimulation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val currentSymbol = state.selectedDataset?.symbol ?: "AAPL"
    val currentPrice = state.bars.lastOrNull()?.close ?: 150.0

    var orderSide by remember { mutableStateOf(OrderSide.BUY) }
    var quantityText by remember { mutableStateOf("10") }

    val positionsValue = state.paperPositions.sumOf { it.quantity * it.currentPrice }
    val totalEquity = state.paperAccount.cash + positionsValue
    val totalRealized = state.paperAccount.realizedPnl
    val totalUnrealized = state.paperPositions.sumOf { it.unrealizedPnl }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        PaperTradingBanner(modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "PAPER TRADING SIMULATOR",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = CyanAccent
        )
        Text(
            text = "Practice execution with simulated virtual capital. Never connects to a live brokerage.",
            fontSize = 12.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Virtual Portfolio Summary Cards
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard(
                title = "Virtual Cash",
                value = String.format(Locale.US, "$%.2f", state.paperAccount.cash),
                subValue = "Available to trade",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Net Portfolio Value",
                value = String.format(Locale.US, "$%.2f", totalEquity),
                subValue = String.format(Locale.US, "Positions: $%.2f", positionsValue),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard(
                title = "Realized P&L",
                value = String.format(Locale.US, "%s$%.2f", if (totalRealized >= 0) "+" else "", totalRealized),
                isPositive = totalRealized >= 0,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Unrealized P&L",
                value = String.format(Locale.US, "%s$%.2f", if (totalUnrealized >= 0) "+" else "", totalUnrealized),
                isPositive = totalUnrealized >= 0,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Auto Strategy Step Button
        Card(
            colors = CardDefaults.cardColors(containerColor = NavySurface),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, NavyBorder, RoundedCornerShape(10.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "AUTOMATED STRATEGY EXECUTION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanAccent
                    )
                    Text(
                        text = "Execute paper order for active ${state.strategyConfig.strategyType.displayName} signal on $currentSymbol.",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onStepSimulation,
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color(0xFF070C18)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("step_paper_trade_button")
                ) {
                    Icon(imageVector = Icons.Default.FastForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Auto Step", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Manual Order Execution Form
        Card(
            colors = CardDefaults.cardColors(containerColor = NavySurface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, NavyBorder, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "SIMULATED ORDER PLACEMENT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanAccent,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Side Toggle Buttons
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { orderSide = OrderSide.BUY },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (orderSide == OrderSide.BUY) EmeraldGreen else NavySurfaceVariant,
                            contentColor = if (orderSide == OrderSide.BUY) Color(0xFF070C18) else TextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("BUY (LONG)", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { orderSide = OrderSide.SELL },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (orderSide == OrderSide.SELL) RubyRed else NavySurfaceVariant,
                            contentColor = if (orderSide == OrderSide.SELL) TextPrimary else TextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("SELL (EXIT)", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = currentSymbol,
                        onValueChange = {},
                        enabled = false,
                        label = { Text("Symbol", fontSize = 11.sp) },
                        colors = textFieldColors(),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = String.format(Locale.US, "%.2f", currentPrice),
                        onValueChange = {},
                        enabled = false,
                        label = { Text("Market Price ($)", fontSize = 11.sp) },
                        colors = textFieldColors(),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { quantityText = it },
                        label = { Text("Quantity (Shares)", fontSize = 11.sp) },
                        colors = textFieldColors(),
                        modifier = Modifier.weight(1f)
                    )
                }

                val enteredQty = quantityText.toDoubleOrNull() ?: 0.0
                val totalOrderEst = enteredQty * currentPrice

                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = "Estimated Order Total:",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = String.format(Locale.US, "$%.2f", totalOrderEst),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        if (enteredQty > 0.0) {
                            onExecuteOrder(currentSymbol, orderSide, enteredQty, currentPrice)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (orderSide == OrderSide.BUY) EmeraldGreen else RubyRed,
                        contentColor = Color(0xFF070C18)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("submit_order_button")
                ) {
                    Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Submit Simulated $orderSide Order", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Open Positions Table
        Text(
            text = "OPEN VIRTUAL POSITIONS (${state.paperPositions.size})",
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
                if (state.paperPositions.isEmpty()) {
                    Text("No active positions held. Portfolio is 100% virtual cash.", color = TextMuted, fontSize = 11.sp)
                } else {
                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
                        Text("Symbol", modifier = Modifier.weight(1f), fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                        Text("Shares", modifier = Modifier.weight(1f), fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                        Text("Avg Cost", modifier = Modifier.weight(1f), fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                        Text("Market Val", modifier = Modifier.weight(1.2f), fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                        Text("Unrealized P&L", modifier = Modifier.weight(1.4f), fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                    }
                    state.paperPositions.forEach { pos ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(pos.symbol, modifier = Modifier.weight(1f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                            Text(String.format(Locale.US, "%.1f", pos.quantity), modifier = Modifier.weight(1f), fontSize = 11.sp, color = TextPrimary)
                            Text(String.format(Locale.US, "$%.2f", pos.averagePrice), modifier = Modifier.weight(1f), fontSize = 11.sp, color = TextSecondary)
                            Text(String.format(Locale.US, "$%.2f", pos.marketValue), modifier = Modifier.weight(1.2f), fontSize = 11.sp, color = TextPrimary)
                            Column(modifier = Modifier.weight(1.4f)) {
                                PnlText(amount = pos.unrealizedPnl, percent = pos.unrealizedPnlPct, fontSize = 11)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Order Book / Recent Paper Orders Table
        Text(
            text = "RECENT SIMULATED ORDERS (${state.paperOrders.size})",
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
                if (state.paperOrders.isEmpty()) {
                    Text("No simulated orders logged yet.", color = TextMuted, fontSize = 11.sp)
                } else {
                    val dateFormat = SimpleDateFormat("MM/dd HH:mm", Locale.US)
                    state.paperOrders.take(8).forEach { ord ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val isBuy = ord.side == OrderSide.BUY
                                    Text(
                                        text = "${ord.side} ${ord.symbol}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isBuy) EmeraldGreen else RubyRed
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${String.format(Locale.US, "%.1f", ord.quantity)} @ $${String.format(Locale.US, "%.2f", ord.price)}",
                                        fontSize = 11.sp,
                                        color = TextPrimary
                                    )
                                }
                                Text(
                                    text = "${dateFormat.format(Date(ord.timestamp))} • ${ord.strategy} • ${ord.executionNote}",
                                    fontSize = 10.sp,
                                    color = TextMuted,
                                    maxLines = 1
                                )
                            }
                            if (ord.realizedPnl != 0.0) {
                                PnlText(amount = ord.realizedPnl, fontSize = 11)
                            }
                        }
                    }
                }
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
