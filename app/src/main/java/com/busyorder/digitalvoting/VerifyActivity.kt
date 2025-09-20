package com.busyorder.digitalvoting


import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory

class VerifyActivity : AppCompatActivity() {
    private lateinit var api: ApiService
    private lateinit var voterIdEt: EditText
    private lateinit var verifyBtn: Button
    private lateinit var nameTv: TextView
    private lateinit var idTv: TextView
    private lateinit var photoImg: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify)

        voterIdEt = findViewById(R.id.etVoterId)
        verifyBtn = findViewById(R.id.btnVerify)
        nameTv = findViewById(R.id.tvName)
        idTv = findViewById(R.id.tvId)
        photoImg = findViewById(R.id.imgPhoto)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://digitalvoting.onrender.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        api = retrofit.create(ApiService::class.java)

        verifyBtn.setOnClickListener { doVerify() }
    }

    private fun doVerify() {
        val voterId = voterIdEt.text.toString()
        if (voterId.isBlank()) {
            Toast.makeText(this, "Enter Voter ID", Toast.LENGTH_SHORT).show()
            return
        }

        api.getVoter(voterId).enqueue(object : Callback<VoterResponse> {
            override fun onResponse(call: Call<VoterResponse>, resp: Response<VoterResponse>) {
                val body = resp.body()
                if (body?.ok == true && body.voter != null) {
                    nameTv.text = "Name: ${body.voter.name}"
                    idTv.text = "ID: ${body.voter.voter_id}"

                    // Load image if file path returned
                    body.voter.face_filename?.let {
                        val imgUrl = "http://10.84.88.187:5000/$it" // adjust if serving static
                        Picasso.get().load(imgUrl).into(photoImg)
                    }
                } else {
                    Toast.makeText(this@VerifyActivity, "❌ ${body?.error}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<VoterResponse>, t: Throwable) {
                Toast.makeText(this@VerifyActivity, "⚠️ Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
