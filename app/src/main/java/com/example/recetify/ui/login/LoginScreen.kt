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

private val Sen = FontFamily(
    Font(R.font.sen_regular, weight = FontWeight.Normal),
    Font(R.font.sen_bold,    weight = FontWeight.Bold)
)

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onLoginSuccess: (token: String) -> Unit,
    onForgot: () -> Unit
) {
    val context = LocalContext.current
    val prefs   = remember { UserPreferences(context) }
    val scope   = rememberCoroutineScope()

    val state by viewModel.state.collectAsState()

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
            SessionManager.setAlumno(context, token)

            // 3) Navegar a Home
            onLoginSuccess(token)
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

        // ── PARTE INFERIOR BLANCA ───────────────────────────────────────────────
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
                // Alias
                OutlinedTextField(
                    value = state.alias,
                    onValueChange = viewModel::onAliasChanged,
                    label = { Text("Alias") },
                    placeholder = { Text("Tu alias") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = TextStyle(color = Color.Black, fontSize = 16.sp)
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
                    textStyle = TextStyle(color = Color.Black, fontSize = 16.sp)
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
                    }
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
                            onLoginSuccess("")
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
                            modifier = Modifier.clickable { /* navegar a registro */ }
                        )
                    }
                }
            }
        }
    }
}