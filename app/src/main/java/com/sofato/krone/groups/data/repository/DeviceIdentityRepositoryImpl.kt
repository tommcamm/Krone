package com.sofato.krone.groups.data.repository

import com.sofato.krone.crypto.Bip39
import com.sofato.krone.crypto.CanonicalSigning
import com.sofato.krone.crypto.Ed25519Signer
import com.sofato.krone.crypto.FingerprintComputer
import com.sofato.krone.crypto.HexCodec
import com.sofato.krone.crypto.KeystoreWrapper
import com.sofato.krone.groups.data.db.dao.DeviceIdentityDao
import com.sofato.krone.groups.data.db.entity.DeviceIdentityEntity
import com.sofato.krone.groups.domain.model.DeviceIdentity
import com.sofato.krone.groups.domain.repository.DeviceIdentityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DeviceIdentityRepositoryImpl @Inject constructor(
    private val dao: DeviceIdentityDao,
    private val signer: Ed25519Signer,
    private val keystore: KeystoreWrapper,
    private val bip39: Bip39,
) : DeviceIdentityRepository {

    override fun observe(): Flow<DeviceIdentity?> = dao.observe().map { it?.toDomain() }

    override suspend fun get(): DeviceIdentity? = dao.get()?.toDomain()

    override suspend fun getOrCreate(): DeviceIdentity {
        dao.get()?.let { return it.toDomain() }
        val keypair = signer.generateKeypair()
        val wrapped = keystore.wrap(keypair.secretKey)
        val entity = DeviceIdentityEntity(
            sigPk = keypair.publicKey,
            sigSkEncIv = wrapped.iv,
            sigSkEnc = wrapped.ciphertext,
            createdAt = System.currentTimeMillis(),
        )
        dao.upsert(entity)
        return entity.toDomain()
    }

    override suspend fun clear() {
        dao.clear()
        keystore.clear()
    }

    private fun DeviceIdentityEntity.toDomain(): DeviceIdentity {
        val sk = keystore.unwrap(KeystoreWrapper.WrappedBlob(sigSkEncIv, sigSkEnc))
        val deviceIdHex = HexCodec.encode(CanonicalSigning.sha256(sigPk).copyOfRange(0, 16))
        return DeviceIdentity(
            deviceIdHex = deviceIdHex,
            identitySigPk = sigPk,
            identitySigSk = sk,
            fingerprint = FingerprintComputer.fromPublicKey(sigPk, bip39),
            createdAtEpochMs = createdAt,
        )
    }
}
