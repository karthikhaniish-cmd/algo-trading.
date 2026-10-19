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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
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
import com.example.domain.risk.RiskEngine
import com.example.model.RiskConfig
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
fun RiskManagementScreen(
    state: TradingUiState,
    onUpdateRiskConfig: (RiskConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val config = state.riskConfig

    var maxPosPct by remember(config) { mutableFloatStateOf(config.maxPositionSizePct.toFloat()) }
    var maxExposurePct by remember(config) { mutableFloatStateOf(config.maxPortfolioExposurePct.toFloat()) }
    var stopLossPct by remember(config) { mutableFloatStateOf(config.defaultStopLossPct.toFloat()) }
    var takeProfitPct by remember(config) { mutableFloatStateOf(config.defaultTakeProfitPct.toFloat()) }

    // Live Order Risk Calculator
    var testQty by remember { mutableStateOf("50") }
    val currentSymbol = state.selectedDataset?.symbol ?: "AAPL"
    val currentPrice = state.bars.lastOrNull()?.close ?: 150.0

    val evalResult = remember(testQty, maxPosPct, maxExposurePct, state.paperAccount, state.paperPositions) {
        val qty = testQty.toDoubleOrNull() ?: 0.0
        RiskEngine.validateOrder(
            symbol = currentSymbol,
            isBuy = true,
            requestedQuantity = qty,
            price = currentPrice,
            account = state.paperAccount,
            existingPositions = state.paperPositions,
            config = RiskConfig(
                maxPositionSizePct = maxPosPct.toDouble(),
                maxPortfolioExposurePct = maxExposurePct.toDouble(),
                defaultStopLossPct = stopLossPct.toDouble(),
                defaultTakeProfitPct = takeProfitPct.toDouble()
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "PORTFOLIO RISK CONTROLS",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CyanAccent
            )
        }
        Text(
            text = "Configurable position size ceilings, portfolio exposure caps, and risk/reward rules.",
            fontSize = 12.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Risk Configuration Card
        Card(
            colors = CardDefaults.cardColors(containerColor = NavySurface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, NavyBorder, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "RISK PARAMETERS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanAccent,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Max Position Size: ${maxPosPct.toInt()}% of Portfolio",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = "Caps the max capital allocated to a single asset to prevent catastrophic concentration risk.",
                    fontSize = 10.sp,
                    color = TextMuted
                )
                Slider(
                    value = maxPosPct,
                    onValueChange = { maxPosPct = it },
                    valueRange = 5f..100f,
                    colors = sliderColors()
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Max Total Portfolio Exposure: ${maxExposurePct.toInt()}%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = "Guarantees a cash buffer (minimum ${100 - maxExposurePct.toInt()}% cash reserve) during market volatility.",
                    fontSize = 10.sp,
                    color = TextMuted
                )
                Slider(
                    value = maxExposurePct,
                    onValueChange = { maxExposurePct = it },
                    valueRange = 20f..100f,
                    colors = sliderColors()
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Stop-Loss: ${stopLossPct.toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = RubyRed
                        )
                        Slider(
                            value = stopLossPct,
                            onValueChange = { stopLossPct = it },
                            valueRange = 1f..15f,
                            colors = SliderDefaults.colors(thumbColor = RubyRed, activeTrackColor = RubyRed)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Take-Profit: ${takeProfitPct.toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = EmeraldGreen
                        )
                        Slider(
                            value = takeProfitPct,
                            onValueChange = { takeProfitPct = it },
                            valueRange = 2f..30f,
                            colors = SliderDefaults.colors(thumbColor = EmeraldGreen, activeTrackColor = EmeraldGreen)
                        )
                    }
                }

                val riskRewardRatio = if (stopLossPct > 0) takeProfitPct / stopLossPct else 0f
                Text(
                    text = String.format(Locale.US, "Calculated Risk-to-Reward Ratio: 1 : %.2f", riskRewardRatio),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (riskRewardRatio >= 1.5) EmeraldGreen else AmberWarning
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        onUpdateRiskConfig(
                            RiskConfig(
                                maxPositionSizePct = maxPosPct.toDouble(),
                                maxPortfolioExposurePct = maxExposurePct.toDouble(),
                                defaultStopLossPct = stopLossPct.toDouble(),
                                defaultTakeProfitPct = takeProfitPct.toDouble()
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color(0xFF070C18)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_risk_config_button")
                ) {
                    Text("Save Risk Policy", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Pre-Trade Risk Evaluator
        Text(
            text = "PRE-TRADE RISK VALIDATOR SIMULATOR",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(6.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = NavySurface),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, NavyBorder, RoundedCornerShape(10.dp))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Evaluate order sizing for $currentSymbol @ $${String.format(Locale.US, "%.2f", currentPrice)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = testQty,
                    onValueChange = { testQty = it },
                    label = { Text("Proposed Shares Quantity", fontSize = 11.sp) },
                    colors = textFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                val isAllowed = evalResult.isAllowed
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isAllowed) EmeraldGreen.copy(alpha = 0.15f) else RubyRed.copy(alpha = 0.15f)
                        )
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isAllowed) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (isAllowed) EmeraldGreen else RubyRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isAllowed) "RISK CHECK PASSED" else "ORDER BLOCKED BY RISK POLICY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isAllowed) EmeraldGreen else RubyRed
                        )
                        Text(
                            text = evalResult.warningMessage ?: "Order complies with portfolio exposure & position concentration limits.",
                            fontSize = 11.sp,
                            color = TextPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Educational Risk Policy Notice
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0x1838BDF8))
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "ACADEMIC PRINCIPLE: Risk controls reduce drawdowns and prevent total capital loss, but they cannot eliminate systematic market risk. Strict position sizing ensures survival during unexpected black-swan events.",
                fontSize = 10.sp,
                color = TextSecondary,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
private fun sliderColors() = SliderDefaults.colors(
    thumbColor = CyanAccent,
    activeTrackColor = CyanAccent,
    inactiveTrackColor = NavySurfaceVariant
)

@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = CyanAccent,
    unfocusedBorderColor = NavyBorder,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedContainerColor = NavySurface,
    unfocusedContainerColor = NavySurface
)
