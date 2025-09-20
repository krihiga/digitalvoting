package com.busyorder.digitalvoting

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ResultsAdapter : RecyclerView.Adapter<ResultsAdapter.ResultViewHolder>() {

    private var results: Map<String, Int> = emptyMap()

    fun setData(newResults: Map<String, Int>) {
        results = newResults
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_result, parent, false)
        return ResultViewHolder(view)
    }

    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        val candidate = results.keys.elementAt(position)
        val count = results[candidate] ?: 0
        holder.bind(candidate, count)
    }

    override fun getItemCount(): Int = results.size

    class ResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val candidateName: TextView = itemView.findViewById(R.id.tvCandidateName)
        private val voteCount: TextView = itemView.findViewById(R.id.tvVoteCount)

        fun bind(candidate: String, count: Int) {
            candidateName.text = candidate
            voteCount.text = "Votes: $count"
        }
    }
}
