package com.example.qrhub

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST


data class BitlyRequest(
    val long_url: String,
    val domain: String = "bit.ly",  // Use the default domain
    val title: String? = null       // Optional: Add a custom title
)

data class BitlyResponse(
    val link: String                // The shortened URL
)
interface BitlyApiInterface {
    @POST("v4/shorten")
    fun shortenUrl(
        @Header("Authorization") token: String,
        @Body body: BitlyRequest
    ): Call<BitlyResponse>
}