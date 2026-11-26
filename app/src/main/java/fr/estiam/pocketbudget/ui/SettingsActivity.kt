package fr.estiam.pocketbudget.ui

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import fr.estiam.pocketbudget.PocketBudgetApplication
import fr.estiam.pocketbudget.data.Currency
import fr.estiam.pocketbudget.data.TransactionSort
import fr.estiam.pocketbudget.databinding.ActivitySettingsBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {
    private val repository get() = (application as PocketBudgetApplication).settingsRepository
    private var loading = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.sortSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listOf("Newest first", "Highest amount first"))
        lifecycleScope.launch {
            val settings = repository.settings.first()
            binding.currencyGroup.check(if (settings.currency == Currency.EUR) binding.eurButton.id else binding.usdButton.id)
            binding.showIncomeSwitch.isChecked = settings.showIncome
            binding.sortSpinner.setSelection(if (settings.transactionSort == TransactionSort.DATE) 0 else 1)
            loading = false
        }
        binding.currencyGroup.setOnCheckedChangeListener { _, checkedId -> if (!loading) lifecycleScope.launch { repository.setCurrency(if (checkedId == binding.eurButton.id) Currency.EUR else Currency.USD) } }
        binding.showIncomeSwitch.setOnCheckedChangeListener { _, checked -> if (!loading) lifecycleScope.launch { repository.setShowIncome(checked) } }
        binding.sortSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) = Unit
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) { if (!loading) lifecycleScope.launch { repository.setTransactionSort(if (position == 0) TransactionSort.DATE else TransactionSort.AMOUNT) } }
        }
    }
}
