package com.busyorder.digitalvoting

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

class RegisterActivity : AppCompatActivity() {

    private lateinit var api: ApiService
    private lateinit var nameEt: EditText
    private lateinit var voterIdEt: EditText
    private lateinit var selectBtn: Button
    private lateinit var registerBtn: Button
    private lateinit var imagePreview: ImageView
    private var imageFile: File? = null

    private lateinit var auth: FirebaseAuth
    private val dbRef by lazy { FirebaseDatabase.getInstance().getReference("voters") }

    companion object {
        const val PICK_IMAGE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        nameEt = findViewById(R.id.etName)
        voterIdEt = findViewById(R.id.etVoterId)
        selectBtn = findViewById(R.id.btnSelectImage)
        registerBtn = findViewById(R.id.btnRegister)
        imagePreview = findViewById(R.id.imagePreview)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.busy-order.com/") // your backend
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        api = retrofit.create(ApiService::class.java)

        selectBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE)
        }

        registerBtn.setOnClickListener { doRegister() }
    }

    private fun doRegister() {
        val name = nameEt.text.toString().trim()
        val voterId = voterIdEt.text.toString().trim()
        val file = imageFile ?: return Toast.makeText(this, "Select an image first", Toast.LENGTH_SHORT).show()

        if (name.isEmpty() || voterId.isEmpty()) {
            Toast.makeText(this, "Enter all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val filePart = fileToMultipartPart("image", file)
        val nameRB = RequestBody.create("text/plain".toMediaTypeOrNull(), name)
        val idRB = RequestBody.create("text/plain".toMediaTypeOrNull(), voterId)

        api.register(nameRB, idRB, filePart).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, resp: Response<ApiResponse>) {
                val body = resp.body()
                if (resp.isSuccessful && body?.ok == true) {
                    Toast.makeText(this@RegisterActivity, "✅ Registered successfully!", Toast.LENGTH_SHORT).show()

                    // ✅ Save voter details in Firebase Realtime Database
                    val uid = auth.currentUser?.uid ?: voterId // fallback if not logged in
                    val voterData = mapOf(
                        "uid" to uid,
                        "name" to name,
                        "voterId" to voterId,
                        "photo" to "uploaded_via_api" // You can save actual photo URL if backend returns it
                    )

                    dbRef.child(uid).setValue(voterData)
                        .addOnSuccessListener {
                            Toast.makeText(this@RegisterActivity, "✅ Saved in Firebase", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this@RegisterActivity, "⚠️ Firebase save failed: ${it.message}", Toast.LENGTH_LONG).show()
                        }

                } else {
                    val errorMsg = body?.error ?: resp.errorBody()?.string() ?: "Unknown error"
                    Toast.makeText(this@RegisterActivity, "❌ Error: $errorMsg", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                t.printStackTrace()
                Toast.makeText(this@RegisterActivity, "⚠️ Network error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun onActivityResult(reqCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(reqCode, resultCode, data)
        if (reqCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            val uri: Uri? = data?.data
            if (uri != null) {
                imageFile = uriToFile(this, uri)
                imagePreview.setImageURI(uri)
            }
        }
    }

    // Convert Uri -> File
    private fun uriToFile(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        val tempFile = File(context.cacheDir, "upload_image.jpg")
        tempFile.outputStream().use { output ->
            inputStream?.copyTo(output)
        }
        return tempFile
    }
}
