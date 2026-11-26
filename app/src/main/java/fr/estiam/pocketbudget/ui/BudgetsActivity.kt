package fr.estiam.pocketbudget.ui

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import fr.estiam.pocketbudget.PocketBudgetApplication
import fr.estiam.pocketbudget.databinding.ActivityManageListBinding
import fr.estiam.pocketbudget.model.Budget
import fr.estiam.pocketbudget.model.Category
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class BudgetsActivity : AppCompatActivity() {
    private val repository get() = (application as PocketBudgetApplication).dashboardRepository
    private var categories: List<Category> = emptyList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityManageListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.title = "Budgets"
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.introText.text = "Set a monthly spending limit and keep an eye on each category."
        binding.emptyText.text = "No budget yet. Set your first monthly limit."
        binding.addButton.text = "Add budget"
        val adapter = BudgetAdapter { openEditor(it.id) }
        binding.manageList.layoutManager = LinearLayoutManager(this)
        binding.manageList.adapter = adapter
        binding.addButton.setOnClickListener { openEditor(null) }
        lifecycleScope.launch { repository.getCategories().let { categories = it.filterNot(Category::isIncome) } }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                repository.observeBudgets().collect { budgets ->
                    adapter.submitList(budgets)
                    binding.emptyText.visibility = if (budgets.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }

    private fun openEditor(id: String?) = lifecycleScope.launch {
        if (categories.isEmpty()) categories = repository.getCategories().filterNot(Category::isIncome)
        if (categories.isEmpty()) { MaterialAlertDialogBuilder(this@BudgetsActivity).setMessage("Create an expense category first.").setPositiveButton("OK", null).show(); return@launch }
        val existing = id?.let { repository.getBudget(it) }
        val padding = (24 * resources.displayMetrics.density).toInt()
        val content = LinearLayout(this@BudgetsActivity).apply { orientation = LinearLayout.VERTICAL; setPadding(padding, 8, padding, 0) }
        val categorySpinner = Spinner(this@BudgetsActivity).apply { adapter = ArrayAdapter(this@BudgetsActivity, android.R.layout.simple_spinner_dropdown_item, categories.map(Category::name)); setSelection(categories.indexOfFirst { it.id == existing?.categoryId }.coerceAtLeast(0)) }
        val limitLayout = TextInputLayout(this@BudgetsActivity).apply { hint = "Monthly limit" }
        val limit = EditText(this@BudgetsActivity).apply { inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL; setText(existing?.let { "%.2f".format(it.limitCents / 100.0) }.orEmpty()) }
        content.addView(categorySpinner); content.addView(limitLayout); limitLayout.addView(limit)
        val dialog = MaterialAlertDialogBuilder(this@BudgetsActivity)
            .setTitle(if (existing == null) "New budget" else "Edit budget")
            .setView(content).setNegativeButton(android.R.string.cancel, null)
            .setNeutralButton(if (existing == null) null else "Delete", null).setPositiveButton("Save", null).create()
        dialog.setOnShowListener {
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val cents = try { BigDecimal(limit.text.toString().trim().replace(',', '.')).movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact() } catch (_: Exception) { 0L }
                if (cents <= 0) { limitLayout.error = "Enter a limit greater than zero"; return@setOnClickListener }
                lifecycleScope.launch {
                    repository.saveBudget(Budget(existing?.id ?: UUID.randomUUID().toString(), categories[categorySpinner.selectedItemPosition].id, existing?.month ?: currentMonth(), cents))
                    dialog.dismiss()
                }
            }
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEUTRAL)?.setOnClickListener {
                MaterialAlertDialogBuilder(this@BudgetsActivity).setTitle("Delete budget?").setNegativeButton(android.R.string.cancel, null).setPositiveButton("Delete") { _, _ -> lifecycleScope.launch { repository.deleteBudget(existing!!.id); dialog.dismiss() } }.show()
            }
        }
        dialog.show()
    }
    private fun currentMonth() = SimpleDateFormat("yyyy-MM", Locale.US).format(Date())
}
