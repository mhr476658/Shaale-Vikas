package com.shaalevikas.models

import com.google.firebase.Timestamp

data class Donor(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val batch: String = "",
    val pledgedAmount: Double = 0.0,
    val needId: String = "",
    val needTitle: String = "",
    val message: String = "",
    val pledgedAt: Timestamp = Timestamp.now()
)