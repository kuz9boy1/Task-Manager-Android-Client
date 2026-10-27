package com.example.myapplication.dto

import java.io.Serializable

data class CurseDto(
    val Date: String,
    val PreviousDate: String,
    val Timestamp: String,
    val Valute: Map<String, ValuteDto>
) : Serializable

