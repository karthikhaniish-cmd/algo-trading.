package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.OrderSide
import com.example.ui.components.PnlText
import com.example.ui.components.StatCard
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavySurface
import com.example.ui.theme.RubyRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.TradingUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReportsScreen(
    state: TradingUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var searchQuery by remember { mutableStateOf("") }
    var sideFilter by remember { mutableStateOf<OrderSide?>(null) }

    val filteredOrders = state.paperOrders.filter { ord ->
        val matchesSearch = ord.symbol.contains(searchQuery, ignoreCase = true) ||
                ord.strategy.contains(searchQuery, ignoreCase = true)
        val matchesSide = sideFilter == null || ord.side == sideFilter
        matchesSearch && matchesSide
    }

    val totalVolumeTraded = state.paperOrders.sumOf { it.quantity * it.price }
    val totalRealized = state.paperAccount.realizedPnl
    val filledOrdersCount = state.paperOrders.count { it.status == com.example.model.OrderStatus.FILLED }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "REPORTS & TRADE HISTORY",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = CyanAccent
                )
                Text(
                    text = "Searchable records, strategy audit trail, and CSV export.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Aggregate Summary Cards
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard(
                title = "Total Orders",
                value = state.paperOrders.size.toString(),
                subValue = "$filledOrdersCount filled",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Volume Traded",
                value = String.format(Locale.US, "$%.2f", totalVolumeTraded),
                subValue = "Simulated flow",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Realized P&L",
                value = String.format(Locale.US, "%s$%.2f", if (totalRealized >= 0) "+" else "", totalRealized),
                isPositive = totalRealized >= 0,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // CSV Export Actions
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
                        text = "EXPORT TRADE AUDIT TO CSV",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanAccent
                    )
                    Text(
                        text = "Generates RFC 4180 compliant CSV of all simulated orders.",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Row {
                    Button(
                        onClick = {
                            val csvContent = buildOrdersCsv(state.paperOrders)
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/csv"
                                putExtra(Intent.EXTRA_SUBJECT, "Trade_History_Report.csv")
                                putExtra(Intent.EXTRA_TEXT, csvContent)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Trade History CSV"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color(0xFF070C18)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("share_csv_button")
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share CSV", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    OutlinedButton(
                        onClick = {
                            val csvContent = buildOrdersCsv(state.paperOrders)
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Trade History CSV", csvContent)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "CSV copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("copy_csv_button")
                    ) {
                        Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp), tint = CyanAccent)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy", fontSize = 11.sp, color = CyanAccent)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search & Filter Row
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by symbol or strategy...", fontSize = 11.sp) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                },
                colors = textFieldColors(),
                modifier = Modifier.weight(1.8f)
            )

            // Side Filter Toggles
            Row(modifier = Modifier.weight(1.2f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                FilterChip(label = "All", isSelected = sideFilter == null) { sideFilter = null }
                FilterChip(label = "Buy", isSelected = sideFilter == OrderSide.BUY) { sideFilter = OrderSide.BUY }
                FilterChip(label = "Sell", isSelected = sideFilter == OrderSide.SELL) { sideFilter = OrderSide.SELL }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Trade History Table
        Text(
            text = "MATCHING SIMULATED ORDERS (${filteredOrders.size})",
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
                if (filteredOrders.isEmpty()) {
                    Text("No orders match the selected search criteria.", color = TextMuted, fontSize = 11.sp)
                } else {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
                    filteredOrders.forEach { ord ->
                        val isBuy = ord.side == OrderSide.BUY
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1.5f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${ord.side} ${ord.symbol}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isBuy) EmeraldGreen else RubyRed
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${String.format(Locale.US, "%.1f", ord.quantity)} sh @ $${String.format(Locale.US, "%.2f", ord.price)}",
                                        fontSize = 11.sp,
                                        color = TextPrimary
                                    )
                                }
                                Text(
                                    text = "${dateFormat.format(Date(ord.timestamp))} • ${ord.strategy}",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                                if (ord.executionNote.isNotBlank()) {
                                    Text(
                                        text = ord.executionNote,
                                        fontSize = 10.sp,
                                        color = TextMuted,
                                        maxLines = 1
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = String.format(Locale.US, "Total: $%.2f", ord.quantity * ord.price),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
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
}

@Composable
private fun FilterChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) CyanAccent else NavySurface)
            .border(1.dp, if (isSelected) CyanAccent else NavyBorder, RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color(0xFF070C18) else TextSecondary
        )
    }
}

private fun buildOrdersCsv(orders: List<com.example.model.PaperOrder>): String {
    val sb = StringBuilder()
    sb.append("Order_ID,Timestamp,Date_Time,Symbol,Side,Quantity,Price,Total_Value,Strategy,Status,Realized_PnL,Note\n")
    val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    for (ord in orders) {
        val dt = df.format(Date(ord.timestamp))
        val tot = ord.quantity * ord.price
        sb.append(String.format(Locale.US, "%s,%d,%s,%s,%s,%.2f,%.2f,%.2f,\"%s\",%s,%.2f,\"%s\"\n",
            ord.id, ord.timestamp, dt, ord.symbol, ord.side.name, ord.quantity, ord.price, tot,
            ord.strategy.replace("\"", "\"\""), ord.status.name, ord.realizedPnl, ord.executionNote.replace("\"", "\"\"")
        ))
    }
    return sb.toString()
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
