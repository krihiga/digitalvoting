package com.busyorder.digitalvoting

data class ResultsResponse(
    val ok: Boolean,
    val results: Map<String, Int>?,
    val error: String? = null
)
