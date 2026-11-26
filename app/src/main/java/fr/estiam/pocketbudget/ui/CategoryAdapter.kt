package fr.estiam.pocketbudget.ui

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import fr.estiam.pocketbudget.model.Category

class CategoryAdapter(private val onClick: (Category) -> Unit) : RecyclerView.Adapter<CategoryAdapter.Holder>() {
    private var items: List<Category> = emptyList()
    fun submitList(value: List<Category>) { items = value; notifyDataSetChanged() }
    override fun getItemCount() = items.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val context = parent.context
        val card = MaterialCardView(context).apply {
            layoutParams = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = (12 * resources.displayMetrics.density).toInt() }
            radius = 20 * resources.displayMetrics.density
            strokeWidth = 1
            strokeColor = Color.rgb(75, 69, 82)
            isClickable = true; isFocusable = true
        }
        val row = LinearLayout(context).apply { gravity = Gravity.CENTER_VERTICAL; setPadding(18.dp, 16.dp, 18.dp, 16.dp) }
        val dot = TextView(context).apply { layoutParams = LinearLayout.LayoutParams(18.dp, 18.dp).apply { marginEnd = 16.dp }; background = GradientDrawable().apply { shape = GradientDrawable.OVAL } }
        val text = TextView(context).apply { layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f); textSize = 18f }
        val type = TextView(context).apply { textSize = 14f }
        row.addView(dot); row.addView(text); row.addView(type); card.addView(row)
        return Holder(card, dot, text, type)
    }
    override fun onBindViewHolder(holder: Holder, position: Int) = holder.bind(items[position], onClick)
    class Holder(private val card: MaterialCardView, private val dot: TextView, private val name: TextView, private val type: TextView) : RecyclerView.ViewHolder(card) {
        fun bind(item: Category, click: (Category) -> Unit) {
            (dot.background as GradientDrawable).setColor(Color.parseColor(item.colorHex))
            name.text = item.name
            type.text = if (item.isIncome) "Income" else "Expense"
            card.setOnClickListener { click(item) }
        }
    }
}
private val Int.dp: Int get() = (this * android.content.res.Resources.getSystem().displayMetrics.density).toInt()
