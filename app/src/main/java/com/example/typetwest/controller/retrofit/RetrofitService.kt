package com.example.typetwest.controller.retrofit

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface RetrofitService {

    @GET("/api/")
    fun getLoremIpsum(@Query("sentences") quantity: Int = 3,
                      @Query("type") type: String = "all-meat",
                      @Query("format") format: String = "format=json"): Call<List<String>>
}