package com.kirakishou.backend.fixmypc.service

import com.kirakishou.backend.fixmypc.util.GenerateUtil
import org.springframework.stereotype.Component

/**
 * Created by kirakishou on 7/11/2017.
 */

@Component
class GeneratorImpl : Generator {

    override fun generateSessionId(): String {
        return GenerateUtil.generateSessionId()
    }

    override fun generateImageName(): String {
        return GenerateUtil.generateImageName()
    }
}