package com.example.aksgbu

// Firestore'a kaydedilecek kullanıcı şablonu
data class User(
    val id: String = "",            // Firebase Authentication UID'si
    val name: String = "",          // Ad Soyad [cite: 30]
    val email: String = "",         // E-posta [cite: 30]
    val role: String = "User",      // Rol: "User" veya "Admin" [cite: 9, 31]
    val department: String = ""     // Birim Bilgisi [cite: 30]
)