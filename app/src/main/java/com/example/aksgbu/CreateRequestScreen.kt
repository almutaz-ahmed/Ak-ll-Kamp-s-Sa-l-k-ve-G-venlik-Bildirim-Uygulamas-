package com.example.aksgbu

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    // FORM DEĞİŞKENLERİ
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    // Tür Seçimi İçin
    val types = listOf("Arıza", "Şikayet", "Öneri", "Güvenlik", "Temizlik")
    var selectedType by remember { mutableStateOf(types[0]) }
    var expanded by remember { mutableStateOf(false) } // Menü açık mı?

    // Konum ve Fotoğraf (Simülasyon)
    var locationData by remember { mutableStateOf("") }
    var hasPhoto by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Yeni Bildirim Oluştur") },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()), // Ekran kaydırılabilir olsun
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Lütfen bildirim detaylarını doldurun.", color = Color.Gray)

            Spacer(modifier = Modifier.height(16.dp))

            // 1. TÜR SEÇİMİ (DROPDOWN)
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Bildirim Türü") },
                    trailingIcon = {
                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Seç")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    types.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(text = type) },
                            onClick = {
                                selectedType = type
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. BAŞLIK
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Konu Başlığı") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. AÇIKLAMA
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Detaylı Açıklama") },
                modifier = Modifier.fillMaxWidth().height(150.dp),
                maxLines = 10
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 4. KONUM SEÇİMİ (BUTON)
            OutlinedButton(
                onClick = {
                    locationData = "39.93, 32.85 (Cihaz Konumu)"
                    Toast.makeText(context, "Konum alındı!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = if(locationData.isNotEmpty()) Color(0xFF2E7D32) else Color.Gray)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = if (locationData.isEmpty()) "Konum Ekle (Zorunlu)" else "Konum: $locationData")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 5. FOTOĞRAF EKLEME (OPSİYONEL BUTON)
            OutlinedButton(
                onClick = {
                    hasPhoto = !hasPhoto
                    if(hasPhoto) Toast.makeText(context, "Fotoğraf eklendi", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(if(hasPhoto) Icons.Default.Check else Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = if (hasPhoto) "1 Fotoğraf Eklendi" else "Fotoğraf Ekle (İsteğe Bağlı)")
            }

            Spacer(modifier = Modifier.height(32.dp))

            // GÖNDER BUTONU
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        // FORM DOĞRULAMASI
                        if (title.isNotEmpty() && content.isNotEmpty() && locationData.isNotEmpty()) {
                            isLoading = true
                            val currentUserEmail = auth.currentUser?.email ?: "Anonim"

                            val requestData = hashMapOf(
                                "studentEmail" to currentUserEmail,
                                "type" to selectedType,   // Türü Kaydet
                                "title" to title,
                                "content" to content,
                                "location" to locationData, // Konumu Kaydet
                                "hasPhoto" to hasPhoto,     // Fotoğraf var mı?
                                "status" to "Bekliyor",
                                "followers" to listOf<String>(),
                                "date" to Timestamp.now()
                            )

                            firestore.collection("requests")
                                .add(requestData)
                                .addOnSuccessListener {
                                    isLoading = false
                                    Toast.makeText(context, "✅ Bildirim başarıyla oluşturuldu!", Toast.LENGTH_LONG).show()
                                    onNavigateBack()
                                }
                                .addOnFailureListener {
                                    isLoading = false
                                    Toast.makeText(context, "Hata: ${it.message}", Toast.LENGTH_LONG).show()
                                }
                        } else {
                            // Hata Mesajı
                            Toast.makeText(context, "⚠️ Lütfen Başlık, İçerik ve Konum alanlarını doldurun.", Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("GÖNDER", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}




/*package com.example.aksgbu


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
}*/