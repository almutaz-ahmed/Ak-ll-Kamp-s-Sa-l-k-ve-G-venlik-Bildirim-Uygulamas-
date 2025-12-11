package com.example.aksgbu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

// Pin Modeli (Güncellendi)
data class MapPin(
    val title: String,
    val type: String,
    val timeAgo: String, // YENİ: Ne kadar önce?
    val position: LatLng
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String, String, String) -> Unit // Detay sayfasına veri gönderme
) {
    // Örnek Veriler
    val campusLocations = remember {
        listOf(
            MapPin("Büyük Sınav", "Duyuru", "2 saat önce", LatLng(39.93, 32.85)),
            MapPin("Bahar Şenliği", "Etkinlik", "1 gün önce", LatLng(39.94, 32.86)),
            MapPin("Kütüphane Dolu", "Bilgi", "15 dk önce", LatLng(39.935, 32.855)),
            MapPin("Rektörlük", "Bilgi", "3 gün önce", LatLng(39.925, 32.845))
        )
    }

    // Seçilen Pini Tutmak İçin (Kartı göstermek için)
    var selectedPin by remember { mutableStateOf<MapPin?>(null) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(39.93, 32.85), 13f)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Kampüs Haritası") },
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
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {

            // 1. HARİTA
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                onMapClick = { selectedPin = null } // Boşluğa tıklayınca kartı kapat
            ) {
                campusLocations.forEach { pin ->
                    val pinColor = when (pin.type) {
                        "Duyuru" -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                        "Etkinlik" -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                        else -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                    }

                    Marker(
                        state = MarkerState(position = pin.position),
                        title = pin.title,
                        icon = pinColor,
                        onClick = {
                            selectedPin = pin // Tıklanan pini seç
                            true // Kamerayı otomatik oynatma
                        }
                    )
                }
            }

            // 2. BİLGİ KARTI (Pin Tıklanınca Altta Çıkar)
            if (selectedPin != null) {
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(8.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Kapatma İkonu ve Başlık
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedPin!!.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { selectedPin = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Kapat")
                            }
                        }

                        // Tür ve Zaman
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Tür: ${selectedPin!!.type}", color = if(selectedPin!!.type=="Duyuru") Color.Red else Color.Blue, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(text = "🕒 ${selectedPin!!.timeAgo}", color = Color.Gray)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // DETAYI GÖR BUTONU
                        Button(
                            onClick = {
                                // Detay sayfasına git (Verileri taşı)
                                onNavigateToDetail(selectedPin!!.title, selectedPin!!.type, selectedPin!!.timeAgo)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("DETAYI GÖR")
                        }
                    }
                }
            }
        }
    }
}






/*package com.example.aksgbu

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory

// Harita Pini Modeli
data class MapPin(
    val title: String,
    val type: String, // "Duyuru", "Etkinlik", "Bilgi"
    val position: LatLng
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onNavigateBack: () -> Unit
) {
    // KAMPÜS KONUMLARI (Örnek Koordinatlar)
    val campusLocations = remember {
        listOf(
            MapPin("Büyük Sınav", "Duyuru", LatLng(39.93, 32.85)),      // Kırmızı
            MapPin("Bahar Şenliği", "Etkinlik", LatLng(39.94, 32.86)),  // Mavi
            MapPin("Kütüphane", "Bilgi", LatLng(39.935, 32.855)),       // Yeşil
            MapPin("Rektörlük", "Bilgi", LatLng(39.925, 32.845))        // Yeşil
        )
    }

    // Harita Başlangıç Kamerası
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(39.93, 32.85), 13f)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Kampüs Haritası") },
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
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                // Pinleri Haritaya Yerleştir
                campusLocations.forEach { pin ->
                    // Türüne Göre Renk Seçimi
                    val pinColor = when (pin.type) {
                        "Duyuru" -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                        "Etkinlik" -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                        else -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                    }

                    Marker(
                        state = MarkerState(position = pin.position),
                        title = pin.title,
                        snippet = "${pin.type} - Detay için tıkla",
                        icon = pinColor
                    )
                }
            }
        }
    }
}*/

