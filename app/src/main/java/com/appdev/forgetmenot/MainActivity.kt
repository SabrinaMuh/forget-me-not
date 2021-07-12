package com.appdev.forgetmenot

import android.app.*
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.ListView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import kotlin.collections.ArrayList


/*
 * Forget me not
 * AbbDev 2021
 * @authers: Sabrina Muhrer, Harald Moitzi
 */

class MainActivity : AppCompatActivity(), AddEnteryDialogFragment.NoticeDialogListener, AddCategoryDialogFragment.NoticeDialogListener{
    lateinit var dbHelper: DBHelper  
  
    private lateinit var adapter: EventCursorAdapter

    private var notificationManager: NotificationManager? = null
    var delay: Long = 0

    /*[SAMU]>: old category version*/
    var categories: ArrayList<String> = arrayListOf("Medical", "Sport", "Shopping", "Important", "Job", "Education", "Occasion", "Others")
    /*[SAMU]<: old category version*/

    /*[HAMO]>: new category version*/
    /*var categories: ArrayList<CategoryEntry> = ArrayList<CategoryEntry>()*/
    /*[HAMO]<: new category version*/

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = DBHelper(applicationContext)

        /*[HAMO]>: new category version*/
        /*initCategories(dbHelper)*/
        /*[HAMO]<: new category version*/

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
                //cancelNotification(id.toInt())
            })
            builder.setPositiveButton("EDIT", DialogInterface.OnClickListener { dialog, which ->
                Log.i("UIAction", "edit button pressed")

                val dialog = AddEnteryDialogFragment()
                val args = Bundle()

                val event: EventEntry? = dbHelper.getEventById(id)

/*[SAMU]>: old category version*/
                args.putStringArrayList("categories", categories)
/*[SAMU]<: old category version*/

/*[HAMO]>: new category version*/
/*                args.putSerializable("categories", categories)*/
/*[HAMO]<: new category version*/

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

    //get milliseconds
    @RequiresApi(Build.VERSION_CODES.O)
    fun getMilliseconds(futureDate: Date): Long {
        val currentldt = LocalDateTime.now(ZoneId.systemDefault())
        val currentzdt = currentldt.atZone(ZoneId.systemDefault())
        val currentdate = Date.from(currentzdt.toInstant())
        return futureDate.time - currentdate.time
    }

    //create notification
    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotification(text: String): Notification {
        return Notification.Builder(this, "forget-me-not")
            .setTicker("Forget-Me-Not")
            .setContentTitle("Forget-Me-Not")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
            .setAutoCancel(true)
            .build()
    }
    //delete notification
    /*private fun cancelNotification(context: Context, text: String){
        notificationManager?.cancel(notificationId)
        Log.i("NotificationDelete", notificationId.toString())
    }*/
    //schedule notification
    @RequiresApi(Build.VERSION_CODES.M)
    private fun scheduleNotification(notificationId: Int, notification: Notification, delay: Long) {
        val notificationIntent = Intent(this, MyNotificationPublisher::class.java)
        notificationIntent.putExtra(MyNotificationPublisher().NOTIFICATION_ID, notificationId)
        notificationIntent.putExtra(MyNotificationPublisher().NOTIFICATION, notification)

        notificationManager = MyNotificationPublisher().notificationManager
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
        scheduleNotification(rootId.toInt(), createNotification(title), delay)

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
                scheduleNotification(prevId.toInt(), createNotification(title), delay)

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
                scheduleNotification(prevId.toInt(), createNotification(title), delay)

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
                scheduleNotification(prevId.toInt(), createNotification(title), delay)

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
                //SAMU
                val startDateTime = LocalDateTime.of(startYear, startMonth, startDay, startTimeHour, startTimeMinute)

                val newEvent: EventEntry = EventEntry(title, category, note, startDateTime, frequency = frequency, isRoot = false, event.rootID, event.prevID)

                //SAMU
                val zdt = startDateTime.atZone(ZoneId.systemDefault())
                val startDate = Date.from(zdt.toInstant())
                delay = getMilliseconds(startDate)
                scheduleNotification(eventIdOnEdit.toInt(), createNotification(title), delay)
                dbHelper.updateEvent(newEvent, event.id)
            }
        }

        // NOW: reading out of DB
        val cursor: Cursor = dbHelper.getAllEvents()
        adapter.changeCursor(cursor)
    }

    override fun onAddCategoryDialogPositiveClick(title: String){
/*[SAMU]>: old category version*/
        categories.add(title)
/*[SAMU]<: old category version*/

        /*[HAMO]>: new category version*/
/*        categories.add((CategoryEntry(title, null)))
        dbHelper.addCategory(title, "","")*/
        /*[HAMO]<: new category version*/

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


/*    @RequiresApi(Build.VERSION_CODES.O)
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
    }*/

/*[HAMO]>: new category version*/
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
/*[HAMO]<: new category version*/

}