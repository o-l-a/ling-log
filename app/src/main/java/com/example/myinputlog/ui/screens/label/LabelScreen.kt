package com.example.myinputlog.ui.screens.label

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun LabelScreen(
    modifier: Modifier = Modifier, labelViewModel: LabelViewModel, onNavigateUp: () -> Unit
) {
    Text("label")
}