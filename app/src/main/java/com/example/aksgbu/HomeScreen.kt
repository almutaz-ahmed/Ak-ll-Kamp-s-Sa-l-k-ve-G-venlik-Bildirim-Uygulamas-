package com.example.aksgbu



import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userRole: String, // Admin mi User mı olduğu bilgisi buraya gelecek
    onLogout: () -> Unit // Çıkış yapma fonksiyonu
) {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Ana Sayfa", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    // Çıkış Yap Butonu (Sağ Üstte)
                    IconButton(onClick = {
                        auth.signOut() // Firebase'den çıkış yap
                        onLogout()     // Giriş ekranına yönlendir
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Çıkış Yap")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Profil İkonu
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Merhaba!", fontSize = 28.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "Giriş Yapan Hesap:", fontSize = 14.sp, color = Color.Gray)
            Text(text = currentUser?.email ?: "Bilinmiyor", fontSize = 18.sp, fontWeight = FontWeight.Medium)

            Spacer(modifier = Modifier.height(32.dp))

            // ROL KONTROLÜ: Eğer Admin ise bunu göster
            if (userRole == "Admin") {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFD7D7)), // Kırmızımsı
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "YÖNETİCİ PANELİ", style = MaterialTheme.typography.titleLarge, color = Color.Red, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Duyuru eklemek için tıklayın (Yakında)", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else {
                // User (Öğrenci) ise bunu göster
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE7FFD7)), // Yeşilimsi
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "ÖĞRENCİ PANELİ", style = MaterialTheme.typography.titleLarge, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Duyuruları görüntülemek için bekleyin.", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}