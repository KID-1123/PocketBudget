package fr.estiam.pocketbudget.ui

import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import fr.estiam.pocketbudget.data.local.BudgetWithProgress
import java.text.NumberFormat

class BudgetAdapter(private val onClick: (BudgetWithProgress) -> Unit) : RecyclerView.Adapter<BudgetAdapter.Holder>() {
    private var items: List<BudgetWithProgress> = emptyList()
    fun submitList(value: List<BudgetWithProgress>) { items = value; notifyDataSetChanged() }
    override fun getItemCount() = items.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val c = parent.context
        val card = MaterialCardView(c).apply { layoutParams = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = (12 * resources.displayMetrics.density).toInt() }; radius = 20 * resources.displayMetrics.density; strokeWidth = 1; strokeColor = Color.rgb(75,69,82); isClickable = true }
        val box = LinearLayout(c).apply { orientation = LinearLayout.VERTICAL; setPadding(18.dp,16.dp,18.dp,16.dp) }
        val top = LinearLayout(c).apply { gravity = Gravity.CENTER_VERTICAL }
        val name = TextView(c).apply { layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f); textSize = 18f }
        val amount = TextView(c).apply { textSize = 16f }
        val caption = TextView(c).apply { textSize = 14f; setPadding(0,8.dp,0,0) }
        top.addView(name); top.addView(amount); box.addView(top); box.addView(caption); card.addView(box)
        return Holder(card, name, amount, caption)
    }
    override fun onBindViewHolder(holder: Holder, position: Int) = holder.bind(items[position], onClick)
    class Holder(private val card: MaterialCardView, private val name: TextView, private val amount: TextView, private val caption: TextView): RecyclerView.ViewHolder(card) {
        fun bind(item: BudgetWithProgress, click: (BudgetWithProgress) -> Unit) {
            val money = NumberFormat.getCurrencyInstance()
            name.text = item.categoryName
            amount.text = "${money.format(item.spentCents / 100.0)} / ${money.format(item.limitCents / 100.0)}"
            caption.text = if (item.isOverLimit) "Over budget · ${item.month}" else "${item.month} · ${money.format((item.limitCents - item.spentCents) / 100.0)} remaining"
            val color = if (item.isOverLimit) Color.rgb(239, 68, 68) else Color.rgb(16, 185, 129)
            amount.setTextColor(color); caption.setTextColor(color)
            card.setOnClickListener { click(item) }
        }
    }
}
private val Int.dp: Int get() = (this * android.content.res.Resources.getSystem().displayMetrics.density).toInt()
