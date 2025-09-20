package com.busyorder.digitalvoting

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class AdminPanelActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_panel) // 👈 make sure you have this XML

        val btnRegister = findViewById<Button>(R.id.btnGoRegister)
        val btnVerify = findViewById<Button>(R.id.btnGoVerify)
        val btnResults = findViewById<Button>(R.id.btnGoResults)

        // Go to Register screen
        btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Go to Verify screen
        btnVerify.setOnClickListener {
            startActivity(Intent(this, VerifyActivity::class.java))
        }

        // Go to Results screen (new Activity)
        btnResults.setOnClickListener {
            startActivity(Intent(this, ResultsActivity::class.java))
        }
    }
}
