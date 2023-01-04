package com.dinhlam.sharebox.ui.setting

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseActivity
import com.dinhlam.sharebox.databinding.ActivitySettingBinding
import com.dinhlam.sharebox.extensions.registerOnBackPressHandler
import com.dinhlam.sharebox.model.SortType
import com.dinhlam.sharebox.pref.AppSharePref
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingActivity : BaseActivity<ActivitySettingBinding>() {

    @Inject
    lateinit var appSharePref: AppSharePref

    override fun onCreateViewBinding(): ActivitySettingBinding {
        return ActivitySettingBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        registerOnBackPressHandler {
            finish()
        }

        when (appSharePref.getSortType()) {
            SortType.NEWEST -> viewBinding.radioButtonSortByNewest.isChecked = true
            SortType.OLDEST -> viewBinding.radioButtonSortByOldest.isChecked = true
            else -> viewBinding.radioButtonNoSort.isChecked = true
        }

        viewBinding.radioButtonGroup.setOnCheckedChangeListener { _, buttonId ->
            val sortType = when (buttonId) {
                R.id.radio_button_sort_by_newest -> SortType.NEWEST
                R.id.radio_button_sort_by_oldest -> SortType.OLDEST
                else -> SortType.NONE
            }
            appSharePref.setSortType(sortType)
            setResult(Activity.RESULT_OK, Intent().apply {
                putExtra("sort-type", sortType)
            })
        }
    }
}
