package com.kirakishou.backend.fixmypc.model.entity

import com.kirakishou.backend.fixmypc.model.AccountType
import java.io.Serializable
import java.sql.Timestamp

/**
 * Created by kirakishou on 7/9/2017.
 */

data class User(var owner_id: Long = 0L,
                var login: String = "",
                var password: String = "",
                var accountType: AccountType = AccountType.Guest,
                var createdOn: Timestamp? = null) : Serializable {

    var sessionId: String? = null
}