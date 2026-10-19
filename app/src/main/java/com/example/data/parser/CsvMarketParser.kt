package com.example.data.parser

import com.example.model.CsvValidationResult
import com.example.model.PriceBar
import java.text.SimpleDateFormat
import java.util.Locale

object CsvMarketParser {

    private val supportedDateFormats = listOf(
        SimpleDateFormat("yyyy-MM-dd", Locale.US),
        SimpleDateFormat("MM/dd/yyyy", Locale.US),
        SimpleDateFormat("yyyy/MM/dd", Locale.US),
        SimpleDateFormat("dd-MM-yyyy", Locale.US)
    )

    fun parseAndValidateCsv(csvContent: String): CsvValidationResult {
        val lines = csvContent.lines().map { it.trim() }.filter { it.isNotEmpty() }
        val errors = ArrayList<String>()

        if (lines.isEmpty()) {
            return CsvValidationResult(
                isValid = false,
                errors = listOf("The provided CSV file is completely empty."),
                validBars = emptyList()
            )
        }

        // Parse header
        val header = lines.first().split(",").map { it.trim().lowercase(Locale.US) }
        val dateIdx = header.indexOfFirst { it == "date" || it == "timestamp" || it == "time" }
        val openIdx = header.indexOfFirst { it == "open" }
        val highIdx = header.indexOfFirst { it == "high" }
        val lowIdx = header.indexOfFirst { it == "low" }
        val closeIdx = header.indexOfFirst { it == "close" || it == "adj close" }
        val volumeIdx = header.indexOfFirst { it == "volume" || it == "vol" }

        if (dateIdx == -1) errors.add("Missing required 'Date' column in header.")
        if (openIdx == -1) errors.add("Missing required 'Open' column in header.")
        if (highIdx == -1) errors.add("Missing required 'High' column in header.")
        if (lowIdx == -1) errors.add("Missing required 'Low' column in header.")
        if (closeIdx == -1) errors.add("Missing required 'Close' column in header.")
        if (volumeIdx == -1) errors.add("Missing required 'Volume' column in header.")

        if (errors.isNotEmpty()) {
            return CsvValidationResult(
                isValid = false,
                errors = errors,
                validBars = emptyList()
            )
        }

        val validBars = ArrayList<PriceBar>()
        val seenDates = HashSet<String>()
        var skippedCount = 0

        for (lineNum in 2..lines.size) {
            val line = lines[lineNum - 1]
            val cols = line.split(",").map { it.trim() }

            val minExpectedCols = maxOf(dateIdx, openIdx, highIdx, lowIdx, closeIdx, volumeIdx) + 1
            if (cols.size < minExpectedCols) {
                if (errors.size < 10) errors.add("Line $lineNum: Insufficient columns (expected at least $minExpectedCols, found ${cols.size}).")
                skippedCount++
                continue
            }

            val rawDate = cols[dateIdx]
            val normalizedDate = normalizeDate(rawDate)
            if (normalizedDate == null) {
                if (errors.size < 10) errors.add("Line $lineNum: Invalid date format '$rawDate'. Expected YYYY-MM-DD or MM/DD/YYYY.")
                skippedCount++
                continue
            }

            if (seenDates.contains(normalizedDate)) {
                if (errors.size < 10) errors.add("Line $lineNum: Duplicate date detected ($normalizedDate). Duplicate dates are not allowed.")
                skippedCount++
                continue
            }

            val open = cols[openIdx].toDoubleOrNull()
            val high = cols[highIdx].toDoubleOrNull()
            val low = cols[lowIdx].toDoubleOrNull()
            val close = cols[closeIdx].toDoubleOrNull()
            val volume = cols[volumeIdx].toDoubleOrNull()?.toLong()

            if (open == null || high == null || low == null || close == null || volume == null) {
                if (errors.size < 10) errors.add("Line $lineNum: Non-numeric value encountered in OHLCV columns.")
                skippedCount++
                continue
            }

            if (open <= 0.0 || high <= 0.0 || low <= 0.0 || close <= 0.0) {
                if (errors.size < 10) errors.add("Line $lineNum: Prices must be strictly positive (Open=$open, Close=$close).")
                skippedCount++
                continue
            }

            if (volume < 0) {
                if (errors.size < 10) errors.add("Line $lineNum: Volume cannot be negative ($volume).")
                skippedCount++
                continue
            }

            // OHLC validity checks
            if (high < low) {
                if (errors.size < 10) errors.add("Line $lineNum: Invalid OHLC: High ($high) cannot be less than Low ($low).")
                skippedCount++
                continue
            }
            if (high < open || high < close) {
                if (errors.size < 10) errors.add("Line $lineNum: Invalid OHLC: High ($high) must be >= Open ($open) and Close ($close).")
                skippedCount++
                continue
            }
            if (low > open || low > close) {
                if (errors.size < 10) errors.add("Line $lineNum: Invalid OHLC: Low ($low) must be <= Open ($open) and Close ($close).")
                skippedCount++
                continue
            }

            seenDates.add(normalizedDate)
            validBars.add(
                PriceBar(
                    date = normalizedDate,
                    open = open,
                    high = high,
                    low = low,
                    close = close,
                    volume = volume
                )
            )
        }

        // Sort chronologically by date
        validBars.sortBy { it.date }

        val isValid = errors.isEmpty() && validBars.isNotEmpty()
        if (validBars.isEmpty() && errors.isEmpty()) {
            errors.add("No valid price bars could be extracted from the file.")
        }

        return CsvValidationResult(
            isValid = isValid,
            errors = errors,
            validBars = validBars,
            parsedCount = validBars.size,
            skippedCount = skippedCount
        )
    }

    private fun normalizeDate(raw: String): String? {
        val standardFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        standardFormat.isLenient = false

        for (df in supportedDateFormats) {
            try {
                df.isLenient = false
                val parsed = df.parse(raw)
                if (parsed != null) {
                    return standardFormat.format(parsed)
                }
            } catch (_: Exception) {
                // try next
            }
        }
        return null
    }
}
