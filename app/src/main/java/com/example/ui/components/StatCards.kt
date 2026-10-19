package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavySurface
import com.example.ui.theme.RubyRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun StatCard(
    title: String,
    value: String,
    subValue: String? = null,
    isPositive: Boolean? = null,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    val valueColor = when (isPositive) {
        true -> EmeraldGreen
        false -> RubyRed
        null -> TextPrimary
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(NavySurface)
            .border(1.dp, NavyBorder, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.uppercase(Locale.US),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextMuted,
                    letterSpacing = 0.5.sp
                )
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
            if (subValue != null) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subValue,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun PnlText(
    amount: Double,
    percent: Double? = null,
    modifier: Modifier = Modifier,
    fontSize: Int = 14
) {
    val isPositive = amount >= 0.0
    val color = if (isPositive) EmeraldGreen else RubyRed
    val prefix = if (isPositive) "+" else ""

    val text = if (percent != null) {
        String.format(Locale.US, "%s$%.2f (%s%.2f%%)", prefix, amount, prefix, percent)
    } else {
        String.format(Locale.US, "%s$%.2f", prefix, amount)
    }

    Text(
        text = text,
        fontSize = fontSize.sp,
        fontWeight = FontWeight.SemiBold,
        color = color,
        modifier = modifier
    )
}
