package com.kirakishou.backend.fixmypc.model.net.response

import com.google.gson.annotations.SerializedName
import com.kirakishou.backend.fixmypc.core.Constant
import com.kirakishou.backend.fixmypc.model.entity.RespondedSpecialist

class SpecialistsListResponse(@SerializedName(Constant.SerializedNames.SPECIALISTS_LIST)
                              val specialists: List<RespondedSpecialist>,

                              @SerializedName(Constant.SerializedNames.SERVER_ERROR_CODE)
                              val errorCode: Int)