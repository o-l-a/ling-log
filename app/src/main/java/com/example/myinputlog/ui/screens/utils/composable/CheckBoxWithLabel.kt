package com.example.myinputlog.ui.screens.utils.composable

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import com.example.myinputlog.ui.theme.spacing

@Composable
fun CheckBoxWithLabel(
    modifier: Modifier = Modifier, value: Boolean, onValueChange: (Boolean) -> Unit, text: String
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = MaterialTheme.spacing.large + MaterialTheme.spacing.medium)
            .toggleable(
                value = value, role = Role.Checkbox, onValueChange = { isChecked ->
                    onValueChange(isChecked)
                }),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = value, onCheckedChange = null
        )
        Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
        Text(
            text = text
        )
    }
}