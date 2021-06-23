package com.appdev.forgetmenot

import android.icu.number.IntegerWidth
import java.time.DateTimeException
import java.time.LocalDateTime

class MainEntry(title: String, category: String, dateTime: LocalDateTime, isRoot: Int = 0, rootID: Long = 0, prevID: Long = 0) {

    var id: Long
    var title: String
    var note: String
    var category: String
    var dateTime: LocalDateTime
    var isRoot: Int = 0
    var rootID: Long = 0
    var prevID: Long = 0

    init {
        this.id = 0
        this.title = title
        this.note = ""
        this.category = category
        this.dateTime = dateTime
        this.isRoot = isRoot
        this.rootID = rootID
        this.prevID = prevID
    }

/*    override fun toString(): String {
        return this.title + " " + this.category + " " + this.time
    }*/
}