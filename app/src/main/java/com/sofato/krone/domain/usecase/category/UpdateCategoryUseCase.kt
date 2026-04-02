package com.sofato.krone.domain.usecase.category

import com.sofato.krone.domain.model.Category
import com.sofato.krone.domain.repository.CategoryRepository
import javax.inject.Inject

class UpdateCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository,
) {
    suspend operator fun invoke(category: Category) {
        categoryRepository.updateCategory(category)
    }
}
