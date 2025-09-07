package com.example.lab4

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog

class MainActivity : AppCompatActivity() {

    private lateinit var todoListView: ListView
    private lateinit var todoEditText: EditText
    private lateinit var urgentSwitch: Switch
    private lateinit var addButton: Button
    private lateinit var todoList: MutableList<TodoItem>
    private lateinit var adapter: TodoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        todoListView = findViewById(R.id.todoListView)
        todoEditText = findViewById(R.id.todoEditText)
        urgentSwitch = findViewById(R.id.urgentSwitch)
        addButton = findViewById(R.id.addButton)

        // Initialize todo list and adapter
        todoList = mutableListOf()
        adapter = TodoAdapter(todoList)
        todoListView.adapter = adapter

        // Add button click listener
        addButton.setOnClickListener {
            val todoText = todoEditText.text.toString().trim()
            if (todoText.isNotEmpty()) {
                val newTodo = TodoItem(todoText, urgentSwitch.isChecked)
                todoList.add(newTodo)
                adapter.notifyDataSetChanged()
                todoEditText.text.clear()
                urgentSwitch.isChecked = false
            } else {
                todoEditText.error = "Please enter a todo item"
            }
        }

        // Long click listener for deletion
        todoListView.onItemLongClickListener = AdapterView.OnItemLongClickListener { _, _, position, _ ->
            showDeleteDialog(position)
            true
        }
    }

    private fun showDeleteDialog(position: Int) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.delete_dialog_title))
            .setMessage(getString(R.string.delete_dialog_message, position))
            .setPositiveButton(getString(R.string.yes)) { dialog, _ ->
                todoList.removeAt(position)
                adapter.notifyDataSetChanged()
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.no)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    // Custom Adapter
    inner class TodoAdapter(private val items: List<TodoItem>) : BaseAdapter() {

        override fun getCount(): Int = items.size

        override fun getItem(position: Int): TodoItem = items[position]

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: layoutInflater.inflate(R.layout.list_item_todo, parent, false)
            val textView = view.findViewById<TextView>(R.id.todoTextView)
            val todoItem = getItem(position)

            textView.text = todoItem.text

            if (todoItem.isUrgent) {
                view.setBackgroundColor(Color.RED)
                textView.setTextColor(Color.WHITE)
            } else {
                view.setBackgroundColor(Color.TRANSPARENT)
                textView.setTextColor(Color.BLACK)
            }

            return view
        }
    }
}