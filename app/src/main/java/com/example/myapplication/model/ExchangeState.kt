package com.example.myapplication.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.api.ApiCurs
import com.example.myapplication.dto.CurseDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class    ExchangeState {
    object Loading : ExchangeState()
    data class Success(val data: CurseDto) : ExchangeState()
    data class Error(val message: String) : ExchangeState()
}

