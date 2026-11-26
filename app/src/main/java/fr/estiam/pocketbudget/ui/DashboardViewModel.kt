package fr.estiam.pocketbudget.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.estiam.pocketbudget.data.DashboardRepository
import fr.estiam.pocketbudget.data.DashboardState
import fr.estiam.pocketbudget.data.SettingsRepository
import fr.estiam.pocketbudget.data.UserSettings
import fr.estiam.pocketbudget.data.TransactionSort
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

data class DashboardUiState(val dashboard: DashboardState = DashboardState(), val settings: UserSettings = UserSettings())

class DashboardViewModel(repository: DashboardRepository, settingsRepository: SettingsRepository) : ViewModel() {
    private val searchQuery = MutableStateFlow("")
    private val categoryFilter = MutableStateFlow<String?>(null)
    val state = combine(repository.observeDashboard(), settingsRepository.settings, searchQuery, categoryFilter) { dashboard, settings, query, category ->
        val visibleTransactions = filterTransactions(
            dashboard.recentTransactions, settings.showIncome, category, query, settings.transactionSort,
        )
        DashboardUiState(dashboard.copy(recentTransactions = visibleTransactions), settings)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())
    fun setSearchQuery(query: String) { searchQuery.value = query.trim() }
    fun setCategoryFilter(category: String?) { categoryFilter.value = category }
    class Factory(private val repository: DashboardRepository, private val settingsRepository: SettingsRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST") override fun <T : ViewModel> create(modelClass: Class<T>): T = DashboardViewModel(repository, settingsRepository) as T
    }
}
