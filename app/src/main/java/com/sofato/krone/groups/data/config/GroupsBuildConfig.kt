package com.sofato.krone.groups.data.config

import com.sofato.krone.BuildConfig

/**
 * Typed access to Groups-related `BuildConfig` fields so the rest of the
 * codebase doesn't import the generated class directly. Makes it trivial to
 * stub in tests.
 */
interface GroupsBuildConfigProvider {
    val donatedServerUrl: String
    val donatedServerPkHex: String
    val allowHttp: Boolean
}

object DefaultGroupsBuildConfig : GroupsBuildConfigProvider {
    override val donatedServerUrl: String = BuildConfig.GROUPS_DONATED_SERVER_URL
    override val donatedServerPkHex: String = BuildConfig.GROUPS_DONATED_SERVER_PK_HEX
    override val allowHttp: Boolean = BuildConfig.GROUPS_ALLOW_HTTP
}
