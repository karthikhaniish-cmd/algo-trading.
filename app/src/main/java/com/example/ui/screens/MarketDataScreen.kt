package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.sample.SampleDataGenerator
import com.example.model.StockDataset
import com.example.ui.components.DatasetTypeBadge
import com.example.ui.components.StatCard
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.RubyRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.TradingUiState
import java.util.Locale

@Composable
fun MarketDataScreen(
    state: TradingUiState,
    onSelectDataset: (String) -> Unit,
    onDeleteDataset: (String) -> Unit,
    onImportCsv: (symbol: String, name: String, desc: String, csv: String) -> Unit,
    onCreateSynthetic: (symbol: String, name: String, desc: String, startPrice: Double, vol: Double, drift: Double, bars: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Datasets, 1: Import CSV, 2: Generator
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "MARKET DATA & DATASETS",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = CyanAccent
        )
        Text(
            text = "Manage historical datasets, upload CSV files, or generate synthetic test data.",
            fontSize = 12.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = NavySurface,
            contentColor = CyanAccent,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, NavyBorder, RoundedCornerShape(8.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Active Datasets", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("CSV Import", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Synthetic Gen", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Column(modifier = Modifier.verticalScroll(scrollState)) {
            when (selectedTab) {
                0 -> ActiveDatasetsTab(
                    datasets = state.datasets,
                    activeDataset = state.selectedDataset,
                    bars = state.bars,
                    onSelectDataset = onSelectDataset,
                    onDeleteDataset = onDeleteDataset
                )
                1 -> CsvImportTab(
                    validation = state.csvValidationResult,
                    onImportCsv = onImportCsv
                )
                2 -> SyntheticGeneratorTab(
                    onCreateSynthetic = { sym, name, desc, price, vol, drift, bars ->
                        onCreateSynthetic(sym, name, desc, price, vol, drift, bars)
                        selectedTab = 0
                    }
                )
            }
        }
    }
}

@Composable
private fun ActiveDatasetsTab(
    datasets: List<StockDataset>,
    activeDataset: StockDataset?,
    bars: List<com.example.model.PriceBar>,
    onSelectDataset: (String) -> Unit,
    onDeleteDataset: (String) -> Unit
) {
    Column {
        Text(
            text = "AVAILABLE DATASETS (${datasets.size})",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        datasets.forEach { ds ->
            val isSelected = ds.id == activeDataset?.id
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) NavySurfaceVariant else NavySurface
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .border(
                        1.dp,
                        if (isSelected) CyanAccent else NavyBorder,
                        RoundedCornerShape(10.dp)
                    )
                    .clickable { onSelectDataset(ds.id) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = ds.symbol,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) CyanAccent else TextPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            DatasetTypeBadge(type = ds.type)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = ds.name, fontSize = 12.sp, color = TextSecondary)
                        Text(text = ds.description, fontSize = 11.sp, color = TextMuted, maxLines = 1)
                    }

                    if (datasets.size > 1) {
                        IconButton(onClick = { onDeleteDataset(ds.id) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Dataset",
                                tint = RubyRed.copy(alpha = 0.7f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Dataset Summary Statistics
        if (activeDataset != null && bars.isNotEmpty()) {
            Text(
                text = "DATASET INSPECTION: ${activeDataset.symbol}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = CyanAccent,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            val highest = bars.maxOf { it.high }
            val lowest = bars.minOf { it.low }
            val avgVol = (bars.sumOf { it.volume }.toDouble() / bars.size).toLong()

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard("Bars Count", bars.size.toString(), modifier = Modifier.weight(1f))
                StatCard("Start Date", bars.first().date, modifier = Modifier.weight(1f))
                StatCard("End Date", bars.last().date, modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard("Range High", String.format(Locale.US, "$%.2f", highest), modifier = Modifier.weight(1f))
                StatCard("Range Low", String.format(Locale.US, "$%.2f", lowest), modifier = Modifier.weight(1f))
                StatCard("Avg Volume", formatVol(avgVol), modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Recent OHLCV Preview Table
            Text(
                text = "BAR PREVIEW (RECENT 5 BARS)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(NavySurface)
                    .border(1.dp, NavyBorder, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("Date", modifier = Modifier.weight(1.3f), fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                        Text("Open", modifier = Modifier.weight(1f), fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                        Text("High", modifier = Modifier.weight(1f), fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                        Text("Low", modifier = Modifier.weight(1f), fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                        Text("Close", modifier = Modifier.weight(1f), fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    bars.takeLast(5).forEach { bar ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                            Text(bar.date, modifier = Modifier.weight(1.3f), fontSize = 11.sp, color = TextPrimary)
                            Text(String.format(Locale.US, "%.2f", bar.open), modifier = Modifier.weight(1f), fontSize = 11.sp, color = TextSecondary)
                            Text(String.format(Locale.US, "%.2f", bar.high), modifier = Modifier.weight(1f), fontSize = 11.sp, color = EmeraldGreen)
                            Text(String.format(Locale.US, "%.2f", bar.low), modifier = Modifier.weight(1f), fontSize = 11.sp, color = RubyRed)
                            Text(String.format(Locale.US, "%.2f", bar.close), modifier = Modifier.weight(1f), fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CsvImportTab(
    validation: com.example.model.CsvValidationResult?,
    onImportCsv: (symbol: String, name: String, desc: String, csv: String) -> Unit
) {
    var symbol by remember { mutableStateOf("GOOGL") }
    var name by remember { mutableStateOf("Alphabet Inc.") }
    var desc by remember { mutableStateOf("Historical equity daily bars") }

    val sampleCsvTemplate = remember {
        """Date,Open,High,Low,Close,Volume
2026-01-02,140.50,142.10,139.80,141.90,1850000
2026-01-05,142.00,144.50,141.20,143.80,2100000
2026-01-06,143.50,145.00,142.60,144.20,1950000
2026-01-07,144.00,146.20,143.80,145.90,2300000
2026-01-08,146.10,147.00,144.50,145.10,2050000
2026-01-09,145.00,148.50,144.80,147.90,2600000
2026-01-12,148.00,150.20,147.10,149.80,2850000
2026-01-13,150.00,151.30,148.90,150.60,2400000
2026-01-14,151.00,152.00,149.50,151.40,2150000
2026-01-15,151.20,153.40,150.80,152.90,2700000"""
    }

    var csvText by remember { mutableStateOf(sampleCsvTemplate) }

    Column {
        Text(
            text = "UPLOAD OR PASTE CSV DATA",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = CyanAccent,
            letterSpacing = 0.5.sp
        )
        Text(
            text = "Required columns: Date, Open, High, Low, Close, Volume. Validates chronological dates and positive OHLC integrity.",
            fontSize = 11.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = symbol,
                onValueChange = { symbol = it },
                label = { Text("Symbol", fontSize = 12.sp) },
                colors = textFieldColors(),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Company / Asset Name", fontSize = 12.sp) },
                colors = textFieldColors(),
                modifier = Modifier.weight(2f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = desc,
            onValueChange = { desc = it },
            label = { Text("Description", fontSize = 12.sp) },
            colors = textFieldColors(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = csvText,
            onValueChange = { csvText = it },
            label = { Text("CSV Text Data (Paste or Edit)", fontSize = 12.sp) },
            colors = textFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            maxLines = 10
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Validation Results Card
        if (validation != null) {
            val isSuccess = validation.isValid
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isSuccess) EmeraldGreen.copy(alpha = 0.15f) else RubyRed.copy(alpha = 0.15f)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (isSuccess) EmeraldGreen else RubyRed,
                        RoundedCornerShape(8.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                            contentDescription = null,
                            tint = if (isSuccess) EmeraldGreen else RubyRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isSuccess) "CSV Validated Successfully (${validation.parsedCount} bars)" else "CSV Validation Failed",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSuccess) EmeraldGreen else RubyRed
                        )
                    }
                    if (validation.errors.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        validation.errors.take(4).forEach { err ->
                            Text("• $err", fontSize = 11.sp, color = RubyRed)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        Button(
            onClick = { onImportCsv(symbol, name, desc, csvText) },
            colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color(0xFF070C18)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("import_csv_button")
        ) {
            Icon(imageVector = Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Validate & Save Dataset", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SyntheticGeneratorTab(
    onCreateSynthetic: (symbol: String, name: String, desc: String, startPrice: Double, vol: Double, drift: Double, bars: Int) -> Unit
) {
    var symbol by remember { mutableStateOf("NVDA") }
    var name by remember { mutableStateOf("NVIDIA Corp (Synthetic)") }
    var startPrice by remember { mutableStateOf("120.0") }
    var volatility by remember { mutableStateOf("0.35") } // 35% annualized volatility
    var drift by remember { mutableStateOf("0.25") }      // 25% annual drift
    var barsCount by remember { mutableStateOf("150") }

    Column {
        Text(
            text = "SYNTHETIC MARKET GENERATOR",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = CyanAccent,
            letterSpacing = 0.5.sp
        )
        Text(
            text = "Uses Geometric Brownian Motion with stochastic intraday wicks to model realistic prices without fabricating live data.",
            fontSize = 11.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = symbol,
                onValueChange = { symbol = it },
                label = { Text("Symbol", fontSize = 12.sp) },
                colors = textFieldColors(),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name", fontSize = 12.sp) },
                colors = textFieldColors(),
                modifier = Modifier.weight(2f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = startPrice,
                onValueChange = { startPrice = it },
                label = { Text("Starting Price ($)", fontSize = 12.sp) },
                colors = textFieldColors(),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = barsCount,
                onValueChange = { barsCount = it },
                label = { Text("Number of Bars", fontSize = 12.sp) },
                colors = textFieldColors(),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = volatility,
                onValueChange = { volatility = it },
                label = { Text("Daily Volatility (0.1 - 0.7)", fontSize = 12.sp) },
                colors = textFieldColors(),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = drift,
                onValueChange = { drift = it },
                label = { Text("Annual Drift (e.g. 0.15)", fontSize = 12.sp) },
                colors = textFieldColors(),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = {
                val p = startPrice.toDoubleOrNull() ?: 100.0
                val v = volatility.toDoubleOrNull() ?: 0.30
                val d = drift.toDoubleOrNull() ?: 0.15
                val b = barsCount.toIntOrNull() ?: 120
                onCreateSynthetic(
                    symbol,
                    name,
                    "Generated via Geometric Brownian Motion (Vol=${v}, Drift=${d})",
                    p, v, d, b
                )
            },
            colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color(0xFF070C18)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("generate_synthetic_button")
        ) {
            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Generate & Load Dataset", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = CyanAccent,
    unfocusedBorderColor = NavyBorder,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedContainerColor = NavySurface,
    unfocusedContainerColor = NavySurface
)

private fun formatVol(volume: Long): String {
    return when {
        volume >= 1_000_000 -> String.format(Locale.US, "%.1fM", volume / 1_000_000.0)
        volume >= 1_000 -> String.format(Locale.US, "%.1fK", volume / 1_000.0)
        else -> volume.toString()
    }
}
