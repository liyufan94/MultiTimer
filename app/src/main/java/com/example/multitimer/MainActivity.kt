package com.example.multitimer

import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private val items = mutableListOf<TimerItem>()
    private lateinit var adapter: TimerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val btnAdd = findViewById<Button>(R.id.btnAdd)

        adapter = TimerAdapter(items) { position ->
            items[position].timer?.cancel()
            items.removeAt(position)
            adapter.notifyItemRemoved(position)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        btnAdd.setOnClickListener { showAddDialog() }
    }

    private fun showAddDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_add, null)
        val etName = view.findViewById<EditText>(R.id.etName)
        val etMinutes = view.findViewById<EditText>(R.id.etMinutes)

        AlertDialog.Builder(this)
            .setTitle("新建倒计时")
            .setView(view)
            .setPositiveButton("开始") { _, _ ->
                val name = etName.text.toString().ifBlank { "计时器" }
                val minutes = etMinutes.text.toString().toLongOrNull() ?: 1L
                addTimer(name, minutes)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun addTimer(name: String, minutes: Long) {
        val totalMillis = minutes * 60 * 1000
        val item = TimerItem(name, totalMillis, totalMillis)
        items.add(item)
        adapter.notifyItemInserted(items.size - 1)

        item.timer = object : CountDownTimer(totalMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                item.remainingMillis = millisUntilFinished
                val index = items.indexOf(item)
                if (index >= 0) adapter.notifyItemChanged(index)
            }

            override fun onFinish() {
                item.remainingMillis = 0
                val index = items.indexOf(item)
                if (index >= 0) adapter.notifyItemChanged(index)
                // 下一步加通知
            }
        }.start()
    }
}