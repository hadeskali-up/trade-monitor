package com.trade.monitor.model

import kotlinx.serialization.Serializable

@Serializable
data class ForexPositionsResponse(
    val positions: List<ForexPosition> = emptyList(),
    val count: Int = 0,
    val total_pnl: Double = 0.0
)

@Serializable
data class ForexPosition(
    val pair: String = "",
    val epic: String = "",
    val direction: String = "",
    val size: Double = 0.0,
    val entry: Double = 0.0,
    val level: Double = 0.0,
    val bid: Double = 0.0,
    val offer: Double = 0.0,
    val current: Double = 0.0,
    val pnl_usd: Double = 0.0,
    val pnl_pct: Double = 0.0,
    val limit: Double = 0.0,
    val stop: Double = 0.0,
    val tp: Double = 0.0,
    val sl: Double = 0.0,
    val tp_progress: Double = 0.0,
    val sl_progress: Double = 0.0,
    val market_value: Double = 0.0
) {
    val isProfitable: Boolean get() = pnl_usd >= 0
    val isLong: Boolean get() = direction.equals("BUY", ignoreCase = true)
}
