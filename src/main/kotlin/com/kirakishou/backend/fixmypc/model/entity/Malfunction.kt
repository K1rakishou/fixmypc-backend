package com.kirakishou.backend.fixmypc.model.entity

data class Malfunction(var category: Int = 0,
                       var description: String = "",
                       var photos: List<String> = arrayListOf())