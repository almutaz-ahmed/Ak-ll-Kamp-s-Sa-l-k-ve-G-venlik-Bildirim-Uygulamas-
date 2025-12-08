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
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAnnouncementScreen(
    onNavigateBack: () -> Unit // İşlem bitince geri dönmek için
) {
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Duyuru Ekle") },
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
            // Başlık Kutusu
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Duyuru Başlığı") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // İçerik Kutusu
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Duyuru İçeriği") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                maxLines = 10
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        if (title.isNotEmpty() && content.isNotEmpty()) {
                            isLoading = true

                            val announcementData = hashMapOf(
                                "title" to title,
                                "content" to content,
                                "date" to Timestamp.now()
                            )

                            firestore.collection("announcements")
                                .add(announcementData)
                                .addOnSuccessListener {
                                    isLoading = false
                                    Toast.makeText(context, "Duyuru Paylaşıldı!", Toast.LENGTH_SHORT).show()
                                    onNavigateBack()
                                }
                                .addOnFailureListener {
                                    isLoading = false
                                    Toast.makeText(context, "Hata: ${it.message}", Toast.LENGTH_LONG).show()
                                }
                        } else {
                            Toast.makeText(context, "Başlık ve içerik boş olamaz", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "PAYLAŞ", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}