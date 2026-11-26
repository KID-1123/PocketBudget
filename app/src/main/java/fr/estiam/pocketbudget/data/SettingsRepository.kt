package fr.estiam.pocketbudget.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(private val store: DataStore<Preferences>) {
    private object Keys {
        val currency = stringPreferencesKey("pref_currency")
        val showIncome = booleanPreferencesKey("pref_show_income")
        val transactionSort = stringPreferencesKey("pref_transaction_sort")
    }
    val settings: Flow<UserSettings> = store.data.map { prefs ->
        UserSettings(
            currency = prefs[Keys.currency].asEnum(Currency.EUR),
            showIncome = prefs[Keys.showIncome] ?: true,
            transactionSort = prefs[Keys.transactionSort].asEnum(TransactionSort.DATE),
        )
    }
    suspend fun setCurrency(value: Currency) = store.edit { it[Keys.currency] = value.name }
    suspend fun setShowIncome(value: Boolean) = store.edit { it[Keys.showIncome] = value }
    suspend fun setTransactionSort(value: TransactionSort) = store.edit { it[Keys.transactionSort] = value.name }
}
private inline fun <reified T : Enum<T>> String?.asEnum(default: T): T =
    runCatching { enumValueOf<T>(this ?: "") }.getOrDefault(default)
