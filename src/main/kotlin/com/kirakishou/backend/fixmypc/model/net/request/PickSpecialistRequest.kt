package com.kirakishou.backend.fixmypc.model.net.request

import com.google.gson.annotations.SerializedName

data class PickSpecialistRequest(@SerializedName("user_id")
                                 val userId: Long)