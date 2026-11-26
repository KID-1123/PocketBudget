package fr.estiam.pocketbudget.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import fr.estiam.pocketbudget.model.Budget
import fr.estiam.pocketbudget.model.Category
import fr.estiam.pocketbudget.model.Transaction

@Database(entities = [Category::class, Transaction::class, Budget::class], version = 1, exportSchema = true)
@TypeConverters(Converters::class)
abstract class PocketBudgetDatabase : RoomDatabase() {
    abstract fun budgetDao(): PocketBudgetDao
    companion object {
        @Volatile private var instance: PocketBudgetDatabase? = null
        fun getInstance(context: Context): PocketBudgetDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, PocketBudgetDatabase::class.java, "pocket_budget.db").build().also { instance = it }
        }
    }
}
