package com.example.aksgbu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.aksgbu.ui.theme.AKSGBUTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.runtime.*

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
                        val navController = rememberNavController()
                        val auth = FirebaseAuth.getInstance()
                        val firestore = FirebaseFirestore.getInstance()

                        // Başlangıç Rota Kontrolü (Yükleniyor ekranı için state)
                        var startDestination by remember { mutableStateOf("splash") }

                        // Uygulama açılınca kontrol et
                        LaunchedEffect(Unit) {
                            val currentUser = auth.currentUser
                            if (currentUser != null) {
                                // Kullanıcı var, rolünü çekip yönlendir
                                firestore.collection("users").document(currentUser.uid).get()
                                    .addOnSuccessListener { document ->
                                        val role = document.getString("role") ?: "User"
                                        startDestination = "home/$role"
                                    }
                                    .addOnFailureListener {
                                        // Hata olursa güvenli şekilde login'e at
                                        startDestination = "login"
                                    }
                            } else {
                                // Kullanıcı yok, login'e git
                                startDestination = "login"
                            }
                        }

                        // Henüz karar verilmediyse (Veri çekiliyorsa) boş veya loading göster
                        if (startDestination == "splash") {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                CircularProgressIndicator()
                            }
                        } else {
                            // Karar verildi, Navigasyonu başlat
                            NavHost(navController = navController, startDestination = startDestination) {

                                // 1. GİRİŞ EKRANI
                                composable("login") {
                                    LoginScreen(
                                        onNavigateToSignUp = {
                                            navController.navigate("signup")
                                        },
                                        onLoginSuccess = {
                                            // Giriş başarılı olunca rolü çekip git
                                            val uid = auth.currentUser?.uid
                                            if (uid != null) {
                                                firestore.collection("users").document(uid).get()
                                                    .addOnSuccessListener { document ->
                                                        val role = document.getString("role") ?: "User"
                                                        navController.navigate("home/$role") {
                                                            popUpTo("login") { inclusive = true }
                                                        }
                                                    }
                                            }
                                        }
                                    )
                                }

                                // 2. KAYIT EKRANI
                                composable("signup") {
                                    SignUpScreen(
                                        onNavigateToLogin = {
                                            navController.popBackStack()
                                        }
                                    )
                                }

                                // 3. ANA SAYFA (Home)
                                composable(
                                    route = "home/{role}",
                                    arguments = listOf(navArgument("role") { type = NavType.StringType })
                                ) { backStackEntry ->
                                    val userRole = backStackEntry.arguments?.getString("role") ?: "User"

                                    HomeScreen(
                                        userRole = userRole,
                                        onLogout = {
                                            // Çıkış yapınca Login'e dön ve geçmişi temizle
                                            navController.navigate("login") {
                                                popUpTo(0) { inclusive = true }
                                            }
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
}
