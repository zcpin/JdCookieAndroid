package com.jdcookie.app

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jdcookie.app.databinding.ItemLogBinding
import org.json.JSONObject

class LogAdapter : RecyclerView.Adapter<LogAdapter.ViewHolder>() {

    private val logs = mutableListOf<JSONObject>()

    fun submitList(items: List<JSONObject>) {
        logs.clear()
        logs.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLogBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(logs[position])
    }

    override fun getItemCount() = logs.size

    class ViewHolder(private val binding: ItemLogBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: JSONObject) {
            binding.tvTime.text = item.optString("time")
            binding.tvTag.text = item.optString("tag")
            binding.tvMessage.text = item.optString("message")

            val level = item.optString("level")
            val color = when (level) {
                "ERROR" -> Color.parseColor("#E53935")
                "WARN" -> Color.parseColor("#FB8C00")
                else -> Color.parseColor("#43A047")
            }
            binding.tvLevel.text = level
            binding.tvLevel.setTextColor(color)
        }
    }
}
