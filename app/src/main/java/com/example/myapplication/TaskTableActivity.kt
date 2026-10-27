package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.dto.TaskResponseDto
import com.example.myapplication.model.Task
import com.example.myapplication.repository.AuthRepository
import com.example.myapplication.repository.TaskRepository
import com.example.myapplication.widget.TaskAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class TaskTableActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TaskAdapter
    private val tasks = mutableListOf<TaskResponseDto>()

    private val authRepository by lazy { AuthRepository(applicationContext) }
    private val taskRepository by lazy { TaskRepository(applicationContext) }

    private var userId: Long = 0
    private var login: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_table)

        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        login = prefs.getString("login", "") ?: ""
        userId = prefs.getLong("userId", 0)

        recyclerView = findViewById(R.id.rvTasks)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = TaskAdapter(tasks) { position ->
            val task = tasks[position]
            val intent = Intent(this, TaskDetailActivity::class.java).apply {
                putExtra("task_id", task.id)
                putExtra("task_title", task.title)
                putExtra("task_description", task.description)
                putExtra("task_dueDate", task.dueDate)
                putExtra("task_status", task.status)
                putExtra("task_position", position)
            }
            detailLauncher.launch(intent)
        }
        recyclerView.adapter = adapter

        val fab = findViewById<FloatingActionButton>(R.id.fabAddTask)
        fab.setOnClickListener {
            addTaskLauncher.launch(Intent(this, AddTaskActivity::class.java))
        }

        val btnLogout = findViewById<Button>(R.id.btnLogout)
        btnLogout.setOnClickListener {
            showLogoutDialog()
        }
        val btnCurrencyRate = findViewById<Button>(R.id.btnCurrencyRate)
        btnCurrencyRate.setOnClickListener {
            val intent = Intent(this, ExchangeRateActivity::class.java)
            startActivity(intent)
        }

        lifecycleScope.launch {
            val serverUserId = userId.takeIf { it > 0 }

            authRepository.syncPendingUsers()
            taskRepository.syncPendingTasks(login, serverUserId)

            loadTasks()
        }
    }

    private fun loadTasks() {
        lifecycleScope.launch {
            try {
                val serverUserId = userId.takeIf { it > 0 }
                val response = taskRepository.getTasks(login, serverUserId)
                tasks.clear()
                tasks.addAll(response)
                adapter.notifyDataSetChanged()
            } catch (e: Exception) {
                Toast.makeText(
                    this@TaskTableActivity,
                    "Ошибка загрузки: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Выйти из аккаунта?")
            .setMessage("Вы уверены, что хотите выйти?")
            .setPositiveButton("Выйти") { _, _ ->
                logout()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun logout() {
        getSharedPreferences("auth", MODE_PRIVATE).edit().clear().apply()

        val intent = Intent(this, LogInActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)

        finish()
    }

    private val addTaskLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            loadTasks()
        }
    }

    private val detailLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            loadTasks()
        }
    }
}