package com.example.multitimer

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.multitimer.util.TimerStore

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
            TimerStore.save(this, items)   // 删除后保存
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        btnAdd.setOnClickListener { showAddDialog() }

        restoreTimers()   // 重开 App 恢复
    }

    // 恢复：算剩余时间，重建计时器
    @SuppressLint("NotifyDataSetChanged")
    private fun restoreTimers() {
        val saved = TimerStore.load(this)
        val now = System.currentTimeMillis()

        saved.forEach { item ->
            var remaining = item.endTime - now
            if (remaining <= 0) {
                // 已经结束的，直接跳过（或你想保留就改成显示 00:00）
//                return@forEach
                remaining = 0
            }
            item.remainingMillis = remaining
            items.add(item)
            startTimer(item, remaining)
        }
        adapter.notifyDataSetChanged()
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
        val endTime = System.currentTimeMillis() + totalMillis

        val item = TimerItem(name, totalMillis, totalMillis, endTime)
        items.add(item)
        adapter.notifyItemInserted(items.size - 1)

        startTimer(item, totalMillis)
        TimerStore.save(this, items)   // 新增后保存
    }

    // 抽出来，新增和恢复都用
    private fun startTimer(item: TimerItem, durationMillis: Long) {
        item.timer = object : CountDownTimer(durationMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                item.remainingMillis = millisUntilFinished
                val index = items.indexOf(item)
                if (index >= 0) adapter.notifyItemChanged(index)
            }

            override fun onFinish() {
                item.remainingMillis = 0
                val index = items.indexOf(item)
                if (index >= 0) adapter.notifyItemChanged(index)
                TimerStore.save(this@MainActivity, items)  // 结束后也存
            }
        }.start()
    }
}