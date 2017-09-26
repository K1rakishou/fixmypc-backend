package com.kirakishou.backend.fixmypc.model.net.response

import com.google.gson.annotations.SerializedName
import com.kirakishou.backend.fixmypc.core.Constant

open class StatusResponse(@SerializedName(Constant.SerializedNames.SERVER_ERROR_CODE)
                          val errorCode: Int)