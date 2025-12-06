package com.example.aksgbu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.aksgbu.ui.theme.AKSGBUTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AKSGBUTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Surface(
                        modifier = Modifier.fillMaxSize().padding(innerPadding)
                    ) {
                        // 1. Navigasyon Yöneticisi
                        val navController = rememberNavController()

                        // 2. Ekranlar Arası Harita (Başlangıç: Login)
                        NavHost(navController = navController, startDestination = "login") {

                            // Giriş Ekranı Rotası
                            composable("login") {
                                LoginScreen(
                                    onNavigateToSignUp = {
                                        navController.navigate("signup")
                                    },
                                    onLoginSuccess = {
                                        // Giriş başarılı olunca yapılacaklar (Sonra ekleyeceğiz)
                                    }
                                )
                            }

                            // Kayıt Ekranı Rotası
                            composable("signup") {
                                SignUpScreen(
                                    onNavigateToLogin = {
                                        navController.popBackStack()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}