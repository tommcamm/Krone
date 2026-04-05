package com.sofato.krone.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlin.time.Instant
import kotlinx.datetime.LocalDate

@Entity(
    tableName = "expense",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
        ),
        ForeignKey(
            entity = CurrencyEntity::class,
            parentColumns = ["code"],
            childColumns = ["currencyCode"],
        ),
    ],
    indices = [Index("categoryId"), Index("date"), Index("currencyCode")],
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amountMinor: Long,
    val currencyCode: String,
    val homeAmountMinor: Long,
    val exchangeRateUsed: Double,
    val categoryId: Long,
    val note: String?,
    val date: LocalDate,
    val createdAt: Instant,
    val isRecurringInstance: Boolean,
    val recurringExpenseId: Long?,
    val mlCategorySuggestion: Long?,
    val mlSuggestionAccepted: Boolean?,
)
