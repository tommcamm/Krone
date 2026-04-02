package com.sofato.krone.domain.usecase.insights

import com.sofato.krone.domain.model.CurrencyBreakdownItem
import com.sofato.krone.domain.repository.CurrencyRepository
import com.sofato.krone.domain.repository.ExpenseRepository
import com.sofato.krone.domain.usecase.budget.CalculateBudgetPeriodUseCase
import com.sofato.krone.util.today
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.LocalDate
import javax.inject.Inject

class GetCurrencyBreakdownUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val currencyRepository: CurrencyRepository,
    private val calculateBudgetPeriod: CalculateBudgetPeriodUseCase,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<List<CurrencyBreakdownItem>> = flow {
        val period = calculateBudgetPeriod()
        emit(period to LocalDate.today())
    }.flatMapLatest { (period, today) ->
        combine(
            expenseRepository.getCurrencyTotals(period.startDate, today),
            currencyRepository.getEnabledCurrencies(),
        ) { totals, currencies ->
            if (totals.size <= 1) return@combine emptyList()
            val currencyMap = currencies.associateBy { it.code }
            totals.mapNotNull { total ->
                val currency = currencyMap[total.currencyCode] ?: return@mapNotNull null
                CurrencyBreakdownItem(
                    currencyCode = total.currencyCode,
                    symbol = currency.symbol,
                    originalTotalMinor = total.originalTotalMinor,
                    homeTotalMinor = total.homeTotalMinor,
                    decimalPlaces = currency.decimalPlaces,
                )
            }
        }
    }
}
