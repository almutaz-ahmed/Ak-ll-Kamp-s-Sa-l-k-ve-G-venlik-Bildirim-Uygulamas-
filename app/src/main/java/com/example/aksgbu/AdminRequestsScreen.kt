package com.example.aksgbu

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminRequestsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit // YENİ: Detaya git fonksiyonu
) {
    val firestore = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    var requestList by remember { mutableStateOf<List<Request>>(emptyList()) }

    LaunchedEffect(Unit) {
        firestore.collection("requests")
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val list = snapshot.documents.map { doc ->
                        Request(
                            id = doc.id,
                            studentEmail = doc.getString("studentEmail") ?: "",
                            title = doc.getString("title") ?: "",
                            content = doc.getString("content") ?: "",
                            status = doc.getString("status") ?: "Bekliyor",
                            date = doc.getTimestamp("date")
                        )
                    }
                    requestList = list
                }
            }
    }

    fun deleteRequest(requestId: String) {
        firestore.collection("requests").document(requestId).delete()
            .addOnSuccessListener { Toast.makeText(context, "Talep silindi", Toast.LENGTH_SHORT).show() }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Gelen Talepler") },
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
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
            if (requestList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Henüz bir talep yok.", color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(requestList) { request ->
                        RequestItem(
                            request = request,
                            onDeleteClick = { deleteRequest(request.id) },
                            // Karta tıklanınca ID'yi alıp yola çıkıyoruz
                            onClick = { onNavigateToDetail(request.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RequestItem(request: Request, onDeleteClick: () -> Unit, onClick: () -> Unit) {
    val formattedDate = remember(request.date) {
        val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
        request.date?.toDate()?.let { sdf.format(it) } ?: "-"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() } // TIKLANABİLİR YAPTIK!
    ) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = request.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = "Gönderen: ${request.studentEmail}", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                // Durumu da gösterelim
                Text(text = "Durum: ${request.status}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = formattedDate, fontSize = 12.sp, color = Color.Gray)
            }
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Default.Delete, contentDescription = "Sil", tint = Color.Red)
            }
        }
    }
}





/*package com.example.aksgbu



import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminRequestsScreen(
    onNavigateBack: () -> Unit
) {
    val firestore = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    // Talepleri tutacak liste
    var requestList by remember { mutableStateOf<List<Request>>(emptyList()) }

    // Talepleri Çek (Realtime)
    LaunchedEffect(Unit) {
        firestore.collection("requests")
            .orderBy("date", Query.Direction.DESCENDING) // En yeni en üstte
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val list = snapshot.documents.map { doc ->
                        Request(
                            id = doc.id,
                            studentEmail = doc.getString("studentEmail") ?: "",
                            title = doc.getString("title") ?: "",
                            content = doc.getString("content") ?: "",
                            status = doc.getString("status") ?: "Bekliyor",
                            date = doc.getTimestamp("date")
                        )
                    }
                    requestList = list
                }
            }
    }

    // Silme Fonksiyonu
    fun deleteRequest(requestId: String) {
        firestore.collection("requests").document(requestId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Talep silindi", Toast.LENGTH_SHORT).show()
            }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Gelen Talepler") },
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
        ) {
            if (requestList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Henüz bir talep yok.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(requestList) { request ->
                        RequestItem(request = request, onDeleteClick = { deleteRequest(request.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun RequestItem(request: Request, onDeleteClick: () -> Unit) {
    val formattedDate = remember(request.date) {
        val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
        request.date?.toDate()?.let { sdf.format(it) } ?: "-"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)), // Hafif Sarı
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = request.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = "Gönderen: ${request.studentEmail}", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = request.content, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = formattedDate, fontSize = 12.sp, color = Color.Gray)
            }
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Default.Delete, contentDescription = "Sil", tint = Color.Red)
            }
        }
    }
}*/