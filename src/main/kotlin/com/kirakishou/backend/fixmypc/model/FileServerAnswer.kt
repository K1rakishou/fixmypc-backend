package com.kirakishou.backend.fixmypc.model

import com.fasterxml.jackson.annotation.JsonProperty

data class FileServerAnswer(@JsonProperty("status_code") val errorCode: Int,
                            @JsonProperty("bad_photo_names") val badPhotoNames: List<String>)