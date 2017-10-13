package com.kirakishou.backend.fixmypc.core

import javax.cache.configuration.Factory
import javax.cache.expiry.Duration
import javax.cache.expiry.ExpiryPolicy

class MyExpiryPolicyFactory(val expiryForCreationDuration: Duration,
                            val expiryForUpdateDuration: Duration?,
                            val expiryForAccessDuration: Duration?) : Factory<ExpiryPolicy> {
    override fun create(): ExpiryPolicy {
        return object : ExpiryPolicy {
            override fun getExpiryForUpdate() = expiryForUpdateDuration
            override fun getExpiryForCreation() = expiryForCreationDuration
            override fun getExpiryForAccess() = expiryForAccessDuration
        }
    }
}