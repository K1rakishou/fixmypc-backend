package com.kirakishou.backend.fixmypc.service

import com.kirakishou.backend.fixmypc.model.ImageInfo

interface ForwardImagesService {
    fun forwardImages(image: Map<Int, ArrayList<ImageInfo>>, imagesType: Int): Boolean
}