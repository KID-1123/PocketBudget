package fr.estiam.pocketbudget.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Monthly spending limit for a category, identified by YYYY-MM. */
@Entity(tableName = "budgets", foreignKeys = [ForeignKey(entity = Category::class, parentColumns = ["id"], childColumns = ["categoryId"], onDelete = ForeignKey.CASCADE)], indices = [Index("categoryId")])
data class Budget(
    @PrimaryKey val id: String,
    val categoryId: String,
    val month: String,
    val limitCents: Long
)
