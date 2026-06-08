package com.shaalevikas.models

import com.google.firebase.Timestamp

data class SchoolNeed(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val costEstimate: Double = 0.0,
    val amountCollected: Double = 0.0,
    val targetAmount: Double = 0.0,
    val status: NeedStatus = NeedStatus.PENDING,
    val imageUrl: String = "",
    val beforeImageUrl: String = "",
    val afterImageUrl: String = "",
    val headmasterId: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
) {
    val progressPercentage: Int
        get() = if (targetAmount > 0) ((amountCollected / targetAmount) * 100).toInt() else 0
    
    val remainingAmount: Double
        get() = targetAmount - amountCollected
}

enum class NeedStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED
}