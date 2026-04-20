package com.sofato.krone.groups.domain.model

/** Pinned donated-server fingerprint did not match what the server returned. */
class PinnedFingerprintMismatch(
    val expectedHex: String,
    val actualHex: String,
) : Exception("Donated server fingerprint mismatch: expected=$expectedHex actual=$actualHex")

/** User attempted to use an `http://` URL on a build that disallows it. */
class InsecureSchemeNotAllowed(url: String) :
    Exception("http:// URLs are only allowed in debug builds (got $url)")

/** URL could not be parsed or lacks a scheme. */
class InvalidServerUrl(url: String, cause: Throwable? = null) :
    Exception("Invalid groups server URL: $url", cause)

/** Server's response signature did not verify against the enrolled pubkey. */
class ServerResponseSignatureInvalid(requestId: String) :
    Exception("Server response signature invalid (request_id=$requestId)")

/** Device-id in the server's /devices response does not match what we sent. */
class DeviceRegistrationMismatch(expected: String, actual: String) :
    Exception("Device registration mismatch: expected=$expected actual=$actual")
