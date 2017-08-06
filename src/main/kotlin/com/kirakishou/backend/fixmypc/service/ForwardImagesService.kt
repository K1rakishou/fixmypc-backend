package com.kirakishou.backend.fixmypc.service

import com.kirakishou.backend.fixmypc.model.ImageInfo

interface ForwardImagesService {
    fun forwardImages(images: Map<Int, ArrayList<ImageInfo>>): Boolean
}