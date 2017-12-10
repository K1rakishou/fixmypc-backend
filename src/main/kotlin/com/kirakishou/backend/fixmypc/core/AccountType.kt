package com.kirakishou.backend.fixmypc.core

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

        fun contains(id: Int): Boolean {
            return AccountType.values().any { it.value == id }
        }
    }
}