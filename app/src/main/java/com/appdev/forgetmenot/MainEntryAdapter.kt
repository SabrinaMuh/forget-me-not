package com.appdev.forgetmenot

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import java.time.format.DateTimeFormatter

class MainEntryAdapter(context: Context, dataSource:ArrayList<EventEntry>) : BaseAdapter(){

    private val context: Context
    private val layoutInflater: LayoutInflater
    private val dataSource:ArrayList<EventEntry>

    init {
        this.context = context
        this.layoutInflater = LayoutInflater.from(context)
        this.dataSource=dataSource
    }

    companion object {
        private val LABEL_COLORS = hashMapOf(
            "Sport" to R.color.colorSport,
            "Shopping" to R.color.colorShopping,
            "Med" to R.color.colorMed,
            "Important" to R.color.colorImportant,
            "Other" to R.color.colorOther
        )
    }

    override fun getCount(): Int {
        return dataSource.size
    }

    override fun getItem(position: Int): Any {
        return dataSource.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        val view: View?
        val listRowHolder: ListRowHolder
        if (convertView == null) {
            view = this.layoutInflater.inflate(R.layout.list_item_main, parent, false)
            listRowHolder = ListRowHolder(view)
            view.tag = listRowHolder
        } else {
            view = convertView
            listRowHolder = view.tag as ListRowHolder
        }

        listRowHolder.tvTitle.text = dataSource.get(position).title
        listRowHolder.tvCategory.text = dataSource.get(position).category
/*        listRowHolder.tvTime.text = dataSource.get(position).dateTime.toString()*/
        dataSource.get(position).dateTime.format(DateTimeFormatter.ofPattern("M/d/y HH:mm:ss"))
            .also { listRowHolder.tvTime.text = it }
        listRowHolder.tvEdit.text = "Edit"

        //formatting
        val font1 = ResourcesCompat.getFont(context, R.font.josefinsans_bold)
        val font2 = ResourcesCompat.getFont(context, R.font.josefinsans_semibolditalic)
        val font3 = ResourcesCompat.getFont(context, R.font.quicksand_bold)

        listRowHolder.tvTitle.typeface = font1
        listRowHolder.tvCategory.typeface = font2
        listRowHolder.tvTime.typeface = font3
        listRowHolder.tvEdit.typeface = font3

        listRowHolder.tvCategory.setTextColor(
            ContextCompat.getColor(context, LABEL_COLORS[dataSource.get(position).category] ?: R.color.colorPrimary)
        )

        listRowHolder.imgCat.setBackgroundColor(
            ContextCompat.getColor(context, LABEL_COLORS[dataSource.get(position).category] ?: R.color.colorPrimary)
        )

        return view
    }
}

private class ListRowHolder(row: View?) {
    public val imgCat: ImageView
    public val tvTitle: TextView
    public val tvCategory: TextView
    public val tvTime: TextView
    public val tvEdit: TextView

    public val relativeLayout: RelativeLayout

    init {
        this.imgCat = row?.findViewById<ImageView>(R.id.main_list_img) as ImageView
        this.tvTitle = row?.findViewById<TextView>(R.id.main_list_title) as TextView
        this.tvCategory = row?.findViewById<TextView>(R.id.main_list_cat) as TextView
        this.tvTime = row?.findViewById<TextView>(R.id.main_list_time) as TextView
        this.tvEdit = row?.findViewById<TextView>(R.id.main_list_edit) as TextView

        this.relativeLayout = row?.findViewById<RelativeLayout>(R.id.main_list_row_entry) as RelativeLayout
    }
}