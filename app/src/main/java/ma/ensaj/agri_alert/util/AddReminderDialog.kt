package ma.ensaj.agri_alert.util

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import ma.ensaj.agri_alert.R
import ma.ensaj.agri_alert.view.fragments.CropAnalysisFragment
import java.text.SimpleDateFormat
import java.util.*

class AddReminderDialog(private val fragmentManager: FragmentManager) : DialogFragment() {
    private lateinit var titleEditText: EditText
    private lateinit var messageEditText: EditText
    private lateinit var dateButton: Button
    private lateinit var timeButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_add_reminder, container, false)

        titleEditText = view.findViewById(R.id.et_title)
        messageEditText = view.findViewById(R.id.et_message)
        dateButton = view.findViewById(R.id.btn_date)
        timeButton = view.findViewById(R.id.btn_time)

        setUpDatePicker()
        setUpTimePicker()

        view.findViewById<Button>(R.id.btn_save).setOnClickListener {
            val title = titleEditText.text.toString()
            val message = messageEditText.text.toString()
            val selectedDateTime = getSelectedDateTime()
            val todayDateTime = Calendar.getInstance()
            val delayInSeconds = (selectedDateTime.timeInMillis / 1000L) - (todayDateTime.timeInMillis / 1000L)

            (parentFragment as CropAnalysisFragment).createWorkRequest(title, message, delayInSeconds)
            dismiss()
        }

        return view
    }

    private fun getSelectedDateTime(): Calendar {
        val calendar = Calendar.getInstance()
        calendar.set(
            dateButton.text.split("-")[0].toInt(),
            dateButton.text.split("-")[1].toInt() - 1,
            dateButton.text.split("-")[2].toInt(),
            timeButton.text.split(":")[0].toInt(),
            timeButton.text.split(":")[1].toInt()
        )
        return calendar
    }

    private fun setUpDatePicker() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                dateButton.text = dateFormat.format(calendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        dateButton.setOnClickListener {
            datePickerDialog.show()
        }
    }

    private fun setUpTimePicker() {
        val calendar = Calendar.getInstance()
        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                timeButton.text = timeFormat.format(calendar.time)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )

        timeButton.setOnClickListener {
            timePickerDialog.show()
        }
    }
}