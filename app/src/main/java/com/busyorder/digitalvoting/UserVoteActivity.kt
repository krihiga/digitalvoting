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
import com.google.firebase.database.FirebaseDatabase
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
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

    private val dbRef by lazy { FirebaseDatabase.getInstance().getReference("votes") }

    // Camera capture launcher
    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            if (bitmap != null) {
                faceBitmap = bitmap
                facePreview.setImageBitmap(bitmap)
                Toast.makeText(this, "Face captured ✅ Verifying...", Toast.LENGTH_SHORT).show()
                verifyFace(bitmap)   // 🔥 Auto verify voter
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
            .baseUrl("https://digital-vote-97d97-default-rtdb.firebaseio.com/") // Flask backend (for face verification)
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

        // ✅ Save vote to Firebase Realtime Database
        val voteData = mapOf(
            "voterId" to voterId,
            "vote" to voteText,
            "timestamp" to System.currentTimeMillis()
        )

        dbRef.child(voterId!!).setValue(voteData)
            .addOnSuccessListener {
                Toast.makeText(this, "✅ Vote saved", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "❌ Failed to save vote: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }
}
