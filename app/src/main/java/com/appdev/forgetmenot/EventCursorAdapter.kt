package com.appdev.forgetmenot

import android.content.Context
import android.database.Cursor
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CursorAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class EventCursorAdapter(context: Context, cursor: Cursor): CursorAdapter(context, cursor, 0) {
    companion object {
        private val CATEGORY_COLORS = hashMapOf(
            "Sport" to R.color.colorSport,
            "Shopping" to R.color.colorShopping,
            "Medical" to R.color.colorMedical,
            "Important" to R.color.colorImportant,
            "Other" to R.color.colorOther
        )

        private val CATEGORY_LOGOS = hashMapOf(
            "Sport" to R.drawable.ic_launcher_foreground,
            "Shopping" to R.drawable.ic_launcher_foreground,
            "Medical" to R.drawable.logo_medical,
            "Important" to R.drawable.ic_launcher_foreground,
            "Other" to R.drawable.ic_launcher_foreground
        )
    }

/*    companion object {
        private val CATEGORY_LOGOS = hashMapOf(
            "Sport" to R.color.colorSport,
            "Shopping" to R.color.colorShopping,
            "Med" to R.drawable.logo_medical,
            "Important" to R.color.colorImportant,
            "Other" to R.color.colorOther
        )
    }*/

    // The newView method is used to inflate a new view and return it,
    // you don't bind any data to the view at this point.
    override fun newView(context: Context, cursor: Cursor, parent: ViewGroup?): View {
        return LayoutInflater.from(context).inflate(R.layout.list_item_main, parent, false)
    }

    // The bindView method is used to bind all data to a given view
    // such as setting the text on a TextView.
    @RequiresApi(Build.VERSION_CODES.O)
    override fun bindView(view: View, context: Context, cursor: Cursor) {
        // Find fields to populate in inflated template
        val imgCat = view.findViewById<ImageView>(R.id.main_list_img) as ImageView
        val tvTitle = view.findViewById<TextView>(R.id.main_list_title) as TextView
        val tvCategory = view.findViewById<TextView>(R.id.main_list_cat) as TextView
        val tvTime = view.findViewById<TextView>(R.id.main_list_time) as TextView
        val tvEdit = view.findViewById<TextView>(R.id.main_list_edit) as TextView

        // Extract properties from cursor
        val id = cursor.getLong(cursor.getColumnIndex("_id"))
        val title = cursor.getString(cursor.getColumnIndex(DBHelper.EventObject.Entry.COLUMN_NAME_TITLE))
        val note = cursor.getString(cursor.getColumnIndex(DBHelper.EventObject.Entry.COLUMN_NAME_NOTE))
        val category = cursor.getString(cursor.getColumnIndex(DBHelper.EventObject.Entry.COLUMN_NAME_CATEGORY))
        val datetime = cursor.getString(cursor.getColumnIndex(DBHelper.EventObject.Entry.COLUMN_NAME_DATETIME))
        val isRoot = cursor.getInt(cursor.getColumnIndex(DBHelper.EventObject.Entry.COLUMN_NAME_IS_ROOT))
        val rootID = cursor.getLong(cursor.getColumnIndex(DBHelper.EventObject.Entry.COLUMN_NAME_ROOT_ID))
        val prevID = cursor.getLong(cursor.getColumnIndex(DBHelper.EventObject.Entry.COLUMN_NAME_PREV_ID))

        // Populate fields with extracted properties

        // set logo of category
        imgCat.setImageDrawable(
            ContextCompat.getDrawable(context, CATEGORY_LOGOS[category] ?: R.color.colorPrimary)
        )

        tvTitle.text = title
        tvCategory.text = category

        // CAST DOES NOT WORK --> EXCEPTION
        // java.time.format.DateTimeParseException: Text '2021-06-15T11:00' could not be parsed at index 10

        /*val formatter: DateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME*/

        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
        val tmpDatetime: LocalDateTime = LocalDateTime.parse(datetime, formatter)
        tvTime.text = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").format(tmpDatetime)


/*        tvTime.text = datetime.replace('T', ' ')*/


        tvEdit.text = "Edit"

        //formatting
        val font1 = ResourcesCompat.getFont(context, R.font.josefinsans_bold)
        val font2 = ResourcesCompat.getFont(context, R.font.josefinsans_semibolditalic)
        val font3 = ResourcesCompat.getFont(context, R.font.quicksand_bold)

        tvTitle.typeface = font1
        tvCategory.typeface = font2
        tvTime.typeface = font3
        tvEdit.typeface = font3

        tvCategory.setTextColor(
            ContextCompat.getColor(context, CATEGORY_COLORS[category] ?: R.color.colorPrimary)
        )

        imgCat.setBackgroundColor(
            ContextCompat.getColor(context, CATEGORY_COLORS[category] ?: R.color.colorPrimary)
        )

    }
}