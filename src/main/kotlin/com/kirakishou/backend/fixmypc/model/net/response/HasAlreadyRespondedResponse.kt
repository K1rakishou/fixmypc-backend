package com.kirakishou.backend.fixmypc.model.net.response

import com.google.gson.annotations.SerializedName
import com.kirakishou.backend.fixmypc.core.Constant

class HasAlreadyRespondedResponse(@SerializedName(Constant.SerializedNames.HAS_SPECIALIST_ALREADY_RESPONDED)
                                  val hasAlreadyResponded: Boolean? = null,

                                  @SerializedName(Constant.SerializedNames.SERVER_ERROR_CODE)
                                  val errorCode: Int)