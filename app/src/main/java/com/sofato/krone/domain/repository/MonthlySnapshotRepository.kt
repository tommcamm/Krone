package com.sofato.krone.domain.repository

import com.sofato.krone.domain.model.MonthlySnapshot
import kotlinx.coroutines.flow.Flow

interface MonthlySnapshotRepository {
    fun getAllSnapshots(): Flow<List<MonthlySnapshot>>
    suspend fun getSnapshotForMonth(month: String): MonthlySnapshot?
    suspend fun upsert(snapshot: MonthlySnapshot): Long
}
