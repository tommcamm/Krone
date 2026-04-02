package com.sofato.krone.domain.usecase.category

import com.sofato.krone.domain.repository.CategoryRepository
import javax.inject.Inject

class ArchiveCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository,
) {
    suspend operator fun invoke(id: Long) {
        categoryRepository.archiveCategory(id)
    }
}
