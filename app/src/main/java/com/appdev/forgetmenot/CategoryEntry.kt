package com.appdev.forgetmenot

import android.graphics.Color

class CategoryEntry(name: String, color: Color?, img: String = "") {

    var name: String
    var color: Color?
    var img: String

    init {
        this.name = name
        this.color = color
        this.img = img
    }
}