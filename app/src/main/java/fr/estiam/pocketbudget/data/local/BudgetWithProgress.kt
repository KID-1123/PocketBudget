package fr.estiam.pocketbudget.data.local

/** Budget together with the amount spent in its category for its month. */
data class BudgetWithProgress(
    val id: String,
    val categoryId: String,
    val month: String,
    val limitCents: Long,
    val categoryName: String,
    val colorHex: String,
    val spentCents: Long
) {
    val isOverLimit: Boolean get() = spentCents > limitCents
}
