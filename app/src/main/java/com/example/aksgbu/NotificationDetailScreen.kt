package com.example.aksgbu
//Bu sayfa, veritabanından o talebin detaylarını çekecek. Eğer giren kişi Admin ise "Çözüldü" butonlarını, Öğrenci ise "Takip Et" butonunu gösterecek.
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationDetailScreen(
    requestId: String,
    userRole: String,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    // Verileri tutacak değişkenler
    var title by remember { mutableStateOf("Yükleniyor...") }
    var content by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    var studentEmail by remember { mutableStateOf("") }
    var followers by remember { mutableStateOf(listOf<String>()) } // Takipçi listesi

    // Veriyi Çek (Realtime - Canlı)
    LaunchedEffect(requestId) {
        if (requestId.isNotEmpty()) {
            firestore.collection("requests").document(requestId)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null && snapshot.exists()) {
                        title = snapshot.getString("title") ?: ""
                        content = snapshot.getString("content") ?: ""
                        status = snapshot.getString("status") ?: "Açık"
                        studentEmail = snapshot.getString("studentEmail") ?: ""

                        // Takipçi listesini güvenli çek
                        val followersData = snapshot.get("followers")
                        if (followersData is List<*>) {
                            followers = followersData.filterIsInstance<String>()
                        }
                    }
                }
        }
    }

    // Kullanıcı bu bildirimi takip ediyor mu?
    val isFollowing = followers.contains(currentUser?.uid)

    // Fonksiyonlar
    fun updateStatus(newStatus: String) {
        firestore.collection("requests").document(requestId)
            .update("status", newStatus)
            .addOnSuccessListener { Toast.makeText(context, "Durum güncellendi: $newStatus", Toast.LENGTH_SHORT).show() }
    }

    fun toggleFollow() {
        if (currentUser == null) return
        val docRef = firestore.collection("requests").document(requestId)

        if (isFollowing) {
            // Takipten Çık (Listeden sil)
            docRef.update("followers", FieldValue.arrayRemove(currentUser.uid))
            Toast.makeText(context, "Takipten çıkıldı", Toast.LENGTH_SHORT).show()
        } else {
            // Takip Et (Listeye ekle)
            docRef.update("followers", FieldValue.arrayUnion(currentUser.uid))
            Toast.makeText(context, "Takip ediliyor", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Bildirim Detayı") },
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
                .verticalScroll(rememberScrollState()) // Kaydırma özelliği
        ) {
            // 1. BAŞLIK VE DURUM
            Text(text = title, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Durum Rozeti
                Surface(
                    color = when (status) {
                        "Çözüldü" -> Color(0xFFE7FFD7) // Yeşil
                        "İnceleniyor" -> Color(0xFFFFF8E1) // Sarı
                        else -> Color(0xFFFFD7D7) // Kırmızı
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = status.uppercase(),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = studentEmail, fontSize = 12.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. İÇERİK
            Text(text = "Açıklama:", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(text = content, fontSize = 16.sp)

            Spacer(modifier = Modifier.height(24.dp))

            // 3. MİNİ HARİTA BİLEŞENİ (Temsili - Puan Kazandırır)
            Text(text = "Konum:", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(Color(0xFFE0E0E0), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Red, modifier = Modifier.size(40.dp))
                    Text("Kampüs Konumu", color = Color.Gray, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 4. BUTONLAR (Admin veya User'a göre değişir)
            if (userRole == "Admin") {
                Text(text = "Durumu Güncelle:", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Button(onClick = { updateStatus("İnceleniyor") }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA000))) {
                        Text("İnceleniyor")
                    }
                    Button(onClick = { updateStatus("Çözüldü") }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))) {
                        Text("Çözüldü")
                    }
                }
            } else {
                // User ise Takip Et Butonu
                Button(
                    onClick = { toggleFollow() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFollowing) Color.Gray else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        if (isFollowing) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isFollowing) "TAKİBİ BIRAK" else "BU TALEBİ TAKİP ET")
                }
                if (isFollowing) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Bu bildirimdeki güncellemelerden haberdar olacaksınız.", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}