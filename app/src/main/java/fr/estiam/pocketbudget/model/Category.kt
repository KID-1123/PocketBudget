package fr.estiam.pocketbudget.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A spending or income category, for example Groceries or Salary. */
@Entity(tableName = "categories")
data class Category(
    @PrimaryKey val id: String,
    val name: String,
    val colorHex: String,
    val isIncome: Boolean = false
)
