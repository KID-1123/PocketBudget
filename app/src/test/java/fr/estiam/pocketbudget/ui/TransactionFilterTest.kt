package fr.estiam.pocketbudget.ui

import fr.estiam.pocketbudget.data.TransactionSort
import fr.estiam.pocketbudget.data.local.TransactionWithCategory
import fr.estiam.pocketbudget.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Test

class TransactionFilterTest {
    private val income = item("salary", "Salary", "October salary", 100_000, TransactionType.INCOME)
    private val food = item("food", "Food", "Coffee shop", 500, TransactionType.EXPENSE)
    private val transport = item("transport", "Transport", "Metro pass", 2_000, TransactionType.EXPENSE)
    private val all = listOf(income, food, transport)

    @Test fun emptyQueryKeepsEveryTransaction() = assertEquals(3, filter(all).size)
    @Test fun queryMatchesNoteIgnoringCase() = assertEquals(listOf(food), filter(all, query = "COFFEE"))
    @Test fun queryMatchesCategory() = assertEquals(listOf(transport), filter(all, query = "transport"))
    @Test fun hiddenIncomeRemovesIncomeTransactions() = assertEquals(listOf(food, transport), filter(all, showIncome = false))
    @Test fun categoryFilterKeepsOnlyThatCategory() = assertEquals(listOf(food), filter(all, category = "Food"))
    @Test fun amountSortOrdersLargestFirst() = assertEquals(listOf(income, transport, food), filter(all, sort = TransactionSort.AMOUNT))

    private fun filter(values: List<TransactionWithCategory>, showIncome: Boolean = true, category: String? = null, query: String = "", sort: TransactionSort = TransactionSort.DATE) =
        filterTransactions(values, showIncome, category, query, sort)
    private fun item(id: String, category: String, note: String, amount: Long, type: TransactionType) =
        TransactionWithCategory(id, amount, type, 1L, note, category, "#000000")
}
