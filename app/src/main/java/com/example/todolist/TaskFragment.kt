package com.example.todolist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.databinding.FragmentTaskBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import androidx.recyclerview.widget.ItemTouchHelper


class TaskFragment : Fragment() {
    private lateinit var binding: FragmentTaskBinding
    private lateinit var tasksItemAdapter: TasksItemAdapter
    private val tasksList = mutableListOf<Task>()
    private val database: DatabaseReference =
        FirebaseDatabase.getInstance().reference.child("tasks")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // RecyclerView için layout manager ve adapter ayarlanıyor
        binding.recyclerViewTasks.layoutManager = LinearLayoutManager(context)
        tasksItemAdapter = TasksItemAdapter(tasksList)
        binding.recyclerViewTasks.adapter = tasksItemAdapter

        // Argümanlardan grup adını alıyoruz
        val group = arguments?.getString(ARG_GROUP) ?: ""
        loadTasks(group)  // Görevleri Firebase'den yüklüyoruz

        // Sürükle-bırak işlevini ekle
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPosition = viewHolder.adapterPosition
                val toPosition = target.adapterPosition

                // Görev listesini güncelle
                tasksList.add(toPosition, tasksList.removeAt(fromPosition))
                tasksItemAdapter.notifyItemMoved(fromPosition, toPosition)

                // Firebase'de yeni sıralamayı kaydet
                saveOrderToFirebase(group)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // Sürükleme işlemi sırasında kaydırma olayı kullanılmadığı için boş bırakıyoruz
            }
        }

        // ItemTouchHelper'ı RecyclerView'a ekle
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewTasks)

    }










    // Görevlerin sırasını Firebase'e kaydet
    private fun saveOrderToFirebase(group: String) {
        tasksList.forEachIndexed { index, task ->
            val taskRef = database.child(task.id)
            taskRef.child("order").setValue(index)
        }
    }


    // Firebase'den belirli gruba ait görevleri yükler
    private fun loadTasks(group: String) {
        database.orderByChild("group").equalTo(group)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    tasksList.clear()  // Önceki listeyi temizliyoruz
                    snapshot.children.forEach { dataSnapshot ->
                        val task = dataSnapshot.getValue(Task::class.java)
                        if (task != null) {
                            tasksList.add(task)  // Her bir görevi listeye ekliyoruz
                        }
                    }
                    tasksItemAdapter.notifyDataSetChanged()  // Adapter'a veri değişikliği bildiriliyor
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        context,
                        "Veriler yüklenirken bir hata oluştu: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    companion object {
        private const val ARG_GROUP = "arg_group"

        // TaskFragment örneği oluşturulurken grup argümanını ekler
        fun newInstance(group: String): TaskFragment {
            val fragment = TaskFragment()
            val args = Bundle()
            args.putString(ARG_GROUP, group)
            fragment.arguments = args
            return fragment
        }
    }


}
