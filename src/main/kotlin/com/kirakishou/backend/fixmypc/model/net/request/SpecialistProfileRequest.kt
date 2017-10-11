package com.kirakishou.backend.fixmypc.model.net.request

import com.google.gson.annotations.SerializedName
import com.kirakishou.backend.fixmypc.core.Constant

class SpecialistProfileRequest(@SerializedName(Constant.SerializedNames.PROFILE_NAME)
                               val profileName: String,

                               @SerializedName(Constant.SerializedNames.PROFILE_PHONE)
                               val profilePhone: String)