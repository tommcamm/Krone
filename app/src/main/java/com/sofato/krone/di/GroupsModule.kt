package com.sofato.krone.di

import com.sofato.krone.crypto.Bip39
import com.sofato.krone.crypto.Ed25519Signer
import com.sofato.krone.groups.data.config.DefaultGroupsBuildConfig
import com.sofato.krone.groups.data.config.GroupsBuildConfigProvider
import com.sofato.krone.groups.data.network.GroupsServerApiImpl
import com.sofato.krone.groups.data.network.GroupsSigning
import com.sofato.krone.groups.data.network.UrlPolicy
import com.sofato.krone.groups.domain.repository.GroupsServerApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GroupsModule {

    @Provides
    @Singleton
    @Named("groupsClient")
    fun provideGroupsHttpClient(): HttpClient = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    @Provides
    @Singleton
    fun provideGroupsBuildConfig(): GroupsBuildConfigProvider = DefaultGroupsBuildConfig

    @Provides
    @Singleton
    fun provideUrlPolicy(config: GroupsBuildConfigProvider): UrlPolicy = UrlPolicy(config)

    @Provides
    @Singleton
    fun provideGroupsSigning(signer: Ed25519Signer): GroupsSigning = GroupsSigning(signer)

    @Provides
    @Singleton
    fun provideGroupsServerApi(
        @Named("groupsClient") httpClient: HttpClient,
        signing: GroupsSigning,
        bip39: Bip39,
    ): GroupsServerApi = GroupsServerApiImpl(httpClient, signing, bip39)
}
