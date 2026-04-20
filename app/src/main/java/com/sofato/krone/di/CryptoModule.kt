package com.sofato.krone.di

import com.goterl.lazysodium.LazySodiumAndroid
import com.goterl.lazysodium.SodiumAndroid
import com.goterl.lazysodium.interfaces.Sign
import com.sofato.krone.crypto.Bip39
import com.sofato.krone.crypto.Ed25519Signer
import com.sofato.krone.crypto.KeystoreWrapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CryptoModule {

    @Provides
    @Singleton
    fun provideLazySodium(): LazySodiumAndroid = LazySodiumAndroid(SodiumAndroid())

    @Provides
    @Singleton
    fun provideSignNative(lazySodium: LazySodiumAndroid): Sign.Native = lazySodium

    @Provides
    @Singleton
    fun provideEd25519Signer(sign: Sign.Native): Ed25519Signer = Ed25519Signer(sign)

    @Provides
    @Singleton
    fun provideKeystoreWrapper(): KeystoreWrapper = KeystoreWrapper()

    @Provides
    @Singleton
    fun provideBip39(): Bip39 = Bip39.loadEnglish()
}
