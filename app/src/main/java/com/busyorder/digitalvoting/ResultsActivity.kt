package com.busyorder.digitalvoting

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class ResultsActivity : AppCompatActivity() {

    private lateinit var recyclerResults: RecyclerView
    private lateinit var adapter: ResultsAdapter
    private val dbRef: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().getReference("votes")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)

        recyclerResults = findViewById(R.id.recyclerResults)
        recyclerResults.layoutManager = LinearLayoutManager(this)

        adapter = ResultsAdapter()
        recyclerResults.adapter = adapter

        loadResults()
    }

    private fun loadResults() {
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(this@ResultsActivity, "No votes yet", Toast.LENGTH_SHORT).show()
                    return
                }

                // Count votes by candidate
                val resultsMap = mutableMapOf<String, Int>()

                for (voteSnap in snapshot.children) {
                    val vote = voteSnap.child("vote").getValue(String::class.java)
                    if (!vote.isNullOrEmpty()) {
                        resultsMap[vote] = (resultsMap[vote] ?: 0) + 1
                    }
                }

                if (resultsMap.isNotEmpty()) {
                    adapter.setData(resultsMap)
                } else {
                    Toast.makeText(this@ResultsActivity, "⚠️ No valid votes found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ResultsActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
