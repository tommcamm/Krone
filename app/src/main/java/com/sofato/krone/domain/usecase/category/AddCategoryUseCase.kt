package com.sofato.krone.domain.usecase.category

import com.sofato.krone.domain.model.Category
import com.sofato.krone.domain.repository.CategoryRepository
import javax.inject.Inject

class AddCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository,
) {
    suspend operator fun invoke(name: String, iconName: String, colorHex: String): Long {
        val sortOrder = categoryRepository.getNextSortOrder()
        val category = Category(
            name = name,
            iconName = iconName,
            colorHex = colorHex,
            isCustom = true,
            sortOrder = sortOrder,
        )
        return categoryRepository.addCategory(category)
    }
}
