package com.example.myapplication.dto

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.R
import com.example.myapplication.repository.AuthRepository
import kotlinx.coroutines.launch

data class RegisterRequestDto(
    val login: String,
    val password: String
)