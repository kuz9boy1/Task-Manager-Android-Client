package com.example.myapplication.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.api.ApiCurs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ExchangeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<ExchangeState>(ExchangeState.Loading)

    val uiState: StateFlow<ExchangeState> = _uiState

    fun fetchExchangeRates() {
        viewModelScope.launch {
            _uiState.value = ExchangeState.Loading
            try {
                val response = ApiCurs.curseApi.getDailyRates()

                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = ExchangeState.Success(response.body()!!)
                } else {
                    _uiState.value = ExchangeState.Error("Ошибка сервера: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = ExchangeState.Error("Ошибка сети: ${e.message}")
            }
        }
    }
}