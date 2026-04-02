package com.sofato.krone.data.repository

import com.sofato.krone.data.db.dao.CategoryDao
import com.sofato.krone.data.db.entity.toDomain
import com.sofato.krone.data.db.entity.toEntity
import com.sofato.krone.domain.model.Category
import com.sofato.krone.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao,
) : CategoryRepository {

    override fun getActiveCategories(): Flow<List<Category>> =
        categoryDao.getActiveCategories().map { list -> list.map { it.toDomain() } }

    override fun getAllCategories(): Flow<List<Category>> =
        categoryDao.getAllCategories().map { list -> list.map { it.toDomain() } }

    override suspend fun getCategoryById(id: Long): Category? =
        categoryDao.getCategoryById(id)?.toDomain()

    override suspend fun addCategory(category: Category): Long =
        categoryDao.insertCategory(category.toEntity())

    override suspend fun updateCategory(category: Category) {
        categoryDao.updateCategory(category.toEntity())
    }

    override suspend fun archiveCategory(id: Long) {
        categoryDao.archiveCategory(id)
    }

    override suspend fun getNextSortOrder(): Int =
        categoryDao.getNextSortOrder()
}
