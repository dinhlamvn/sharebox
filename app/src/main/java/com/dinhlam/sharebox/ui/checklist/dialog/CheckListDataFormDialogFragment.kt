package com.dinhlam.sharebox.ui.checklist.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseDialogFragment
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.databinding.DialogFragmentCheckListDataFormBinding
import com.dinhlam.sharebox.extensions.format
import com.dinhlam.sharebox.extensions.getParcelableExtraCompat
import com.dinhlam.sharebox.extensions.ifNotZero
import com.dinhlam.sharebox.extensions.ifTrue
import com.dinhlam.sharebox.extensions.nowUTCTimeInMillis
import com.dinhlam.sharebox.extensions.showToast
import com.dinhlam.sharebox.extensions.trimmedString
import com.dinhlam.sharebox.model.ShareData
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import java.util.Calendar
import java.util.TimeZone

class CheckListDataFormDialogFragment :
    BaseDialogFragment<DialogFragmentCheckListDataFormBinding>() {

    interface OnSaveCheckListListener {
        fun onSaveCheckListData(
            checkListData: ShareData.ShareCheckList.CheckListData,
            params: Bundle
        )
    }

    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogFragmentCheckListDataFormBinding {
        return DialogFragmentCheckListDataFormBinding.inflate(inflater, container, false)
    }

    override val isUseMaterialDialog: Boolean
        get() = false

    private val checkListData by lazy {
        arguments?.getParcelableExtraCompat<ShareData.ShareCheckList.CheckListData>(AppExtras.EXTRA_DATA)
    }

    var saveCheckListListener: OnSaveCheckListListener? = null

    private var checkListDateTime: Long = 0L
        set(value) {
            field = value
            binding.textDatetime.text =
                value.ifNotZero.ifTrue(value.format("dd MMM yyyy, hh:mm a"), "-")
            binding.iconClearDatetime.isVisible = value.ifNotZero
        }

    private var checkListReminder: Long = 0L
        set(value) {
            field = value
            binding.textReminder.text =
                value.ifNotZero.ifTrue(value.format("dd MMM yyyy, hh:mm a"), "-")
            binding.iconClearReminder.isVisible = value.ifNotZero
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.editTitle.setText(checkListData?.title)

        checkListDateTime = checkListData?.datetime ?: 0
        checkListReminder = checkListData?.reminder ?: 0

        binding.textDatetime.setOnClickListener {
            showCheckListDateTimePicker()
        }

        binding.textReminder.setOnClickListener {
            showCheckListReminderPicker()
        }

        binding.iconClearDatetime.setOnClickListener {
            checkListDateTime = 0L
        }

        binding.iconClearReminder.setOnClickListener {
            checkListReminder = 0L
        }

        binding.buttonSave.setOnClickListener {
            onSave()
        }
    }

    private fun showCheckListDateTimePicker() {
        var timestamp = checkListDateTime.ifNotZero.ifTrue(checkListDateTime, nowUTCTimeInMillis())
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
        }

        val timezone = TimeZone.getDefault()
        timestamp += timezone.getOffset(timestamp)
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Date")
            .setSelection(timestamp)
            .build()

        val timePicker = MaterialTimePicker.Builder()
            .setTitleText("Select Time")
            .setHour(calendar.get(Calendar.HOUR_OF_DAY))
            .setMinute(calendar.get(Calendar.MINUTE))
            .build()

        datePicker.addOnPositiveButtonClickListener { datetime ->
            calendar.timeInMillis = datetime
            timePicker.addOnDismissListener {
                calendar.set(Calendar.HOUR, timePicker.hour)
                calendar.set(Calendar.MINUTE, timePicker.minute)
                checkListDateTime = calendar.timeInMillis
            }
            timePicker.show(childFragmentManager, "dialog_time_picker")
            checkListDateTime = calendar.timeInMillis
        }

        datePicker.show(childFragmentManager, "dialog_date_picker")
    }

    private fun showCheckListReminderPicker() {
        var timestamp = checkListReminder.ifNotZero.ifTrue(checkListReminder, nowUTCTimeInMillis())
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
        }

        val timezone = TimeZone.getDefault()
        timestamp += timezone.getOffset(timestamp)
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Date")
            .setSelection(timestamp)
            .build()

        val timePicker = MaterialTimePicker.Builder()
            .setTitleText("Select Time")
            .setHour(calendar.get(Calendar.HOUR_OF_DAY))
            .setMinute(calendar.get(Calendar.MINUTE))
            .build()

        datePicker.addOnPositiveButtonClickListener { datetime ->
            calendar.timeInMillis = datetime
            timePicker.addOnDismissListener {
                calendar.set(Calendar.HOUR, timePicker.hour)
                calendar.set(Calendar.MINUTE, timePicker.minute)
                checkListReminder = calendar.timeInMillis
            }
            timePicker.show(childFragmentManager, "dialog_time_picker")
            checkListReminder = calendar.timeInMillis
        }

        datePicker.show(childFragmentManager, "dialog_date_picker")
    }

    private fun onSave() {
        val title = binding.editTitle.text?.trimmedString().orEmpty()
        if (title.isEmpty()) {
            showToast(R.string.require_check_list_title)
            return
        }
        val data = ShareData.ShareCheckList.CheckListData(
            title,
            checkListData?.done ?: false,
            checkListDateTime,
            checkListReminder,
            nowUTCTimeInMillis()
        )
        val bundle = arguments ?: bundleOf()
        saveCheckListListener?.onSaveCheckListData(data, bundle)
        dismiss()
    }
}