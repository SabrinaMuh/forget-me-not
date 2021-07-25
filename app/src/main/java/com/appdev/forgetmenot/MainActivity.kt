package com.appdev.forgetmenot

import android.app.*
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.ListView
import android.widget.SearchView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList


/*
 * Forget me not
 * AbbDev 2021
 * @authers: Sabrina Muhrer, Harald Moitzi
 */

class MainActivity : AppCompatActivity(), AddEnteryDialogFragment.NoticeDialogListener {
    lateinit var dbHelper: DBHelper  
  
    private lateinit var adapter: EventCursorAdapter

    var delay: Long = 0

    var categories: ArrayList<String> = arrayListOf("Medical", "Sport", "Shopping", "Important", "Job", "Education", "Occasion", "Others")

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = DBHelper(applicationContext)

        //init example events
        if(dbHelper.getAllEvents().count == 0) {
            initExampleEntries()
        }

        val lvMain = findViewById<ListView>(R.id.lvMain)

        // NOW: reading out of DB
        val cursor: Cursor = dbHelper.getAllEvents()
        adapter = EventCursorAdapter(this, cursor)
        lvMain.adapter = adapter

        lvMain.onItemLongClickListener = OnItemLongClickListener { arg0, arg1, pos, id ->
            /*Toast.makeText(applicationContext, "long clicked", Toast.LENGTH_SHORT).show()*/

            val builder = AlertDialog.Builder(this)
            builder.setMessage(R.string.dialog_edit_or_delete_message).setTitle(R.string.dialog_edit_or_delete_title)

            builder.setNeutralButton(R.string.cancel, DialogInterface.OnClickListener { dialog, which ->
                Log.i("UIAction", "cancel button pressed")
                Log.i("UIAction", "ID $id") // is the id of the database
            })
            builder.setNegativeButton(R.string.dialog_delete, DialogInterface.OnClickListener { dialog, which ->
                Log.i("UIAction", "delete button pressed")

                val event: EventEntry? = dbHelper.getEventById(id)

                if(event != null) {
                    val builder2 = AlertDialog.Builder(this)

                    if(event.isRoot) {
                        builder2.setMessage(R.string.dialog_delete_root_event_message)
                    }

                    builder2.setTitle(R.string.dialog_delete_event_title)

                    builder2.setPositiveButton(
                        R.string.dialog_yes,
                        DialogInterface.OnClickListener { dialog2, which ->
                            if (event!!.isRoot) {
                                cancelNotification(event!!.title, id.toInt())
                                var i: Long = id
                                while (dbHelper.getEventById(i) != null && dbHelper.getEventById(i)!!.rootID == id) {
                                    cancelNotification(dbHelper.getEventById(i)?.title, i.toInt())
                                    i++
                                }
                            } else {
                                cancelNotification(event!!.title, id.toInt())
                            }
                            dbHelper.deleteEventById(id)
                            val cursor: Cursor = dbHelper.getAllEvents()
                            adapter.changeCursor(cursor)
                        })

                    builder2.setNegativeButton(
                        R.string.dialog_no,
                        DialogInterface.OnClickListener { dialog2, which ->
                        })

                    val dialog2 = builder2.create()
                    dialog2.show()
                }
            })
            builder.setPositiveButton(R.string.dialog_edit, DialogInterface.OnClickListener { dialog, which ->
                Log.i("UIAction", "edit button pressed")

                val dialog = AddEnteryDialogFragment()
                val args = Bundle()

                val event: EventEntry? = dbHelper.getEventById(id)

                args.putStringArrayList("categories", categories)

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
            args.putSerializable("categories", categories)
            dialog.arguments = args
            dialog.show(supportFragmentManager, "addEntery")
        }
    }

    //get milliseconds
    @RequiresApi(Build.VERSION_CODES.O)
    fun getMilliseconds(futureDate: Date): Long {
        val currentldt = LocalDateTime.now(ZoneId.systemDefault())
        val currentzdt = currentldt.atZone(ZoneId.systemDefault())
        val currentdate = Date.from(currentzdt.toInstant())
        return futureDate.time - currentdate.time
    }

    //delete notification
    private fun cancelNotification(text: String?, notificationId: Int){
        val notificationIntent = Intent(this, MyNotificationPublisher::class.java)
        notificationIntent.putExtra(MyNotificationPublisher().NOTIFICATION_ID, notificationId)
        notificationIntent.putExtra(MyNotificationPublisher().TEXT, text)

        val pendingIntent = PendingIntent.getBroadcast(this, notificationId, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val alarmManager: AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }
    //schedule notification
    @RequiresApi(Build.VERSION_CODES.M)
    private fun scheduleNotification(notificationId: Int, title: String, text: String, delay: Long) {
        val notificationIntent = Intent(this, MyNotificationPublisher::class.java)
        notificationIntent.putExtra(MyNotificationPublisher().NOTIFICATION_ID, notificationId)
        notificationIntent.putExtra(MyNotificationPublisher().TITLE, title)
        notificationIntent.putExtra(MyNotificationPublisher().TEXT, text)

        //notificationid needs to be unique by pendingIntent
        val pendingIntent = PendingIntent.getBroadcast(this, notificationId, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val alarmManager: AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerAtMillis: Long = SystemClock.elapsedRealtime() + delay
        if (alarmManager!=null) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onAddEnteryDialogPositiveClick(eventIdOnEdit: Long, title: String, category: String, note: String, startDay: Int,
                                       startMonth: Int, startYear: Int, startTimeHour: Int, startTimeMinute: Int, frequency: String,
                                       endDay: Int, endMonth: Int, endYear: Int, endTimeHour: Int, endTimeMinute: Int
    ) {
        // add new Event
        if(eventIdOnEdit.compareTo(0) == 0) {
            addEvent(eventIdOnEdit, title, category, note, startDay,
                startMonth+1, startYear, startTimeHour, startTimeMinute, frequency,
                endDay, endMonth+1, endYear, endTimeHour, endTimeMinute)
        }
        // edit existing Event
        else {
            editEvent(eventIdOnEdit, title, category, note, startDay,
                startMonth+1, startYear, startTimeHour, startTimeMinute, frequency,
                endDay, endMonth+1, endYear, endTimeHour, endTimeMinute)
        }

        Snackbar.make(findViewById(R.id.view), "Entry saved", Snackbar.LENGTH_LONG).show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun addEvent(eventIdOnEdit: Long, title: String, category: String, note: String, startDay: Int,
                 startMonth: Int, startYear: Int, startTimeHour: Int, startTimeMinute: Int, frequency: String,
                 endDay: Int, endMonth: Int, endYear: Int, endTimeHour: Int, endTimeMinute: Int) {

        //generate number of events and save into db
        var startDateTime: LocalDateTime = LocalDateTime.of(startYear, startMonth, startDay, startTimeHour, startTimeMinute)
        val endDateTime: LocalDateTime = LocalDateTime.of(endYear, endMonth, endDay, endTimeHour, endTimeMinute)

        // at root event
        val event: EventEntry = EventEntry(title, category, note, LocalDateTime.of(startYear, startMonth, startDay, startTimeHour, startTimeMinute), frequency = frequency, isRoot = true)
        val rootId = dbHelper.addEvent(event)
        // set rootid of root to itself --> not needed any more

        //SAMU
        var zdt = startDateTime.atZone(ZoneId.systemDefault())
        var startDate = Date.from(zdt.toInstant())
        delay = getMilliseconds(startDate)
        scheduleNotification(rootId.toInt(), title, note, delay)

        event.rootID = rootId
        dbHelper.updateEvent(event, rootId) // set rootid of root to itself


        if(frequency.equals("daily")) {
            startDateTime = startDateTime.plusDays(1)

            var prevId = rootId
            while(startDateTime.compareTo(endDateTime) <= 0) {
                prevId = dbHelper.addEvent(EventEntry(title, category, note, startDateTime, frequency = frequency, isRoot = false, rootId, prevId))

                //SAMU
                zdt = startDateTime.atZone(ZoneId.systemDefault())
                startDate = Date.from(zdt.toInstant())
                delay = getMilliseconds(startDate)
                scheduleNotification(prevId.toInt(), title, note, delay)

                startDateTime = startDateTime.plusDays(1)
            }
        }
        else if(frequency.equals("weekly")) {
            startDateTime = startDateTime.plusWeeks(1)

            var prevId = rootId
            while(startDateTime.compareTo(endDateTime) <= 0) {
                prevId = dbHelper.addEvent(EventEntry(title, category, note, startDateTime, frequency = frequency, isRoot = false, rootId, prevId))

                //SAMU
                zdt = startDateTime.atZone(ZoneId.systemDefault())
                startDate = Date.from(zdt.toInstant())
                delay = getMilliseconds(startDate)
                scheduleNotification(prevId.toInt(), title, note, delay)

                startDateTime = startDateTime.plusWeeks(1)
            }
        }
        else if(frequency.equals("monthly")) {
            startDateTime = startDateTime.plusMonths(1)

            var prevId = rootId
            while(startDateTime.compareTo(endDateTime) <= 0) {
                prevId = dbHelper.addEvent(EventEntry(title, category, note, startDateTime, frequency = frequency, isRoot = false, rootId, prevId))

                //SAMU
                zdt = startDateTime.atZone(ZoneId.systemDefault())
                startDate = Date.from(zdt.toInstant())
                delay = getMilliseconds(startDate)
                scheduleNotification(prevId.toInt(), title, note, delay)

                startDateTime = startDateTime.plusMonths(1)
            }
        }

        // NOW: reading out of DB
        val cursor: Cursor = dbHelper.getAllEvents()
        adapter.changeCursor(cursor)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun editEvent(eventIdOnEdit: Long, title: String, category: String, note: String, startDay: Int,
                  startMonth: Int, startYear: Int, startTimeHour: Int, startTimeMinute: Int, frequency: String,
                  endDay: Int, endMonth: Int, endYear: Int, endTimeHour: Int, endTimeMinute: Int) {

        val event: EventEntry? = dbHelper.getEventById(eventIdOnEdit)

        if(event != null) {
            //delete the whole series and create a new one, instead of updating all events
            if(event.isRoot) {
                dbHelper.deleteEventById(event.id)
                addEvent(eventIdOnEdit, title, category, note, startDay,
                    startMonth, startYear, startTimeHour, startTimeMinute, frequency,
                    endDay, endMonth, endYear, endTimeHour, endTimeMinute)
            }
            else {
                val startDateTime = LocalDateTime.of(startYear, startMonth, startDay, startTimeHour, startTimeMinute)

                val newEvent: EventEntry = EventEntry(title, category, note, startDateTime, frequency = frequency, isRoot = false, event.rootID, event.prevID)

                //SAMU
                val zdt = startDateTime.atZone(ZoneId.systemDefault())
                val startDate = Date.from(zdt.toInstant())
                delay = getMilliseconds(startDate)
                scheduleNotification(eventIdOnEdit.toInt(), title, note, delay)
                dbHelper.updateEvent(newEvent, event.id)
            }
        }

        // NOW: reading out of DB
        val cursor: Cursor = dbHelper.getAllEvents()
        adapter.changeCursor(cursor)
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)

        val searchItem = menu?.findItem(R.id.app_bar_search)

        if(searchItem != null) {
            val appBarSearch = searchItem.actionView as SearchView
            appBarSearch.queryHint = "search for title"

            appBarSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {

                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    if(newText.isNullOrEmpty() == false) {
                        val cursor: Cursor = dbHelper.getAllEventsByTitle(newText.trim())
                        adapter.changeCursor(cursor)
                    }
                    else { // on empty string read all entries
                        val cursor: Cursor = dbHelper.getAllEvents()
                        adapter.changeCursor(cursor)
                    }

                    return true
                }
            })
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId){
        R.id.info -> {
            val intent = Intent(this, InfoActivity::class.java)
            startActivity(intent)
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun initExampleEntries() {
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")

        var event: EventEntry = EventEntry("Running", "Sport", "series example", LocalDateTime.parse("2021-09-01T18:00", formatter), "daily", true, 0, 0)
        var rootId = dbHelper.addEvent(event)
        event.rootID = rootId
        dbHelper.updateEvent(event, rootId)

        event = EventEntry("Running", "Sport", "series example", LocalDateTime.parse("2021-09-02T18:00", formatter), "daily", false, rootId, rootId)
        var id = dbHelper.addEvent(event)
        event = EventEntry("Running", "Sport", "series example", LocalDateTime.parse("2021-09-03T18:00", formatter), "daily", false, rootId, id)
        id = dbHelper.addEvent(event)
        event = EventEntry("Running", "Sport", "series example", LocalDateTime.parse("2021-09-04T18:00", formatter), "daily", false, rootId, id)
        id = dbHelper.addEvent(event)
        event = EventEntry("Running", "Sport", "series example", LocalDateTime.parse("2021-09-05T18:00", formatter), "daily", false, rootId, id)
        id = dbHelper.addEvent(event)
        event = EventEntry("Running", "Sport", "series example", LocalDateTime.parse("2021-09-06T18:00", formatter), "daily", false, rootId, id)
        id = dbHelper.addEvent(event)
        event = EventEntry("Running", "Sport", "series example", LocalDateTime.parse("2021-09-07T18:00", formatter), "daily", false, rootId, id)
        id = dbHelper.addEvent(event)

        event = EventEntry("Project", "Education", "one time event", LocalDateTime.parse("2021-07-31T23:00", formatter), "daily", true, 0, 0)
        rootId = dbHelper.addEvent(event)
        event.rootID = rootId
        dbHelper.updateEvent(event, rootId)

        event = EventEntry("Birthday", "Occasion", "one time event", LocalDateTime.parse("2021-09-03T08:00", formatter), "daily", true, 0, 0)
        rootId = dbHelper.addEvent(event)
        event.rootID = rootId
        dbHelper.updateEvent(event, rootId)
    }

}