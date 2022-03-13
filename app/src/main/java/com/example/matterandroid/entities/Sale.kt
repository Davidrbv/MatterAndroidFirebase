package com.example.matterandroid.entities

import java.io.Serializable

data class Sale(var saleId : String? = "",
                var date : String? = "",
                var cash : Float? = 0f,
                var card : Float? = 0f,
                var total : Float? = 0f): Serializable {
                    constructor(): this("","",0f,0f,0f)
                }
