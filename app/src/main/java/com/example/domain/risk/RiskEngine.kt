package com.example.domain.risk

import com.example.model.PaperAccount
import com.example.model.PaperPosition
import com.example.model.RiskConfig
import com.example.model.RiskEvaluationResult
import java.util.Locale

object RiskEngine {

    fun validateOrder(
        symbol: String,
        isBuy: Boolean,
        requestedQuantity: Double,
        price: Double,
        account: PaperAccount,
        existingPositions: List<PaperPosition>,
        config: RiskConfig
    ): RiskEvaluationResult {
        if (price <= 0.0 || requestedQuantity <= 0.0) {
            return RiskEvaluationResult(
                isAllowed = false,
                adjustedQuantity = 0.0,
                warningMessage = "Invalid price or quantity."
            )
        }

        if (!isBuy) {
            // For sell, check if we hold sufficient shares
            val currentPos = existingPositions.find { it.symbol.equals(symbol, ignoreCase = true) }
            val currentQty = currentPos?.quantity ?: 0.0
            if (currentQty <= 0.0) {
                return RiskEvaluationResult(
                    isAllowed = false,
                    adjustedQuantity = 0.0,
                    warningMessage = "No existing position in $symbol to sell (Short selling disabled)."
                )
            }
            val allowedQty = minOf(requestedQuantity, currentQty)
            return RiskEvaluationResult(
                isAllowed = true,
                adjustedQuantity = allowedQty,
                warningMessage = if (allowedQty < requestedQuantity) "Sell quantity capped to current holdings ($allowedQty)" else null
            )
        }

        // For Buy: evaluate portfolio limits
        val totalPositionsValue = existingPositions.sumOf { it.quantity * it.currentPrice }
        val totalPortfolioValue = account.cash + totalPositionsValue

        // Rule 1: Cash availability
        val requestedCost = requestedQuantity * price
        if (requestedCost > account.cash) {
            val maxAffordableQty = account.cash / price
            if (maxAffordableQty < 0.001) {
                return RiskEvaluationResult(
                    isAllowed = false,
                    adjustedQuantity = 0.0,
                    warningMessage = String.format(
                        Locale.US,
                        "Insufficient virtual cash! Required $%.2f, Available $%.2f.",
                        requestedCost, account.cash
                    )
                )
            }
        }

        // Rule 2: Max Position Size (% of total portfolio in this single asset)
        val currentAssetValue = (existingPositions.find { it.symbol.equals(symbol, ignoreCase = true) }?.marketValue ?: 0.0)
        val maxAllowedForSymbol = totalPortfolioValue * (config.maxPositionSizePct / 100.0)
        val remainingCapForSymbol = maxOf(0.0, maxAllowedForSymbol - currentAssetValue)

        // Rule 3: Max Portfolio Exposure
        val maxTotalInvested = totalPortfolioValue * (config.maxPortfolioExposurePct / 100.0)
        val remainingInvestable = maxOf(0.0, maxTotalInvested - totalPositionsValue)

        val maxAllowedCost = minOf(account.cash, remainingCapForSymbol, remainingInvestable)

        if (maxAllowedCost <= 0.0) {
            return RiskEvaluationResult(
                isAllowed = false,
                adjustedQuantity = 0.0,
                warningMessage = "Risk limit reached: Position or Portfolio Exposure Cap exceeded."
            )
        }

        val maxAllowedQty = maxAllowedCost / price
        if (requestedQuantity > maxAllowedQty) {
            return RiskEvaluationResult(
                isAllowed = true,
                adjustedQuantity = maxAllowedQty,
                warningMessage = String.format(
                    Locale.US,
                    "Risk Control: Quantity adjusted from %.2f to %.2f to respect max position size limit (%.0f%%).",
                    requestedQuantity, maxAllowedQty, config.maxPositionSizePct
                )
            )
        }

        return RiskEvaluationResult(
            isAllowed = true,
            adjustedQuantity = requestedQuantity,
            warningMessage = null
        )
    }
}
