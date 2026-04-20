package com.sofato.krone.groups.data.network

import com.sofato.krone.groups.data.config.GroupsBuildConfigProvider
import com.sofato.krone.groups.domain.model.InsecureSchemeNotAllowed
import com.sofato.krone.groups.domain.model.InvalidServerUrl

/**
 * Enforces the scheme policy before we even make a network call: https is
 * always fine; http is fine only when `BuildConfig.GROUPS_ALLOW_HTTP` is
 * true (set on debug builds for local server testing).
 */
class UrlPolicy(private val config: GroupsBuildConfigProvider) {
    fun validate(url: String): String {
        val trimmed = url.trim()
        val lower = trimmed.lowercase()
        when {
            lower.startsWith("https://") -> return trimmed
            lower.startsWith("http://") -> {
                if (!config.allowHttp) throw InsecureSchemeNotAllowed(trimmed)
                return trimmed
            }
            else -> throw InvalidServerUrl(trimmed)
        }
    }
}
