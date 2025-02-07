package com.dinhlam.sharebox.ui.boxinvited

import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.activity.viewModels
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseViewModelActivity
import com.dinhlam.sharebox.databinding.ActivityBoxInvitedBinding
import com.dinhlam.sharebox.extensions.dp
import com.dinhlam.sharebox.listmodel.LoadingListModel
import com.dinhlam.sharebox.listmodel.TextListModel
import com.dinhlam.sharebox.listmodel.VerticalDividerListModel
import com.dinhlam.sharebox.model.Spacing
import com.dinhlam.sharebox.router.Router
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BoxInvitedActivity :
    BaseViewModelActivity<BoxInvitedState, BoxInvitedViewModel, ActivityBoxInvitedBinding>() {

    @Inject
    lateinit var router: Router

    private val invitedBoxAdapter = BaseListAdapter.create {
        getState(viewModel) { state ->
            if (state.loading) {
                LoadingListModel("loading").attachTo(this)
                return@getState
            }

            state.boxList.forEach { box ->
                TextListModel(
                    "box_${box.boxId}",
                    box.boxName,
                    height = 50.dp(),
                    gravity = Gravity.CENTER_VERTICAL,
                    padding = Spacing.Horizontal(16.dp(), 16.dp()),
                    actionClick = BaseListAdapter.NoHashProp(View.OnClickListener {
                        startActivity(router.boxDetail(this@BoxInvitedActivity, box.boxId, true))
                    })
                ).attachTo(this)
                VerticalDividerListModel("divider_${box.boxId}").attachTo(this)
            }
        }
    }

    override fun onCreateViewBinding(): ActivityBoxInvitedBinding {
        return ActivityBoxInvitedBinding.inflate(layoutInflater)
    }

    override val viewModel: BoxInvitedViewModel by viewModels()

    override fun onStateChanged(state: BoxInvitedState) {
        invitedBoxAdapter.requestBuildListModels()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)

        invitedBoxAdapter.attachTo(binding.recyclerView, this)
    }
}