package com.example.todolist

import android.content.Context
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

class GroupManager(
    private val context: Context,
    private val firebaseHelper: FirebaseHelper,
    private val reloadTasksCallback: () -> Unit  // Callback ekliyoruz
) {

    // Grup seçenekleri diyalogunu gösterir (Güncelle veya Sil)
    fun showGroupOptionsDialog(group: String) {
        val options = arrayOf("Grubu Güncelle", "Grubu Sil")
        AlertDialog.Builder(context)
            .setTitle("Seçenekler")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showUpdateGroupDialog(group)  // Grup güncelleme diyalogunu göster
                    1 -> showDeleteConfirmationDialog(group)  // Grup silme diyalogunu göster
                }
            }
            .show()
    }

    // Grup güncelleme diyalogu
    private fun showUpdateGroupDialog(oldGroup: String) {
        val editText = EditText(context).apply { hint = "Yeni grup adını girin" }

        AlertDialog.Builder(context)
            .setTitle("Grup Güncelle")
            .setView(editText)
            .setPositiveButton("Güncelle") { _, _ ->
                val newGroupName = editText.text.toString().trim()
                if (newGroupName.isNotEmpty()) {
                    updateGroupName(oldGroup, newGroupName)
                } else {
                    showToast("Grup adı boş olamaz")
                }
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    // Firebase'de grup adını günceller
    private fun updateGroupName(oldGroup: String, newGroup: String) {
        firebaseHelper.updateGroupName(oldGroup, newGroup) { success ->
            if (success) {
                reloadTasksCallback()  // Görevleri yeniden yüklemek için callback
                showToast("Grup adı güncellendi")
            } else {
                showToast("Grup adı güncellenemedi")
            }
        }
    }

    // Grup silme onayı diyalogu
    private fun showDeleteConfirmationDialog(group: String) {
        AlertDialog.Builder(context)
            .setTitle("Grubu Sil")
            .setMessage("Bu grup içindeki tüm görevlerle birlikte silinecek. Emin misiniz?")
            .setPositiveButton("Evet") { _, _ -> deleteGroupAndTasks(group) }
            .setNegativeButton("Hayır", null)
            .show()
    }

    // Grubu ve içindeki görevleri Firebase'den siler
    private fun deleteGroupAndTasks(group: String) {
        firebaseHelper.deleteGroup(group) { success ->
            if (success) {
                reloadTasksCallback()  // Görevlerin yeniden yüklenmesi için callback
                showToast("Grup ve görevler silindi")
            } else {
                showToast("Grup silinemedi")
            }
        }
    }

    // Toast mesajı gösterir
    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
