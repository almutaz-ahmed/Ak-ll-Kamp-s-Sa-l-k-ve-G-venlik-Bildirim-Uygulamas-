package com.example.aksgbu

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userRole: String,
    onLogout: () -> Unit,
    onNavigateToAddAnnouncement: () -> Unit,
    onNavigateToCreateRequest: () -> Unit,
    onNavigateToAdminRequests: () -> Unit, // Talepleri görme sayfasına git
    onNavigateToMap: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser
    val context = LocalContext.current

    var announcementList by remember { mutableStateOf<List<Announcement>>(emptyList()) }

    fun deleteAnnouncement(announcementId: String) {
        firestore.collection("announcements").document(announcementId)
            .delete()
            .addOnSuccessListener { Toast.makeText(context, "Duyuru silindi", Toast.LENGTH_SHORT).show() }
    }

    LaunchedEffect(Unit) {
        firestore.collection("announcements")
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val list = snapshot.documents.map { doc ->
                        Announcement(
                            id = doc.id,
                            title = doc.getString("title") ?: "",
                            content = doc.getString("content") ?: "",
                            date = doc.getTimestamp("date")
                        )
                    }
                    announcementList = list
                }
            }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Ana Sayfa", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    IconButton(onClick = {
                        auth.signOut()
                        onLogout()
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profil
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(50.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = "Merhaba,", fontSize = 16.sp, color = Color.Gray)
                    Text(text = currentUser?.email ?: "", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ROL KONTROLÜ (GÜNCELLENDİ)
            if (userRole == "Admin") {
                // Admin Butonları (Kırmızı/Sarı)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFD7D7)),
                        modifier = Modifier.weight(1f).height(100.dp).clickable { onNavigateToAddAnnouncement() },
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = "DUYURU\nEKLE", color = Color.Red, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        }
                    }
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                        modifier = Modifier.weight(1f).height(100.dp).clickable { onNavigateToAdminRequests() },
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = "TALEPLERİ\nGÖR", color = Color(0xFFF57F17), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        }
                    }
                }
            } else {
                // ÖĞRENCİ PANELİ (GÜNCELLENDİ: ARTIK 2 BUTON VAR)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    // 1. Talep Oluştur
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE7FFD7)),
                        modifier = Modifier.weight(1f).height(100.dp).clickable { onNavigateToCreateRequest() },
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = "TALEP\nOLUŞTUR", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        }
                    }
                    // 2. Talepleri Gör (LİSTEYE GİDER)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE1F5FE)), // Açık Mavi
                        modifier = Modifier.weight(1f).height(100.dp).clickable { onNavigateToAdminRequests() },
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = "TALEPLERİ\nLİSTELE", color = Color(0xFF0277BD), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Harita Butonu
            Button(
                onClick = { onNavigateToMap() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("KAMPÜS HARİTASINI GÖR")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "📢 GÜNCEL DUYURULAR", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(announcementList) { announcement ->
                    AnnouncementItem(
                        announcement = announcement,
                        isAdmin = (userRole == "Admin"),
                        onDeleteClick = { deleteAnnouncement(announcement.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun AnnouncementItem(announcement: Announcement, isAdmin: Boolean, onDeleteClick: () -> Unit) {
    val formattedDate = remember(announcement.date) {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        announcement.date?.toDate()?.let { sdf.format(it) } ?: "Tarih Yok"
    }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = announcement.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = announcement.content, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = formattedDate, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            if (isAdmin) {
                IconButton(onClick = onDeleteClick) { Icon(Icons.Default.Delete, contentDescription = "Sil", tint = Color.Red) }
            }
        }
    }
}






/*package com.example.aksgbu

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userRole: String,
    onLogout: () -> Unit,
    onNavigateToAddAnnouncement: () -> Unit,
    onNavigateToCreateRequest: () -> Unit,
    onNavigateToAdminRequests: () -> Unit,
    onNavigateToMap: () -> Unit // YENİ: Haritaya gitme emri
) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser
    val context = LocalContext.current

    var announcementList by remember { mutableStateOf<List<Announcement>>(emptyList()) }

    fun deleteAnnouncement(announcementId: String) {
        firestore.collection("announcements").document(announcementId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Duyuru silindi", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Silinemedi: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    LaunchedEffect(Unit) {
        firestore.collection("announcements")
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val list = snapshot.documents.map { doc ->
                        Announcement(
                            id = doc.id,
                            title = doc.getString("title") ?: "",
                            content = doc.getString("content") ?: "",
                            date = doc.getTimestamp("date")
                        )
                    }
                    announcementList = list
                }
            }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Ana Sayfa", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    IconButton(onClick = {
                        auth.signOut()
                        onLogout()
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(50.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = "Merhaba,", fontSize = 16.sp, color = Color.Gray)
                    Text(text = currentUser?.email ?: "", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (userRole == "Admin") {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFD7D7)),
                        modifier = Modifier.weight(1f).height(100.dp).clickable { onNavigateToAddAnnouncement() },
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = "DUYURU\nEKLE", color = Color.Red, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        }
                    }
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                        modifier = Modifier.weight(1f).height(100.dp).clickable { onNavigateToAdminRequests() },
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = "TALEPLERİ\nGÖR", color = Color(0xFFF57F17), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        }
                    }
                }
            } else {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE7FFD7)),
                    modifier = Modifier.fillMaxWidth().height(100.dp).clickable { onNavigateToCreateRequest() },
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "TALEP / ŞİKAYET OLUŞTUR", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- YENİ EKLENEN HARİTA BUTONU ---
            Button(
                onClick = { onNavigateToMap() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("KAMPÜS HARİTASINI GÖR")
            }
            // ----------------------------------

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "📢 GÜNCEL DUYURULAR", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(announcementList) { announcement ->
                    AnnouncementItem(
                        announcement = announcement,
                        isAdmin = (userRole == "Admin"),
                        onDeleteClick = { deleteAnnouncement(announcement.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun AnnouncementItem(announcement: Announcement, isAdmin: Boolean, onDeleteClick: () -> Unit) {
    val formattedDate = remember(announcement.date) {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        announcement.date?.toDate()?.let { sdf.format(it) } ?: "Tarih Yok"
    }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = announcement.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = announcement.content, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = formattedDate, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            if (isAdmin) {
                IconButton(onClick = onDeleteClick) { Icon(Icons.Default.Delete, contentDescription = "Sil", tint = Color.Red) }
            }
        }
    }
}
*/


/*package com.example.aksgbu

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userRole: String,
    onLogout: () -> Unit,
    onNavigateToAddAnnouncement: () -> Unit,
    onNavigateToCreateRequest: () -> Unit,
    onNavigateToAdminRequests: () -> Unit // YENİ: Admin talep ekranına gitme emri
) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser
    val context = LocalContext.current

    var announcementList by remember { mutableStateOf<List<Announcement>>(emptyList()) }

    fun deleteAnnouncement(announcementId: String) {
        firestore.collection("announcements").document(announcementId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Duyuru silindi", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Silinemedi: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    LaunchedEffect(Unit) {
        firestore.collection("announcements")
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val list = snapshot.documents.map { doc ->
                        Announcement(
                            id = doc.id,
                            title = doc.getString("title") ?: "",
                            content = doc.getString("content") ?: "",
                            date = doc.getTimestamp("date")
                        )
                    }
                    announcementList = list
                }
            }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Ana Sayfa", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    IconButton(onClick = {
                        auth.signOut()
                        onLogout()
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(50.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = "Merhaba,", fontSize = 16.sp, color = Color.Gray)
                    Text(text = currentUser?.email ?: "", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (userRole == "Admin") {
                // Admin Paneli (İKİ BUTONLU)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    // 1. Kutu: Duyuru Ekle
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFD7D7)),
                        modifier = Modifier
                            .weight(1f)
                            .height(120.dp)
                            .clickable { onNavigateToAddAnnouncement() },
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = "DUYURU\nEKLE", color = Color.Red, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        }
                    }

                    // 2. Kutu: Talepleri Gör
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)), // Sarımsı
                        modifier = Modifier
                            .weight(1f)
                            .height(120.dp)
                            .clickable { onNavigateToAdminRequests() }, // YENİ EKRANA GİDER
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = "TALEPLERİ\nGÖR", color = Color(0xFFF57F17), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        }
                    }
                }
            } else {
                // ÖĞRENCİ PANELİ
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE7FFD7)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clickable { onNavigateToCreateRequest() },
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "TALEP / ŞİKAYET OLUŞTUR", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(text = "(Tıklayın)", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(text = "📢 GÜNCEL DUYURULAR", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(announcementList) { announcement ->
                    AnnouncementItem(
                        announcement = announcement,
                        isAdmin = (userRole == "Admin"),
                        onDeleteClick = { deleteAnnouncement(announcement.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun AnnouncementItem(
    announcement: Announcement,
    isAdmin: Boolean,
    onDeleteClick: () -> Unit
) {
    val formattedDate = remember(announcement.date) {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        announcement.date?.toDate()?.let { sdf.format(it) } ?: "Tarih Yok"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = announcement.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = announcement.content, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = formattedDate, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            if (isAdmin) {
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Default.Delete, contentDescription = "Sil", tint = Color.Red)
                }
            }
        }
    }
}


package com.example.aksgbu

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userRole: String,
    onLogout: () -> Unit,
    onNavigateToAddAnnouncement: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser
    val context = LocalContext.current

    // Duyuruları tutacak listemiz
    var announcementList by remember { mutableStateOf<List<Announcement>>(emptyList()) }

    // Silme Fonksiyonu
    fun deleteAnnouncement(announcementId: String) {
        firestore.collection("announcements").document(announcementId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Duyuru silindi", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Silinemedi: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Verileri Çekme (Realtime)
    LaunchedEffect(Unit) {
        firestore.collection("announcements")
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val list = snapshot.documents.map { doc ->
                        Announcement(
                            id = doc.id,
                            title = doc.getString("title") ?: "",
                            content = doc.getString("content") ?: "",
                            date = doc.getTimestamp("date")
                        )
                    }
                    announcementList = list
                }
            }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Ana Sayfa", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    IconButton(onClick = {
                        auth.signOut()
                        onLogout()
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profil Kısmı
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(50.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = "Merhaba,", fontSize = 16.sp, color = Color.Gray)
                    Text(text = currentUser?.email ?: "", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ROL KONTROLÜ
            if (userRole == "Admin") {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFD7D7)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clickable { onNavigateToAddAnnouncement() },
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "+ YENİ DUYURU EKLE", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
            } else {
                Text(text = "📢 GÜNCEL DUYURULAR", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // DUYURU LİSTESİ
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(announcementList) { announcement ->
                    AnnouncementItem(
                        announcement = announcement,
                        isAdmin = (userRole == "Admin"), // Admin mi bilgisini gönderiyoruz
                        onDeleteClick = { deleteAnnouncement(announcement.id) } // Silme emrini gönderiyoruz
                    )
                }
            }
        }
    }
}

@Composable
fun AnnouncementItem(
    announcement: Announcement,
    isAdmin: Boolean,
    onDeleteClick: () -> Unit
) {
    val formattedDate = remember(announcement.date) {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        announcement.date?.toDate()?.let { sdf.format(it) } ?: "Tarih Yok"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sol Taraf: Yazılar
            Column(modifier = Modifier.weight(1f)) {
                Text(text = announcement.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = announcement.content, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = formattedDate, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }

            // Sağ Taraf: Silme Butonu (Sadece Admin görür)
            if (isAdmin) {
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Default.Delete, contentDescription = "Sil", tint = Color.Red)
                }
            }
        }
    }
}


package com.example.aksgbu

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userRole: String,
    onLogout: () -> Unit,
    onNavigateToAddAnnouncement: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser

    // Duyuruları tutacak listemiz
    var announcementList by remember { mutableStateOf<List<Announcement>>(emptyList()) }

    // Ekran açılınca verileri çek (Realtime - Canlı)
    LaunchedEffect(Unit) {
        firestore.collection("announcements")
            .orderBy("date", Query.Direction.DESCENDING) // En yenisi en üstte
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val list = snapshot.documents.map { doc ->
                        Announcement(
                            id = doc.id,
                            title = doc.getString("title") ?: "",
                            content = doc.getString("content") ?: "",
                            date = doc.getTimestamp("date")
                        )
                    }
                    announcementList = list
                }
            }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Ana Sayfa", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    IconButton(onClick = {
                        auth.signOut()
                        onLogout()
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profil Kısmı
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(50.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = "Merhaba,", fontSize = 16.sp, color = Color.Gray)
                    Text(text = currentUser?.email ?: "", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ROL KONTROLÜ
            if (userRole == "Admin") {
                // Admin Paneli Butonu
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFD7D7)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clickable { onNavigateToAddAnnouncement() },
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "+ YENİ DUYURU EKLE", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
            } else {
                // Öğrenci Başlığı
                Text(text = "📢 GÜNCEL DUYURULAR", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // DUYURU LİSTESİ (HERKES GÖREBİLİR)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(announcementList) { announcement ->
                    AnnouncementItem(announcement)
                }
            }
        }
    }
}

// Duyuru Kartı Tasarımı
@Composable
fun AnnouncementItem(announcement: Announcement) {
    // Tarihi güzel formatta göstermek için
    val formattedDate = remember(announcement.date) {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        announcement.date?.toDate()?.let { sdf.format(it) } ?: "Tarih Yok"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = announcement.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = announcement.content, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = formattedDate, style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.align(Alignment.End))
        }
    }
}
*/











