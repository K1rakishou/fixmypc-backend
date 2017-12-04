package com.kirakishou.backend.fixmypc.model.net.response

import com.google.gson.annotations.SerializedName
import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.model.entity.SpecialistProfile

class SpecialistProfileResponse(@SerializedName(Constant.SerializedNames.SPECIALIST_PROFILE)
                                val profile: SpecialistProfile?,

                                @SerializedName(Constant.SerializedNames.IS_PROFILE_FILLED_IN)
                                val isProfileFilledIn: Boolean?,

                                @SerializedName(Constant.SerializedNames.SERVER_ERROR_CODE)
                                val errorCode: Int)