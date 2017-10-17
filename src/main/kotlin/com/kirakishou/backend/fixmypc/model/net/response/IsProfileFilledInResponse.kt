package com.kirakishou.backend.fixmypc.model.net.response

import com.google.gson.annotations.SerializedName
import com.kirakishou.backend.fixmypc.core.Constant

class IsProfileFilledInResponse(@SerializedName(Constant.SerializedNames.IS_PROFILE_FILLED_IN)
                                val isProfileFilledIn: Boolean = false,

                                @SerializedName(Constant.SerializedNames.SERVER_ERROR_CODE)
                                val errorCode: Int)