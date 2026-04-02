package com.sofato.krone.data.network

import com.sofato.krone.data.network.dto.FrankfurterResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import javax.inject.Inject

class FrankfurterApi @Inject constructor(
    private val httpClient: HttpClient,
) {
    suspend fun getLatestRates(): FrankfurterResponse {
        return httpClient.get("$BASE_URL/latest?base=EUR").body()
    }

    companion object {
        private const val BASE_URL = "https://api.frankfurter.app"
    }
}
