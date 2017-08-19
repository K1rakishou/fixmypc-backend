package com.kirakishou.backend.fixmypc.model.entity

import java.sql.Timestamp

data class Malfunction(var id: Long = 0L,
                       var owner_id: Long = 0L,
                       var malfunctionRequestId: String = "",
                       var category: Int = 0,
                       var description: String = "",
                       var createdOn: Timestamp? = null,
                       var imageNamesList: List<String> = arrayListOf())