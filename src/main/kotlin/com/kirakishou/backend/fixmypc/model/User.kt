package com.kirakishou.backend.fixmypc.model

import java.io.Serializable
import java.sql.Timestamp

/**
 * Created by kirakishou on 7/9/2017.
 */

data class User(var login: String = "",
                var password: String = "",
                var accountType: AccountType = AccountType.Guest,
                var createdOn: Timestamp? = null,
                var id: Long = 0) : Serializable {

    var sessionId: String? = null
}

enum class AccountType(val value: Int) {
    Guest(0),
    Client(1),
    Specialist(2);

    companion object {
        fun from(id: Int): AccountType {
            for (type in AccountType.values()) {
                if (type.value == id) {
                    return type
                }
            }

            throw RuntimeException("unknown accountType: $id")
        }
    }
}