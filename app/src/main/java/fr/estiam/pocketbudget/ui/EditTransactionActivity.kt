package fr.estiam.pocketbudget.ui

import android.app.DatePickerDialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import fr.estiam.pocketbudget.PocketBudgetApplication
import fr.estiam.pocketbudget.R
import fr.estiam.pocketbudget.databinding.ActivityEditTransactionBinding
import fr.estiam.pocketbudget.model.Category
import fr.estiam.pocketbudget.model.Transaction
import fr.estiam.pocketbudget.model.TransactionType
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DateFormat
import java.util.Calendar
import java.util.UUID

class EditTransactionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditTransactionBinding
    private val repository get() = (application as PocketBudgetApplication).dashboardRepository
    private var allCategories: List<Category> = emptyList()
    private var categories: List<Category> = emptyList()
    private var selectedDate = Calendar.getInstance()
    private var editingId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        editingId = intent.getStringExtra(EXTRA_TRANSACTION_ID)
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.dateButton.setOnClickListener { selectDate() }
        binding.saveButton.setOnClickListener { save() }
        binding.deleteButton.setOnClickListener { confirmDelete() }
        binding.amountInput.setOnClickListener { showAmountKeyboard() }
        binding.amountInput.setOnFocusChangeListener { _, focused -> if (focused) showAmountKeyboard() }
        binding.typeToggle.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val type = if (checkedId == binding.incomeButton.id) TransactionType.INCOME else TransactionType.EXPENSE
                updateTypeUi(type)
                showCategories(type)
            }
        }
        displayDate()
        updateTypeUi(TransactionType.EXPENSE)
        if (editingId != null) {
            binding.toolbar.title = getString(R.string.edit_transaction)
            binding.deleteButton.visibility = android.view.View.VISIBLE
        }
        lifecycleScope.launch {
            allCategories = repository.getCategories()
            val id = editingId
            if (id == null) showCategories(TransactionType.EXPENSE) else loadTransaction(id)
        }
    }

    private fun showCategories(type: TransactionType) {
        categories = allCategories.filter { it.isIncome == (type == TransactionType.INCOME) }
        binding.categorySpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            categories.map { it.name }
        )
    }

    private fun updateTypeUi(type: TransactionType) {
        val neutral = MaterialColors.getColor(binding.expenseButton, com.google.android.material.R.attr.colorSurfaceVariant)
        val neutralText = MaterialColors.getColor(binding.expenseButton, com.google.android.material.R.attr.colorOnSurfaceVariant)
        fun style(button: com.google.android.material.button.MaterialButton, selected: Boolean, color: Int) {
            button.backgroundTintList = ColorStateList.valueOf(if (selected) color else neutral)
            button.setTextColor(if (selected) Color.WHITE else neutralText)
        }
        style(binding.expenseButton, type == TransactionType.EXPENSE, getColor(R.color.expense))
        style(binding.incomeButton, type == TransactionType.INCOME, getColor(R.color.income))
    }

    private fun showAmountKeyboard() {
        binding.amountInput.requestFocus()
        binding.amountInput.post {
            (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .showSoftInput(binding.amountInput, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun selectDate() {
        DatePickerDialog(this, { _, year, month, day ->
            selectedDate.set(year, month, day)
            displayDate()
        }, selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH), selectedDate.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun displayDate() {
        binding.dateButton.text = DateFormat.getDateInstance(DateFormat.MEDIUM).format(selectedDate.time)
    }

    private suspend fun loadTransaction(id: String) {
        val transaction = repository.getTransaction(id) ?: run { finish(); return }
        selectedDate.timeInMillis = transaction.dateMillis
        displayDate()
        binding.amountInput.setText("%.2f".format(transaction.amountCents / 100.0))
        binding.noteInput.setText(transaction.note)
        binding.typeToggle.check(if (transaction.type == TransactionType.INCOME) binding.incomeButton.id else binding.expenseButton.id)
        updateTypeUi(transaction.type)
        showCategories(transaction.type)
        binding.categorySpinner.setSelection(categories.indexOfFirst { it.id == transaction.categoryId }.coerceAtLeast(0))
    }

    private fun save() {
        binding.amountInputLayout.error = null
        val amount = try {
            BigDecimal(binding.amountInput.text?.toString()?.trim()?.replace(',', '.') ?: "")
                .movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact()
        } catch (_: Exception) { 0L }
        if (amount <= 0L) {
            binding.amountInputLayout.error = getString(R.string.invalid_amount)
            return
        }
        val category = categories.getOrNull(binding.categorySpinner.selectedItemPosition) ?: return
        val type = if (binding.incomeButton.isChecked) TransactionType.INCOME else TransactionType.EXPENSE
        binding.saveButton.isEnabled = false
        lifecycleScope.launch {
            repository.saveTransaction(
                Transaction(editingId ?: UUID.randomUUID().toString(), category.id, amount, type, selectedDate.timeInMillis, binding.noteInput.text?.toString()?.trim().orEmpty())
            )
            finish()
        }
    }

    private fun confirmDelete() {
        val id = editingId ?: return
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_transaction)
            .setMessage(R.string.delete_transaction_confirmation)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.delete) { _, _ ->
                lifecycleScope.launch { repository.deleteTransaction(id); finish() }
            }
            .show()
    }

    companion object {
        const val EXTRA_TRANSACTION_ID = "transaction_id"
    }
}
