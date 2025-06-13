package com.example.recetify.ui.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.recetify.ui.theme.Ladrillo
import com.example.recetify.ui.theme.TextoSecundario
import com.example.recetify.ui.theme.TextoTitulo
import com.example.recetify.R
@Composable
fun Start01Screen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        Image(
            painter = painterResource(id = R.drawable.onboarding1),
            contentDescription = "Imagen de bienvenida",
            modifier = Modifier
                .height(292.dp)
                .width(240.dp)
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Te damos la bienvenida",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextoTitulo,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "En RECIPEFY te ofrecemos una experiencia única para llevar tus recetas al siguiente nivel.",
                fontSize = 14.sp,
                color = TextoSecundario,
                textAlign = TextAlign.Center
            )
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { navController.navigate("start02") },
                colors = ButtonDefaults.buttonColors(containerColor = Ladrillo),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Siguiente", color = Color.White)
            }
            TextButton(
                onClick = { navController.navigate("login") },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Omitir", color = TextoSecundario)
            }
        }
    }
}
