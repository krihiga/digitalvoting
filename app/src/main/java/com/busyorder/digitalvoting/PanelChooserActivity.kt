package com.busyorder.digitalvoting

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class PanelChooserActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_panel_chooser)

        val btnAdmin = findViewById<Button>(R.id.btnAdminPanel)
        val btnUser = findViewById<Button>(R.id.btnUserPanel)

        btnAdmin.setOnClickListener {
            // Go to Admin panel (Register + Verify options)
            startActivity(Intent(this, AdminPanelActivity::class.java))
        }

        btnUser.setOnClickListener {
            // Go to User Vote screen
            startActivity(Intent(this, UserVoteActivity::class.java))
        }
    }
}
