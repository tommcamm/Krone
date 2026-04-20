package com.sofato.krone.groups.data.network

import com.sofato.krone.crypto.Fingerprint
import com.sofato.krone.crypto.FingerprintComputer
import com.sofato.krone.crypto.Bip39
import com.sofato.krone.crypto.HexCodec
import com.sofato.krone.groups.data.network.dto.DeviceRegistrationRequestDto
import com.sofato.krone.groups.data.network.dto.DeviceRegistrationResponseDto
import com.sofato.krone.groups.data.network.dto.ServerInfoResponseDto
import com.sofato.krone.groups.domain.model.DeviceIdentity
import com.sofato.krone.groups.domain.model.DeviceRegistrationMismatch
import com.sofato.krone.groups.domain.model.PendingEnrollment
import com.sofato.krone.groups.domain.model.ServerPolicy
import com.sofato.krone.groups.domain.repository.GroupsServerApi
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsBytes
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.serialization.json.Json

class GroupsServerApiImpl(
    private val httpClient: HttpClient,
    private val signing: GroupsSigning,
    private val bip39: Bip39,
    private val json: Json = Json { ignoreUnknownKeys = true },
) : GroupsServerApi {

    override suspend fun fetchServerInfo(baseUrl: String): PendingEnrollment {
        val url = baseUrl.trimEnd('/') + PATH_SERVER_INFO
        val response = httpClient.get(url)
        val bodyText = response.bodyAsText()
        val dto = json.decodeFromString(ServerInfoResponseDto.serializer(), bodyText)
        val serverPk = HexCodec.decode(dto.serverPk)
        val fingerprint: Fingerprint = FingerprintComputer.fromPublicKey(serverPk, bip39)
        return PendingEnrollment(
            url = baseUrl,
            serverSigPk = serverPk,
            fingerprint = fingerprint,
            protocolVersion = dto.protocolVersion,
            serverVersion = dto.serverVersion,
            policy = ServerPolicy(
                ttlSeconds = dto.policy.ttlSeconds,
                maxEnvelopeBytes = dto.policy.maxEnvelopeBytes,
                maxInboxPerDevice = dto.policy.maxInboxPerDevice,
                maxEnvelopesPerDevicePerHour = dto.policy.maxEnvelopesPerDevicePerHour,
                clockSkewSeconds = dto.policy.clockSkewSeconds,
            ),
        )
    }

    override suspend fun registerDevice(
        baseUrl: String,
        identity: DeviceIdentity,
        serverSigPk: ByteArray,
    ) {
        val body = DeviceRegistrationRequestDto(
            deviceId = identity.deviceIdHex,
            identityPk = HexCodec.encode(identity.identitySigPk),
        )
        val bodyJson = json.encodeToString(DeviceRegistrationRequestDto.serializer(), body)
        val bodyBytes = bodyJson.toByteArray(Charsets.UTF_8)
        val headers = signing.signRequest(identity, "POST", PATH_DEVICES, bodyBytes)

        val response = httpClient.post(baseUrl.trimEnd('/') + PATH_DEVICES) {
            contentType(ContentType.Application.Json)
            header(HDR_DEVICE_ID, headers.deviceId)
            header(HDR_TIMESTAMP, headers.timestamp)
            header(HDR_SIGNATURE, headers.signature)
            setBody(bodyBytes)
        }
        verifyAndDecodeRegistration(response, serverSigPk, identity.deviceIdHex)
    }

    override suspend fun deleteSelf(
        baseUrl: String,
        identity: DeviceIdentity,
        serverSigPk: ByteArray,
    ) {
        val headers = signing.signRequest(identity, "DELETE", PATH_DEVICES_SELF, ByteArray(0))
        val response = httpClient.delete(baseUrl.trimEnd('/') + PATH_DEVICES_SELF) {
            header(HDR_DEVICE_ID, headers.deviceId)
            header(HDR_TIMESTAMP, headers.timestamp)
            header(HDR_SIGNATURE, headers.signature)
        }
        verifyResponseSignature(response, serverSigPk)
    }

    private suspend fun verifyAndDecodeRegistration(
        response: HttpResponse,
        serverSigPk: ByteArray,
        expectedDeviceId: String,
    ) {
        val body = verifyResponseSignature(response, serverSigPk)
        val dto = json.decodeFromString(DeviceRegistrationResponseDto.serializer(), body.toString(Charsets.UTF_8))
        if (dto.deviceId != expectedDeviceId) {
            throw DeviceRegistrationMismatch(expectedDeviceId, dto.deviceId)
        }
    }

    private suspend fun verifyResponseSignature(
        response: HttpResponse,
        serverSigPk: ByteArray,
    ): ByteArray {
        val body = response.bodyAsBytes()
        val requestId = response.headers[HDR_REQUEST_ID]
            ?: throw IllegalStateException("Missing $HDR_REQUEST_ID response header")
        val sig = response.headers[HDR_SERVER_SIG]
            ?: throw IllegalStateException("Missing $HDR_SERVER_SIG response header")
        signing.verifyResponse(
            serverSigPk = serverSigPk,
            requestId = requestId,
            statusCode = response.status.value,
            body = body,
            signatureBase64 = sig,
        )
        return body
    }

    private suspend fun HttpResponse.bodyAsText(): String =
        bodyAsBytes().toString(Charsets.UTF_8)

    companion object {
        private const val PATH_SERVER_INFO = "/server-info"
        private const val PATH_DEVICES = "/devices"
        private const val PATH_DEVICES_SELF = "/devices/self"

        private const val HDR_DEVICE_ID = "x-krone-device-id"
        private const val HDR_TIMESTAMP = "x-krone-timestamp"
        private const val HDR_SIGNATURE = "x-krone-signature"
        private const val HDR_REQUEST_ID = "x-request-id"
        private const val HDR_SERVER_SIG = "x-server-signature"
    }
}
