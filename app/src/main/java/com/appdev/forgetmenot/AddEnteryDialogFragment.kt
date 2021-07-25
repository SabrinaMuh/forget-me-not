package com.appdev.forgetmenot

import android.app.AlertDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import android.widget.TimePicker.OnTimeChangedListener
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import java.util.*


class AddEnteryDialogFragment : DialogFragment(){
    internal lateinit var listener: NoticeDialogListener

    private val TIME_PICKER_INTERVAL = 15
    private var mIgnoreEvent = false

    private val mTimePickerListener =
        OnTimeChangedListener { timePicker, hourOfDay, minute ->
            var minute = minute
            if (mIgnoreEvent) return@OnTimeChangedListener
            if (minute % TIME_PICKER_INTERVAL !== 0) {
                val minuteFloor: Int = minute - minute % TIME_PICKER_INTERVAL
                minute = minuteFloor + if (minute == minuteFloor + 1) TIME_PICKER_INTERVAL else 0
                if (minute == 60) minute = 0
                mIgnoreEvent = true
                timePicker.currentMinute = minute
                mIgnoreEvent = false
            }
        }

    interface NoticeDialogListener{
        fun onAddEnteryDialogPositiveClick(evendIdOnEdit: Long, title: String, category: String, note: String, startDay: Int, startMonth: Int,
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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.dialog_addentery, null)
            val tvEventIdOnEdit = view.findViewById<TextView>(R.id.event_id_on_edit)
            val editText = view.findViewById<EditText>(R.id.memory_title)
            val note = view.findViewById<EditText>(R.id.note)
            val spinner = view.findViewById<Spinner>(R.id.category)
            val datePickerStart = view.findViewById<DatePicker>(R.id.date_picker_start)
            val timePickerStart = view.findViewById<TimePicker>(R.id.time_picker_start)
            val datePickerEnd = view.findViewById<DatePicker>(R.id.date_picker_end)
            val radioGroup = view.findViewById<RadioGroup>(R.id.radio_group)
            val timePickerEnd = view.findViewById<TimePicker>(R.id.time_picker_end)
            val tvWarning = view.findViewById<TextView>(R.id.textViewWarning)

            val args: Bundle = requireArguments()

            val categories: ArrayList <String> = args.getStringArrayList("categories") as ArrayList<String>
            val adapter: ArrayAdapter<String> = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)

            var event: EventEntry? = null
            if(args.containsKey("event")) {
                event = args.getSerializable("event") as EventEntry
            }

            spinner.adapter = adapter
            timePickerStart.setIs24HourView(true)
            timePickerEnd.setIs24HourView(true)

            timePickerStart.minute = timePickerStart.minute - (timePickerStart.minute % TIME_PICKER_INTERVAL)
            timePickerEnd.minute = timePickerEnd.minute - (timePickerEnd.minute % TIME_PICKER_INTERVAL)

            timePickerStart.setOnTimeChangedListener(mTimePickerListener)
            timePickerEnd.setOnTimeChangedListener(mTimePickerListener)

            //EDIT --> fill fields of selected Event
            if(event != null) {
                tvEventIdOnEdit.setText(event?.id.toString()) // hidden field
                editText.setText(event?.title)
                note.setText(event?.note)
                spinner.setSelection(adapter.getPosition(event?.category))

                when (event?.frequency) {
                    "daily" -> radioGroup.check(R.id.daily)
                    "weekly" -> radioGroup.check(R.id.weekly)
                    "monthly" -> radioGroup.check(R.id.monthly)
                }

                datePickerStart.init(event?.dateTime?.year!!, event?.dateTime.monthValue-1, event?.dateTime.dayOfMonth, null)
                timePickerStart.hour = event?.dateTime.hour
                timePickerStart.minute = event?.dateTime.minute

                val dbHelper = DBHelper(view.context)

                var lastEvent: EventEntry? = null

                if(event.isRoot) {
                    lastEvent = dbHelper.getLastEventOfSeries(event.rootID)
                }
                else {
                    lastEvent = event
                }

                datePickerEnd.init(lastEvent?.dateTime?.year!!, lastEvent?.dateTime.monthValue-1, lastEvent?.dateTime.dayOfMonth, null)
                timePickerEnd.hour = lastEvent?.dateTime.hour
                timePickerEnd.minute = lastEvent?.dateTime.minute
            }


            builder.setView(view)
            val buttonSave = view.findViewById<Button>(R.id.buttonSave)
            buttonSave.setOnClickListener {
                val eventIdOnEdit: Long = tvEventIdOnEdit.text.toString().toLong()
                val title: String = editText.text.toString()
                val category: String = spinner.selectedItem.toString()
                val note: String = note.text.toString()
                val selectedRadioButton: Int = radioGroup.checkedRadioButtonId
                val radioButton = view.findViewById<RadioButton>(selectedRadioButton)

                val startDateDay: Int = datePickerStart.dayOfMonth
                val startDateMonth: Int = datePickerStart.month;

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
                val endDateMonth: Int = datePickerEnd.month

                val endDateYear: Int = datePickerEnd.year
                val endTimeHour: Int = timePickerEnd.hour
                val endTimeMinute: Int = timePickerEnd.minute

                if (title != "") {
                    listener.onAddEnteryDialogPositiveClick(eventIdOnEdit, title, category, note, startDateDay, startDateMonth, startDateYear, startTimeHour, startTimeMinute, frequency, endDateDay, endDateMonth, endDateYear, endTimeHour, endTimeMinute)
                    dismiss()
                }else tvWarning.visibility = View.VISIBLE
            }

            val buttonCancel = view.findViewById<Button>(R.id.buttonCancel)
            buttonCancel.setOnClickListener {
                dismiss()
            }
            builder.create()
        } ?:throw IllegalStateException("Activity cannot be null")
    }
}