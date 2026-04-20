package com.sofato.krone.groups.domain.repository

import com.sofato.krone.groups.domain.model.ServerEnrollment
import kotlinx.coroutines.flow.Flow

interface ServerEnrollmentRepository {
    fun observe(): Flow<ServerEnrollment?>
    suspend fun get(): ServerEnrollment?
    suspend fun save(enrollment: ServerEnrollment)
    suspend fun clear()
}
