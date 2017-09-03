package com.kirakishou.backend.fixmypc.model.entity

import com.kirakishou.backend.fixmypc.model.entity.FileServerAnswer

data class FileServerAnswerWrapper(val answer: FileServerAnswer,
                                   val newImageName: String)