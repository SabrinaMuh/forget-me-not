package com.appdev.forgetmenot

import android.database.Cursor
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.annotation.RequiresApi
import java.sql.Timestamp
import java.time.LocalDateTime

/*
 * Forget me not
 * AbbDev 2021
 * @authers: Sabrina Muhrer, Harald Moitzi
 */

class MainActivity : AppCompatActivity() {
    lateinit var dbHelper: DBHelper

    //Test
    //Test2
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var alMainEntries: ArrayList<MainEntry> = ArrayList<MainEntry>()

        var dateTime = LocalDateTime.of(2021, 6, 15, 8, 0, 0, 0)

/*        alMainEntries.apply {
            add(MainEntry("Fleisch", "Shopping", LocalDateTime.of(2021, 6, 15, 11, 0, 0, 0)))
            add(MainEntry("Aspro", "Med", LocalDateTime.of(2021, 6, 15, 8, 0, 0, 0)))
            add(MainEntry("Laufen", "Sport", LocalDateTime.of(2021, 6, 15, 18, 0, 0, 0)))
            add(MainEntry("Cola", "Shopping", LocalDateTime.of(2021, 6, 22, 11, 0, 0, 0)))
            add(MainEntry("Aspro", "Med", LocalDateTime.of(2021, 6, 16, 8, 0, 0, 0)))
            add(MainEntry("Laufen", "Sport", LocalDateTime.of(2021, 6, 16, 18, 0, 0, 0)))
            add(MainEntry("Zahnpasta", "Shopping", LocalDateTime.of(2021, 6, 29, 11, 0, 0, 0)))
            add(MainEntry("Aspro", "Med", LocalDateTime.of(2021, 6, 17, 8, 0, 0, 0)))
            add(MainEntry("Laufen", "Sport", LocalDateTime.of(2021, 6, 17, 18, 0, 0, 0)))
            add(MainEntry("Aspro", "Med", LocalDateTime.of(2021, 6, 18, 8, 0, 0, 0)))
            add(MainEntry("Laufen", "Sport", LocalDateTime.of(2021, 6, 18, 18, 0, 0, 0)))
            add(MainEntry("Aspro", "Med", LocalDateTime.of(2021, 6, 19, 8, 0, 0, 0)))
            add(MainEntry("Laufen", "Sport", LocalDateTime.of(2021, 6, 19, 18, 0, 0, 0)))
            add(MainEntry("Aspro", "Med", LocalDateTime.of(2021, 6, 20, 8, 0, 0, 0)))
            add(MainEntry("Laufen", "Sport", LocalDateTime.of(2021, 6, 20, 18, 0, 0, 0)))
            add(MainEntry("Aspro", "Med", LocalDateTime.of(2021, 6, 21, 8, 0, 0, 0)))
            add(MainEntry("Laufen", "Sport", LocalDateTime.of(2021, 6, 21, 18, 0, 0, 0)))
            add(MainEntry("Aspro", "Med", LocalDateTime.of(2021, 6, 22, 8, 0, 0, 0)))
            add(MainEntry("Laufen", "Sport", LocalDateTime.of(2021, 6, 22, 18, 0, 0, 0)))
            add(MainEntry("Aspro", "Med", LocalDateTime.of(2021, 6, 23, 8, 0, 0, 0)))
            add(MainEntry("Laufen", "Sport", LocalDateTime.of(2021, 6, 23, 18, 0, 0, 0)))
            add(MainEntry("Strom", "Important", LocalDateTime.of(2021, 6, 15, 8, 0, 0, 0) ))
            add(MainEntry("Birthday", "Other", LocalDateTime.of(2021, 6, 15, 8, 0, 0, 0) ))
        }*/

        val lvMain = findViewById<ListView>(R.id.lvMain)

/*        val adapter = MainEntryAdapter(this, alMainEntries)
        lvMain.adapter = adapter*/


        createDB()
        val cursor: Cursor = dbHelper.getAllEvents()
        val adapter = EventCursorAdapter(this, cursor)
        lvMain.adapter = adapter
/*        displayAllEvents()*/
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createDB() {
        dbHelper = DBHelper(applicationContext)
        dbHelper.deleteAllEvents()

        dbHelper.addEvent(
            MainEntry("Fleisch", "Shopping",
                LocalDateTime.of(2021, 6, 15, 11, 0, 0, 0), isRoot = true, rootID = 1
            )
        )

        dbHelper.addEvent(MainEntry("Cola", "Shopping", LocalDateTime.of(2021, 6, 22, 11, 0, 0, 0), rootID = 1))

        dbHelper.addEvent(MainEntry("Zahnpasta", "Shopping", LocalDateTime.of(2021, 6, 29, 11, 0, 0, 0), rootID = 1))

        // root-event
        var dateTime: LocalDateTime = LocalDateTime.of(2021, 6, 15, 18, 0, 0, 0)
        var entry: MainEntry = MainEntry("Laufen", "Sport", dateTime, isRoot = true)
        var root_id = dbHelper.addEvent(entry)

        //set just created id as root_id of the root entry
        entry.rootID = root_id
        var count_updated: Int = dbHelper.updateEvent(entry, root_id)
        Log.d("myDB", "$count_updated updated")

        var prevId: Long = root_id

        var x = 9
        do {
            dateTime = dateTime.plusDays(1)
            entry = MainEntry("Laufen", "Sport", dateTime, rootID = root_id, prevID = prevId)
            prevId = dbHelper.addEvent(entry) // new prevId for next entry
            x--
        } while(x > 0)

        val count = dbHelper.deleteEventById(4)
        Log.d("myDB", "$count deleted")


/*        with(cursor) {
            while (moveToFirst()) {
                val title = getString(getColumnIndex(DBHelper.EventObject.Entry.COLUMN_NAME_TITLE))
                val note = getText(getColumnIndex(DBHelper.EventObject.Entry.COLUMN_NAME_NOTE))
                val category = getString(getColumnIndex(DBHelper.EventObject.Entry.COLUMN_NAME_CATEGORY))
                val datetime = getString(getColumnIndex(DBHelper.EventObject.Entry.COLUMN_NAME_DATETIME))
                val isRoot = getInt(getColumnIndex(DBHelper.EventObject.Entry.COLUMN_NAME_IS_ROOT))
                val rootID  = getLong(getColumnIndex(DBHelper.EventObject.Entry.COLUMN_NAME_ROOT_ID))
                val prevID  = getLong(getColumnIndex(DBHelper.EventObject.Entry.COLUMN_NAME_PREV_ID))
            }
        }*/

    }

/*    fun displayAllEvents() {
        Log.i("myDB", "Reading all events:")
        val cursor = dbHelper.getAllEvents()
        with(cursor) {
            while (moveToNext()) {
                val title = getString(getColumnIndex(DBHelper.EventObject.Entry.COLUMN_NAME_TITLE))
                val note = getText(getColumnIndex(DBHelper.EventObject.Entry.COLUMN_NAME_NOTE))
                val category = getString(getColumnIndex(DBHelper.EventObject.Entry.COLUMN_NAME_CATEGORY))
                val datetime = getString(getColumnIndex(DBHelper.EventObject.Entry.COLUMN_NAME_DATETIME))
                val isRoot = getInt(getColumnIndex(DBHelper.EventObject.Entry.COLUMN_NAME_IS_ROOT))
                val rootID  = getLong(getColumnIndex(DBHelper.EventObject.Entry.COLUMN_NAME_ROOT_ID))
                val prevID  = getLong(getColumnIndex(DBHelper.EventObject.Entry.COLUMN_NAME_PREV_ID))
            }
        }
    }*/
}