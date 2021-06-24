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
import java.lang.ClassCastException

class AddCategoryDialogFragment : DialogFragment() {
    internal lateinit var listener: NoticeDialogListener

    interface NoticeDialogListener{
        fun onAddCategoryDialogPositiveClick(category: String)
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
            val view = inflater.inflate(R.layout.dialog_addcategory, null)
            val editText = view.findViewById<EditText>(R.id.category_title)

            builder.setView(view)
                .setPositiveButton(R.string.save,
                    DialogInterface.OnClickListener { dialog, id ->
                        val title: String = editText.text.toString()
                        listener.onAddCategoryDialogPositiveClick(title)
                    })
                .setNegativeButton(R.string.chancel,
                    DialogInterface.OnClickListener { dialog, id ->
                        getDialog()?.cancel()
                    })
            builder.create()
        } ?:throw IllegalStateException("Activity cannot be null")
    }
}