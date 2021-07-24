package com.appdev.forgetmenot

import android.icu.number.IntegerWidth
import android.os.Build
import androidx.annotation.RequiresApi
import java.io.Serializable
import java.time.DateTimeException
import java.time.LocalDateTime

class EventEntry
@RequiresApi(Build.VERSION_CODES.O) constructor(title: String = "", category: String = "", note: String = "", dateTime: LocalDateTime = LocalDateTime.MIN, frequency: String = "", isRoot: Boolean = false, rootID: Long = 0, prevID: Long = 0)
    : Serializable {

    var id: Long = 0
    var title: String
    var note: String
    var category: String
    var dateTime: LocalDateTime
    var frequency: String
    var isRoot: Boolean = false
    var rootID: Long = 0
    var prevID: Long = 0

    init {
        this.id = 0
        this.title = title
        this.note = note
        this.category = category
        this.dateTime = dateTime
        this.frequency = frequency
        this.isRoot = isRoot
        this.rootID = rootID
        this.prevID = prevID
    }

}