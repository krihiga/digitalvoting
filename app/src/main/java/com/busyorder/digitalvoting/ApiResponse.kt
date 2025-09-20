package com.busyorder.digitalvoting

data class ApiResponse(
    val ok: Boolean,
    val message: String? = null,
    val error: String? = null,
    val vote_hash: String? = null,
    val predicted: String? = null,
    val confidence: Double? = null
)
