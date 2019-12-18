package com.example.itproject

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface RetrofitService {

    @Headers(
        "x-rapidapi-host:twinword-word-graph-dictionary.p.rapidapi.com",
        "x-rapidapi-key:0eff5e3e43mshba9ccc31d69bb44p1409f5jsnfbf890437b50"
    )
    @GET("definition_kr/")
    fun getWordRetrofit(@Query("entry") entry: String): Call<WordRepo>

    companion object{
        fun create():RetrofitService{
            val retrofit=Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://twinword-word-graph-dictionary.p.rapidapi.com/")
                .build()

            return retrofit.create(RetrofitService::class.java)
        }
    }
}

