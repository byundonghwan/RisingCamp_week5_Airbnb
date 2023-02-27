package com.byundonghwan.airbnb_week5

import retrofit2.Call
import retrofit2.http.GET

interface HouseService {
    @GET("/v3/999a34e3-98a1-4983-99da-2ef2b273c25a")
    fun getHouseList(): Call<HouseDto>
}