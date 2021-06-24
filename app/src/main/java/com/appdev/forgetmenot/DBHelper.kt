package com.appdev.forgetmenot

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Build
import android.provider.BaseColumns
import android.util.Log
import androidx.annotation.RequiresApi
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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
                    "$COLUMN_NAME_DATETIME TEXT, " +
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


    fun getAllEvents() : Cursor {
        val db = this.readableDatabase
        val query = "SELECT * FROM ${EventObject.Entry.TABLE_NAME}"
        Log.d("myDB", "read all events")
        return db.rawQuery(query, null)
    }


    fun getEventById(id: Long) : Cursor {
        val db = this.readableDatabase
        val query = "SELECT * FROM ${EventObject.Entry.TABLE_NAME} WHERE ${BaseColumns._ID} = $id"
        Log.d("myDB", "read event with id ${id}")
        return db.rawQuery(query, null)
    }


    fun getEventByPrevId(prevId: Long) : Cursor {
        val db = this.readableDatabase
        val query = "SELECT * FROM ${EventObject.Entry.TABLE_NAME} WHERE ${EventObject.Entry.COLUMN_NAME_PREV_ID} = $prevId"
        Log.d("myDB", "read event with prevId ${prevId}")
        return db.rawQuery(query, null)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun addEvent(entry: MainEntry) : Long {
        val db = this.writableDatabase

        //create content values
        val values = ContentValues().apply {
            put(EventObject.Entry.COLUMN_NAME_TITLE, entry.title)
            put(EventObject.Entry.COLUMN_NAME_NOTE, entry.note)
            put(EventObject.Entry.COLUMN_NAME_CATEGORY, entry.category)
            put(EventObject.Entry.COLUMN_NAME_DATETIME, entry.dateTime.toString())
            //save as ISO8601 string: YYYY-MM-DD HH:MM:SS.SSS
/*            put(
                EventObject.Entry.COLUMN_NAME_DATETIME,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").format(entry.dateTime)
            )*/
            put(EventObject.Entry.COLUMN_NAME_IS_ROOT, entry.isRoot)
            put(EventObject.Entry.COLUMN_NAME_ROOT_ID, entry.rootID)
            put(EventObject.Entry.COLUMN_NAME_PREV_ID, entry.prevID)
        }

        Log.d("myDB", "event added: ${entry.toString()}")
        return db.insert(EventObject.Entry.TABLE_NAME, null, values)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun updateEvent(entry: MainEntry, id: Long) : Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(EventObject.Entry.COLUMN_NAME_TITLE, entry.title)
            put(EventObject.Entry.COLUMN_NAME_NOTE, entry.note)
            put(EventObject.Entry.COLUMN_NAME_CATEGORY, entry.category)
            put(EventObject.Entry.COLUMN_NAME_DATETIME, entry.dateTime.toString())
            //save as ISO8601 string: YYYY-MM-DD HH:MM:SS.SSS
/*            put(
                EventObject.Entry.COLUMN_NAME_DATETIME,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").format(entry.dateTime)
            )*/
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


    @RequiresApi(Build.VERSION_CODES.O)
    fun deleteEventById(id: Long) : Int {
        val db = this.writableDatabase
        var count = 0

        val cursor = getEventById(id)

        if(cursor.count > 0) {
            cursor.moveToFirst()

            val isRoot = cursor.getInt(cursor.getColumnIndex(EventObject.Entry.COLUMN_NAME_IS_ROOT))

            // selected event is the root --> delete the whole series (all events with the same root-id, including root itself)
            if(isRoot == 1) {
                count = db.delete(
                    EventObject.Entry.TABLE_NAME,
                    "${EventObject.Entry.COLUMN_NAME_ROOT_ID} = ?",
                    arrayOf("$id")
                )
            }
            // selected event is NOT root --> if event is in the middle, set the prevID of the next event to the prevID of this event, cause this event will be deleted
            // finally delete this event
            else {
                val newCursor = getEventByPrevId(id)

                if(newCursor.count > 0) {
                    newCursor.moveToFirst()

                    val newId = newCursor.getLong(newCursor.getColumnIndex("_id"))
                    val title = newCursor.getString(newCursor.getColumnIndex(DBHelper.EventObject.Entry.COLUMN_NAME_TITLE))
                    val note = newCursor.getString(newCursor.getColumnIndex(DBHelper.EventObject.Entry.COLUMN_NAME_NOTE))
                    val category = newCursor.getString(newCursor.getColumnIndex(DBHelper.EventObject.Entry.COLUMN_NAME_CATEGORY))
                    val datetime = newCursor.getString(newCursor.getColumnIndex(DBHelper.EventObject.Entry.COLUMN_NAME_DATETIME))
                    val isRoot = newCursor.getInt(newCursor.getColumnIndex(DBHelper.EventObject.Entry.COLUMN_NAME_IS_ROOT))
                    val rootID = newCursor.getLong(newCursor.getColumnIndex(DBHelper.EventObject.Entry.COLUMN_NAME_ROOT_ID))
                    val prevID = cursor.getLong(cursor.getColumnIndex(DBHelper.EventObject.Entry.COLUMN_NAME_PREV_ID))

                    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
                    val tmpDatetime: LocalDateTime = LocalDateTime.parse(datetime, formatter)
                    val myIsRoot: Boolean = if(isRoot == 1) true; else false

                    var entry: MainEntry = MainEntry(title, category, tmpDatetime, myIsRoot, rootID, prevID)

                    updateEvent(entry, newId)
                }

                count = db.delete(
                    EventObject.Entry.TABLE_NAME,
                    " ${BaseColumns._ID} = ?",
                    arrayOf("$id")
                )
            }
        }

        Log.d("myDB", "event with $id deleted: count $count")
        return count
    }
}