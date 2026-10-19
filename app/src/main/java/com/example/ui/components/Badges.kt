package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DatasetType
import com.example.model.SignalType
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RubyRed

@Composable
fun DatasetTypeBadge(type: DatasetType, modifier: Modifier = Modifier) {
    val (label, bgColor, borderColor, textColor) = when (type) {
        DatasetType.SYNTHETIC -> Quadruple(
            "SYNTHETIC DATA",
            Color(0x2238BDF8),
            CyanAccent.copy(alpha = 0.5f),
            CyanAccent
        )
        DatasetType.HISTORICAL -> Quadruple(
            "HISTORICAL DATA",
            Color(0x2200E676),
            EmeraldGreen.copy(alpha = 0.5f),
            EmeraldGreen
        )
        DatasetType.CUSTOM_CSV -> Quadruple(
            "CUSTOM CSV",
            Color(0x22FFD600),
            AmberWarning.copy(alpha = 0.5f),
            AmberWarning
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun SignalBadge(signal: SignalType, modifier: Modifier = Modifier) {
    val (label, bgColor, textColor) = when (signal) {
        SignalType.BUY -> Triple("BUY", EmeraldGreen.copy(alpha = 0.2f), EmeraldGreen)
        SignalType.SELL -> Triple("SELL", RubyRed.copy(alpha = 0.2f), RubyRed)
        SignalType.HOLD -> Triple("HOLD", Color(0x2264748B), Color(0xFFCBD5E1))
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(textColor)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
fun PaperTradingBanner(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(AmberWarning.copy(alpha = 0.15f))
            .border(1.dp, AmberWarning.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(AmberWarning)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "PAPER TRADING (VIRTUAL MONEY ONLY - NO REAL ORDERS)",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = AmberWarning
        )
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
