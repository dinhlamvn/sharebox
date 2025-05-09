package com.dinhlam.sharebox.ui.checklist

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.core.text.HtmlCompat
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseListAdapter.NoHashProp
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.base.BaseViewModelActivity
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.data.local.entity.Share
import com.dinhlam.sharebox.databinding.ActivityCheckListBinding
import com.dinhlam.sharebox.extensions.castNonNull
import com.dinhlam.sharebox.extensions.dp
import com.dinhlam.sharebox.extensions.format
import com.dinhlam.sharebox.extensions.getParcelableExtraCompat
import com.dinhlam.sharebox.extensions.getSystemServiceCompat
import com.dinhlam.sharebox.extensions.getTrimmedText
import com.dinhlam.sharebox.extensions.ifTrue
import com.dinhlam.sharebox.extensions.isNotZero
import com.dinhlam.sharebox.extensions.nowUTCTimeInMillis
import com.dinhlam.sharebox.extensions.showToast
import com.dinhlam.sharebox.listmodel.PrimaryButtonListModel
import com.dinhlam.sharebox.listmodel.CheckListListModel
import com.dinhlam.sharebox.listmodel.VerticalDividerListModel
import com.dinhlam.sharebox.model.ShareData
import com.dinhlam.sharebox.model.Spacing
import com.dinhlam.sharebox.receiver.ShareCheckListAlarmBroadcastReceiver
import com.dinhlam.sharebox.router.Router
import com.dinhlam.sharebox.ui.checklist.dialog.CheckListDataFormDialogFragment
import com.dinhlam.sharebox.utils.Ids
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.timepicker.MaterialTimePicker
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import java.util.TimeZone
import javax.inject.Inject

@AndroidEntryPoint
class CheckListActivity :
    BaseViewModelActivity<CheckListState, CheckListViewModel, ActivityCheckListBinding>(),
    CheckListDataFormDialogFragment.OnSaveCheckListListener {

    override val viewModel: CheckListViewModel by viewModels()

    private val chooseBoxLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data ?: return@registerForActivityResult showToast(
                    R.string.require_choose_box
                )
                val boxId =
                    data.getStringExtra(AppExtras.EXTRA_BOX_ID)
                        ?: return@registerForActivityResult showToast(
                            R.string.require_choose_box
                        )
                viewModel.setCurrentBoxId(boxId)
            }
        }

    private val createBoxResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.getStringExtra(AppExtras.EXTRA_BOX_ID)?.let { boxId ->
                    viewModel.setCurrentBoxId(boxId)
                }
            }
        }

    private val checkListAdapter = BaseListAdapter.create {
        getState(viewModel) { state ->
            state.checkListDataList.forEachIndexed { idx, checkListData ->
                CheckListListModel(
                    "checkList_$idx",
                    checkListData.title,
                    checkListData.done,
                    checkListData.datetime,
                    checkListData.reminder,
                    checkListData.updatedAt,
                    NoHashProp(View.OnClickListener {
                        showCheckListDataForm(checkListData)
                    }),
                    NoHashProp(View.OnClickListener {
                        showConfirmDoneCheckList(checkListData)
                    }),
                    NoHashProp(View.OnClickListener {
                        if (checkListData.reminder.isNotZero) {
                            showResetAlertReminder(checkListData)
                        } else {
                            showReminderDateTimePicker(checkListData)
                        }
                    })
                ).attachTo(this)

                VerticalDividerListModel("divider_$idx").attachTo(this)
            }

            PrimaryButtonListModel(
                "button_create_checklist",
                "+",
                margin = Spacing.Only(16.dp(), 16.dp(), 16.dp(), 0),
                onClick = NoHashProp(View.OnClickListener {
                    showCheckListDataForm(null)
                })
            ).attachTo(
                this
            )
        }
    }

    private fun showResetAlertReminder(checkListData: ShareData.ShareCheckList.CheckListData) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.alert_notice)
            .setMessage(
                HtmlCompat.fromHtml(
                    getString(
                        R.string.confirm_reset_reminder,
                        checkListData.reminder.format("dd MMM yyyy, hh:mm a")
                    ), HtmlCompat.FROM_HTML_MODE_COMPACT
                )
            )
            .setPositiveButton(R.string.change) { _, _ ->
                showReminderDateTimePicker(checkListData)
            }
            .setNegativeButton(R.string.delete) { _, _ ->
                viewModel.setCheckListDataReminder(checkListData, 0)
            }.create().show()
    }

    private fun showConfirmDoneCheckList(checkListData: ShareData.ShareCheckList.CheckListData) {
        MaterialAlertDialogBuilder(this)
            .setMessage(R.string.confirm_done_checklist)
            .setPositiveButton(R.string.dialog_ok) { _, _ ->
                viewModel.markTaskDone(checkListData)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showCheckListDataForm(checkListData: ShareData.ShareCheckList.CheckListData?) {
        CheckListDataFormDialogFragment()
            .apply {
                arguments = bundleOf(AppExtras.EXTRA_DATA to checkListData)
                saveCheckListListener = this@CheckListActivity
            }
            .show(
                supportFragmentManager,
                "dialog-check-list-form"
            )
    }

    @Inject
    lateinit var router: Router

    override fun onStateChanged(state: CheckListState) {
        binding.loading.toggle(state.asyncArchive is BaseViewModel.AsyncLoad.Loading)
        checkListAdapter.requestBuildListModels()
    }

    override fun onCreateViewBinding(): ActivityCheckListBinding {
        return ActivityCheckListBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        checkListAdapter.attachTo(binding.recyclerView, this)

        binding.iconDone.setOnClickListener {
            getState(viewModel) { state ->
                if (state.checkListDataList.isEmpty()) {
                    showToast(R.string.nothing_to_save)
                    return@getState
                }
                if (state.currentBox == null) {
                    showToast(R.string.please_choose_box)
                    binding.boxSectionButton.playZoomAnimation()
                    return@getState
                }

                val note = binding.editNote.getTrimmedText()
                viewModel.saveCheckList(note)
            }
        }

        binding.boxSectionButton.setOnClickListener {
            chooseBoxLauncher.launch(router.boxList(this, null))
        }

        binding.boxSectionButton.setOnAddIconClickListener {
            createBoxResultLauncher.launch(router.boxForm(this, null))
        }

        onChange(CheckListState::currentBox) { currentBox ->
            val boxName = currentBox?.boxName
            val isLock = currentBox?.passcode?.isNotBlank() ?: false
            binding.boxSectionButton.setBoxName(boxName)
            binding.boxSectionButton.showLock(isLock)
        }

        onChange(CheckListState::asyncArchive) { asyncLoad ->
            if (asyncLoad is BaseViewModel.AsyncLoad.Success) {
                setupCheckListAlarm(asyncLoad.value)
                showToast(
                    getString(
                        (getState(
                            viewModel,
                            CheckListState::shareId
                        ) != null).ifTrue(R.string.saved, R.string.archive_url_success),
                        asyncLoad.data?.shareNote.orEmpty()
                    )
                )
                finish()
            } else if (asyncLoad is BaseViewModel.AsyncLoad.Failed) {
                showToast(asyncLoad.error.message)
            }
        }

        onChange(CheckListState::shareDetail) { shareDetail ->
            binding.editNote.setText(shareDetail?.shareNote)
        }
    }

    override fun onSaveCheckListData(
        checkListData: ShareData.ShareCheckList.CheckListData,
        params: Bundle
    ) {
        val oldCheckList =
            params.getParcelableExtraCompat<ShareData.ShareCheckList.CheckListData>(AppExtras.EXTRA_DATA)
        viewModel.saveCheckListData(oldCheckList, checkListData)
    }

    private fun showReminderDateTimePicker(checkListData: ShareData.ShareCheckList.CheckListData) {
        val datetime = checkListData.datetime
        val reminder = checkListData.reminder
        var timestamp = reminder.isNotZero.ifTrue(
            reminder,
            datetime.isNotZero.ifTrue(datetime, nowUTCTimeInMillis())
        )
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

        datePicker.addOnPositiveButtonClickListener { pickedDatetime ->
            calendar.timeInMillis = pickedDatetime
            timePicker.addOnDismissListener {
                calendar.set(Calendar.HOUR, timePicker.hour)
                calendar.set(Calendar.MINUTE, timePicker.minute)
                viewModel.setCheckListDataReminder(checkListData, calendar.timeInMillis)
            }
            timePicker.show(supportFragmentManager, "dialog_time_picker")
        }

        datePicker.show(supportFragmentManager, "dialog_date_picker")
    }

    private fun setupCheckListAlarm(share: Share) {
        val shareData = share.shareData.castNonNull<ShareData.ShareCheckList>()
        val alarmManager = getSystemServiceCompat<AlarmManager>(Context.ALARM_SERVICE)
        shareData.checkListDataList.forEachIndexed { idx, checkListData ->
            if (!checkListData.done && checkListData.reminder.isNotZero && checkListData.reminder > nowUTCTimeInMillis()) {
                val idStr = "${share.shareId}-$idx"
                val requestCode = Ids.hashString64Bit(idStr)

                val intent =
                    Intent(applicationContext, ShareCheckListAlarmBroadcastReceiver::class.java)
                        .putExtra(AppExtras.EXTRA_SHARE_ID, share.shareId)
                        .putExtra(AppExtras.EXTRA_POSITION, idx)

                val alarmIntent = PendingIntent.getBroadcast(
                    this,
                    requestCode.toInt(),
                    intent,
                    PendingIntent.FLAG_MUTABLE
                )
                if (alarmIntent != null) {
                    alarmManager.cancel(alarmIntent)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        checkListData.reminder,
                        alarmIntent
                    )
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S_V2) {
                    if (ActivityCompat.checkSelfPermission(
                            this,
                            android.Manifest.permission.SCHEDULE_EXACT_ALARM
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        alarmManager.setExact(
                            AlarmManager.RTC_WAKEUP,
                            checkListData.reminder,
                            alarmIntent
                        )
                    }
                }
            }
        }
    }
}