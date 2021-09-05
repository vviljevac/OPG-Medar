package com.example.opgmedar.model

import java.util.*
import kotlin.collections.ArrayList


data class OrderModel(
    val firstName: String?=null,
    val lastName: String?=null,
    val address: String?=null,
    val email: String?=null,
    val postalCode: String?=null,
    val phone:String?=null,
    val totalPriceOrder: String?=null,
)
