package com.example.lab4

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class TodoDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "todo.db"
        const val DATABASE_VERSION = 1
        const val TABLE_NAME = "todos"
        const val COLUMN_ID = "_id"
        const val COLUMN_TEXT = "text"
        const val COLUMN_URGENT = "urgent"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TEXT TEXT NOT NULL,
                $COLUMN_URGENT INTEGER DEFAULT 0
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    // Insert new todo
    fun insertTodo(text: String, isUrgent: Boolean): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TEXT, text)
            put(COLUMN_URGENT, if (isUrgent) 1 else 0)
        }
        return db.insert(TABLE_NAME, null, values)
    }

    // Get all todos
    fun getAllTodos(): Cursor {
        val db = readableDatabase
        return db.query(
            TABLE_NAME,
            null, // all columns
            null, // where clause
            null, // where args
            null, // group by
            null, // having
            null  // order by
        )
    }

    // Delete todo by ID
    fun deleteTodo(id: Long): Int {
        val db = writableDatabase
        return db.delete(TABLE_NAME, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }
}