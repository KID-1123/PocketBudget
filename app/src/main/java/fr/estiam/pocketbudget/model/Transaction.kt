package fr.estiam.pocketbudget.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class TransactionType { INCOME, EXPENSE }

/** A monetary operation linked to one category. Amounts are stored as cents to avoid rounding errors. */
@Entity(tableName = "transactions", foreignKeys = [ForeignKey(entity = Category::class, parentColumns = ["id"], childColumns = ["categoryId"], onDelete = ForeignKey.CASCADE)], indices = [Index("categoryId")])
data class Transaction(
    @PrimaryKey val id: String,
    val categoryId: String,
    val amountCents: Long,
    val type: TransactionType,
    val dateMillis: Long,
    val note: String = ""
)
