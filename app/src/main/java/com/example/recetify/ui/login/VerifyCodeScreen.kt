// app/src/main/java/com/example/recetify/ui/login/VerifyCodeScreen.kt
package com.example.recetify.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.recetify.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val Sen = FontFamily(
    Font(R.font.sen_regular, weight = FontWeight.Normal),
    Font(R.font.sen_bold,    weight = FontWeight.Bold)
)

private enum class Validation { Valid, Invalid }

@Composable
fun VerifyCodeScreen(
    viewModel: PasswordResetViewModel = viewModel(),
    onNext: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var otp by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }
    var cursorVisible by remember { mutableStateOf(false) }
    var validationState by remember { mutableStateOf<Validation?>(null) }
    val keyboard = LocalSoftwareKeyboardController.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    // 1) Al entrar limpiamos cualquier error o state previo
    LaunchedEffect(Unit) {
        viewModel.clearError()
        validationState = null
        otp = ""
    }

    // 2) Cursor blink
    LaunchedEffect(isFocused) {
        while (isFocused) {
            cursorVisible = true
            delay(500)
            cursorVisible = false
            delay(500)
        }
        cursorVisible = false
    }

    // 3) Pedimos foco en STARTED
    LaunchedEffect(Unit) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            focusRequester.requestFocus()
            keyboard?.show()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // — PARTE SUPERIOR OSCURA —
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(Color(0xFF0D0B1F)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(R.drawable.logo_chef),
                    contentDescription = null,
                    modifier = Modifier.size(100.dp)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "VERIFICAR CÓDIGO",
                    fontFamily = Sen,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Ingresá el código que te enviamos",
                    fontFamily = Sen,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
        }

        // — PARTE INFERIOR BLANCA —
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Header con “Reenviar”
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "CÓDIGO DE VERIFICACIÓN",
                        fontFamily = Sen, fontSize = 14.sp, color = Color.Black
                    )
                    Text(
                        "Reenviar",
                        fontFamily = Sen, fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xCCBC6154),
                        modifier = Modifier.clickable {
                            // 1) Limpiar estado
                            viewModel.clearError()
                            validationState = null
                            otp = ""
                            // 2) Reenviar mail
                            viewModel.requestReset { /* no navega */ }
                        }
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Input oculto que recibe los dígitos
                BasicTextField(
                    value = otp,
                    onValueChange = { new ->
                        if (new.length <= 6 && new.all(Char::isDigit)) {
                            otp = new
                            validationState = null
                            viewModel.clearError()
                            if (new.length == 6) {
                                // validamos automáticamente
                                viewModel.verifyCode(
                                    code = new,
                                    onSuccess = {
                                        validationState = Validation.Valid
                                        scope.launch {
                                            delay(300)
                                            onNext()
                                        }
                                    },
                                    onError = {
                                        validationState = Validation.Invalid
                                    }
                                )
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .onFocusChanged { isFocused = it.isFocused }
                        .size(width = 1.dp, height = 1.dp)
                )

                // Cajitas de visualización
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(6) { i ->
                        val char = otp.getOrNull(i)?.toString() ?: ""
                        val bg = when (validationState) {
                            Validation.Valid   -> Color(0xFF4CAF50)
                            Validation.Invalid -> Color(0xFFF44336)
                            else               -> Color(0xFFF2F4F7)
                        }
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(48.dp, 64.dp)
                                .background(bg, RoundedCornerShape(8.dp))
                                .clickable {
                                    focusRequester.requestFocus()
                                    keyboard?.show()
                                }
                        ) {
                            if (char.isNotEmpty()) {
                                Text(
                                    char,
                                    fontFamily = Sen,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Black
                                )
                            } else if (i == otp.length && isFocused && cursorVisible) {
                                Box(
                                    Modifier
                                        .width(2.dp)
                                        .height(32.dp)
                                        .background(Color.Black)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Botón manual (opcional)
                Button(
                    onClick = {
                        viewModel.clearError()
                        validationState = null
                        viewModel.verifyCode(
                            code = otp,
                            onSuccess = onNext,
                            onError = { validationState = Validation.Invalid }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xCCBC6154))
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("VERIFICAR", color = Color.White, fontFamily = Sen)
                    }
                }

                // Mostrar error si existe
                state.error?.let { err ->
                    Text(
                        text = err,
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