package com.example.matterandroid.entities

import java.io.Serializable

data class User(var userId : String? = "",
                    var name : String?  = "",
                    var surname : String?  = "",
                    var email : String?  = "",
                    var pass1 : String? = "",
                    var pass2 : String?  = "",
                    var image : String? = ""): Serializable {

    constructor():this("","","","","","")
}