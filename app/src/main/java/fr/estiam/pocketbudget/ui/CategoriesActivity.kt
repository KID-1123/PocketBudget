package fr.estiam.pocketbudget.ui

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import fr.estiam.pocketbudget.PocketBudgetApplication
import fr.estiam.pocketbudget.databinding.ActivityManageListBinding
import fr.estiam.pocketbudget.model.Category
import kotlinx.coroutines.launch
import java.util.UUID

class CategoriesActivity : AppCompatActivity() {
    private val repository get() = (application as PocketBudgetApplication).dashboardRepository
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityManageListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.title = "Categories"
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.introText.text = "Create categories to organise every income and expense."
        binding.emptyText.text = "No category yet. Add your first one."
        binding.addButton.text = "Add category"
        val adapter = CategoryAdapter { openEditor(it) }
        binding.manageList.layoutManager = LinearLayoutManager(this)
        binding.manageList.adapter = adapter
        binding.addButton.setOnClickListener { openEditor(null) }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                repository.observeCategories().collect { categories ->
                    adapter.submitList(categories)
                    binding.emptyText.visibility = if (categories.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }

    private fun openEditor(existing: Category?) {
        val padding = (24 * resources.displayMetrics.density).toInt()
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(padding, 8, padding, 0) }
        val nameLayout = TextInputLayout(this).apply { hint = "Category name" }
        val name = EditText(this).apply { setText(existing?.name.orEmpty()); setSingleLine() }
        nameLayout.addView(name)
        val type = Switch(this).apply { text = "Income category"; isChecked = existing?.isIncome ?: false; setPadding(0, 16, 0, 8) }
        content.addView(nameLayout); content.addView(type)
        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(if (existing == null) "New category" else "Edit category")
            .setView(content)
            .setNegativeButton(android.R.string.cancel, null)
            .setNeutralButton(if (existing == null) null else "Delete", null)
            .setPositiveButton("Save", null)
            .create()
        dialog.setOnShowListener {
            val save = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
            save.setOnClickListener {
                val cleanName = name.text.toString().trim()
                if (cleanName.isBlank()) { nameLayout.error = "A name is required"; return@setOnClickListener }
                lifecycleScope.launch {
                    repository.saveCategory(Category(existing?.id ?: UUID.randomUUID().toString(), cleanName, existing?.colorHex ?: COLORS.random(), type.isChecked))
                    dialog.dismiss()
                }
            }
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEUTRAL)?.setOnClickListener {
                MaterialAlertDialogBuilder(this)
                    .setTitle("Delete category?")
                    .setMessage("Transactions and budgets in this category will also be removed.")
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton("Delete") { _, _ -> lifecycleScope.launch { repository.deleteCategory(existing!!.id); dialog.dismiss() } }
                    .show()
            }
        }
        name.doAfterTextChanged { nameLayout.error = null }
        dialog.show()
    }
    companion object { private val COLORS = listOf("#2563EB", "#F97316", "#8B5CF6", "#EC4899", "#14B8A6", "#EAB308") }
}
