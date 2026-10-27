package com.example.myapplication.dto

import java.io.Serializable

data class ValuteDto(
    val ID: String,
    val NumCode: String,
    val CharCode: String,
    val Nominal: Int,
    val Name: String,
    val Value: Double,
    val Previous: Double
) : Serializable