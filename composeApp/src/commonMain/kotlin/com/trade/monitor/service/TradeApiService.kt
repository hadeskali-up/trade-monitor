package com.trade.monitor.service

import com.trade.monitor.model.TradeHistoryResponse
import com.trade.monitor.model.ForexPositionsResponse
import com.trade.monitor.model.CryptoPositionsResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class TradeApiService(
    private val baseUrl: String = "https://bridge.alisuhari.top"
) {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 15_000
            socketTimeoutMillis = 30_000
        }
    }

    suspend fun fetchTradeHistory(limit: Int = 89): Result<TradeHistoryResponse> = runCatching {
        client.get("$baseUrl/api/trade-history?limit=$limit").body()
    }

    suspend fun fetchForexPositions(): Result<ForexPositionsResponse> = runCatching {
        client.get("$baseUrl/api/forex-positions").body()
    }

    suspend fun fetchCryptoPositions(): Result<CryptoPositionsResponse> = runCatching {
        client.get("$baseUrl/api/crypto-positions").body()
    }

    fun cleanup() {
        client.close()
    }
}
