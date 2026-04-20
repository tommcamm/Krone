package com.sofato.krone.groups.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ServerInfoResponseDto(
    @SerialName("protocol_version") val protocolVersion: String,
    @SerialName("server_version") val serverVersion: String,
    @SerialName("server_pk") val serverPk: String,
    @SerialName("policy") val policy: ServerPolicyDto,
)

@Serializable
data class ServerPolicyDto(
    @SerialName("ttl_seconds") val ttlSeconds: Int,
    @SerialName("max_envelope_bytes") val maxEnvelopeBytes: Int,
    @SerialName("max_inbox_per_device") val maxInboxPerDevice: Int,
    @SerialName("max_envelopes_per_device_per_hour") val maxEnvelopesPerDevicePerHour: Int,
    @SerialName("clock_skew_seconds") val clockSkewSeconds: Int,
)

@Serializable
data class DeviceRegistrationRequestDto(
    @SerialName("device_id") val deviceId: String,
    @SerialName("identity_pk") val identityPk: String,
)

@Serializable
data class DeviceRegistrationResponseDto(
    @SerialName("device_id") val deviceId: String,
    @SerialName("registered_at") val registeredAt: String,
)

@Serializable
data class ErrorResponseDto(val error: ErrorBodyDto)

@Serializable
data class ErrorBodyDto(val code: String, val message: String)
