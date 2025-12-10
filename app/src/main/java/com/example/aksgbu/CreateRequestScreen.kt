package com.example.aksgbu


import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRequestScreen(
    onNavigateBack: () -> Unit // Gönderince veya iptal edince geri dönmek için
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    // Ekranda yazılanları tutacak değişkenler
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Talep Oluştur") },
                navigationIcon = {
                    IconButton(onClick = { onNavigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Yönetime bir istek, öneri veya şikayet bildirin.",
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Konu Başlığı
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Konu Başlığı") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Detaylı Açıklama
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Detaylı Açıklama") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp), // Kutuyu büyük yaptık
                maxLines = 10
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        // Boş değilse gönder
                        if (title.isNotEmpty() && content.isNotEmpty()) {
                            isLoading = true
                            val currentUserEmail = auth.currentUser?.email ?: "Anonim"

                            // Veritabanına gidecek paket
                            val requestData = hashMapOf(
                                "studentEmail" to currentUserEmail,
                                "title" to title,
                                "content" to content,
                                "status" to "Bekliyor", // Varsayılan durum
                                "date" to Timestamp.now()
                            )

                            // 'requests' tablosuna ekle
                            firestore.collection("requests")
                                .add(requestData)
                                .addOnSuccessListener {
                                    isLoading = false
                                    Toast.makeText(context, "Talebiniz İletildi!", Toast.LENGTH_SHORT).show()
                                    onNavigateBack() // Başarılı olunca ana sayfaya dön
                                }
                                .addOnFailureListener {
                                    isLoading = false
                                    Toast.makeText(context, "Hata: ${it.message}", Toast.LENGTH_LONG).show()
                                }
                        } else {
                            Toast.makeText(context, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("GÖNDER", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}