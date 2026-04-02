package com.sofato.krone.domain.model

data class Category(
    val id: Long = 0,
    val name: String,
    val iconName: String,
    val colorHex: String,
    val isCustom: Boolean,
    val sortOrder: Int,
    val isArchived: Boolean = false,
)
