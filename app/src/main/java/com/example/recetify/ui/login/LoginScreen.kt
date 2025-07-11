package com.example.recetify.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.recetify.R
import com.example.recetify.data.local.UserPreferences
import com.example.recetify.data.remote.model.SessionManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import android.content.Intent
import android.net.Uri

private val Sen = FontFamily(
    Font(R.font.sen_regular, weight = FontWeight.Normal),
    Font(R.font.sen_bold,    weight = FontWeight.Bold)
)

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onLoginSuccess: (token: String, email: String) -> Unit,
    onForgot: () -> Unit,
    onVisitor: () -> Unit,
    passwordChanged: Boolean = false,
) {
    val context = LocalContext.current
    val prefs   = remember { UserPreferences(context) }
    val scope   = rememberCoroutineScope()

    val state by viewModel.state.collectAsState()
    var showError by remember { mutableStateOf(false) }
    val errorMsg = "Los datos ingresados no son correctos"
    var showPasswordChanged by remember { mutableStateOf(passwordChanged) }

    // Mostrar el error solo si hay error en el state
    LaunchedEffect(state.error) {
        if (state.error != null) {
            showError = true
            delay(3000)
            showError = false
        }
    }

    // Mostrar mensaje de contraseña cambiada exitosamente
    LaunchedEffect(passwordChanged) {
        if (passwordChanged) {
            showPasswordChanged = true
            delay(2000)
            showPasswordChanged = false
        }
    }

    // Credenciales guardadas
    val savedRemember by prefs.rememberFlow.collectAsState(initial = false)
    val savedAlias    by prefs.aliasFlow.collectAsState(initial = "")
    val savedEmail    by prefs.emailFlow.collectAsState(initial = "")
    val savedPwd      by prefs.passwordFlow.collectAsState(initial = "")

    var rememberMe by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(savedRemember) { rememberMe = savedRemember }
    LaunchedEffect(savedRemember) {
        if (savedRemember) {
            viewModel.onAliasChanged(savedAlias ?: "")
            viewModel.onEmailChanged(savedEmail ?: "")
            viewModel.onPasswordChanged(savedPwd ?: "")
        }
    }
    LaunchedEffect(state.token) {
        state.token?.let { token ->
            // 1) Guardar o limpiar credenciales
            if (rememberMe) {
                prefs.saveLoginData(
                    alias    = state.alias,
                    email    = state.email,
                    password = state.password,
                    remember = true
                )
            } else {
                prefs.clearLoginData()
            }

            // 2) Setear el token de alumno
            SessionManager.setAlumno(context, token, state.email)

            // 3) Navegar a Home
            onLoginSuccess(token, state.email)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // ── PARTE SUPERIOR OSCURA ───────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(Color(0xFF0D0B1F)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.logo_chef),
                    contentDescription = "Logo Recetify",
                    modifier = Modifier.size(120.dp)
                )
                Spacer(Modifier.height(24.dp))
                Text(
                    "INICIO DE SESIÓN",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    fontFamily = Sen
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Ingresá para descubrir las mejores recetas",
                    fontSize = 15.sp,
                    color = Color.White,
                    fontFamily = Sen
                )
            }
        }

        // Mensaje de éxito de cambio de contraseña
        if (showPasswordChanged) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Surface(
                    color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(24.dp),
                    shadowElevation = 2.dp,
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.logo_chef),
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Contraseña cambiada exitosamente",
                            color = Color(0xFF4CAF50),
                            fontFamily = Sen,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }

        // ── PARTE INFERIOR BLANCA ───────────────────────────────────────────────
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Box(Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Alias
                    OutlinedTextField(
                        value = state.alias,
                        onValueChange = viewModel::onAliasChanged,
                        label = { Text("Alias") },
                        placeholder = { Text("Tu alias") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = TextStyle(color = Color.Black, fontSize = 16.sp),
                        singleLine = true
                    )

                    // Email
                    OutlinedTextField(
                        value = state.email,
                        onValueChange = viewModel::onEmailChanged,
                        label = { Text("Email") },
                        placeholder = { Text("example@gmail.com") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        textStyle = TextStyle(color = Color.Black, fontSize = 16.sp),
                        singleLine = true
                    )

                    // Contraseña
                    OutlinedTextField(
                        value = state.password,
                        onValueChange = viewModel::onPasswordChanged,
                        label = { Text("Contraseña") },
                        placeholder = { Text("••••••••") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = TextStyle(color = Color.Black, fontSize = 16.sp),
                        visualTransformation = if (state.isPasswordVisible)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = viewModel::togglePasswordVisibility) {
                                Icon(
                                    imageVector = if (state.isPasswordVisible)
                                        Icons.Filled.Visibility
                                    else
                                        Icons.Filled.VisibilityOff,
                                    contentDescription = null
                                )
                            }
                        },
                        singleLine = true
                    )

                    // Recordarme
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(
                            checked = rememberMe,
                            onCheckedChange = { rememberMe = it }
                        )
                        Text(
                            "Recordarme",
                            fontSize = 14.sp,
                            color = Color(0xFF555555)
                        )
                    }

                    // Botón INICIAR SESIÓN
                    Button(
                        onClick = viewModel::onLoginClicked,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xCCBC6154))
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Text(
                                "INICIAR SESIÓN",
                                color = Color.White,
                                fontFamily = Sen
                            )
                        }
                    }
                    Button(
                        onClick = {
                            scope.launch {
                                SessionManager.setVisitante(context)
                                onVisitor()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .offset(y = (-6).dp),     // <-- esto lo sube 4dp
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray))
                    {
                        Text("INGRESAR COMO VISITANTE", color = Color.White, fontFamily = Sen)
                    }

                    // — Enlaces pequeños debajo —
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "¿Olvidaste la contraseña?",
                            fontSize = 14.sp,
                            color = Color(0xFFBC6154),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = onForgot)
                                .padding(vertical = 2.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "¿No tenés una cuenta? ",
                                fontSize = 14.sp,
                                color = Color(0xFF555555)
                            )
                            Text(
                                "REGÍSTRATE",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xCCBC6154),
                                modifier = Modifier.clickable {
                                    val url = "https://eu.login.vorwerk.com/ciam/register?ui_locales=es-ES&requestId=582214e7-6368-4e43-b346-832c9b1b414e&view_type=register&market=es"
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                }
                            )
                        }
                    }
                }
                // Mensaje de éxito de cambio de contraseña (abajo de todo)
                if (showPasswordChanged) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(24.dp),
                            shadowElevation = 2.dp
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.logo_chef),
                                    contentDescription = null,
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Contraseña cambiada exitosamente",
                                    color = Color(0xFF4CAF50),
                                    fontFamily = Sen,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }
                // Popup de error
                if (showError) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Surface(
                            color = Color(0xFFD32F2F),
                            shape = RoundedCornerShape(8.dp),
                            shadowElevation = 4.dp
                        ) {
                            Text(
                                errorMsg,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}