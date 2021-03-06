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
            const val COLUMN_NAME_FREQUENCY = "frequency"
            const val COLUMN_NAME_IS_ROOT = "is_root"
            const val COLUMN_NAME_ROOT_ID = "root_id"
            const val COLUMN_NAME_PREV_ID = "prev_id"

            const val SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS $TABLE_NAME (" +
                    "${BaseColumns._ID} INTEGER PRIMARY KEY, " +
                    "$COLUMN_NAME_TITLE TEXT, " +
                    "$COLUMN_NAME_NOTE TEXT, " +
                    "$COLUMN_NAME_CATEGORY TEXT, " +
                    "$COLUMN_NAME_DATETIME TEXT, " +
                    "$COLUMN_NAME_FREQUENCY TEXT, " +
                    "$COLUMN_NAME_IS_ROOT BOOLEAN, " +
                    "$COLUMN_NAME_ROOT_ID INTEGER, " +
                    "$COLUMN_NAME_PREV_ID INTEGER)"

            const val SQL_DROP_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME"
        }
    }

    object CategoryObject {
        object Entry: BaseColumns {
            const val TABLE_NAME = "Category"
            const val COLUMN_NAME_NAME = "name"
            const val COLUMN_NAME_COLOR = "color"
            const val COLUMN_NAME_IMG = "img"


            const val SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS $TABLE_NAME (" +
                    "${BaseColumns._ID} INTEGER PRIMARY KEY, " +
                    "$COLUMN_NAME_NAME TEXT, " +
                    "$COLUMN_NAME_COLOR TEXT, " +
                    "$COLUMN_NAME_IMG TEXT)"

            const val SQL_DROP_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME"
        }
    }


    override fun onCreate(db: SQLiteDatabase?) {
        Log.d("myDB", "${EventObject.Entry.SQL_CREATE_TABLE}")
        db?.execSQL(CategoryObject.Entry.SQL_CREATE_TABLE)
        db?.execSQL(EventObject.Entry.SQL_CREATE_TABLE)
    }


    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL(CategoryObject.Entry.SQL_DROP_TABLE)
        db?.execSQL(EventObject.Entry.SQL_DROP_TABLE)
        onCreate(db)
    }


    fun getAllCategories() : Cursor {
        val db = this.readableDatabase
        val query = "SELECT * FROM ${CategoryObject.Entry.TABLE_NAME}"
        Log.d("myDB", "read all events")
        return db.rawQuery(query, null)
    }

    fun addCategory(name: String, color: String, img: String) : Long {
        val db = this.writableDatabase

        //create content values
        val values = ContentValues().apply {
            put(CategoryObject.Entry.COLUMN_NAME_NAME, name)
            put(CategoryObject.Entry.COLUMN_NAME_COLOR, color)
            put(CategoryObject.Entry.COLUMN_NAME_IMG, img)
        }

        Log.d("myDB", "category added: ${name}, ${color}, ${img}")
        return db.insert(CategoryObject.Entry.TABLE_NAME, null, values)
    }



    fun getAllEvents() : Cursor {
        val db = this.readableDatabase
        val query = "SELECT * FROM ${EventObject.Entry.TABLE_NAME} ORDER BY ${EventObject.Entry.COLUMN_NAME_DATETIME}"
        Log.d("myDB", "read all events")
        return db.rawQuery(query, null)
    }


    fun getAllEventsByTitle(title: String) : Cursor {
        val db = this.readableDatabase
        val query = "SELECT * FROM ${EventObject.Entry.TABLE_NAME} WHERE LOWER(${EventObject.Entry.COLUMN_NAME_TITLE}) = LOWER('$title')"
        Log.d("myDB", "read all events")
        return db.rawQuery(query, null)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun getEventById(id: Long) : EventEntry? {
        val db = this.readableDatabase
        val query = "SELECT * FROM ${EventObject.Entry.TABLE_NAME} WHERE ${BaseColumns._ID} = $id"
        Log.d("myDB", "read event with id ${id}")
        val cursor: Cursor = db.rawQuery(query, null)

        if(cursor.count > 0) {
            cursor.moveToFirst()

            with(cursor) {
                val id = getLong(getColumnIndex(BaseColumns._ID))
                val title = getString(getColumnIndex(DBHelper.EventObject.Entry.COLUMN_NAME_TITLE))
                val note = getString(getColumnIndex(DBHelper.EventObject.Entry.COLUMN_NAME_NOTE))
                val category = getString(getColumnIndex(DBHelper.EventObject.Entry.COLUMN_NAME_CATEGORY))
                val dateTime = getString(getColumnIndex(DBHelper.EventObject.Entry.COLUMN_NAME_DATETIME))
                val frequency = getString(getColumnIndex(DBHelper.EventObject.Entry.COLUMN_NAME_FREQUENCY))
                val isRoot = getInt(getColumnIndex(DBHelper.EventObject.Entry.COLUMN_NAME_IS_ROOT))
                val rootId = getLong(getColumnIndex(DBHelper.EventObject.Entry.COLUMN_NAME_ROOT_ID))
                //IMPORTANT: set prev_id of next event to the prev_id of the current event, which will be deleted
                val prevId = cursor.getLong(cursor.getColumnIndex(DBHelper.EventObject.Entry.COLUMN_NAME_PREV_ID))

                val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
                val newDatetime: LocalDateTime = LocalDateTime.parse(dateTime, formatter) //convert from String e.g. "2021-06-29T11:00" to LocalDateTime
                val newIsRoot: Boolean = if(isRoot == 1) true; else false // convert to Boolean

                var entry: EventEntry = EventEntry(title, category, note, newDatetime, frequency, newIsRoot, rootId, prevId)
                entry.id = id

                return entry
            }
        }

        return null
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun getEventByPrevId(prevId: Long) : EventEntry? {
        val db = this.readableDatabase
        val query = "SELECT * FROM ${EventObject.Entry.TABLE_NAME} WHERE ${EventObject.Entry.COLUMN_NAME_PREV_ID} = $prevId"
        Log.d("myDB", "read event with prevId ${prevId}")
        val cursor: Cursor = db.rawQuery(query, null)

        if(cursor.count > 0) {
            cursor.moveToFirst()

            with(cursor) {
                val id = getLong(getColumnIndex(BaseColumns._ID))
                return getEventById(id)
            }
        }

        return null
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun addEvent(entry: EventEntry) : Long {
        val db = this.writableDatabase

        //create content values
        val values = ContentValues().apply {
            put(EventObject.Entry.COLUMN_NAME_TITLE, entry.title)
            put(EventObject.Entry.COLUMN_NAME_NOTE, entry.note)
            put(EventObject.Entry.COLUMN_NAME_CATEGORY, entry.category)
            put(EventObject.Entry.COLUMN_NAME_DATETIME, entry.dateTime.toString())
            put(EventObject.Entry.COLUMN_NAME_FREQUENCY, entry.frequency)
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
    fun updateEvent(entry: EventEntry, id: Long) : Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(EventObject.Entry.COLUMN_NAME_TITLE, entry.title)
            put(EventObject.Entry.COLUMN_NAME_NOTE, entry.note)
            put(EventObject.Entry.COLUMN_NAME_CATEGORY, entry.category)
            put(EventObject.Entry.COLUMN_NAME_DATETIME, entry.dateTime.toString())
            put(EventObject.Entry.COLUMN_NAME_FREQUENCY, entry.frequency)
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

        val event: EventEntry? = getEventById(id)

        if(event != null) {
            // selected event is the root --> delete the whole series (all events with the same root-id, including root itself)
            if(event.isRoot == true) {
                count = db.delete(
                    EventObject.Entry.TABLE_NAME,
                    "${EventObject.Entry.COLUMN_NAME_ROOT_ID} = ?",
                    arrayOf("$id")
                )
            }
            // selected event is NOT root --> if event is in the middle, set the prevID of the next event to the prevID of this event, cause this event will be deleted
            // finally delete this event
            else {
                val nextEvent: EventEntry?  = getEventByPrevId(id)

                if(nextEvent != null) {
                    nextEvent.prevID = event.prevID

                    updateEvent(nextEvent, nextEvent.id)
                }

                // delete current event
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


    @RequiresApi(Build.VERSION_CODES.O)
    fun getLastEventOfSeries(rootId: Long) : EventEntry? {
        val db = this.readableDatabase
        val query = "SELECT ${BaseColumns._ID} FROM ${EventObject.Entry.TABLE_NAME} " +
                "WHERE ${EventObject.Entry.COLUMN_NAME_ROOT_ID} = ${rootId} " +
                "AND ${EventObject.Entry.COLUMN_NAME_DATETIME} = " +
                "(SELECT MAX(${EventObject.Entry.COLUMN_NAME_DATETIME}) FROM EVENT " +
                "WHERE ${EventObject.Entry.COLUMN_NAME_ROOT_ID} = ${rootId})"
        Log.d("myDB", "getLatestEventOfSeries with prevId ${rootId}")
        val cursor: Cursor = db.rawQuery(query, null)

        if(cursor.count > 0) {
            cursor.moveToFirst()

            with(cursor) {
                val id = getLong(getColumnIndex(BaseColumns._ID))
                return getEventById(id)
            }
        }

        return null
    }

}