package com.example.recetify.ui.login

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextAlign
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
    navController: NavController? = null
) {
    val state by viewModel.state.collectAsState()
    var showSuccessMessage by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    val errorIsMismatch = state.error == "Las contraseñas no coinciden"
    val borderColor = if (errorIsMismatch) Color.Red else Color(0xFFBC6154)
    val borderWidth = if (errorIsMismatch) 2.dp else 1.dp
    val focusColor = if (errorIsMismatch) Color(0xFF60A5FA) else Color(0xFFBC6154)  // Celeste suave
    val unfocusColor = if (errorIsMismatch) Color(0xFF60A5FA) else Color(0xFFBC6154)  // Celeste suave
    val successColor = Color(0xFF4CAF50)
    val passwordsMatch = state.newPassword == state.confirmPassword && state.confirmPassword.isNotEmpty()
    val allValid = state.isLengthValid && state.hasUppercase && state.hasNumber && state.hasSpecialChar && passwordsMatch
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val newPassFocusRequester = remember { FocusRequester() }
    val confirmPassFocusRequester = remember { FocusRequester() }

    // Limpiar el estado al entrar a la pantalla
    LaunchedEffect(Unit) {
        viewModel.clearResetState()
        showSuccessMessage = false
    }

    // Observar cuando el botón de cambiar es presionado exitosamente
    LaunchedEffect(showSuccessMessage) {
        if (showSuccessMessage) {
            delay(1500)
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
            // FLECHA DE RETROCESO con diálogo de confirmación
            IconButton(
                onClick = { showExitDialog = true },
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
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
                        fontSize   = 16.sp,
                        color      = Color.Black
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (errorIsMismatch) Color(0xFF60A5FA) else MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = if (errorIsMismatch) Color(0xFF60A5FA) else MaterialTheme.colorScheme.outline,
                        focusedLabelColor = if (errorIsMismatch) Color(0xFF60A5FA) else MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = if (errorIsMismatch) Color(0xFF60A5FA) else MaterialTheme.colorScheme.onSurfaceVariant,
                        errorBorderColor = Color(0xFF60A5FA),
                        errorLabelColor = Color(0xFF60A5FA)
                    ),
                    isError = errorIsMismatch,
                    singleLine = true

                )

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
                            if (allValid) {
                                viewModel.resetPassword {
                                    showSuccessMessage = true
                                }
                            }
                        }
                    ),
                    textStyle = TextStyle(
                        fontFamily = Sen,
                        fontSize   = 16.sp,
                        color      = Color.Black
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (errorIsMismatch) Color(0xFF60A5FA) else MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = if (errorIsMismatch) Color(0xFF60A5FA) else MaterialTheme.colorScheme.outline,
                        focusedLabelColor = if (errorIsMismatch) Color(0xFF60A5FA) else MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = if (errorIsMismatch) Color(0xFF60A5FA) else MaterialTheme.colorScheme.onSurfaceVariant,
                        errorBorderColor = Color(0xFF60A5FA),
                        errorLabelColor = Color(0xFF60A5FA)
                    ),
                    isError = errorIsMismatch,
                    singleLine = true

                )

                // Sección de validaciones con estilo mejorado
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Requisitos de la contraseña",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2D3748),
                            fontFamily = Sen,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        ValidationRow("Mínimo 8 caracteres", state.isLengthValid)
                        ValidationRow("Al menos una mayúscula", state.hasUppercase)
                        ValidationRow("Al menos un número", state.hasNumber)
                        ValidationRow("Al menos un carácter especial", state.hasSpecialChar)
                        ValidationRow("Las contraseñas coinciden", passwordsMatch)
                    }
                }


                Button(
                    onClick = {
                        viewModel.resetPassword {
                            // Cuando la API responde exitosamente, mostrar mensaje de éxito
                            showSuccessMessage = true
                        }
                    },
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

                if (showSuccessMessage) {
                    Text(
                        text = "Contraseña cambiada exitosamente",
                        color = successColor,
                        fontSize = 14.sp,
                        fontFamily = Sen,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                state.error?.let {
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

    // Diálogo de confirmación para salir - Diseño mejorado
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Icono con fondo circular
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(
                                Color(0xFFFEF2F2),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "¿Salir del proceso?",
                        fontFamily = Sen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color(0xFF1F2937),
                        textAlign = TextAlign.Center
                    )
                }
            },
            text = {
                Text(
                    text = "Si salís ahora, perderás el progreso y tendrás que empezar de nuevo el proceso de cambio de contraseña.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = Sen,
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        color = Color(0xFF6B7280)
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Botón primario - Salir
                    Button(
                        onClick = {
                            showExitDialog = false
                            navController?.navigate("login") {
                                popUpTo("forgot") { inclusive = true }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEF4444)
                        ),
                        shape = RoundedCornerShape(24.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 2.dp
                        )
                    ) {
                        Text(
                            "Sí, salir",
                            color = Color.White,
                            fontFamily = Sen,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    }

                    // Botón secundario - Continuar
                    OutlinedButton(
                        onClick = { showExitDialog = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(2.dp, Color(0xFFBC6154)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFBC6154)
                        )
                    ) {
                        Text(
                            "Continuar aquí",
                            fontFamily = Sen,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    }
                }
            },
            dismissButton = null, // Removemos el dismissButton ya que usamos una estructura custom
            shape = RoundedCornerShape(28.dp),
            containerColor = Color.White,
            modifier = Modifier.padding(16.dp)
        )
    }

    // Manejo del botón de retroceso del sistema
    BackHandler {
        if (showExitDialog) {
            showExitDialog = false
        } else {
            showExitDialog = true
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
