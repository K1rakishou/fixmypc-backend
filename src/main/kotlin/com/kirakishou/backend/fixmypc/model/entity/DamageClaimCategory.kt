package com.kirakishou.backend.fixmypc.model.entity

enum class DamageClaimCategory {
    Computer,
    Notebook,
    Phone;

    companion object {
        fun contains(value: Int): Boolean {
            return DamageClaimCategory.values().any { it.ordinal == value }
        }
    }
}