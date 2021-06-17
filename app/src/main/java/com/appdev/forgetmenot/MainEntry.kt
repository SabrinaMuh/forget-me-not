package com.appdev.forgetmenot

import java.time.DateTimeException
import java.time.LocalDateTime

class MainEntry(title: String, category: String, dateTime: LocalDateTime) {

    var title: String
    var category: String
    var dateTime: LocalDateTime

    init {
        this.title = title
        this.category = category
        this.dateTime = dateTime
    }

/*    override fun toString(): String {
        return this.title + " " + this.category + " " + this.time
    }*/
}