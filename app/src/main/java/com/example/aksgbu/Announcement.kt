package com.example.aksgbu



import com.google.firebase.Timestamp

// Veritabanından gelen duyuruyu karşılayacak şablon
data class Announcement(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val date: Timestamp? = null
)