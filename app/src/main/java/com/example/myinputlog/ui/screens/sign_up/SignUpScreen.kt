package com.example.myinputlog.ui.screens.sign_up

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
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

object SignUpDestination : NavigationDestination {
    override val route: String = "sign_up"
    override val titleRes: Int = 0
}

@Composable
fun SignUpScreen(
    modifier: Modifier = Modifier,
    onSignInClick: () -> Unit,
    onSignUpClick: () -> Unit,
    viewModel: SignUpViewModel
) {
    val signUpUiState = viewModel.signUpUiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        MyInputLogAppIcon(modifier = Modifier.size(72.dp))
        Spacer(modifier = Modifier.height(20.dp))
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            label = { Text(stringResource(R.string.username) + "*") },
            leadingIcon = {
                Icon(Icons.Filled.Person, contentDescription = null)
            },
            trailingIcon = {
                if (!signUpUiState.value.isUsernameValid) {
                    Icon(Icons.Filled.Error, contentDescription = null)
                }
            },
            value = signUpUiState.value.username,
            onValueChange = {
                viewModel.updateUsername(it)
            },
            isError = !signUpUiState.value.isUsernameValid,
            supportingText = {
                if (!signUpUiState.value.isUsernameValid) {
                    Text(stringResource(R.string.username_hint))
                }
            },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            label = { Text(stringResource(R.string.email) + "*") },
            leadingIcon = {
                Icon(Icons.Filled.Email, contentDescription = null)
            },
            trailingIcon = {
                if (!signUpUiState.value.isEmailValid) {
                    Icon(Icons.Filled.Error, contentDescription = null)
                }
            },
            value = signUpUiState.value.email,
            onValueChange = {
                viewModel.updateEmail(it)
            },
            isError = !signUpUiState.value.isEmailValid,
            supportingText = {
                if (!signUpUiState.value.isEmailValid) {
                    Text(stringResource(signUpUiState.value.emailErrorMessage))
                }
            },
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
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            label = { Text(stringResource(R.string.password) + "*") },
            leadingIcon = {
                Icon(Icons.Filled.Lock, contentDescription = null)
            },
            trailingIcon = {
                if (signUpUiState.value.isPasswordVisible) {
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
            visualTransformation = if (signUpUiState.value.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            value = signUpUiState.value.password,
            onValueChange = {
                viewModel.updatePassword(it)
            },
            isError = !signUpUiState.value.isPasswordValid,
            supportingText = { Text(stringResource(R.string.password_hint)) },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Password
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    viewModel.onSignUpClick(onSignUpClick)
                    focusManager.clearFocus()
                }
            )
        )
        Spacer(modifier = Modifier.height(15.dp))
        Button(
            modifier = Modifier
                .fillMaxWidth(),
            onClick = { viewModel.onSignUpClick(onSignUpClick) }
        ) {
            Text(text = stringResource(R.string.sign_up))
        }
        Row(modifier = Modifier.weight(1f)) {
            AnimatedVisibility(visible = signUpUiState.value.isGeneralErrorVisible) {
                SomethingWentWrongBox(
                    errorMessage = signUpUiState.value.generalErrorMessage
                )
            }
        }
        Row {
            Text(stringResource(R.string.sign_up_bottom_text) + " ")
            Text(
                modifier = Modifier.clickable(
                    onClick = onSignInClick
                ),
                text = stringResource(R.string.sign_in),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}