package com.dinhlam.sharebox.ui.profile

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseViewModelFragment
import com.dinhlam.sharebox.databinding.FragmentProfileBinding
import com.dinhlam.sharebox.extensions.dp
import com.dinhlam.sharebox.extensions.heightPercentage
import com.dinhlam.sharebox.extensions.screenWidth
import com.dinhlam.sharebox.helper.ShareHelper
import com.dinhlam.sharebox.helper.UserHelper
import com.dinhlam.sharebox.listmodel.ButtonListModel
import com.dinhlam.sharebox.listmodel.DrawableImageListModel
import com.dinhlam.sharebox.listmodel.TextListModel
import com.dinhlam.sharebox.listmodel.VerticalDividerListModel
import com.dinhlam.sharebox.listmodel.profile.ProfileInfoListModel
import com.dinhlam.sharebox.model.Spacing
import com.dinhlam.sharebox.pref.AppSharePref
import com.dinhlam.sharebox.router.Router
import com.dinhlam.sharebox.utils.Icons
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment :
    BaseViewModelFragment<ProfileState, ProfileViewModel, FragmentProfileBinding>() {

    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentProfileBinding {
        return FragmentProfileBinding.inflate(layoutInflater)
    }

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(), ::handleSignInResult
    )

    private val adapter = BaseListAdapter.create {
        getState(viewModel) { state ->
            val nonNullUser = state.currentUser
            if (nonNullUser == null) {
                TextListModel(
                    "text_sign_in_message",
                    getString(R.string.sign_in_message),
                    height = heightPercentage(70)
                ).attachTo(this)

                ButtonListModel(
                    "button_sign_in",
                    getString(R.string.sign_in),
                    Spacing.All(16.dp()),
                    BaseListAdapter.NoHashProp(View.OnClickListener {
                        signInLauncher.launch(router.signIn(true))
                    })
                ).attachTo(this)

                val margin = screenWidth().minus(48.dp()).div(2)

                DrawableImageListModel(
                    Icons.settingIcon(requireContext()),
                    width = 48.dp(),
                    height = 48.dp(),
                    scaleType = ImageView.ScaleType.CENTER_INSIDE,
                    actionClick = BaseListAdapter.NoHashProp {
                        openSettingPage()
                    },
                    margin = Spacing.Horizontal(margin, margin)
                ).attachTo(this)

                return@getState
            }

            ProfileInfoListModel(
                nonNullUser.id,
                nonNullUser.avatar,
                nonNullUser.name,
                state.shareCount,
                getLevel(state.shareCount),
                nonNullUser.joinDate,
                Icons.shareIcon(requireContext()),
                Icons.levelIcon(requireContext()),
                BaseListAdapter.NoHashProp(View.OnClickListener {
                    openSettingPage()
                })
            ).attachTo(this)

            VerticalDividerListModel(
                "divider_profile", height = 1.dp()
            ).attachTo(this)

            TextListModel(
                "title_bookmark_collection",
                getString(R.string.title_bookmark_collection),
                textAppearance = R.style.TextBodyMedium,
                height = 50.dp(),
                gravity = Gravity.START.or(Gravity.CENTER_VERTICAL),
                startIcon = Icons.bookmarkIcon(requireContext()),
                actionClick = BaseListAdapter.NoHashProp(View.OnClickListener {
                    startActivity(router.bookmark(requireContext()))
                })
            ).attachTo(this)

            VerticalDividerListModel(
                "divider_bookmark_collection", height = 1.dp()
            ).attachTo(this)

            TextListModel(
                "title_trash",
                getString(R.string.title_trash),
                textAppearance = R.style.TextBodyMedium,
                height = 50.dp(),
                gravity = Gravity.START.or(Gravity.CENTER_VERTICAL),
                startIcon = Icons.trashIcon(requireContext()),
                actionClick = BaseListAdapter.NoHashProp(View.OnClickListener {
                    startActivity(router.trash(requireContext()))
                })
            ).attachTo(this)

            VerticalDividerListModel(
                "divider_trash", height = 1.dp()
            ).attachTo(this)

            if (userHelper.isSignedIn()) {
                TextListModel(
                    "title_box_invited",
                    getString(R.string.title_box_invited),
                    textAppearance = R.style.TextBodyMedium,
                    height = 50.dp(),
                    gravity = Gravity.START.or(Gravity.CENTER_VERTICAL),
                    startIcon = Icons.linkIcon(requireContext()),
                    actionClick = BaseListAdapter.NoHashProp(View.OnClickListener {
                        startActivity(router.boxInvited(requireContext()))
                    })
                ).attachTo(this)

                VerticalDividerListModel(
                    "divider_box_invited", height = 1.dp()
                ).attachTo(this)
            }
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
        adapter.requestBuildListModels()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter.attachTo(binding.recyclerView, this)

        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.swipeRefreshLayout.isRefreshing = false
            viewModel.getCurrentUserProfile()
        }
    }

    private fun openSettingPage() {
        startActivity(router.setting())
    }

    private fun handleSignInResult(activityResult: ActivityResult?) {
        if (activityResult?.resultCode == Activity.RESULT_OK) {
            viewModel.getCurrentUserProfile()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.getCurrentUserProfile()
    }

    private fun getLevel(shareCount: Int): Int {
        return when (shareCount) {
            in 0..1000 -> 0
            in 1001..3000 -> 1
            in 3001..10000 -> 2
            else -> 3
        }
    }
}


