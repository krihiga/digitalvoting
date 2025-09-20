package com.busyorder.digitalvoting

data class VerifyResponse(
    val ok: Boolean,
    val predicted: String?,   // Predicted voter_id from backend
    val confidence: Float?
)
