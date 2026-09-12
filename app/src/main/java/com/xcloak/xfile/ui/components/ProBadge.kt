package com.xcloak.xfile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xcloak.xfile.ui.theme.ElectricBlue

@Composable
fun ProBadge(
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(ElectricBlue.copy(alpha = 0.2f), ElectricBlue.copy(alpha = 0.1f))
                ),
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(ElectricBlue.copy(alpha = 0.5f), Color.Transparent)
                ),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = "PRO",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                color = ElectricBlue,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp
            )
        )
    }
}
