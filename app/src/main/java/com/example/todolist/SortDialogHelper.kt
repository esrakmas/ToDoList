package com.example.todolist

import android.app.AlertDialog
import android.content.Context

class SortDialogHelper(private val context: Context, private val onSortSelected: (SortOption) -> Unit) {

    fun showSortDialog() {
        val sortOptions = arrayOf("Sırala: Eskiden Yeniye", "Sırala: Yeniden Eskiye", "Sırala: Sürükleyerek")

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Sıralama Seçenekleri")
        builder.setItems(sortOptions) { _, which ->
            when (which) {
                0 -> onSortSelected(SortOption.OLD_TO_NEW)
                1 -> onSortSelected(SortOption.NEW_TO_OLD)
                2 -> onSortSelected(SortOption.DRAG_AND_DROP)
            }
        }
        builder.show()
    }

    enum class SortOption {
        OLD_TO_NEW,
        NEW_TO_OLD,
        DRAG_AND_DROP
    }
}
