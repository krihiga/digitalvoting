package com.busyorder.digitalvoting

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    // 🔹 Register voter with photo
    @Multipart
    @POST("/register")
    fun register(
        @Part("name") name: RequestBody,
        @Part("voter_id") voterId: RequestBody,
        @Part image: MultipartBody.Part
    ): Call<ApiResponse>

    // 🔹 Verify voter with photo (NO voter_id needed)
    @Multipart
    @POST("/verify")
    fun verify(
        @Part image: MultipartBody.Part
    ): Call<VerifyResponse>

    // Verify with only face image (no voter_id)
    @Multipart
    @POST("/verify")
    fun verifyWithoutId(
        @Part image: MultipartBody.Part
    ): Call<VerifyResponse>


    // 🔹 Cast encrypted vote (JSON body)
    @POST("/cast_vote")
    fun castVote(
        @Body body: RequestBody
    ): Call<ApiResponse>

    // 🔹 Get voter details by ID
    @GET("/get_voter")
    fun getVoter(
        @Query("voter_id") voterId: String
    ): Call<VoterResponse>

    // Admin: Get results
    @GET("/results")
    fun getResults(): Call<ResultsResponse>

}
