package com.example.myapplication
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
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

class AddTaskActivity : AppCompatActivity() {

    private val taskRepository by lazy { TaskRepository(applicationContext) }
    private var userId: Long = 0
    private var login: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        userId = prefs.getLong("userId", 0)
        login = prefs.getString("login", "") ?: ""

        val etName = findViewById<TextInputEditText>(R.id.etName)
        val etDescription = findViewById<TextInputEditText>(R.id.etDescription)
        val etDeadline = findViewById<TextInputEditText>(R.id.etDeadline)
        val btnSave = findViewById<Button>(R.id.btnSave)

        etDeadline.setOnClickListener { showDatePicker(etDeadline) }

        btnSave.setOnClickListener {
            val title = etName.text.toString().trim()
            val description = etDescription.text.toString().trim()
            val dueDate = convertDateToIso(etDeadline.text.toString().trim())

            if (title.isEmpty()) {
                Toast.makeText(this, "Введите название", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                btnSave.isEnabled = false
                try {
                    val serverUserId = userId.takeIf { it > 0 }

                    val request = TaskRequestDto(
                        userId = serverUserId ?: 0,
                        title = title,
                        description = description,
                        dueDate = dueDate,
                        status = "TODO"
                    )

                    val created = taskRepository.createTask(login, serverUserId, request)
                    if (created != null) {
                        setResult(RESULT_OK)
                        finish()
                    } else {
                        Toast.makeText(this@AddTaskActivity, "Ошибка сохранения", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@AddTaskActivity, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    btnSave.isEnabled = true
                }
            }
        }
    }

    private fun convertDateToIso(date: String): String? {
        return try {
            val parts = date.split(".")
            if (parts.size != 3) return null
            "${parts[2]}-${parts[1]}-${parts[0]}"
        } catch (e: Exception) {
            null
        }
    }

    private fun showDatePicker(target: TextInputEditText) {
        val constraints = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointForward.now())
            .build()

        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Выберите дедлайн")
            .setCalendarConstraints(constraints)
            .build()

        picker.addOnPositiveButtonClickListener { selection ->
            val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            formatter.timeZone = TimeZone.getTimeZone("UTC")
            target.setText(formatter.format(Date(selection)))
        }
        picker.show(supportFragmentManager, "DATE_PICKER")
    }
}