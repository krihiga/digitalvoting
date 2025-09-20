package com.busyorder.digitalvoting

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import com.squareup.picasso.Picasso

class VerifyActivity : AppCompatActivity() {
    private lateinit var voterIdEt: EditText
    private lateinit var verifyBtn: Button
    private lateinit var nameTv: TextView
    private lateinit var idTv: TextView
    private lateinit var photoImg: ImageView

    private val dbRef: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().getReference("voters")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify)

        voterIdEt = findViewById(R.id.etVoterId)
        verifyBtn = findViewById(R.id.btnVerify)
        nameTv = findViewById(R.id.tvName)
        idTv = findViewById(R.id.tvId)
        photoImg = findViewById(R.id.imgPhoto)

        verifyBtn.setOnClickListener { doVerify() }
    }

    private fun doVerify() {
        val voterId = voterIdEt.text.toString().trim()
        if (voterId.isBlank()) {
            Toast.makeText(this, "Enter Voter ID", Toast.LENGTH_SHORT).show()
            return
        }

        // ✅ Query by voterId inside "voters" node
        dbRef.orderByChild("voterId").equalTo(voterId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (child in snapshot.children) {
                            val name = child.child("name").value?.toString() ?: "Unknown"
                            val id = child.child("voterId").value?.toString() ?: "Unknown"
                            val photoUrl = child.child("photo").value?.toString()

                            nameTv.text = "Name: $name"
                            idTv.text = "ID: $id"

                            if (!photoUrl.isNullOrEmpty()) {
                                Picasso.get().load(photoUrl).into(photoImg)
                            } else {
                                photoImg.setImageResource(R.drawable.ic_person) // fallback icon
                            }
                        }
                    } else {
                        Toast.makeText(this@VerifyActivity, "❌ Voter not found", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@VerifyActivity, "⚠️ Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
