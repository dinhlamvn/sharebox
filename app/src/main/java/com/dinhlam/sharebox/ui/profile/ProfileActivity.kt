package com.dinhlam.sharebox.ui.profile

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseViewModelActivity
import com.dinhlam.sharebox.databinding.ActivityProfileBinding
import com.dinhlam.sharebox.extensions.dp
import com.dinhlam.sharebox.extensions.heightPercentage
import com.dinhlam.sharebox.extensions.screenWidth
import com.dinhlam.sharebox.helper.ShareHelper
import com.dinhlam.sharebox.helper.UserHelper
import com.dinhlam.sharebox.listmodel.ButtonListModel
import com.dinhlam.sharebox.listmodel.DrawableImageListModel
import com.dinhlam.sharebox.listmodel.LoadingListModel
import com.dinhlam.sharebox.listmodel.SizedBoxListModel
import com.dinhlam.sharebox.listmodel.TextListModel
import com.dinhlam.sharebox.listmodel.profile.ProfileInfoListModel
import com.dinhlam.sharebox.model.Spacing
import com.dinhlam.sharebox.pref.AppSharePref
import com.dinhlam.sharebox.router.Router
import com.dinhlam.sharebox.utils.Icons
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProfileActivity :
    BaseViewModelActivity<ProfileState, ProfileViewModel, ActivityProfileBinding>() {

    override fun onCreateViewBinding(): ActivityProfileBinding {
        return ActivityProfileBinding.inflate(layoutInflater)
    }

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(), ::handleSignInResult
    )

    private val adapter = BaseListAdapter.createAdapter {
        getState(viewModel) { state ->
            if (state.isRefreshing) {
                LoadingListModel("loading").attachTo(this)
                return@getState
            }

            val nonNullUser = state.currentUser ?: return@getState run {
                TextListModel(
                    "text_sign_in_message",
                    getString(R.string.sign_in_message),
                    height = heightPercentage(70)
                ).attachTo(this)

                ButtonListModel(
                    "button_sign_in",
                    getString(R.string.sign_in),
                    Spacing.Only(16.dp(), 16.dp(), 16.dp(), 16.dp()),
                    BaseListAdapter.NoHashProp(View.OnClickListener {
                        signInLauncher.launch(router.signIn(true))
                    })
                ).attachTo(this)

                val margin = screenWidth().minus(48.dp()).div(2)

                DrawableImageListModel(
                    Icons.settingIcon(this@ProfileActivity),
                    width = 48.dp(),
                    height = 48.dp(),
                    scaleType = ImageView.ScaleType.CENTER_INSIDE,
                    actionClick = BaseListAdapter.NoHashProp {
                        openSettingPage()
                    },
                    margin = Spacing.Horizontal(margin, margin)
                ).attachTo(this)
            }

            ProfileInfoListModel(
                nonNullUser.id,
                nonNullUser.avatar,
                nonNullUser.name,
                nonNullUser.drama,
                nonNullUser.level,
                nonNullUser.joinDate,
                Icons.dramaIcon(this@ProfileActivity),
                Icons.levelIcon(this@ProfileActivity),
                BaseListAdapter.NoHashProp(View.OnClickListener {
                    openSettingPage()
                })
            ).attachTo(this)

            SizedBoxListModel(
                "divider_profile", height = 1.dp()
            ).attachTo(this)

            TextListModel(
                "title_bookmark_collection",
                getString(R.string.title_bookmark_collection),
                textAppearance = R.style.TextBodyMedium,
                height = 50.dp(),
                gravity = Gravity.START.or(Gravity.CENTER_VERTICAL),
                actionClick = BaseListAdapter.NoHashProp(View.OnClickListener {
                    startActivity(router.bookmark(this@ProfileActivity))
                })
            ).attachTo(this)

            SizedBoxListModel(
                "divider_bookmark_collection", height = 1.dp()
            ).attachTo(this)
        }
    }

    @Inject
    lateinit var shareHelper: ShareHelper

    @Inject
    lateinit var appSharePref: AppSharePref

    @Inject
    lateinit var userHelper: UserHelper

    @Inject
    lateinit var router: Router

    override val viewModel: ProfileViewModel by viewModels()

    override fun onStateChanged(state: ProfileState) {
        adapter.requestBuildModelViews()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.recyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.adapter = adapter

        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.swipeRefreshLayout.isRefreshing = false
            viewModel.getCurrentUserProfile()
        }
    }

    private fun openSettingPage() {
        startActivity(router.settingIntent())
    }

    private fun handleSignInResult(activityResult: ActivityResult?) {
        if (activityResult?.resultCode == Activity.RESULT_OK) {
            viewModel.getCurrentUserProfile()
        }
    }
}


