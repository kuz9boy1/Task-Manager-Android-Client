package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.model.UserStorage
import com.example.myapplication.repository.AuthRepository
import kotlinx.coroutines.launch


class LogInActivity : AppCompatActivity() {

    private val authRepository by lazy { AuthRepository(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second_lay)

        val etLogin = findViewById<EditText>(R.id.etdLogin)
        val etPassword = findViewById<EditText>(R.id.etdPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogIn)
        val btnRegister = findViewById<Button>(R.id.btnRegistration)

        btnLogin.setOnClickListener {
            val login = etLogin.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (login.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Введите логин и пароль", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                btnLogin.isEnabled = false
                try {
                    val response = authRepository.login(login, password)
                    if (response.success) {
                        getSharedPreferences("auth", MODE_PRIVATE).edit()
                            .putLong("userId", response.userId ?: 0)
                            .putString("login", response.login ?: login)
                            .putBoolean("isLoggedIn", true)
                            .apply()

                        startActivity(Intent(this@LogInActivity, TaskTableActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this@LogInActivity, response.message, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@LogInActivity, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    btnLogin.isEnabled = true
                }
            }
        }

        btnRegister.setOnClickListener {
            startActivity(Intent(this, RegistrationActivity::class.java))
        }
    }
}