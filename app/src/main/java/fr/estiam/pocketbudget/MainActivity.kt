package fr.estiam.pocketbudget

import android.os.Bundle
import android.content.Intent
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import fr.estiam.pocketbudget.databinding.ActivityMainBinding
import fr.estiam.pocketbudget.ui.DashboardViewModel
import fr.estiam.pocketbudget.ui.EditTransactionActivity
import fr.estiam.pocketbudget.ui.RecentTransactionAdapter
import fr.estiam.pocketbudget.ui.CategoriesActivity
import fr.estiam.pocketbudget.ui.BudgetsActivity
import fr.estiam.pocketbudget.ui.SettingsActivity
import fr.estiam.pocketbudget.data.Currency
import kotlinx.coroutines.launch
import androidx.appcompat.widget.SearchView
import fr.estiam.pocketbudget.data.MoneyFormatter

class MainActivity : AppCompatActivity() {
    private val viewModel: DashboardViewModel by viewModels { DashboardViewModel.Factory((application as PocketBudgetApplication).dashboardRepository, (application as PocketBudgetApplication).settingsRepository) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val adapter = RecentTransactionAdapter { transaction ->
            startActivity(Intent(this, EditTransactionActivity::class.java)
                .putExtra(EditTransactionActivity.EXTRA_TRANSACTION_ID, transaction.id))
        }
        binding.transactionsList.layoutManager = LinearLayoutManager(this)
        binding.transactionsList.adapter = adapter
        binding.addTransactionButton.setOnClickListener { startActivity(Intent(this, EditTransactionActivity::class.java)) }
        binding.categoriesButton.setOnClickListener { startActivity(Intent(this, CategoriesActivity::class.java)) }
        binding.budgetsButton.setOnClickListener { startActivity(Intent(this, BudgetsActivity::class.java)) }
        binding.settingsButton.setOnClickListener { startActivity(Intent(this, SettingsActivity::class.java)) }
        binding.searchTransactions.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(value: String?): Boolean { viewModel.setSearchQuery(value.orEmpty()); return true }
        })
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                (application as PocketBudgetApplication).dashboardRepository.observeCategories().collect { categories ->
                    val labels = listOf("All categories") + categories.map { it.name }
                    binding.categoryFilter.adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, labels)
                    binding.categoryFilter.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: android.widget.AdapterView<*>?) = Unit
                        override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                            viewModel.setCategoryFilter(categories.getOrNull(position - 1)?.name)
                        }
                    }
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    binding.balanceValue.text = MoneyFormatter.format(state.dashboard.balanceCents, state.settings.currency)
                    binding.incomeValue.text = MoneyFormatter.format(state.dashboard.incomeCents, state.settings.currency)
                    binding.expenseValue.text = MoneyFormatter.format(state.dashboard.expenseCents, state.settings.currency)
                    binding.incomeValue.parent.parent.let { it as android.view.View }.visibility = if (state.settings.showIncome) android.view.View.VISIBLE else android.view.View.GONE
                    binding.emptyText.visibility = if (state.dashboard.recentTransactions.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
                    adapter.setCurrency(state.settings.currency)
                    adapter.submitList(state.dashboard.recentTransactions)
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                (application as PocketBudgetApplication).dashboardRepository.observeBudgets().collect { budgets ->
                    val alerts = budgets.filter { it.isOverLimit }
                    if (alerts.isNotEmpty()) Snackbar.make(binding.root, "Budget alert: ${alerts.joinToString { it.categoryName }} exceeded its monthly limit.", Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }
}
