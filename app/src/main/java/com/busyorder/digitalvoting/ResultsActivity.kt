package com.busyorder.digitalvoting

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory

class ResultsActivity : AppCompatActivity() {

    private lateinit var api: ApiService
    private lateinit var recyclerResults: RecyclerView
    private lateinit var adapter: ResultsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)

        recyclerResults = findViewById(R.id.recyclerResults)
        recyclerResults.layoutManager = LinearLayoutManager(this)

        adapter = ResultsAdapter()
        recyclerResults.adapter = adapter

        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.84.88.187:5000/") // Flask server
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        api = retrofit.create(ApiService::class.java)

        loadResults()
    }

    private fun loadResults() {
        api.getResults().enqueue(object : Callback<ResultsResponse> {
            override fun onResponse(call: Call<ResultsResponse>, response: Response<ResultsResponse>) {
                val body = response.body()
                if (body?.ok == true && body.results != null) {
                    adapter.setData(body.results)
                } else {
                    Toast.makeText(this@ResultsActivity, "❌ Failed: ${body?.error}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResultsResponse>, t: Throwable) {
                Toast.makeText(this@ResultsActivity, "⚠️ Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
