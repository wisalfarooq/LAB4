package com.example.lab4

import android.graphics.Color
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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
    private lateinit var dbHelper: TodoDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize database helper
        dbHelper = TodoDatabaseHelper(this)

        // Initialize views
        todoListView = findViewById(R.id.todoListView)
        todoEditText = findViewById(R.id.todoEditText)
        urgentSwitch = findViewById(R.id.urgentSwitch)
        addButton = findViewById(R.id.addButton)

        // Initialize todo list from database
        todoList = mutableListOf()
        loadTodosFromDatabase()

        // Initialize adapter
        adapter = TodoAdapter(todoList)
        todoListView.adapter = adapter

        // Print cursor info for debugging
        val cursor = dbHelper.getAllTodos()
        printCursor(cursor)
        cursor.close()

        // Add button click listener
        addButton.setOnClickListener {
            val todoText = todoEditText.text.toString().trim()
            if (todoText.isNotEmpty()) {
                // Insert into database
                val newId = dbHelper.insertTodo(todoText, urgentSwitch.isChecked)

                // Add to local list
                val newTodo = TodoItem(newId, todoText, urgentSwitch.isChecked)
                todoList.add(newTodo)
                adapter.notifyDataSetChanged()

                // Clear input
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

    private fun loadTodosFromDatabase() {
        val cursor = dbHelper.getAllTodos()
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(TodoDatabaseHelper.COLUMN_ID))
                val text = cursor.getString(cursor.getColumnIndexOrThrow(TodoDatabaseHelper.COLUMN_TEXT))
                val urgent = cursor.getInt(cursor.getColumnIndexOrThrow(TodoDatabaseHelper.COLUMN_URGENT)) == 1

                todoList.add(TodoItem(id, text, urgent))
            } while (cursor.moveToNext())
        }
        cursor.close()
    }

    private fun showDeleteDialog(position: Int) {
        val todoItem = todoList[position]
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.delete_dialog_title))
            .setMessage("${getString(R.string.delete_dialog_message, position)}\nTask: ${todoItem.text}")
            .setPositiveButton(getString(R.string.yes)) { dialog, _ ->
                // Delete from database
                dbHelper.deleteTodo(todoItem.id)

                // Delete from local list
                todoList.removeAt(position)
                adapter.notifyDataSetChanged()
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.no)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    // Debug function to print cursor info
    private fun printCursor(cursor: Cursor) {
        Log.d("DB_DEBUG", "=== Database Cursor Information ===")

        // a. Database version
        val db = dbHelper.readableDatabase
        Log.d("DB_DEBUG", "Database Version: ${db.version}")

        // b. Number of columns
        val columnCount = cursor.columnCount
        Log.d("DB_DEBUG", "Number of columns: $columnCount")

        // c. Column names
        val columnNames = cursor.columnNames.joinToString(", ")
        Log.d("DB_DEBUG", "Column names: $columnNames")

        // d. Number of results
        val resultCount = cursor.count
        Log.d("DB_DEBUG", "Number of results: $resultCount")

        // e. Each row of results
        if (cursor.moveToFirst()) {
            Log.d("DB_DEBUG", "=== Results ===")
            var rowNumber = 1
            do {
                val rowData = StringBuilder()
                for (i in 0 until columnCount) {
                    rowData.append("${cursor.columnNames[i]}: ${cursor.getString(i)} | ")
                }
                Log.d("DB_DEBUG", "Row $rowNumber: $rowData")
                rowNumber++
            } while (cursor.moveToNext())
        } else {
            Log.d("DB_DEBUG", "No results found")
        }

        Log.d("DB_DEBUG", "=== End of Cursor Information ===")
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

    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }
}