package com.example.todolist

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView

class SortDialogHelper(
    private val context: Context,
    private val onSortSelected: (SortOption) -> Unit
) {

    fun showSortDialog() {
        val sortOptions = listOf(
            SortOptionItem(
                SortOption.OLD_TO_NEW,
                "Sırala: Eskiden Yeniye",
                R.drawable.ic_sort_old_to_new
            ),
            SortOptionItem(
                SortOption.NEW_TO_OLD,
                "Sırala: Yeniden Eskiye",
                R.drawable.ic_sort_new_to_old
            ),
            SortOptionItem(SortOption.DRAG_AND_DROP, "Sırala: Sürükleyerek",
                R.drawable.touch
            )
        )

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Sıralama Seçenekleri")

        // Özel bir ArrayAdapter oluştur
        val adapter = SortOptionsArrayAdapter(context, sortOptions) { option ->
            onSortSelected(option)
        }

        // ListView için adapter ayarla
        builder.setAdapter(adapter, null)
        builder.show()
    }

    data class SortOptionItem(val option: SortOption, val text: String, val iconResId: Int)

    enum class SortOption {
        OLD_TO_NEW,
        NEW_TO_OLD,
        DRAG_AND_DROP
    }

    private inner class SortOptionsArrayAdapter(
        context: Context,
        private val items: List<SortOptionItem>,
        private val onOptionSelected: (SortOption) -> Unit
    ) : ArrayAdapter<SortOptionItem>(context, 0, items) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(context)
                .inflate(R.layout.dialog_sort_option, parent, false)

            val item = getItem(position)!!
            val icon: ImageView = view.findViewById(R.id.sort_icon)
            val text: TextView = view.findViewById(R.id.sort_text)

            icon.setImageResource(item.iconResId)
            text.text = item.text

            view.setOnClickListener {
                onOptionSelected(item.option)
            }

            return view
        }
    }
}
