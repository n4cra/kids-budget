package com.example.kidsbudget.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.kidsbudget.theme.KidPalette

@Composable
fun ColorPickerRow(
  selectedArgb: Int,
  onSelect: (Int) -> Unit,
  modifier: Modifier = Modifier,
  colors: List<Color> = KidPalette,
) {
  Row(
    modifier = modifier.horizontalScroll(rememberScrollState()),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    colors.forEach { color ->
      val argb = color.toArgb()
      val selected = argb == selectedArgb
      Box(
        modifier =
          Modifier.size(40.dp)
            .clip(CircleShape)
            .background(color)
            .then(
              if (selected) Modifier.border(2.dp, Color.White, CircleShape)
              else Modifier
            )
            .clickable { onSelect(argb) }
            .semantics { contentDescription = "Color" },
        contentAlignment = Alignment.Center,
      ) {
        if (selected) {
          Icon(imageVector = Icons.Filled.Check, contentDescription = null, tint = Color.White)
        }
      }
    }
  }
}
