package fr.estiam.pocketbudget.data

import org.junit.Assert.assertTrue
import org.junit.Test

class MoneyFormatterTest {
    @Test fun formatsEuroWithEuroSymbol() = assertTrue(MoneyFormatter.format(12345, Currency.EUR).contains("€"))
    @Test fun formatsDollarWithDollarSymbol() = assertTrue(MoneyFormatter.format(12345, Currency.USD).contains("$"))
}
