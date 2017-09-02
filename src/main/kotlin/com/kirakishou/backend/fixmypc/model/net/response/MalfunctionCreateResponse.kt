package com.kirakishou.backend.fixmypc.model.net.response

import com.fasterxml.jackson.annotation.JsonProperty
import com.kirakishou.backend.fixmypc.model.Constant

class MalfunctionCreateResponse(@JsonProperty(Constant.SerializedNames.SERVER_ERROR_CODE)
                          val errorCode: Int)