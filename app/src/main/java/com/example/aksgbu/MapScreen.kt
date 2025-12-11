package com.example.aksgbu

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
}

