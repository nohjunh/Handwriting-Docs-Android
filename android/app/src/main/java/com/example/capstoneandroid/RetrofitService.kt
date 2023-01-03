package com.example.capstoneandroid

import com.example.capstoneandroid.DTO.CraftResponseDTO
import okhttp3.MultipartBody
import retrofit2.*
import retrofit2.http.*

interface RetrofitService {
    @Multipart
    @POST("/")
    fun getBoxInfo(
        @Part image: MultipartBody.Part
    ): Call<CraftResponseDTO.BoxInfo>

}