package com.example.aksgbu

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun SignUpScreen(
    onNavigateToLogin: () -> Unit // Giriş ekranına dönmek için yol
) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Kayıt Ol", fontSize = 30.sp, style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Ad Soyad") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("E-posta") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Şifre") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = department, onValueChange = { department = it }, label = { Text("Birim") }, modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    if (email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty()) {
                        isLoading = true
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnSuccessListener { authResult ->
                                val uid = authResult.user?.uid
                                if (uid != null) {
                                    val newUser = User(uid, name, email, "User", department)
                                    firestore.collection("users").document(uid).set(newUser)
                                        .addOnSuccessListener {
                                            isLoading = false
                                            Toast.makeText(context, "Kayıt Başarılı!", Toast.LENGTH_SHORT).show()
                                            onNavigateToLogin() // Başarılı olunca giriş ekranına at
                                        }
                                }
                            }
                            .addOnFailureListener {
                                isLoading = false
                                Toast.makeText(context, "Hata: ${it.message}", Toast.LENGTH_LONG).show()
                            }
                    } else {
                        Toast.makeText(context, "Tüm alanları doldurun", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Kayıt Ol")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Giriş Ekranına Dönüş Linki
        Text(
            text = "Zaten hesabın var mı? Giriş Yap",
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable {
                onNavigateToLogin()
            }
        )
    }
}