package com.sofato.krone.groups.domain.repository

import com.sofato.krone.groups.domain.model.DeviceIdentity
import kotlinx.coroutines.flow.Flow

interface DeviceIdentityRepository {
    fun observe(): Flow<DeviceIdentity?>
    suspend fun get(): DeviceIdentity?
    suspend fun getOrCreate(): DeviceIdentity
    suspend fun clear()
}
