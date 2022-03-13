package com.example.matterandroid.entities

import java.io.Serializable

data class Photo(var photoId : String? = "",
                   var url : String? = "",
                   var location: String? = ""): Serializable {

    constructor(): this("","","")
}