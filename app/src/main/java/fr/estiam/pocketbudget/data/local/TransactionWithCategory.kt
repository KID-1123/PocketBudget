package fr.estiam.pocketbudget.data.local

import fr.estiam.pocketbudget.model.TransactionType

data class TransactionWithCategory(val id: String, val amountCents: Long, val type: TransactionType, val dateMillis: Long, val note: String, val categoryName: String, val colorHex: String)
