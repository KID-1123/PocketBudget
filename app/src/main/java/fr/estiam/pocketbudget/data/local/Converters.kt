package fr.estiam.pocketbudget.data.local

import androidx.room.TypeConverter
import fr.estiam.pocketbudget.model.TransactionType

class Converters {
    @TypeConverter fun fromType(value: TransactionType): String = value.name
    @TypeConverter fun toType(value: String): TransactionType = TransactionType.entries.firstOrNull { it.name == value } ?: TransactionType.EXPENSE
}
