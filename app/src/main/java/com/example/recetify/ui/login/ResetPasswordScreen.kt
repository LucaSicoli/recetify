package com.example.recetify.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.recetify.R
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.navigation.NavController
import kotlinx.coroutines.delay

private val Sen = FontFamily(
    Font(R.font.sen_regular, weight = FontWeight.Normal),
    Font(R.font.sen_bold,    weight = FontWeight.Bold)
)

@Composable
fun ResetPasswordScreen(
    viewModel: PasswordResetViewModel = viewModel(),
    onFinish: () -> Unit,
    navController: NavController? = null // <-- Permitir pasar NavController
) {
    val state by viewModel.state.collectAsState()
    var showSuccessMessage by remember { mutableStateOf(false) }
    val errorIsMismatch = state.error == "Las contraseñas no coinciden"
    val borderColor = if (errorIsMismatch) Color.Red else Color(0xFFBC6154)
    val borderWidth = if (errorIsMismatch) 2.dp else 1.dp
    val focusColor = if (errorIsMismatch) Color.Red else Color(0xFFBC6154)
    val unfocusColor = if (errorIsMismatch) Color.Red else Color(0xFFBC6154)
    val successColor = Color(0xFF4CAF50)
    val passwordsMatch = state.newPassword == state.confirmPassword && state.confirmPassword.isNotEmpty()
    val allValid = state.isLengthValid && state.hasUppercase && state.hasNumberOrSymbol && passwordsMatch
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val newPassFocusRequester = remember { FocusRequester() }
    val confirmPassFocusRequester = remember { FocusRequester() }




    LaunchedEffect(state.error) {
        if (state.error == null && !state.isLoading && state.newPassword.isNotEmpty() && state.confirmPassword.isNotEmpty()) {
            showSuccessMessage = true
            kotlinx.coroutines.delay(1500)
            showSuccessMessage = false
            onFinish()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(Color(0xFF0D0B1F)),
            contentAlignment = Alignment.Center
        ) {
            // FLECHA DE RETROCESO
            IconButton(
                onClick = {
                    navController?.navigate("forgot") {
                        popUpTo("forgot") { inclusive = true }
                    }
                },
                modifier = Modifier
                    .padding(16.dp)
                    .size(40.dp)
                    .background(Color(0x66000000), shape = androidx.compose.foundation.shape.CircleShape)
                    .align(Alignment.TopStart)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.White
                )
            }
            // CONTENIDO CENTRAL
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(id = R.drawable.logo_chef),
                    contentDescription = "Logo Recetify",
                    modifier = Modifier.size(100.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "CAMBIAR CONTRASEÑA",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = Sen
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Elegí tu nueva contraseña",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontFamily = Sen
                )
            }
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = state.newPassword,
                        onValueChange = viewModel::onNewPassChange,
                        label = { Text("Nueva contraseña") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color.DarkGray
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(newPassFocusRequester),
                        shape = RoundedCornerShape(12.dp),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { confirmPassFocusRequester.requestFocus() }
                        ),
                        textStyle = TextStyle(
                            fontFamily = Sen,
                            fontSize = 16.sp,
                            color = Color.Black
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = focusColor,
                            unfocusedBorderColor = unfocusColor,
                            errorBorderColor = Color.Red
                        ),
                        isError = errorIsMismatch,
                        singleLine = true

                    )
                }
                item {
                    OutlinedTextField(
                        value = state.confirmPassword,
                        onValueChange = viewModel::onConfirmPassChange,
                        label = { Text("Repetir contraseña") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color.DarkGray
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(confirmPassFocusRequester),
                        shape = RoundedCornerShape(12.dp),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                if (allValid) viewModel.resetPassword(onFinish)
                            }
                        ),
                        textStyle = TextStyle(
                            fontFamily = Sen,
                            fontSize = 16.sp,
                            color = Color.Black
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = focusColor,
                            unfocusedBorderColor = unfocusColor,
                            errorBorderColor = Color.Red
                        ),
                        isError = errorIsMismatch,
                        singleLine = true
                    )
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        ValidationRow("Mínimo 8 caracteres", state.isLengthValid)
                        ValidationRow("Al menos una mayúscula", state.hasUppercase)
                        ValidationRow("Número o símbolo", state.hasNumberOrSymbol)
                        ValidationRow("Las contraseñas coinciden", passwordsMatch)
                    }
                }
                item {
                    Button(
                        onClick = { viewModel.resetPassword(onFinish) },
                        enabled = allValid,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xCCBC6154))
                    ) {
                        if (state.isLoading)
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        else
                            Text("CAMBIAR", color = Color.White, fontFamily = Sen)
                    }
                }

                if (showSuccessMessage) {
                    item {
                        Text(
                            text = "Contraseña cambiada exitosamente",
                            color = successColor,
                            fontSize = 14.sp,
                            fontFamily = Sen,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }

                state.error?.let {
                    item {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            fontFamily = Sen
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun ValidationRow(text: String, isValid: Boolean) {
    val icon = if (isValid) Icons.Default.Check else Icons.Default.Close
    val color = if (isValid) Color(0xFF4CAF50) else Color.Red

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            color = color,
            fontSize = 12.sp,
            fontFamily = Sen
        )
    }
}

