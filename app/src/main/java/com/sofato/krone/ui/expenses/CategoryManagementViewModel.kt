package com.sofato.krone.ui.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sofato.krone.domain.model.Category
import com.sofato.krone.domain.usecase.category.AddCategoryUseCase
import com.sofato.krone.domain.usecase.category.ArchiveCategoryUseCase
import com.sofato.krone.domain.usecase.category.GetCategoriesUseCase
import com.sofato.krone.domain.usecase.category.UpdateCategoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryManagementViewModel @Inject constructor(
    getCategoriesUseCase: GetCategoriesUseCase,
    private val addCategoryUseCase: AddCategoryUseCase,
    private val updateCategoryUseCase: UpdateCategoryUseCase,
    private val archiveCategoryUseCase: ArchiveCategoryUseCase,
) : ViewModel() {

    private val _error = MutableSharedFlow<String>()
    val error = _error.asSharedFlow()

    val categories: StateFlow<List<Category>> =
        getCategoriesUseCase()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addCategory(name: String, iconName: String, colorHex: String) {
        viewModelScope.launch {
            try {
                addCategoryUseCase(name, iconName, colorHex)
            } catch (e: Exception) {
                _error.emit("Failed to add category")
            }
        }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch {
            try {
                updateCategoryUseCase(category)
            } catch (e: Exception) {
                _error.emit("Failed to update category")
            }
        }
    }

    fun archiveCategory(id: Long) {
        viewModelScope.launch {
            try {
                archiveCategoryUseCase(id)
            } catch (e: Exception) {
                _error.emit("Failed to archive category")
            }
        }
    }
}
