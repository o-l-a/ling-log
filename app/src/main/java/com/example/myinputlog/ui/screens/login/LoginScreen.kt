package com.example.myinputlog.ui.screens.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myinputlog.R
import com.example.myinputlog.ui.navigation.NavigationDestination
import com.example.myinputlog.ui.screens.utils.composable.MyInputLogAppIcon
import com.example.myinputlog.ui.screens.utils.composable.SomethingWentWrongBox

object LoginDestination : NavigationDestination {
    override val route: String = "login"
    override val titleRes: Int = 0
}

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onSignUpClick: () -> Unit,
    onLoginClick: () -> Unit,
    viewModel: LoginViewModel
) {
    val loginUiState = viewModel.loginUiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        MyInputLogAppIcon(modifier = Modifier.size(72.dp))
        Spacer(modifier = Modifier.height(20.dp))
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            label = { Text(stringResource(R.string.email)) },
            value = loginUiState.value.email,
            leadingIcon = {
                Icon(Icons.Filled.Email, contentDescription = null)
            },
            onValueChange = {
                viewModel.updateEmail(it)
            },
            supportingText = { Text("") },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Email
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth(),
            label = { Text(stringResource(R.string.password)) },
            leadingIcon = {
                Icon(Icons.Filled.Lock, contentDescription = null)
            },
            trailingIcon = {
                if (loginUiState.value.isPasswordVisible) {
                    Icon(
                        modifier = Modifier.clickable(
                            onClick = { viewModel.togglePasswordVisibility(false) }
                        ),
                        imageVector = Icons.Filled.Visibility,
                        tint = MaterialTheme.colorScheme.onSurface,
                        contentDescription = null
                    )
                } else {
                    Icon(
                        modifier = Modifier.clickable(
                            onClick = { viewModel.togglePasswordVisibility(true) }
                        ),
                        imageVector = Icons.Filled.VisibilityOff,
                        tint = MaterialTheme.colorScheme.onSurface,
                        contentDescription = null
                    )
                }
            },
            isError = !loginUiState.value.isFormValid,
            supportingText = {
                if (!loginUiState.value.isFormValid) {
                    Text(stringResource(R.string.password_invalid))
                }
            },
            visualTransformation = if (loginUiState.value.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            value = loginUiState.value.password,
            onValueChange = {
                viewModel.updatePassword(it)
            },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Password
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    viewModel.onLoginClick(onLoginClick)
                    focusManager.clearFocus()
                }
            )
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            modifier = Modifier
                .fillMaxWidth(),
            onClick = {
                viewModel.onLoginClick(onLoginClick)
            }
        ) {
            Text(text = stringResource(R.string.sign_in))
        }
        Row(modifier = Modifier.weight(1f)) {
            AnimatedVisibility(visible = loginUiState.value.isGeneralErrorVisible) {
                SomethingWentWrongBox(
                    errorMessage = loginUiState.value.generalErrorMessage
                )
            }
        }
        Row {
            Text(stringResource(R.string.sign_in_bottom_text) + " ")
            Text(
                modifier = Modifier.clickable(
                    onClick = onSignUpClick
                ),
                text = stringResource(R.string.sign_up),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}