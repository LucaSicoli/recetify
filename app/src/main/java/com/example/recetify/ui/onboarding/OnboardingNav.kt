package com.example.recetify.ui.onboarding

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.NavHost


import com.example.recetify.ui.onboarding.Start01Screen
import com.example.recetify.ui.onboarding.Start02Screen
import com.example.recetify.ui.onboarding.Start03Screen

@Composable
fun OnboardingNav(navController: NavHostController) {
    NavHost(navController, startDestination = "start01") {
        composable("start01") { Start01Screen(navController) }
        composable("start02") { Start02Screen(navController) }
        composable("start03") { Start03Screen(navController) }
    }
}