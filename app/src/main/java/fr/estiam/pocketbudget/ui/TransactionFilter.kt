package fr.estiam.pocketbudget.ui

import fr.estiam.pocketbudget.data.TransactionSort
import fr.estiam.pocketbudget.data.local.TransactionWithCategory
import fr.estiam.pocketbudget.model.TransactionType

fun filterTransactions(
    transactions: List<TransactionWithCategory>,
    showIncome: Boolean,
    category: String?,
    query: String,
    sort: TransactionSort,
): List<TransactionWithCategory> = transactions
    .filter { showIncome || it.type != TransactionType.INCOME }
    .filter { item -> category == null || item.categoryName == category }
    .filter { item -> query.isBlank() || item.categoryName.contains(query, ignoreCase = true) || item.note.contains(query, ignoreCase = true) }
    .let { list -> if (sort == TransactionSort.AMOUNT) list.sortedByDescending { it.amountCents } else list }
