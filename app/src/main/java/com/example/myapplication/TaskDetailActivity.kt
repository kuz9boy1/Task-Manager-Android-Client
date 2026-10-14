package com.example.myapplication

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.dto.TaskRequestDto
import com.example.myapplication.repository.TaskRepository
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class TaskDetailActivity : AppCompatActivity() {

    private val taskRepository by lazy { TaskRepository(applicationContext) }

    private var taskId: Long = 0
    private var userId: Long = 0
    private var login: String = ""
    private var taskStatus: String = "TODO"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_detail)

        taskId = intent.getLongExtra("task_id", 0)
        val title = intent.getStringExtra("task_title") ?: ""
        val description = intent.getStringExtra("task_description") ?: ""
        val dueDateIso = intent.getStringExtra("task_dueDate")
        taskStatus = intent.getStringExtra("task_status") ?: "TODO"

        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        userId = prefs.getLong("userId", 0)
        login = prefs.getString("login", "") ?: ""

        val etName = findViewById<TextInputEditText>(R.id.etName)
        val etDescription = findViewById<TextInputEditText>(R.id.etDescription)
        val etDeadline = findViewById<TextInputEditText>(R.id.etDeadline)
        val btnUpdate = findViewById<Button>(R.id.btnUpdate)
        val btnDelete = findViewById<Button>(R.id.btnDelete)

        etName.setText(title)
        etDescription.setText(description)
        etDeadline.setText(isoToDisplay(dueDateIso))

        etDeadline.setOnClickListener {
            val constraints = CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.now())
                .build()

            val picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Изменить дедлайн")
                .setCalendarConstraints(constraints)
                .build()

            picker.addOnPositiveButtonClickListener { selection ->
                val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                formatter.timeZone = TimeZone.getTimeZone("UTC")
                etDeadline.setText(formatter.format(Date(selection)))
            }
            picker.show(supportFragmentManager, "DATE_PICKER")
        }

        btnUpdate.setOnClickListener {
            val newTitle = etName.text.toString().trim()
            val newDescription = etDescription.text.toString().trim()
            val newDueDate = displayToIso(etDeadline.text.toString().trim())

            if (newTitle.isEmpty()) {
                etName.error = "Введите название"
                return@setOnClickListener
            }

            lifecycleScope.launch {
                btnUpdate.isEnabled = false
                try {
                    val serverUserId = userId.takeIf { it > 0 }

                    val request = TaskRequestDto(
                        userId = serverUserId ?: 0,
                        title = newTitle,
                        description = newDescription,
                        dueDate = newDueDate,
                        status = taskStatus
                    )

                    val updated = taskRepository.updateTask(login, serverUserId, taskId, request)
                    if (updated != null) {
                        Toast.makeText(this@TaskDetailActivity, "Сохранено", Toast.LENGTH_SHORT).show()
                        setResult(RESULT_OK)
                        finish()
                    } else {
                        Toast.makeText(this@TaskDetailActivity, "Не удалось сохранить", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@TaskDetailActivity, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    btnUpdate.isEnabled = true
                }
            }
        }

        btnDelete.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Удалить задачу?")
                .setMessage("Действие нельзя отменить.")
                .setPositiveButton("Удалить") { _, _ ->
                    lifecycleScope.launch {
                        try {
                            val serverUserId = userId.takeIf { it > 0 }
                            val success = taskRepository.deleteTask(login, serverUserId, taskId)
                            if (success) {
                                Toast.makeText(this@TaskDetailActivity, "Удалено", Toast.LENGTH_SHORT).show()
                                setResult(RESULT_OK)
                                finish()
                            } else {
                                Toast.makeText(this@TaskDetailActivity, "Не удалось удалить", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(this@TaskDetailActivity, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .setNegativeButton("Отмена", null)
                .show()
        }
    }

    private fun isoToDisplay(iso: String?): String {
        if (iso.isNullOrBlank()) return ""
        return try {
            val p = iso.split("-")
            if (p.size == 3) "${p[2]}.${p[1]}.${p[0]}" else iso
        } catch (e: Exception) { iso }
    }

    private fun displayToIso(display: String): String? {
        if (display.isBlank()) return null
        return try {
            val p = display.split(".")
            if (p.size == 3) "${p[2]}-${p[1]}-${p[0]}" else null
        } catch (e: Exception) { null }
    }
}