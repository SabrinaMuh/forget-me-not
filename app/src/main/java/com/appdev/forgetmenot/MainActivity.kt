package com.appdev.forgetmenot

import android.database.Cursor
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.view.Menu
import android.view.MenuItem
import android.widget.ListView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.time.LocalDateTime
import kotlin.collections.ArrayList

/*
 * Forget me not
 * AbbDev 2021
 * @authers: Sabrina Muhrer, Harald Moitzi
 */

class MainActivity : AppCompatActivity(), AddEnteryDialogFragment.NoticeDialogListener, AddCategoryDialogFragment.NoticeDialogListener{
    lateinit var dbHelper: DBHelper  
  
    var alMainEntries: ArrayList<MainEntry> = ArrayList<MainEntry>()
/*    lateinit var adapter: MainEntryAdapter*/
    private lateinit var adapter: EventCursorAdapter
    var categories: ArrayList<String> = arrayListOf("Category", "Med", "Sport", "Shopping", "Important", "Others")

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = DBHelper(applicationContext)

/*        var dateTime = LocalDateTime.of(2021, 6, 15, 8, 0, 0, 0)

        alMainEntries.apply {
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

/*        adapter = MainEntryAdapter(this, alMainEntries)
        lvMain.adapter = adapter*/

        // NOW: reading out of DB
        val cursor: Cursor = dbHelper.getAllEvents()
        adapter = EventCursorAdapter(this, cursor)
        lvMain.adapter = adapter

        val button = findViewById<FloatingActionButton>(R.id.addButton)
        button.setOnClickListener {
            val dialog = AddEnteryDialogFragment()
            val args = Bundle()
            args.putStringArrayList("categories", categories)
            dialog.arguments = args
            dialog.show(supportFragmentManager, "addEntery")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onAddEnteryDialogPositiveClick(title: String, category: String, startDay: Int,
                                       startMonth: Int, startYear: Int, startTimeHour: Int, startTimeMinute: Int, frequency: String,
                                       endDay: Int, endMonth: Int, endYear: Int, endTimeHour: Int, endTimeMinute: Int
    ) {
/*        alMainEntries.add(MainEntry(title, category, LocalDateTime.of(startYear, startMonth, startDay, startTimeHour, startTimeMinute)))*/

        dbHelper.addEvent(MainEntry(title, category, LocalDateTime.of(startYear, startMonth, startDay, startTimeHour, startTimeMinute)))

/*        adapter.notifyDataSetChanged()*/

        // NOW: reading out of DB
        val cursor: Cursor = dbHelper.getAllEvents()
        adapter.changeCursor(cursor);

        Toast.makeText(this, "Entry saved", Toast.LENGTH_SHORT).show()
    }

    override fun onAddCategoryDialogPositiveClick(title: String){
        categories.add(title)
        Toast.makeText(this, "Added Category", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId){
        R.id.add_category->{
            val dialog = AddCategoryDialogFragment()
            dialog.show(supportFragmentManager, "addCategorie")
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
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

        val count = dbHelper.deleteEventById(13)
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