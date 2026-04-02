package com.sofato.krone.domain.usecase.category

import com.sofato.krone.domain.model.Category
import com.sofato.krone.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCategoriesUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository,
) {
    operator fun invoke(): Flow<List<Category>> =
        categoryRepository.getActiveCategories()
}
