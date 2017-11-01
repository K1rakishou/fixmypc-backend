package com.kirakishou.backend.fixmypc.model.net.response

import com.google.gson.annotations.SerializedName
import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.model.entity.SpecialistProfile

class SpecialistsListResponse(@SerializedName(Constant.SerializedNames.SPECIALIST_PROFILES_LIST)
                              val specialists: List<SpecialistProfile>?,

                              @SerializedName(Constant.SerializedNames.SERVER_ERROR_CODE)
                              val errorCode: Int)