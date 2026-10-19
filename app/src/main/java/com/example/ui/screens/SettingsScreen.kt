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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

@Composable
fun SettingsScreen(
    state: TradingUiState,
    onResetDemoData: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var showResetDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "APPLICATION SETTINGS",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CyanAccent
            )
        }
        Text(
            text = "Configure default execution variables, review architecture, or reset demo database.",
            fontSize = 12.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Reset Demo Data Card
        Card(
            colors = CardDefaults.cardColors(containerColor = NavySurface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, NavyBorder, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "DATABASE & FACTORY RESET",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = RubyRed,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Restores initial synthetic datasets (AAPL, TSLA, SPY, BTC-USD), clears paper trading positions and orders, and resets virtual cash balance to $10,000.00.",
                    fontSize = 11.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { showResetDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = RubyRed, contentColor = TextPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("reset_demo_data_button")
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reset All Data to Demo Defaults", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // College Final Year Project Architecture Card
        Card(
            colors = CardDefaults.cardColors(containerColor = NavySurface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, NavyBorder, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "PROJECT ARCHITECTURE & SPECIFICATION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanAccent,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                SpecificationItem(title = "Project Title", value = "Algorithmic Trading System (College Final-Year Project)")
                SpecificationItem(title = "Client Layer", value = "Jetpack Compose M3 (Kotlin 2.2, Android SDK 36)")
                SpecificationItem(title = "Database Engine", value = "SQLite via Android Room ORM (Coroutines & StateFlow)")
                SpecificationItem(title = "Indicators Implemented", value = "Simple Moving Average (SMA), Exponential Moving Average (EMA), Wilder's RSI")
                SpecificationItem(title = "Backtest Model", value = "Next-bar execution (Zero look-ahead bias), slippage model, transaction fees, Stop-Loss / Take-Profit")
                SpecificationItem(title = "Risk Management", value = "Max position sizing, max portfolio exposure ceiling, capital preservation buffer")
                SpecificationItem(title = "Paper Trading", value = "Simulated virtual cash ledger, average entry calculation, mark-to-market P&L")
                SpecificationItem(title = "Backend Companion", value = "Python FastAPI + Pandas / NumPy + Pytest suite (included in repo)")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Security & Safety Banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0x1800E676))
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "SAFETY VERIFIED: This application contains strictly zero real-money broker routing. All trades, orders, and portfolio metrics are executed in isolated simulation memory.",
                fontSize = 10.sp,
                color = EmeraldGreen,
                lineHeight = 14.sp
            )
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Text("Confirm Database Reset", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Are you sure you want to reset all data? This will clear all custom CSV datasets, delete paper orders, and restore factory synthetic datasets with $10,000 cash.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetDemoData()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RubyRed)
                ) {
                    Text("Yes, Reset Everything", color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = NavySurfaceVariant,
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
private fun SpecificationItem(title: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = title, fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
        Text(text = value, fontSize = 11.sp, color = TextPrimary)
    }
}
