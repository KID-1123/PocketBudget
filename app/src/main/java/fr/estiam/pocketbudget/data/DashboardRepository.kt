package fr.estiam.pocketbudget.data

import android.content.Context
import fr.estiam.pocketbudget.data.local.PocketBudgetDao
import fr.estiam.pocketbudget.data.local.TransactionWithCategory
import fr.estiam.pocketbudget.data.local.BudgetWithProgress
import fr.estiam.pocketbudget.model.Budget
import fr.estiam.pocketbudget.model.Category
import fr.estiam.pocketbudget.model.Transaction
import fr.estiam.pocketbudget.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.json.JSONArray

data class DashboardState(val incomeCents: Long = 0, val expenseCents: Long = 0, val recentTransactions: List<TransactionWithCategory> = emptyList()) { val balanceCents get() = incomeCents - expenseCents }

class DashboardRepository(
    private val context: Context,
    private val dao: PocketBudgetDao,
) {
    fun observeDashboard(): Flow<DashboardState> = combine(dao.observeIncome(), dao.observeExpenses(), dao.observeRecentTransactions()) { income, expenses, items -> DashboardState(income, expenses, items) }
    suspend fun seedDevelopmentDataIfEmpty() {
        if (dao.categoryCount() == 0) dao.insertCategories(listOf(Category("salary", "Salary", "#2563EB", true), Category("food", "Food & groceries", "#F97316"), Category("transport", "Transport", "#8B5CF6"), Category("leisure", "Leisure", "#EC4899")))
        if (dao.transactionCount() == 0) {
            val validCategoryIds = dao.getCategories().associateBy { it.id }
            val source = context.assets.open("transactions.json").bufferedReader().use { it.readText() }
            val rows = JSONArray(source)
            val transactions = buildList {
                for (index in 0 until rows.length()) {
                    val item = rows.optJSONObject(index) ?: continue
                    val id = item.optString("id").trim()
                    val categoryId = item.optString("categoryId").trim()
                    val amount = item.optLong("amountCents", 0)
                    val date = item.optLong("dateMillis", 0)
                    val type = runCatching { TransactionType.valueOf(item.optString("type")) }.getOrNull()
                    val category = validCategoryIds[categoryId]
                    if (id.isNotBlank() && category != null && amount > 0 && date > 0 && type != null && category.isIncome == (type == TransactionType.INCOME)) {
                        add(Transaction(id, categoryId, amount, type, date, item.optString("note").trim()))
                    }
                }
            }
            if (transactions.isNotEmpty()) dao.insertTransactions(transactions)
        }
    }

    suspend fun getCategories(): List<Category> = dao.getCategories()
    fun observeCategories(): Flow<List<Category>> = dao.observeCategories()
    suspend fun saveCategory(category: Category) {
        require(category.name.isNotBlank()) { "Category name is required" }
        dao.insertCategory(category)
    }
    suspend fun deleteCategory(id: String) = dao.deleteCategory(id)

    fun observeBudgets(): Flow<List<BudgetWithProgress>> = dao.observeBudgets()
    suspend fun getBudget(id: String): Budget? = dao.getBudget(id)
    suspend fun saveBudget(budget: Budget) {
        require(budget.limitCents > 0) { "Budget limit must be positive" }
        dao.insertBudget(budget)
    }
    suspend fun deleteBudget(id: String) = dao.deleteBudget(id)

    suspend fun saveTransaction(transaction: Transaction) {
        require(transaction.amountCents > 0) { "Amount must be positive" }
        dao.insertTransaction(transaction)
    }

    suspend fun getTransaction(id: String): Transaction? = dao.getTransaction(id)

    suspend fun deleteTransaction(id: String) = dao.deleteTransaction(id)
}
