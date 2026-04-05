package com.sofato.krone.ui.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sofato.krone.domain.model.BudgetAllocation
import com.sofato.krone.domain.model.Category
import com.sofato.krone.domain.model.Currency
import com.sofato.krone.domain.repository.BudgetAllocationRepository
import com.sofato.krone.domain.repository.CategoryRepository
import com.sofato.krone.domain.repository.CurrencyRepository
import com.sofato.krone.domain.repository.UserPreferencesRepository
import com.sofato.krone.domain.model.Defaults
import com.sofato.krone.domain.usecase.budget.CalculateBudgetPeriodUseCase
import com.sofato.krone.domain.usecase.budget.GetOrCopyForwardAllocationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoryBudgetItem(
    val allocation: BudgetAllocation,
    val category: Category,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MonthlyBudgetsViewModel @Inject constructor(
    private val budgetAllocationRepository: BudgetAllocationRepository,
    private val categoryRepository: CategoryRepository,
    private val calculateBudgetPeriod: CalculateBudgetPeriodUseCase,
    private val getOrCopyForwardAllocations: GetOrCopyForwardAllocationsUseCase,
    private val userPreferencesRepository: UserPreferencesRepository,
    currencyRepository: CurrencyRepository,
) : ViewModel() {

    private val monthFlow = flow {
        val period = calculateBudgetPeriod()
        emit(GetOrCopyForwardAllocationsUseCase.formatMonth(period.startDate))
    }

    val currentMonth: StateFlow<String> =
        monthFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val budgetItems: StateFlow<List<CategoryBudgetItem>> =
        monthFlow.flatMapLatest { month ->
            combine(
                getOrCopyForwardAllocations(month),
                categoryRepository.getActiveCategories(),
            ) { allocations, categories ->
                val categoryMap = categories.associateBy { it.id }
                allocations.mapNotNull { alloc ->
                    categoryMap[alloc.categoryId]?.let { cat ->
                        CategoryBudgetItem(allocation = alloc, category = cat)
                    }
                }.sortedBy { it.category.sortOrder }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val availableCategories: StateFlow<List<Category>> =
        monthFlow.flatMapLatest { month ->
            combine(
                getOrCopyForwardAllocations(month),
                categoryRepository.getActiveCategories(),
            ) { allocations, categories ->
                val allocatedIds = allocations.map { it.categoryId }.toSet()
                categories.filter { it.id !in allocatedIds && !(it.iconName == Defaults.OTHER_CATEGORY_ICON && !it.isCustom) }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val homeCurrency: StateFlow<Currency?> =
        userPreferencesRepository.homeCurrencyCode
            .flatMapLatest { code ->
                currencyRepository.getEnabledCurrencies()
                    .combine(flowOf(code)) { currencies, homeCode ->
                        currencies.find { it.code == homeCode }
                    }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun saveAllocation(categoryId: Long, amountMinor: Long) {
        viewModelScope.launch {
            try {
                val month = currentMonth.value.ifEmpty { return@launch }
                val currencyCode = userPreferencesRepository.homeCurrencyCode.first()
                val existing = budgetAllocationRepository.getAllocationsForMonth(month)
                    .first()
                    .find { it.categoryId == categoryId }
                if (existing != null) {
                    budgetAllocationRepository.updateAllocation(
                        existing.copy(allocatedAmountMinor = amountMinor)
                    )
                } else {
                    budgetAllocationRepository.addAllocation(
                        BudgetAllocation(
                            categoryId = categoryId,
                            month = month,
                            allocatedAmountMinor = amountMinor,
                            currencyCode = currencyCode,
                        )
                    )
                }
            } catch (_: Exception) { /* budget save is best-effort */ }
        }
    }

    fun deleteAllocation(allocationId: Long) {
        viewModelScope.launch {
            try {
                budgetAllocationRepository.deleteAllocation(allocationId)
            } catch (_: Exception) { /* best-effort */ }
        }
    }
}
