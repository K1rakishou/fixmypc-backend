package com.kirakishou.backend.fixmypc.model.entity

data class ClientProfile(val userId: Long = 0L,
                         val name: String = "",
                         val phone: String = "",
                         val isFilledOut: Boolean = false)