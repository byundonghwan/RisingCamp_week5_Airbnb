package com.byundonghwan.airbnb_week5

data class HouseModel(
    val id : Int,
    val title : String,
    val price : String,
    val imgUrl : String,
    val lat : Double,
    val lng : Double
)