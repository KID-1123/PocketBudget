package fr.estiam.pocketbudget.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import fr.estiam.pocketbudget.R
import fr.estiam.pocketbudget.data.local.TransactionWithCategory
import fr.estiam.pocketbudget.data.Currency
import fr.estiam.pocketbudget.data.MoneyFormatter
import fr.estiam.pocketbudget.databinding.ItemTransactionBinding
import fr.estiam.pocketbudget.model.TransactionType

class RecentTransactionAdapter(
    private val onTransactionClick: (TransactionWithCategory) -> Unit
) : ListAdapter<TransactionWithCategory, RecentTransactionAdapter.ViewHolder>(Diff) {
    private var currency = Currency.EUR
    fun setCurrency(value: Currency) {
        if (currency != value) { currency = value; notifyDataSetChanged() }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position), currency, onTransactionClick)
    class ViewHolder(private val binding: ItemTransactionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TransactionWithCategory, currency: Currency, onClick: (TransactionWithCategory) -> Unit) = with(binding) {
            categoryText.text = item.categoryName; noteText.text = item.note.ifBlank { root.context.getString(R.string.no_note) }; categoryDot.setCardBackgroundColor(Color.parseColor(item.colorHex))
            val amount = MoneyFormatter.format(item.amountCents, currency)
            amountText.text = if (item.type == TransactionType.INCOME) "+$amount" else "-$amount"
            amountText.setTextColor(root.context.getColor(if (item.type == TransactionType.INCOME) R.color.income else R.color.expense))
            root.setOnClickListener { onClick(item) }
        }
    }
    private object Diff : DiffUtil.ItemCallback<TransactionWithCategory>() { override fun areItemsTheSame(old: TransactionWithCategory, new: TransactionWithCategory) = old.id == new.id; override fun areContentsTheSame(old: TransactionWithCategory, new: TransactionWithCategory) = old == new }
}
