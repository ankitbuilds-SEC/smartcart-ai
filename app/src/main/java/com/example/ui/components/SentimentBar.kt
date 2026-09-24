package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun AiBadge(
    label: String = "AI Recommendation",
    modifier: Modifier = Modifier
) {
    Surface(
        color = Purple500.copy(alpha = 0.15f),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Purple500,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                color = Purple500,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SentimentBar(
    positivePercent: Int,
    neutralPercent: Int,
    negativePercent: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "AI Sentiment Analysis",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "$positivePercent% Positive",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Emerald600
                )
                Text(
                    text = "$negativePercent% Negative",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Rose500
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Multi-segment progress bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
        ) {
            if (positivePercent > 0) {
                Box(
                    modifier = Modifier
                        .weight(positivePercent.coerceAtLeast(1).toFloat())
                        .fillMaxHeight()
                        .background(Emerald500)
                )
            }
            if (neutralPercent > 0) {
                Box(
                    modifier = Modifier
                        .weight(neutralPercent.coerceAtLeast(1).toFloat())
                        .fillMaxHeight()
                        .background(Amber500)
                )
            }
            if (negativePercent > 0) {
                Box(
                    modifier = Modifier
                        .weight(negativePercent.coerceAtLeast(1).toFloat())
                        .fillMaxHeight()
                        .background(Rose500)
                )
            }
        }
    }
}
