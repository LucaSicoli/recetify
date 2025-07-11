package com.example.recetify.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.recetify.R
import android.content.Intent
import android.net.Uri

private val Sen = FontFamily(
    Font(R.font.sen_regular, weight = FontWeight.Normal),
    Font(R.font.sen_bold,    weight = FontWeight.Bold)
)

@Composable
fun ForgotPasswordScreen(
    viewModel: PasswordResetViewModel = viewModel(),
    onNext: () -> Unit,
    onRegister: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        // — PARTE SUPERIOR OSCURA —
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(Color(0xFF0D0B1F)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_chef),
                    contentDescription = "Logo Recetify",
                    modifier = Modifier.size(100.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "OLVIDASTE LA CONTRASEÑA",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = Sen
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Ups… eso le pasa hasta a los mejores",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontFamily = Sen
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
                // Campo de email (single line para que no "brinque" al escribir)
                OutlinedTextField(
                    value = state.email,
                    onValueChange = viewModel::onEmailChange,
                    singleLine = true,
                    label = { Text("Mail", fontFamily = Sen) },
                    placeholder = {
                        Text(
                            text = "example@gmail.com",
                            color = Color.Black.copy(alpha = 0.5f),
                            fontFamily = Sen
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.MailOutline,
                            contentDescription = null,
                            tint = Color.Black.copy(alpha = 0.6f)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    textStyle = TextStyle(
                        color = Color.Black,
                        fontFamily = Sen,
                        fontSize = 16.sp
                    )
                )

                // Mostrar mensaje de error personalizado si hay error
                if (state.error != null) {
                    Text(
                        text = "El mail ingresado está mal escrito o no existe",
                        color = Color.Red,
                        fontFamily = Sen,
                        fontSize = 14.sp,
                        modifier = Modifier.align(Alignment.Start).padding(top = 4.dp)
                    )
                }

                // empujamos el botón hacia abajo
                Spacer(modifier = Modifier.weight(0.6f))

                // Botón "ENVIAR CÓDIGO"
                Button(
                    onClick = { viewModel.requestReset(onNext) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xCCBC6154))
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "ENVIAR CÓDIGO",
                            color = Color.White,
                            fontFamily = Sen
                        )
                    }
                }

                // un pequeño espacio antes del texto de registro
                Spacer(modifier = Modifier.height(8.dp))

                // Texto "¿No tenés una cuenta? REGISTRATE"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "¿No tenés una cuenta? ",
                        fontFamily = Sen,
                        fontSize = 14.sp,
                        color = Color(0xFF555555)
                    )
                    Text(
                        text = "REGISTRATE",
                        fontFamily = Sen,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xCCBC6154),
                        modifier = Modifier.clickable {
                            val url = "https://eu.login.vorwerk.com/ciam/register?ui_locales=es-ES&requestId=582214e7-6368-4e43-b346-832c9b1b414e&view_type=register&market=es"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        }
                    )
                }

                // y algo de padding final para que no quede pegado al borde
                Spacer(modifier = Modifier.height(90.dp))
            }
        }
    }
}
