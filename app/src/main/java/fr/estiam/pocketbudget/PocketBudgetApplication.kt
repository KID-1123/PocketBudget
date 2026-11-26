package fr.estiam.pocketbudget

import android.app.Application
import androidx.datastore.preferences.preferencesDataStore
import fr.estiam.pocketbudget.data.DashboardRepository
import fr.estiam.pocketbudget.data.SettingsRepository
import fr.estiam.pocketbudget.data.local.PocketBudgetDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

private val Application.dataStore by preferencesDataStore(name = "pocketbudget_settings")

class PocketBudgetApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    val dashboardRepository by lazy { DashboardRepository(applicationContext, PocketBudgetDatabase.getInstance(this).budgetDao()) }
    val settingsRepository by lazy { SettingsRepository(dataStore) }
    override fun onCreate() { super.onCreate(); applicationScope.launch { dashboardRepository.seedDevelopmentDataIfEmpty() } }
}
