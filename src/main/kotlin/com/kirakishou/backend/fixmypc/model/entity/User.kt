package com.kirakishou.backend.fixmypc.model.entity

import com.kirakishou.backend.fixmypc.core.AccountType
import java.io.Serializable

/**
 * Created by kirakishou on 7/9/2017.
 */

data class User(var id: Long = 0L,
                var login: String = "",
                var password: String = "",
                var accountType: AccountType = AccountType.Guest,
                var createdOn: Long = 0L) : Serializable {

    var sessionId: String? = null
}