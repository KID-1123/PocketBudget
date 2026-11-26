package fr.estiam.pocketbudget.data

enum class Currency { EUR, USD }
enum class TransactionSort { DATE, AMOUNT }

data class UserSettings(
    val currency: Currency = Currency.EUR,
    val showIncome: Boolean = true,
    val transactionSort: TransactionSort = TransactionSort.DATE,
)
