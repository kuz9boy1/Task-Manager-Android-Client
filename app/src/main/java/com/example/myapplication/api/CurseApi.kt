package com.example.myapplication.api

import com.example.myapplication.dto.CurseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Query

interface CurseApi {

    @GET("daily_json.js")
    suspend fun getDailyRates(): Response<CurseDto>

}