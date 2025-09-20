package com.busyorder.digitalvoting

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream

class UserVoteActivity : AppCompatActivity() {

    private lateinit var api: ApiService
    private lateinit var cameraBtn: Button
    private lateinit var voteBtn: Button
    private lateinit var radioGroup: RadioGroup
    private lateinit var facePreview: ImageView
    private lateinit var tvVoterId: TextView
    private var faceBitmap: Bitmap? = null
    private var voterId: String? = null   // filled after verification

    // Camera capture launcher
    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            if (bitmap != null) {
                faceBitmap = bitmap
                facePreview.setImageBitmap(bitmap)
                Toast.makeText(this, "Face captured ✅ Verifying...", Toast.LENGTH_SHORT).show()
                verifyFace(bitmap)   // 🔥 Auto verify
            } else {
                Toast.makeText(this, "Failed to capture face", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_vote)

        cameraBtn = findViewById(R.id.btnCaptureFace)
        voteBtn = findViewById(R.id.btnCastVote)
        radioGroup = findViewById(R.id.radioGroupCandidates)
        facePreview = findViewById(R.id.facePreview)
        tvVoterId = findViewById(R.id.tvVoterId)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.busy-order.com/") // Flask backend
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        api = retrofit.create(ApiService::class.java)

        // Capture face button
        cameraBtn.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 1001)
            } else {
                takePicture.launch(null)
            }
        }

        // Cast vote button
        voteBtn.setOnClickListener { castVote() }
    }

    /**
     * 🔎 Verify voter by sending face to backend
     */
    private fun verifyFace(bitmap: Bitmap) {
        try {
            val file = File(cacheDir, "verify_face.jpg")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }

            val reqFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("image", file.name, reqFile)

            // 🚫 Do NOT send voter_id here, let server detect it
            api.verifyWithoutId(body).enqueue(object : Callback<VerifyResponse> {
                override fun onResponse(call: Call<VerifyResponse>, response: Response<VerifyResponse>) {
                    val resp = response.body()
                    if (resp?.ok == true && !resp.predicted.isNullOrBlank()) {
                        voterId = resp.predicted
                        tvVoterId.text = "✅ Voter Verified: $voterId"
                    } else {
                        voterId = null
                        tvVoterId.text = "❌ Verification failed"
                    }
                }

                override fun onFailure(call: Call<VerifyResponse>, t: Throwable) {
                    voterId = null
                    tvVoterId.text = "⚠️ Verification error"
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
            tvVoterId.text = "❌ Error preparing image"
        }
    }

    /**
     * 🗳️ Cast the vote after face verification
     */
    private fun castVote() {
        if (faceBitmap == null) {
            Toast.makeText(this, "Capture & verify face first", Toast.LENGTH_SHORT).show()
            return
        }

        if (voterId.isNullOrBlank()) {
            Toast.makeText(this, "❌ Voter ID missing! Verify first", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedId = radioGroup.checkedRadioButtonId
        if (selectedId == -1) {
            Toast.makeText(this, "Select a candidate", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedBtn = findViewById<RadioButton>(selectedId)
        val voteText = selectedBtn.text.toString()

        // Encrypt vote
        val encryptedVoteObj = CryptoUtils.encrypt(voteText)

        val payload = JSONObject()
        payload.put("voter_id", voterId)   // ✅ verified voterId
        payload.put("encrypted_vote", encryptedVoteObj)

        val body: RequestBody =
            payload.toString().toRequestBody("application/json".toMediaTypeOrNull())

        api.castVote(body).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (!response.isSuccessful) {
                    Toast.makeText(this@UserVoteActivity, "❌ You have already Voted: ${response.code()}", Toast.LENGTH_LONG).show()
                    return
                }
                val resp = response.body()
                if (resp?.ok == true) {
                    Toast.makeText(this@UserVoteActivity, "✅ Vote cast! Hash: ${resp.vote_hash}", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this@UserVoteActivity, "❌ Error: ${resp?.error}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Toast.makeText(this@UserVoteActivity, "⚠️ Network error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}
