package com.appdev.forgetmenot

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import java.util.*


class AddEnteryDialogFragment : DialogFragment(){
    internal lateinit var listener: NoticeDialogListener

    interface NoticeDialogListener{
        fun onAddEnteryDialogPositiveClick(title: String, category: String, startDay: Int, startMonth: Int,
                                  startYear: Int, startTimeHour: Int, startTimeMinute: Int,
                                  frequency: String, endDay: Int, endMonth: Int,
                                  endYear: Int, endTimeHour: Int, endTimeMinute: Int)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as NoticeDialogListener
        } catch (e: ClassCastException){
            throw ClassCastException(context.toString() + " must implement NoticeDialogListener")
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.dialog_addentery, null)
            val editText = view.findViewById<EditText>(R.id.memory_title)
            val spinner = view.findViewById<Spinner>(R.id.category)
            val datePickerStart = view.findViewById<DatePicker>(R.id.date_picker_start)
            val timePickerStart = view.findViewById<TimePicker>(R.id.time_picker_start)
            val datePickerEnd = view.findViewById<DatePicker>(R.id.date_picker_end)
            val radioGroup = view.findViewById<RadioGroup>(R.id.radio_group)
            val timePickerEnd = view.findViewById<TimePicker>(R.id.time_picker_end)

            val args: Bundle = requireArguments()
            //[SAMU]
/*            val categories: ArrayList <String> = args.getStringArrayList("categories") as ArrayList<String>
            val adapter: ArrayAdapter<String> = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)*/

            //[HAMO]>
            var event: EventEntry? = null
            if(args.containsKey("event")) {
                event = args.getSerializable("event") as EventEntry
            }
            val myCategories: ArrayList <CategoryEntry> = args.getSerializable("categories") as ArrayList<CategoryEntry>
            val categories: ArrayList <String> = ArrayList <String>()

            for(item: CategoryEntry in myCategories) {
                categories.add(item.name)
            }

            val adapter: ArrayAdapter<String> = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
            //[HAMO]<
            spinner.adapter = adapter
            timePickerStart.setIs24HourView(true)
            timePickerEnd.setIs24HourView(true)

            builder.setView(view)
                .setPositiveButton(R.string.save,
                    DialogInterface.OnClickListener { dialog, id ->
                        val title: String = editText.text.toString()
                        val category: String = spinner.selectedItem.toString()
                        val selectedRadioButton: Int = radioGroup.checkedRadioButtonId
                        val radioButton = view.findViewById<RadioButton>(selectedRadioButton)
                        val startDateDay: Int = datePickerStart.dayOfMonth
                        val startDateMonth: Int = datePickerStart.month + 1
                        val startDateYear: Int = datePickerStart.year
                        val startTimeHour: Int = timePickerStart.hour
                        val startTimeMinute: Int = timePickerStart.minute
                        var frequency: String = "null"
                        if(radioButton != null){
                            val mySelection = view.findViewById(R.id.radio_group) as RadioGroup
                            val radioButtonId = mySelection.checkedRadioButtonId
                            when (radioButtonId) {
                                R.id.daily -> frequency = "daily"
                                R.id.weekly -> frequency = "weekly"
                                R.id.monthly -> frequency = "monthly"
                            }
                        }
                        val endDateDay: Int = datePickerEnd.dayOfMonth
                        val endDateMonth: Int = datePickerEnd.month + 1
                        val endDateYear: Int = datePickerEnd.year
                        val endTimeHour: Int = timePickerEnd.hour
                        val endTimeMinute: Int = timePickerEnd.minute

                        listener.onAddEnteryDialogPositiveClick(title, category, startDateDay, startDateMonth, startDateYear, startTimeHour, startTimeMinute, frequency, endDateDay, endDateMonth, endDateYear, endTimeHour, endTimeMinute)
                    })
                .setNegativeButton(R.string.chancel,
                    DialogInterface.OnClickListener { dialog, id ->
                        getDialog()?.cancel()
                    })
            builder.create()
        } ?:throw IllegalStateException("Activity cannot be null")
    }
}