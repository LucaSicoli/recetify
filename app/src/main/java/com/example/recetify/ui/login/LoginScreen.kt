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
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.recetify.R

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
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.token) {
        state.token?.let(onLoginSuccess)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // PARTE SUPERIOR OSCURA
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
                    painter = painterResource(id = R.drawable.logo_chef),
                    contentDescription = "Logo Recetify",
                    modifier = Modifier.size(120.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "INICIO DE SESION",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    fontFamily = Sen
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Ingresá para descubrir las mejores recetas",
                    fontSize = 15.sp,
                    color = Color.White,
                    fontFamily = Sen
                )
            }
        }

        // PARTE INFERIOR BLANCA
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
                    textStyle = TextStyle(color = Color.Black, fontSize = 16.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
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
                                    Icons.Default.Visibility
                                else
                                    Icons.Default.VisibilityOff,
                                contentDescription = "Toggle Password Visibility"
                            )
                        }
                    }
                )

                // Recordarme
                var rememberMe by remember { mutableStateOf(false) }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { rememberMe = it }
                    )
                    Text(
                        text = "Recordarme",
                        fontSize = 14.sp,
                        color = Color(0xFF555555)
                    )
                }

                // Botón LOG IN
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
                        Text("LOG IN", color = Color.White)
                    }
                }

                // Mensaje de error
                state.error?.let { err ->
                    Text(
                        text = err,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                // Enlaces debajo del botón
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "¿Olvidaste la contraseña?",
                        fontSize = 14.sp,
                        color = Color(0xFFBC6154),
                        modifier = Modifier
                            .clickable(onClick = onForgot)
                            .padding(vertical = 4.dp)
                    )
                    Row {
                        Text(
                            text = "¿No tenés una cuenta? ",
                            fontSize = 14.sp,
                            color = Color(0xFF555555)
                        )
                        Text(
                            text = "REGÍSTRATE",
                            fontSize = 14.sp,
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
