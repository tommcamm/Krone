package com.sofato.krone.data.repository

import com.sofato.krone.data.db.dao.MonthlySnapshotDao
import com.sofato.krone.data.db.entity.toDomain
import com.sofato.krone.data.db.entity.toEntity
import com.sofato.krone.domain.model.MonthlySnapshot
import com.sofato.krone.domain.repository.MonthlySnapshotRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MonthlySnapshotRepositoryImpl @Inject constructor(
    private val monthlySnapshotDao: MonthlySnapshotDao,
) : MonthlySnapshotRepository {

    override fun getAllSnapshots(): Flow<List<MonthlySnapshot>> =
        monthlySnapshotDao.getAllSnapshots().map { list -> list.map { it.toDomain() } }

    override suspend fun getSnapshotForMonth(month: String): MonthlySnapshot? =
        monthlySnapshotDao.getSnapshotForMonth(month)?.toDomain()

    override suspend fun upsert(snapshot: MonthlySnapshot): Long =
        monthlySnapshotDao.upsert(snapshot.toEntity())
}
