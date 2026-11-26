package fr.estiam.pocketbudget.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import fr.estiam.pocketbudget.model.Category
import fr.estiam.pocketbudget.model.Budget
import fr.estiam.pocketbudget.model.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface PocketBudgetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertCategories(categories: List<Category>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertTransactions(transactions: List<Transaction>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertTransaction(transaction: Transaction)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertCategory(category: Category)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertBudget(budget: Budget)
    @Query("SELECT COUNT(*) FROM categories") suspend fun categoryCount(): Int
    @Query("SELECT COUNT(*) FROM transactions") suspend fun transactionCount(): Int
    @Query("SELECT * FROM categories ORDER BY isIncome DESC, name COLLATE NOCASE ASC") suspend fun getCategories(): List<Category>
    @Query("SELECT * FROM categories ORDER BY isIncome DESC, name COLLATE NOCASE ASC") fun observeCategories(): Flow<List<Category>>
    @Query("DELETE FROM categories WHERE id = :id") suspend fun deleteCategory(id: String)
    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1") suspend fun getCategory(id: String): Category?
    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1") suspend fun getTransaction(id: String): Transaction?
    @Query("DELETE FROM transactions WHERE id = :id") suspend fun deleteTransaction(id: String)
    @Query("SELECT b.id, b.categoryId, b.month, b.limitCents, c.name AS categoryName, c.colorHex, COALESCE(SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amountCents ELSE 0 END), 0) AS spentCents FROM budgets b INNER JOIN categories c ON c.id = b.categoryId LEFT JOIN transactions t ON t.categoryId = b.categoryId AND strftime('%Y-%m', t.dateMillis / 1000, 'unixepoch', 'localtime') = b.month GROUP BY b.id ORDER BY b.month DESC, c.name COLLATE NOCASE") fun observeBudgets(): Flow<List<BudgetWithProgress>>
    @Query("SELECT * FROM budgets WHERE id = :id LIMIT 1") suspend fun getBudget(id: String): Budget?
    @Query("DELETE FROM budgets WHERE id = :id") suspend fun deleteBudget(id: String)
    @Query("SELECT COALESCE(SUM(amountCents), 0) FROM transactions WHERE type = 'INCOME'") fun observeIncome(): Flow<Long>
    @Query("SELECT COALESCE(SUM(amountCents), 0) FROM transactions WHERE type = 'EXPENSE'") fun observeExpenses(): Flow<Long>
    @Query("SELECT t.id, t.amountCents, t.type, t.dateMillis, t.note, c.name AS categoryName, c.colorHex FROM transactions t INNER JOIN categories c ON c.id = t.categoryId ORDER BY t.dateMillis DESC LIMIT 20") fun observeRecentTransactions(): Flow<List<TransactionWithCategory>>
}
