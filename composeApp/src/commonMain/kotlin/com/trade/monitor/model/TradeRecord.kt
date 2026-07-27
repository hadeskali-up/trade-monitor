package com.trade.monitor.model

import kotlinx.serialization.Serializable

@Serializable
data class TradeHistoryResponse(
    val trades: List<TradeRecord> = emptyList(),
    val count: Int = 0,
    val total_pnl: Double = 0.0
)

@Serializable
data class TradeRecord(
    val source: String = "",
    val symbol: String = "",
    val side: String = "",
    val entry: Double = 0.0,
    val exit: Double = 0.0,
    val qty: Double = 0.0,
    val pnl_usd: Double = 0.0,
    val pnl_pct: Double = 0.0,
    val status: String = "",
    val date: String = "",
    val deal_ref: String? = null
) {
    val isProfitable: Boolean get() = pnl_usd > 0
    val isOpen: Boolean get() = status == "open"
    val isClosed: Boolean get() = status == "closed" || status == "filled"
    val isForex: Boolean get() = source == "forex"
    val isCrypto: Boolean get() = source == "crypto"

    val normalizedDate: String get() {
        return if (date.contains("T")) {
            date.substringBefore("T")
        } else if (date.length >= 8) {
            // DD/MM/YY -> YYYY-MM-DD
            val parts = date.split("/")
            if (parts.size == 3) {
                "20${parts[2]}-${parts[1].padStart(2, '0')}-${parts[0].padStart(2, '0')}"
            } else date
        } else date
    }

    val displayDate: String get() {
        val d = normalizedDate
        return if (d.length >= 10) {
            "${d.substring(5, 7)}/${d.substring(8, 10)}"
        } else d
    }

    val displayTime: String get() {
        return if (date.contains("T")) {
            date.substringAfter("T").substringBefore(".").take(5)
        } else ""
    }
}
