package com.kirakishou.backend.fixmypc.model.net.response

import com.google.gson.annotations.SerializedName
import com.kirakishou.backend.fixmypc.core.Constant

class UpdateSpecialistProfileResponse(@SerializedName(Constant.SerializedNames.NEW_SPECIALIST_PROFILE_PHOTO_NAME)
                                      val newPhotoName: String,

                                      @SerializedName(Constant.SerializedNames.SERVER_ERROR_CODE)
                                      val errorCode: Int)