package com.busyorder.digitalvoting

data class VoterResponse(
    val ok: Boolean,
    val voter: Voter? = null,
    val error: String? = null
)

data class Voter(
    val voter_id: String,
    val name: String,
    val face_filename: String?
)
