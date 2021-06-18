package com.appdev.forgetmenot

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.lang.IllegalStateException

class AddDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val layout = inflater.inflate(R.layout.dialog_add, null)
            val timePickerStart = layout.findViewById<TimePicker>(R.id.time_picker_start)
            timePickerStart.setIs24HourView(true)
            val timePickerEnd = layout.findViewById<TimePicker>(R.id.time_picker_end)
            timePickerEnd.setIs24HourView(true)
            builder.setView(layout)
                .setPositiveButton(R.string.save,
                    DialogInterface.OnClickListener { dialog, id ->

                    })
                .setNegativeButton(R.string.chancel,
                    DialogInterface.OnClickListener { dialog, id ->
                        getDialog()?.cancel()
                    })
            builder.create()
        } ?:throw IllegalStateException("Activity cannot be null")
    }

}