package fr.estiam.pocketbudget.data

import java.text.NumberFormat
import java.util.Locale

/** Amounts are stored in EUR cents. USD is a fixed demo conversion, not a live exchange rate. */
object MoneyFormatter {
    private const val EUR_TO_USD = 1.09
    fun format(cents: Long, currency: Currency): String {
        val amountInEuros = cents / 100.0
        return when (currency) {
            Currency.EUR -> NumberFormat.getCurrencyInstance(Locale.FRANCE).format(amountInEuros)
            Currency.USD -> NumberFormat.getCurrencyInstance(Locale.US).format(amountInEuros * EUR_TO_USD)
        }
    }
}
