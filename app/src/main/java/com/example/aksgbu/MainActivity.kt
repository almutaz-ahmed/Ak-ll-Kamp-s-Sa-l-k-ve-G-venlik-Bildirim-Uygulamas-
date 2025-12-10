package com.example.aksgbu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.aksgbu.ui.theme.AKSGBUTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.runtime.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AKSGBUTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Surface(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                        val navController = rememberNavController()
                        val auth = FirebaseAuth.getInstance()
                        val firestore = FirebaseFirestore.getInstance()

                        var startDestination by remember { mutableStateOf("splash") }

                        LaunchedEffect(Unit) {
                            val currentUser = auth.currentUser
                            if (currentUser != null) {
                                firestore.collection("users").document(currentUser.uid).get()
                                    .addOnSuccessListener { document ->
                                        val role = document.getString("role") ?: "User"
                                        startDestination = "home/$role"
                                    }
                                    .addOnFailureListener { startDestination = "login" }
                            } else {
                                startDestination = "login"
                            }
                        }

                        if (startDestination == "splash") {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                CircularProgressIndicator()
                            }
                        } else {
                            NavHost(navController = navController, startDestination = startDestination) {

                                // 1. GİRİŞ
                                composable("login") {
                                    LoginScreen(
                                        onNavigateToSignUp = { navController.navigate("signup") },
                                        onLoginSuccess = {
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

                                // 2. KAYIT
                                composable("signup") {
                                    SignUpScreen(onNavigateToLogin = { navController.popBackStack() })
                                }

                                // 3. ANA SAYFA
                                composable(
                                    route = "home/{role}",
                                    arguments = listOf(navArgument("role") { type = NavType.StringType })
                                ) { backStackEntry ->
                                    val userRole = backStackEntry.arguments?.getString("role") ?: "User"

                                    HomeScreen(
                                        userRole = userRole,
                                        onLogout = {
                                            navController.navigate("login") { popUpTo(0) { inclusive = true } }
                                        },
                                        onNavigateToAddAnnouncement = { navController.navigate("add_announcement") },
                                        onNavigateToCreateRequest = { navController.navigate("create_request") },
                                        // İŞTE YENİ BAĞLANTI (Admin için):
                                        onNavigateToAdminRequests = { navController.navigate("admin_requests") }
                                    )
                                }

                                // 4. DUYURU EKLE
                                composable("add_announcement") {
                                    AddAnnouncementScreen(onNavigateBack = { navController.popBackStack() })
                                }

                                // 5. TALEP OLUŞTUR
                                composable("create_request") {
                                    CreateRequestScreen(onNavigateBack = { navController.popBackStack() })
                                }

                                // 6. GELEN TALEPLERİ GÖR (Admin İçin) -> YENİ ROTA
                                composable("admin_requests") {
                                    AdminRequestsScreen(onNavigateBack = { navController.popBackStack() })
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}



/*package com.example.aksgbu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.aksgbu.ui.theme.AKSGBUTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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

                        var startDestination by remember { mutableStateOf("splash") }

                        LaunchedEffect(Unit) {
                            val currentUser = auth.currentUser
                            if (currentUser != null) {
                                firestore.collection("users").document(currentUser.uid).get()
                                    .addOnSuccessListener { document ->
                                        val role = document.getString("role") ?: "User"
                                        startDestination = "home/$role"
                                    }
                                    .addOnFailureListener {
                                        startDestination = "login"
                                    }
                            } else {
                                startDestination = "login"
                            }
                        }

                        if (startDestination == "splash") {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                CircularProgressIndicator()
                            }
                        } else {
                            NavHost(navController = navController, startDestination = startDestination) {

                                // 1. GİRİŞ
                                composable("login") {
                                    LoginScreen(
                                        onNavigateToSignUp = { navController.navigate("signup") },
                                        onLoginSuccess = {
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

                                // 2. KAYIT
                                composable("signup") {
                                    SignUpScreen(
                                        onNavigateToLogin = { navController.popBackStack() }
                                    )
                                }

                                // 3. ANA SAYFA
                                composable(
                                    route = "home/{role}",
                                    arguments = listOf(navArgument("role") { type = NavType.StringType })
                                ) { backStackEntry ->
                                    val userRole = backStackEntry.arguments?.getString("role") ?: "User"

                                    HomeScreen(
                                        userRole = userRole,
                                        onLogout = {
                                            navController.navigate("login") {
                                                popUpTo(0) { inclusive = true }
                                            }
                                        },
                                        // İŞTE YENİ EKLENEN KISIM BURASI:
                                        onNavigateToAddAnnouncement = {
                                            navController.navigate("add_announcement")
                                        }
                                    )
                                }

                                // 4. DUYURU EKLEME EKRANI (YENİ ROTA)
                                composable("add_announcement") {
                                    AddAnnouncementScreen(
                                        onNavigateBack = {
                                            navController.popBackStack() // İşlem bitince geri dön
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


*/
