package com.sofato.krone.data.db.converter

import androidx.room.TypeConverter
import com.sofato.krone.domain.model.SavingsBucketType
import com.sofato.krone.domain.model.SymbolPosition
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

class Converters {

    @TypeConverter
    fun fromLocalDate(value: LocalDate): String = value.toString()

    @TypeConverter
    fun toLocalDate(value: String): LocalDate = LocalDate.parse(value)

    @TypeConverter
    fun fromInstant(value: Instant): Long = value.toEpochMilliseconds()

    @TypeConverter
    fun toInstant(value: Long): Instant = Instant.fromEpochMilliseconds(value)

    @TypeConverter
    fun fromSymbolPosition(value: SymbolPosition): String = value.name

    @TypeConverter
    fun toSymbolPosition(value: String): SymbolPosition = SymbolPosition.valueOf(value)

    @TypeConverter
    fun fromBucketType(value: SavingsBucketType): String = value.name

    @TypeConverter
    fun toBucketType(value: String): SavingsBucketType = SavingsBucketType.valueOf(value)
}
