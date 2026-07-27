package com.trade.monitor.model

import kotlinx.serialization.Serializable

@Serializable
data class CryptoPositionsResponse(
    val positions: List<CryptoPosition> = emptyList(),
    val count: Int = 0
)

@Serializable
data class CryptoPosition(
    val symbol: String = "",
    val raw_symbol: String = "",
    val qty: Double = 0.0,
    val entry: Double = 0.0,
    val current: Double = 0.0,
    val pnl_usd: Double = 0.0,
    val pnl_pct: Double = 0.0,
    val tp: Double = 0.0,
    val sl: Double = 0.0,
    val tp_progress: Double = 0.0,
    val sl_progress: Double = 0.0,
    val market_value: Double = 0.0
) {
    val isProfitable: Boolean get() = pnl_usd >= 0
}
