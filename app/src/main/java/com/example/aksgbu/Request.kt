package com.example.aksgbu



import com.google.firebase.Timestamp

data class Request(
    val id: String = "",
    val studentEmail: String = "",   // Hangi öğrenci gönderdi?
    val title: String = "",          // Konu
    val content: String = "",        // Şikayet/İstek içeriği
    val status: String = "Bekliyor", // Durumu (Bekliyor / Çözüldü)
    val date: Timestamp? = null      // Ne zaman gönderildi?
)