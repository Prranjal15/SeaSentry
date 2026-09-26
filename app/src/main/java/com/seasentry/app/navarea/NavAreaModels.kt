package com.seasentry.app.navarea

data class NavigationalWarning(
    val id: String,
    val title: String,
    val text: String,
    val area: String,
    val latitude: Double,
    val longitude: Double,
    val authority: String,
    val issuedAt: Long
)
