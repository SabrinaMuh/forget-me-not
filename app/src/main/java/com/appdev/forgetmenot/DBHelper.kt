package com.appdev.forgetmenot

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import android.util.Log

class DBHelper(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "ForgetMeNot.db"
        const val DATABASE_VERSION = 1
    }

    object EventObject {
        object Entry: BaseColumns {
            const val TABLE_NAME = "Event"
            const val COLUMN_NAME_TITLE = "title"
            const val COLUMN_NAME_NOTE = "note"
            const val COLUMN_NAME_CATEGORY = "category"
            const val COLUMN_NAME_DATETIME = "datetime"
            const val COLUMN_NAME_IS_ROOT = "is_root"
            const val COLUMN_NAME_ROOT_ID = "root_id"
            const val COLUMN_NAME_PREV_ID = "prev_id"

            const val SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS $TABLE_NAME (" +
                    "${BaseColumns._ID} INTEGER PRIMARY KEY, " +
                    "$COLUMN_NAME_TITLE TEXT, " +
                    "$COLUMN_NAME_NOTE TEXT, " +
                    "$COLUMN_NAME_CATEGORY TEXT, " +
                    "$COLUMN_NAME_DATETIME DATETIME, " +
                    "$COLUMN_NAME_IS_ROOT BOOLEAN, " +
                    "$COLUMN_NAME_ROOT_ID INTEGER, " +
                    "$COLUMN_NAME_PREV_ID INTEGER)"

            const val SQL_DROP_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME"
        }
    }


    override fun onCreate(db: SQLiteDatabase?) {
        Log.d("myDB", "${EventObject.Entry.SQL_CREATE_TABLE}")
        db?.execSQL(EventObject.Entry.SQL_CREATE_TABLE)
    }


    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL(EventObject.Entry.SQL_DROP_TABLE)
        onCreate(db)
    }


    fun readAllEvents() : Cursor {
        val db = this.readableDatabase
        val query = "SELECT * FROM ${EventObject.Entry.TABLE_NAME}"
        Log.d("myDB", "read all events")
        return db.rawQuery(query, null)
    }


    fun addEvent(entry: MainEntry) : Long {
        val db = this.writableDatabase
//create content values
        val values = ContentValues().apply {
            put(EventObject.Entry.COLUMN_NAME_TITLE, entry.title)
            put(EventObject.Entry.COLUMN_NAME_NOTE, entry.note)
            put(EventObject.Entry.COLUMN_NAME_CATEGORY, entry.category)
            put(EventObject.Entry.COLUMN_NAME_DATETIME, entry.dateTime.toString())
            put(EventObject.Entry.COLUMN_NAME_IS_ROOT, entry.isRoot)
            put(EventObject.Entry.COLUMN_NAME_ROOT_ID, entry.rootID)
            put(EventObject.Entry.COLUMN_NAME_PREV_ID, entry.prevID)
        }

        Log.d("myDB", "event added: ${entry.toString()}")
        return db.insert(EventObject.Entry.TABLE_NAME, null, values)
    }


    fun updateEvent(entry: MainEntry, id: Long) : Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(EventObject.Entry.COLUMN_NAME_TITLE, entry.title)
            put(EventObject.Entry.COLUMN_NAME_NOTE, entry.note)
            put(EventObject.Entry.COLUMN_NAME_CATEGORY, entry.category)
            put(EventObject.Entry.COLUMN_NAME_DATETIME, entry.dateTime.toString())
            put(EventObject.Entry.COLUMN_NAME_IS_ROOT, entry.isRoot)
            put(EventObject.Entry.COLUMN_NAME_ROOT_ID, entry.rootID)
            put(EventObject.Entry.COLUMN_NAME_PREV_ID, entry.prevID)
        }
        val selection = "${BaseColumns._ID} = ?"
        val selectionArgs = arrayOf("$id")
        val count = db.update(EventObject.Entry.TABLE_NAME, values, selection, selectionArgs)
        Log.d("myDB", "event with $id updated: count $count")
        return count
    }


    fun deleteAllEvents() : Int {
        val db = this.writableDatabase
        val count = db.delete(
            EventObject.Entry.TABLE_NAME,
            null,
            null
        )
        Log.d("myDB", "all events deleted: count $count")
        return count
    }


    fun deleteEventById(id: Long) : Int {
        val db = this.writableDatabase
        val count = db.delete(
            EventObject.Entry.TABLE_NAME,
            "${BaseColumns._ID} = ?",
            arrayOf("$id")
        )
        Log.d("myDB", "event with $id deleted: count $count")
        return count
    }
}