package com.sofato.krone.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.sofato.krone.data.db.entity.BudgetAllocationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetAllocationDao {

    @Query("SELECT * FROM budget_allocation WHERE month = :month ORDER BY categoryId ASC")
    fun getAllocationsForMonth(month: String): Flow<List<BudgetAllocationEntity>>

    @Query("SELECT * FROM budget_allocation WHERE id = :id")
    suspend fun getById(id: Long): BudgetAllocationEntity?

    @Insert
    suspend fun insert(entity: BudgetAllocationEntity): Long

    @Update
    suspend fun update(entity: BudgetAllocationEntity)

    @Query("DELETE FROM budget_allocation WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<BudgetAllocationEntity>)
}
