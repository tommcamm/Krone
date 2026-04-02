package com.sofato.krone.domain.repository

import com.sofato.krone.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getActiveCategories(): Flow<List<Category>>
    fun getAllCategories(): Flow<List<Category>>
    suspend fun getCategoryById(id: Long): Category?
    suspend fun addCategory(category: Category): Long
    suspend fun updateCategory(category: Category)
    suspend fun archiveCategory(id: Long)
    suspend fun getNextSortOrder(): Int
}
