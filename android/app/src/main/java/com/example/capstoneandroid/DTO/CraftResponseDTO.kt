package com.example.capstoneandroid.DTO

import com.google.gson.annotations.SerializedName

class CraftResponseDTO {
    data class BoxInfo(
        @SerializedName("bbox")
        val bbox: List<List<FloatArray>>,
    )
}