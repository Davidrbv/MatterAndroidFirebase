package com.example.matterandroid.entities

import java.io.Serializable

data class Employee(var employeeId : String? = "",
                    var name : String?  = "",
                    var surname : String?  = "",
                    var salary : Int? = 0,
                    var email : String?  = "",
                    var job : String?  = "",
                    var image : String? = "",
                    var genre : String?  = ""): Serializable {

                    constructor():this("","","",0,"","")
                    }
