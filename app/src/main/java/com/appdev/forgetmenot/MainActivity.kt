package com.appdev.forgetmenot

import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.ListView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import java.time.LocalDateTime


/*
 * Forget me not
 * AbbDev 2021
 * @authers: Sabrina Muhrer, Harald Moitzi
 */

class MainActivity : AppCompatActivity(), AddEnteryDialogFragment.NoticeDialogListener, AddCategoryDialogFragment.NoticeDialogListener{
    lateinit var dbHelper: DBHelper  
  
    private lateinit var adapter: EventCursorAdapter
    /*var categories: ArrayList<String> = arrayListOf("Category", "Med", "Sport", "Shopping", "Important", "Others")*/
    var categories: ArrayList<CategoryEntry> = ArrayList<CategoryEntry>()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = DBHelper(applicationContext)
        initCategories(dbHelper)

        val lvMain = findViewById<ListView>(R.id.lvMain)

        // NOW: reading out of DB
        val cursor: Cursor = dbHelper.getAllEvents()
        adapter = EventCursorAdapter(this, cursor)
        lvMain.adapter = adapter

        lvMain.onItemLongClickListener = OnItemLongClickListener { arg0, arg1, pos, id ->
            /*Toast.makeText(applicationContext, "long clicked", Toast.LENGTH_SHORT).show()*/

            val builder = AlertDialog.Builder(this)
            builder.setMessage("Please choose your option").setTitle("Edit or Delete Event?")

            builder.setNeutralButton("CANCEL", DialogInterface.OnClickListener { dialog, which ->
                Log.i("UIAction", "cancel button pressed")
                Log.i("UIAction", "ID $id") // is the id of the database
            })
            builder.setNegativeButton("DELETE", DialogInterface.OnClickListener { dialog, which ->
                Log.i("UIAction", "delete button pressed")

                dbHelper.deleteEventById(id)
                val cursor: Cursor = dbHelper.getAllEvents()
                adapter.changeCursor(cursor)
            })
            builder.setPositiveButton("EDIT", DialogInterface.OnClickListener { dialog, which ->
                Log.i("UIAction", "edit button pressed")

                val dialog = AddEnteryDialogFragment()
                val args = Bundle()

                val event: EventEntry? = dbHelper.getEventById(id)
/*            args.putStringArrayList("categories", categories)*/

                args.putSerializable("categories", categories)
                args.putSerializable("event", event)
                dialog.arguments = args
                dialog.show(supportFragmentManager, "addEntery")
            })

            val dialog = builder.create()
            dialog.show()

            true
        }

        val button = findViewById<FloatingActionButton>(R.id.addButton)
        button.setOnClickListener {
            val dialog = AddEnteryDialogFragment()
            val args = Bundle()
/*            args.putStringArrayList("categories", categories)*/
            args.putSerializable("categories", categories)
            dialog.arguments = args
            dialog.show(supportFragmentManager, "addEntery")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onAddEnteryDialogPositiveClick(title: String, category: String, startDay: Int,
                                       startMonth: Int, startYear: Int, startTimeHour: Int, startTimeMinute: Int, frequency: String,
                                       endDay: Int, endMonth: Int, endYear: Int, endTimeHour: Int, endTimeMinute: Int
    ) {
        //generate number of events and save into db
        var startDateTime: LocalDateTime = LocalDateTime.of(startYear, startMonth, startDay, startTimeHour, startTimeMinute)
        val endDateTime: LocalDateTime = LocalDateTime.of(endYear, endMonth, endDay, endTimeHour, endTimeMinute)

        // at root event
        val rootId = dbHelper.addEvent(EventEntry(title, category, LocalDateTime.of(startYear, startMonth, startDay, startTimeHour, startTimeMinute), isRoot = true))
        // set rootid of root to itself --> not needed any more
        /*
        event.rootID = rootId
        dbHelper.updateEvent(event, rootId) // set rootid of root to itself
        */

        if(frequency.equals("daily")) {
            startDateTime = startDateTime.plusDays(1)

            var prevId = rootId
            while(startDateTime.compareTo(endDateTime) <= 0) {
                prevId = dbHelper.addEvent(EventEntry(title, category, startDateTime, isRoot = false, rootId, prevId))
                startDateTime = startDateTime.plusDays(1)
            }
        }
        else if(frequency.equals("weekly")) {
            startDateTime = startDateTime.plusWeeks(1)

            var prevId = rootId
            while(startDateTime.compareTo(endDateTime) <= 0) {
                prevId = dbHelper.addEvent(EventEntry(title, category, startDateTime, isRoot = false, rootId, prevId))
                startDateTime = startDateTime.plusWeeks(1)
            }
        }
        else if(frequency.equals("monthly")) {
            startDateTime = startDateTime.plusMonths(1)

            var prevId = rootId
            while(startDateTime.compareTo(endDateTime) <= 0) {
                prevId = dbHelper.addEvent(EventEntry(title, category, startDateTime, isRoot = false, rootId, prevId))
                startDateTime = startDateTime.plusMonths(1)
            }
        }

        // NOW: reading out of DB
        val cursor: Cursor = dbHelper.getAllEvents()
        adapter.changeCursor(cursor);

        Snackbar.make(findViewById(R.id.view), "Entry saved", Snackbar.LENGTH_LONG).show()
        //Toast.makeText(this, "Entry saved", Toast.LENGTH_SHORT).show()
    }

    override fun onAddCategoryDialogPositiveClick(title: String){
        /*categories.add(title)*/
        categories.add((CategoryEntry(title, null)))
        dbHelper.addCategory(title, "","")
        Snackbar.make(findViewById(R.id.view), "Added Category", Snackbar.LENGTH_LONG).show()
        //Toast.makeText(this, "Added Category", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId){
        /*R.id.add_category->{
            val dialog = AddCategoryDialogFragment()
            dialog.show(supportFragmentManager, "addCategorie")
            true
        }*/
        R.id.info->{
            val intent = Intent(this, InfoActivity::class.java)
            startActivity(intent)
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun initCategories(dbHelper: DBHelper)
    {
        var cursorCategory: Cursor = dbHelper.getAllCategories()

        // init categories
        if(cursorCategory.count == 0) {
            dbHelper.addCategory("Important", "#FF4081", "")
            dbHelper.addCategory("Sport", "#4183D7", "")
            dbHelper.addCategory("Medical", "#D2527F", "")
            dbHelper.addCategory("Shopping", "#F4D03F", "")
            dbHelper.addCategory("Other", "#26A65B", "")

            cursorCategory = dbHelper.getAllCategories()
        }

        with(cursorCategory) {
            while(moveToNext()) {
                val name = getString(getColumnIndex(DBHelper.CategoryObject.Entry.COLUMN_NAME_NAME))
                val color = getString(getColumnIndex(DBHelper.CategoryObject.Entry.COLUMN_NAME_COLOR))
                val img = getString(getColumnIndex(DBHelper.CategoryObject.Entry.COLUMN_NAME_IMG))

                if(color.isNullOrEmpty()) {
                    categories.add(CategoryEntry(name, null, img))
                }
                else {
                    categories.add(CategoryEntry(name, Color.valueOf(Color.parseColor(color)), img))
                }
            }
        }
    }

/*    @RequiresApi(Build.VERSION_CODES.O)
    fun createDB() {
        dbHelper = DBHelper(applicationContext)
        dbHelper.deleteAllEvents()

        dbHelper.addEvent(
            EventEntry("Fleisch", "Shopping",
                LocalDateTime.of(2021, 6, 15, 11, 0, 0, 0), isRoot = true, rootID = 1
            )
        )

        dbHelper.addEvent(EventEntry("Cola", "Shopping", LocalDateTime.of(2021, 6, 22, 11, 0, 0, 0), rootID = 1))

        dbHelper.addEvent(EventEntry("Zahnpasta", "Shopping", LocalDateTime.of(2021, 6, 29, 11, 0, 0, 0), rootID = 1))

        // root-event
        var dateTime: LocalDateTime = LocalDateTime.of(2021, 6, 15, 18, 0, 0, 0)
        var entry: EventEntry = EventEntry("Laufen", "Sport", dateTime, isRoot = true)
        var root_id = dbHelper.addEvent(entry)

        //set just created id as root_id of the root entry
        entry.rootID = root_id
        var count_updated: Int = dbHelper.updateEvent(entry, root_id)
        Log.d("myDB", "$count_updated updated")

        var prevId: Long = root_id

        var x = 9
        do {
            dateTime = dateTime.plusDays(1)
            entry = EventEntry("Laufen", "Sport", dateTime, rootID = root_id, prevID = prevId)
            prevId = dbHelper.addEvent(entry) // new prevId for next entry
            x--
        } while(x > 0)

        val count = dbHelper.deleteEventById(13)
        Log.d("myDB", "$count deleted")
    }*/

}