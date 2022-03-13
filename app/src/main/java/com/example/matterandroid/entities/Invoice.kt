package com.example.matterandroid.entities

import java.io.Serializable

data class Invoice(var invoiceId : String? = "",
                   var provider : String? = "",
                   var date: String? = "",
                   var quantity : Int? = 0,
                   var state : Boolean? = false): Serializable {

                       constructor(): this("","","",0,false)
                   }
