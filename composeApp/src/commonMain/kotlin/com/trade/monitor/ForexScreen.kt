package com.trade.monitor

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trade.monitor.core.UiState
import com.trade.monitor.core.fold
import com.trade.monitor.model.*
import com.trade.monitor.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForexScreen(
    onBack: () -> Unit
) {
    val service: TradeApiService = koinInject()
    var positionsState by remember { mutableStateOf<UiState<ForexPositionsResponse>>(UiState.Loading) }
    var historyState by remember { mutableStateOf<UiState<TradeHistoryResponse>>(UiState.Loading) }
    var autoRefresh by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    fun loadData() {
        positionsState = UiState.Loading
        historyState = UiState.Loading
        scope.launch {
            service.fetchForexPositions().fold(
                onSuccess = { positionsState = UiState.Success(it) },
                onFailure = { positionsState = UiState.Error(it.message ?: "Failed") }
            )
            service.fetchTradeHistory(89).fold(
                onSuccess = { historyState = UiState.Success(it) },
                onFailure = { historyState = UiState.Error(it.message ?: "Failed") }
            )
        }
    }

    LaunchedEffect(Unit) {
        while (autoRefresh) {
            service.fetchForexPositions().fold(
                onSuccess = { positionsState = UiState.Success(it) },
                onFailure = { positionsState = UiState.Error(it.message ?: "Failed") }
            )
            service.fetchTradeHistory(89).fold(
                onSuccess = { historyState = UiState.Success(it) },
                onFailure = { historyState = UiState.Error(it.message ?: "Failed") }
            )
            delay(30_000)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Forex", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                },
                actions = {
                    IconButton(onClick = { loadData() }) { Icon(Icons.Default.Refresh, "Refresh") }
                    IconButton(onClick = { autoRefresh = !autoRefresh }) {
                        Icon(
                            if (autoRefresh) Icons.Default.Sync else Icons.Default.SyncDisabled,
                            "Auto-refresh"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // ── Current Holdings Section ──
            item {
                Text(
                    "Current Holdings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            when (val s = positionsState) {
                is UiState.Loading -> item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                is UiState.Error -> item {
                    ErrorCard(s.message) { loadData() }
                }
                is UiState.Success -> {
                    val data = s.data
                    if (data.positions.isEmpty()) {
                        item { EmptyCard("No open forex positions") }
                    } else {
                        // Summary card
                        item {
                            ForexSummaryCard(
                                totalPnl = data.total_pnl,
                                count = data.count,
                                winners = data.positions.count { it.isProfitable },
                                losers = data.positions.count { !it.isProfitable }
                            )
                        }
                        items(data.positions) { pos ->
                            ForexPositionCard(pos)
                        }
                    }
                }
            }

            // ── Trade History (last 89) ──
            item {
                Spacer(Modifier.height(16.dp))
                Text(
                    "Last 89 Trades",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            when (val s = historyState) {
                is UiState.Loading -> item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                is UiState.Error -> item { ErrorCard(s.message) { loadData() } }
                is UiState.Success -> {
                    val forexTrades = s.data.trades.filter { it.isForex }

                    // Daily PnL breakdown
                    item {
                        DailyPnlBreakdown(
                            trades = forexTrades,
                            label = "Daily PnL"
                        )
                    }

                    // Trade list
                    items(forexTrades) { trade ->
                        TradeRow(trade)
                    }

                    // Total PnL summary
                    item {
                        val totalPnl = forexTrades.sumOf { it.pnl_usd }
                        TotalPnlCard(totalPnl, forexTrades.size)
                    }
                }
            }
        }
    }
}

@Composable
private fun ForexSummaryCard(
    totalPnl: Double,
    count: Int,
    winners: Int,
    losers: Int
) {
    val pnlColor = if (totalPnl >= 0) ProfitGreen else LossRed
    val sign = if (totalPnl >= 0) "+" else ""

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Open Positions", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text("$count", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total PnL", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text(
                    "$sign$${"%.2f".format(totalPnl)}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = pnlColor
                )
            }
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Winners: $winners", fontSize = 12.sp, color = ProfitGreen, fontWeight = FontWeight.Medium)
                Text("Losers: $losers", fontSize = 12.sp, color = LossRed, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun ForexPositionCard(pos: ForexPosition) {
    val pnlColor = if (pos.isProfitable) ProfitGreen else LossRed
    val sign = if (pos.pnl_usd >= 0) "+" else ""
    val dirColor = if (pos.isLong) ProfitGreen else LossRed
    val dirText = if (pos.isLong) "LONG" else "SHORT"

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(pos.pair, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = dirColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        dirText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = dirColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Entry", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${"%.5f".format(pos.entry)}", fontSize = 13.sp)
                }
                Column {
                    Text("Current", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${"%.5f".format(pos.current)}", fontSize = 13.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("PnL", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "$sign$${"%.2f".format(pos.pnl_usd)}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = pnlColor
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            // TP/SL progress bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("TP: ${"%.5f".format(pos.tp)}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("SL: ${"%.5f".format(pos.sl)}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { (pos.tp_progress / 100f).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                color = ProfitGreen,
                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            )
        }
    }
}

@Composable
private fun TradeRow(trade: TradeRecord) {
    val pnlColor = if (trade.pnl_usd > 0) ProfitGreen else if (trade.pnl_usd < 0) LossRed else NeutralGray
    val sign = if (trade.pnl_usd >= 0) "+" else ""
    val statusColor = if (trade.isOpen) Color(0xFF2196F3) else NeutralGray

    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: symbol + side
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(trade.symbol, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = (if (trade.side == "BUY") ProfitGreen else LossRed).copy(alpha = 0.12f)
                    ) {
                        Text(
                            trade.side,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (trade.side == "BUY") ProfitGreen else LossRed,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
                Text(
                    "${trade.displayDate} ${trade.displayTime}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Right: PnL
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "$sign$${"%.2f".format(trade.pnl_usd)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = pnlColor
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = statusColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        trade.status,
                        fontSize = 9.sp,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyPnlBreakdown(trades: List<TradeRecord>, label: String) {
    // Group trades by normalized date, sum PnL per day
    val dailyPnl = trades.groupBy { it.normalizedDate }
        .mapValues { (_, dayTrades) -> dayTrades.sumOf { it.pnl_usd } }
        .toList()
        .sortedByDescending { it.first }
        .take(10) // last 10 days

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
            Spacer(Modifier.height(8.dp))
            dailyPnl.forEach { (date, pnl) ->
                val pnlColor = if (pnl >= 0) ProfitGreen else LossRed
                val sign = if (pnl >= 0) "+" else ""
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        date.substring(5).replace("-", "/"),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        "$sign$${"%.2f".format(pnl)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = pnlColor
                    )
                }
            }
            if (dailyPnl.isEmpty()) {
                Text("No data", fontSize = 12.sp, color = NeutralGray)
            }
        }
    }
}

@Composable
private fun TotalPnlCard(totalPnl: Double, tradeCount: Int) {
    val pnlColor = if (totalPnl >= 0) ProfitGreen else LossRed
    val sign = if (totalPnl >= 0) "+" else ""

    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Total PnL", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Spacer(Modifier.height(4.dp))
            Text(
                "$sign$${"%.2f".format(totalPnl)}",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = pnlColor
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "From $tradeCount trades",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun ErrorCard(message: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = LossRed.copy(alpha = 0.08f))
    ) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.CloudOff, null, tint = LossRed, modifier = Modifier.size(36.dp))
            Spacer(Modifier.height(8.dp))
            Text("Connection Error", fontWeight = FontWeight.Bold, color = LossRed)
            Text(message, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Button(onClick = onRetry) { Text("Retry") }
        }
    }
}

@Composable
private fun EmptyCard(msg: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
            Text(msg, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
