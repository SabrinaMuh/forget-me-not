package com.appdev.forgetmenot

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList

/*
 * Forget me not
 * AbbDev 2021
 * @authers: Sabrina Muhrer, Harald Moitzi
 */

class MainActivity : AppCompatActivity(), AddDialogFragment.NoticeDialogListener{
    var alMainEntries: ArrayList<MainEntry> = ArrayList<MainEntry>()
    lateinit var adapter: MainEntryAdapter

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var dateTime = LocalDateTime.of(2021, 6, 15, 8, 0, 0, 0)

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
        }

        val lvMain = findViewById<ListView>(R.id.lvMain)

        adapter = MainEntryAdapter(this, alMainEntries)
        lvMain.adapter = adapter

        val button = findViewById<FloatingActionButton>(R.id.addButton)
        button.setOnClickListener {
            val dialog = AddDialogFragment()
            dialog.show(supportFragmentManager, "add")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDialogPositiveClick(title: String, category: String, startDay: Int,
                                       startMonth: Int, startYear: Int, startTimeHour: Int, startTimeMinute: Int, frequency: String,
                                       endDay: Int, endMonth: Int, endYear: Int, endTimeHour: Int, endTimeMinute: Int
    ) {
        alMainEntries.add(MainEntry(title, category, LocalDateTime.of(startYear, startMonth, startDay, startTimeHour, startTimeMinute)))
        adapter.notifyDataSetChanged()
        Toast.makeText(this, "Entry saved", Toast.LENGTH_SHORT).show()
    }

}