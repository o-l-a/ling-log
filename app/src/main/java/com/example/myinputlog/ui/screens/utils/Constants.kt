package com.example.myinputlog.ui.screens.utils

import android.annotation.SuppressLint
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import java.text.SimpleDateFormat

val IME_ACTION_NEXT = KeyboardOptions.Default.copy(
    imeAction = ImeAction.Next
)

val IME_ACTION_DONE = KeyboardOptions.Default.copy(
    imeAction = ImeAction.Done
)

@SuppressLint("SimpleDateFormat")
val dateFormatter = SimpleDateFormat("dd/MM/yyyy")

@Composable
fun myInputLogTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    disabledContainerColor = Color.Transparent,
    errorContainerColor = Color.Transparent,
    focusedBorderColor = Color.Transparent,
    unfocusedBorderColor = Color.Transparent,
    disabledBorderColor = Color.Transparent,
    errorBorderColor = Color.Transparent
)