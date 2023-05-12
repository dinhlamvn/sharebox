package com.dinhlam.sharebox.ui.home

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.text.HtmlCompat
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.*
import com.dinhlam.sharebox.database.entity.Folder
import com.dinhlam.sharebox.databinding.ActivityHomeBinding
import com.dinhlam.sharebox.dialog.folder.confirmpassword.FolderConfirmPasswordDialogFragment
import com.dinhlam.sharebox.dialog.folder.creator.FolderCreatorDialogFragment
import com.dinhlam.sharebox.dialog.folder.detail.FolderDetailDialogFragment
import com.dinhlam.sharebox.dialog.folder.rename.RenameFolderDialogFragment
import com.dinhlam.sharebox.dialog.folder.resetpassword.ResetPasswordFolderDialogFragment
import com.dinhlam.sharebox.dialog.guideline.GuidelineDialogFragment
import com.dinhlam.sharebox.dialog.singlechoose.SingleChoiceDialogFragment
import com.dinhlam.sharebox.dialog.tag.ChoiceTagDialogFragment
import com.dinhlam.sharebox.extensions.*
import com.dinhlam.sharebox.helper.ShareHelper
import com.dinhlam.sharebox.model.SortType
import com.dinhlam.sharebox.pref.AppSharePref
import com.dinhlam.sharebox.router.AppRouter
import com.dinhlam.sharebox.ui.home.community.CommunityFragment
import com.dinhlam.sharebox.ui.home.profile.ProfileFragment
import com.dinhlam.sharebox.utils.*
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.collections.set

@AndroidEntryPoint
class HomeActivity : BaseViewModelActivity<HomeState, HomeViewModel, ActivityHomeBinding>(),
    FolderCreatorDialogFragment.OnFolderCreatorCallback,
    FolderConfirmPasswordDialogFragment.OnConfirmPasswordCallback,
    RenameFolderDialogFragment.OnConfirmRenameCallback,
    ChoiceTagDialogFragment.OnTagSelectedListener {

    private val settingLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                viewModel.reloadShareRecently()
                val sortType = result.data?.getSerializableExtraCompat<SortType>("sort-type")
                    ?: return@registerForActivityResult
                viewModel.setSortType(sortType)
            }
        }

    private val shareListLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                viewModel.loadShareListRecently()
                viewModel.loadFolderShareCount()
            }
        }

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var appRouter: AppRouter

    @Inject
    lateinit var appSharePref: AppSharePref

    @Inject
    lateinit var shareHelper: ShareHelper

    override val viewModel: HomeViewModel by viewModels()

    private val pageAdapter = object : FragmentStateAdapter(this) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return if (position == 1) ProfileFragment() else CommunityFragment()
        }
    }

    override fun onCreateViewBinding(): ActivityHomeBinding {
        return ActivityHomeBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setSupportActionBar(viewBinding.toolbar)

        viewModel.setSortType(appSharePref.getSortType())

        viewBinding.viewPager.adapter = pageAdapter
        viewBinding.viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> viewBinding.bottomNavigationView.selectedItemId = R.id.navigation_community
                    1 -> viewBinding.bottomNavigationView.selectedItemId = R.id.navigation_profile
                }
            }
        })
        viewBinding.bottomNavigationView.setOnItemSelectedListener { menuItem ->
            val pos = when (menuItem.itemId) {
                R.id.navigation_community -> 0
                else -> 1
            }
            viewBinding.viewPager.setCurrentItem(pos, true)
            return@setOnItemSelectedListener true
        }

//        viewBinding.recyclerView.layoutManager = LinearLayoutManager(this)
//        viewBinding.recyclerView.adapter = homeAdapter
//
//        viewBinding.swipeRefreshLayout.setOnRefreshListener {
//            viewModel.loadFolders()
//            viewModel.loadShareListRecently()
//            viewBinding.swipeRefreshLayout.isRefreshing = false
//        }

        viewModel.consume(this, HomeState::toastRes) { toastRes ->
            if (toastRes != 0) {
                showToast(getString(toastRes))
                viewModel.clearToast()
            }
        }

        viewModel.consume(this, HomeState::folderActionConfirmation, true, ::handleFolderAction)

        viewModel.consume(this, HomeState::folderToOpen) { folderToOpen ->
            folderToOpen?.let {
                viewModel.clearOpenFolder()
                openFolder(it)
            }
        }

        viewModel.consume(this, HomeState::tag) {
            invalidateOptionsMenu()
        }

        viewBinding.buttonCreateFolder.setOnClickListener {
            showDialogCreateFolder()
        }

        if (appSharePref.isShowGuideLine()) {
            //openGuideLineDialog()
            //appSharePref.turnOffShowGuideline()
        }

        viewBinding.bottomNavigationView.background = null
        viewBinding.bottomNavigationView.menu.getItem(1).isEnabled = false
    }

    private fun openGuideLineDialog() {
        BaseDialogFragment.showDialog(GuidelineDialogFragment::class, supportFragmentManager)
    }

    private fun openFolder(folder: Folder) {
        shareListLauncher.launch(appRouter.shareList(folder.id))
    }

    override fun onStateChanged(state: HomeState) {
        //viewBinding.frameProgress.frameContainer.isVisible = state.showProgress
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.item_clear_tag)?.isVisible = getState(viewModel) { it.tag != null }
        return super.onPrepareOptionsMenu(menu)
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.home_menu, menu)
        menu.cast<MenuBuilder>()?.setOptionalIconsVisible(true)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.item_setting) {
            settingLauncher.launch(appRouter.setting())
            return true
        }

        if (viewModel.isHandleByTagSelected(item.itemId)) {
            return true
        }

        if (item.itemId == R.id.item_clear_tag) {
            viewModel.clearSelectedTag()
            return true
        }

        if (item.itemId == R.id.item_about) {
            showAbout()
            return true
        }

        if (item.itemId == R.id.item_guideline) {
            openGuideLineDialog()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun showDialogCreateFolder() {
        val dialog = FolderCreatorDialogFragment()
        dialog.show(supportFragmentManager, "DialogCreateFolder")
    }

    override fun onFolderCreated(folderId: String) {
        viewModel.loadFolders()
    }

    private fun onFolderOptionClick(position: Int) {
        val folder = getState(viewModel) { state -> state.folders.getOrNull(position) } ?: return
        val items = mutableMapOf<String, () -> Unit>()
        val icons = mutableListOf<Int>()

        if (!FolderUtils.isProtectedFolder(folder.id)) {
            items[getString(R.string.delete)] = {
                viewModel.processFolderForDelete(folder)
            }
            icons.add(R.drawable.ic_delete_primary)

            items[getString(R.string.rename)] = {
                viewModel.processFolderForRename(folder)
            }
            icons.add(R.drawable.ic_rename_primary)
        }

        if (folder.isHasPassword()) {
            items[getString(R.string.reset_password)] = {
                viewModel.processFolderForResetPassword(folder)
            }
            icons.add(R.drawable.ic_reset_password_primary)
        }

        items[getString(R.string.tag)] = {
            viewModel.processFolderForTag(folder)
        }
        icons.add(R.drawable.ic_tag_primary)

        items[getString(R.string.detail)] = {
            viewModel.processFolderForDetail(folder)
        }
        icons.add(R.drawable.ic_info_primary)

        BaseBottomSheetDialogFragment.showDialog(
            SingleChoiceDialogFragment::class, supportFragmentManager
        ) {
            arguments = Bundle().apply {
                putStringArray(
                    SingleChoiceDialogFragment.EXTRA_ITEM, items.keys.toTypedArray()
                )
                putIntArray(SingleChoiceDialogFragment.EXTRA_ICON, icons.toIntArray())
            }
            listener = SingleChoiceDialogFragment.OnItemSelectedListener { position ->
                val key = items.keys.toList()[position]
                items[key]?.invoke()
            }
        }
    }

    private fun handleFolderAction(confirmation: HomeState.FolderActionConfirmation?) {
        val nonNull = confirmation ?: return
        return when (nonNull.folderActionType) {
            HomeState.FolderActionConfirmation.FolderActionType.DELETE -> maybeShowConfirmPasswordToDeleteFolder(
                nonNull
            )

            HomeState.FolderActionConfirmation.FolderActionType.OPEN -> maybeShowConfirmPasswordToOpenFolder(
                nonNull
            )

            HomeState.FolderActionConfirmation.FolderActionType.RENAME -> maybeShowConfirmPasswordToRenameFolder(
                nonNull
            )

            HomeState.FolderActionConfirmation.FolderActionType.RESET_PASSWORD -> showDialogToResetPassword(
                nonNull
            )

            HomeState.FolderActionConfirmation.FolderActionType.DETAIL -> maybeShowConfirmPasswordToViewDetailFolder(
                nonNull
            )

            HomeState.FolderActionConfirmation.FolderActionType.TAG -> showTagChoose(nonNull)
        }
    }

    private fun showTagChoose(confirmation: HomeState.FolderActionConfirmation) {
        val folder = confirmation.folder
        val dialog = ChoiceTagDialogFragment()
        dialog.arguments = Bundle().apply {
            putString(ExtraUtils.EXTRA_TITLE, "Choose tag")
            putInt(ExtraUtils.EXTRA_POSITION, folder.tag ?: 0)
        }
        dialog.show(supportFragmentManager, "dialog")
    }

    private fun maybeShowConfirmPasswordToOpenFolder(confirmation: HomeState.FolderActionConfirmation) {
        val folder = confirmation.folder
        if (!folder.isHasPassword()) {
            viewModel.openFolderAfterPasswordVerified(false)
        } else {
            showConfirmPasswordDialog(folder.id)
        }
    }

    private fun maybeShowConfirmPasswordToDeleteFolder(confirmation: HomeState.FolderActionConfirmation) {
        val folder = confirmation.folder
        val title = getString(R.string.confirmation)
        val numberOfShare = resources.getQuantityString(
            R.plurals.share_count_text, confirmation.shareCount, confirmation.shareCount
        )
        val message = HtmlCompat.fromHtml(
            getString(
                R.string.delete_folder_confirmation_message, folder.name, numberOfShare
            ), HtmlCompat.FROM_HTML_MODE_LEGACY
        )
        showAlert(title,
            message,
            getString(R.string.delete),
            getString(R.string.cancel),
            onPosClickListener = { _, _ ->
                if (folder.isHasPassword().not() || confirmation.ignorePassword) {
                    viewModel.deleteFolder(folder)
                } else {
                    showConfirmPasswordDialog(folder.id)
                }
            })
    }

    private fun maybeShowConfirmPasswordToRenameFolder(confirmation: HomeState.FolderActionConfirmation) {
        val folder = confirmation.folder
        if (folder.isHasPassword().not() || confirmation.ignorePassword) {
            BaseDialogFragment.showDialog(
                RenameFolderDialogFragment::class, supportFragmentManager
            ) {
                arguments = Bundle().apply {
                    putString(ExtraUtils.EXTRA_FOLDER_ID, folder.id)
                }
            }
        } else {
            showConfirmPasswordDialog(folder.id)
        }
    }

    private fun showDialogToResetPassword(confirmation: HomeState.FolderActionConfirmation) {
        val folder = confirmation.folder
        BaseDialogFragment.showDialog(
            ResetPasswordFolderDialogFragment::class, supportFragmentManager
        ) {
            arguments = Bundle().apply {
                putString(ExtraUtils.EXTRA_FOLDER_ID, folder.id)
            }
        }
        viewModel.clearFolderActionConfirmation()
    }

    private fun maybeShowConfirmPasswordToViewDetailFolder(confirmation: HomeState.FolderActionConfirmation) {
        val folder = confirmation.folder
        if (folder.isHasPassword().not() || confirmation.ignorePassword) {
            BaseDialogFragment.showDialog(
                FolderDetailDialogFragment::class, supportFragmentManager
            ) {
                arguments = Bundle().apply {
                    putString(ExtraUtils.EXTRA_FOLDER_ID, folder.id)
                }
            }
            viewModel.clearFolderActionConfirmation()
        } else {
            showConfirmPasswordDialog(folder.id)
        }
    }

    private fun showConfirmPasswordDialog(id: String) = getState(viewModel) { state ->
        if (state.folderPasswordConfirmRemind.contains(id)) {
            return@getState onPasswordVerified(false)
        }
        BaseDialogFragment.showDialog(
            FolderConfirmPasswordDialogFragment::class, supportFragmentManager
        ) {
            arguments = Bundle().apply {
                putString(ExtraUtils.EXTRA_FOLDER_ID, id)
            }
        }
    }

    override fun onPasswordVerified(isRemindPassword: Boolean) {
        val actionType =
            getState(viewModel) { state -> state.folderActionConfirmation?.folderActionType }
                ?: return viewModel.clearFolderActionConfirmation()
        when (actionType) {
            HomeState.FolderActionConfirmation.FolderActionType.OPEN -> viewModel.openFolderAfterPasswordVerified(
                isRemindPassword
            )

            HomeState.FolderActionConfirmation.FolderActionType.DELETE -> viewModel.deleteFolderAfterPasswordVerified()
            HomeState.FolderActionConfirmation.FolderActionType.RENAME -> viewModel.renameFolderAfterPasswordVerified()
            HomeState.FolderActionConfirmation.FolderActionType.DETAIL -> viewModel.showDetailFolderAfterPasswordVerified()
            else -> {}
        }
    }

    override fun onCancelConfirmPassword() {
        viewModel.clearFolderActionConfirmation()
    }

    override fun onRenameSuccess() {
        viewModel.loadFolders()
    }

    override fun onCancelRename() {
        viewModel.clearFolderActionConfirmation()
    }

    override fun onTagSelected(tagId: Int) {
        viewModel.setFolderTag(tagId)
    }

    private fun showAbout() =
        AlertDialog.Builder(this).setTitle(R.string.app_name).setMessage(buildSpannedString {
            append("\n")
            bold {
                append(getString(R.string.developer_title))
            }
            append(" ${getString(R.string.developer_name)}")
            append("\n\n")
            bold {
                append(getString(R.string.contact_title))
            }
            append(" ${getString(R.string.developer_email)}")
        }).setPositiveButton(R.string.button_close, null).setCancelable(true).show()
}
