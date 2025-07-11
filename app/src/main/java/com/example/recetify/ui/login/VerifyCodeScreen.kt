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
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.repeatable
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.math.roundToInt

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

    // Animación de shake
    val shakeOffset = remember { Animatable(0f) }
    var showRedBoxes by remember { mutableStateOf(false) }
    var showErrorMessage by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    var showResendMessage by remember { mutableStateOf(false) }
    LaunchedEffect(validationState) {
        if (validationState == Validation.Invalid) {
            showRedBoxes = true
            showErrorMessage = true
            shakeOffset.snapTo(0f)
            shakeOffset.animateTo(
                targetValue = 1f,
                animationSpec = repeatable(
                    iterations = 3,
                    animation = tween(50, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
            shakeOffset.snapTo(0f)
            kotlinx.coroutines.delay(2000)
            showRedBoxes = false
            otp = ""
        } else if (validationState == Validation.Valid) {
            showSuccessMessage = true
            kotlinx.coroutines.delay(1500)
            showSuccessMessage = false
        }
    }

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
                        fontFamily = Sen,
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                    Text(
                        "Reenviar",
                        fontFamily = Sen,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xCCBC6154),
                        modifier = Modifier.clickable {
                            viewModel.clearError()
                            validationState = null
                            otp = ""
                            viewModel.requestReset { /* no navega */ }
                            showResendMessage = true
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
                    Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            translationX = if (showRedBoxes) shakeOffset.value * 16 else 0f
                        },
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(6) { i ->
                        val char = otp.getOrNull(i)?.toString() ?: ""
                        val bg = when {
                            showRedBoxes -> Color(0xFFF44336)
                            validationState == Validation.Valid -> Color(0xFF4CAF50)
                            else -> Color(0xFFF2F4F7)
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

                // Mostrar mensaje de éxito si el código es correcto
                if (showSuccessMessage) {
                    Text(
                        text = "Código correcto",
                        color = Color(0xFF4CAF50),
                        fontSize = 14.sp,
                        fontFamily = Sen,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else if (showErrorMessage) {
                    Text(
                        text = "Código incorrecto",
                        color = Color.Red,
                        fontSize = 14.sp,
                        fontFamily = Sen,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else {
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
                // Mostrar mensaje de código reenviado
                if (showResendMessage) {
                    LaunchedEffect(showResendMessage) {
                        kotlinx.coroutines.delay(2000)
                        showResendMessage = false
                    }
                    Text(
                        text = "Código reenviado exitosamente",
                        color = Color(0xFF4CAF50),
                        fontSize = 14.sp,
                        fontFamily = Sen,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}