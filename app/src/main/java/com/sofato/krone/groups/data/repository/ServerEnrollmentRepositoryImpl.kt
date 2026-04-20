package com.sofato.krone.groups.data.repository

import com.sofato.krone.crypto.Bip39
import com.sofato.krone.crypto.FingerprintComputer
import com.sofato.krone.groups.data.db.dao.ServerEnrollmentDao
import com.sofato.krone.groups.data.db.entity.ServerEnrollmentEntity
import com.sofato.krone.groups.domain.model.ServerEnrollment
import com.sofato.krone.groups.domain.repository.ServerEnrollmentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ServerEnrollmentRepositoryImpl @Inject constructor(
    private val dao: ServerEnrollmentDao,
    private val bip39: Bip39,
) : ServerEnrollmentRepository {

    override fun observe(): Flow<ServerEnrollment?> = dao.observe().map { it?.toDomain() }

    override suspend fun get(): ServerEnrollment? = dao.get()?.toDomain()

    override suspend fun save(enrollment: ServerEnrollment) {
        dao.upsert(
            ServerEnrollmentEntity(
                url = enrollment.url,
                serverSigPk = enrollment.serverSigPk,
                fingerprintWords = enrollment.fingerprint.words.joinToString(" "),
                fingerprintHex = enrollment.fingerprint.shortHex,
                enrolledAt = enrollment.enrolledAtEpochMs,
            )
        )
    }

    override suspend fun clear() = dao.clear()

    private fun ServerEnrollmentEntity.toDomain(): ServerEnrollment {
        val fingerprint = FingerprintComputer.fromPublicKey(serverSigPk, bip39)
        return ServerEnrollment(
            url = url,
            serverSigPk = serverSigPk,
            fingerprint = fingerprint,
            enrolledAtEpochMs = enrolledAt,
        )
    }
}
