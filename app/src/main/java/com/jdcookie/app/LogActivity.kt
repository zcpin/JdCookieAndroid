package com.jdcookie.app

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.jdcookie.app.databinding.ActivityLogBinding
import org.json.JSONObject

class LogActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityLogBinding
    private val adapter = LogAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        viewBinding = ActivityLogBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        viewBinding.recyclerLogs.layoutManager = LinearLayoutManager(this)
        viewBinding.recyclerLogs.adapter = adapter

        loadLogs()

        viewBinding.btnClear.setOnClickListener {
            LogHelper.clearLogs(this)
            loadLogs()
        }
    }

    override fun onResume() {
        super.onResume()
        loadLogs()
    }

    private fun loadLogs() {
        val jsonArray = LogHelper.getLogs(this)
        val items = mutableListOf<JSONObject>()
        for (i in 0 until jsonArray.length()) {
            items.add(jsonArray.getJSONObject(i))
        }
        adapter.submitList(items.reversed())

        viewBinding.tvEmpty.visibility = if (items.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        viewBinding.recyclerLogs.visibility = if (items.isEmpty()) android.view.View.GONE else android.view.View.VISIBLE
    }
}
