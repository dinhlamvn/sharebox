package com.dinhlam.sharesaver.ui.home

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.view.menu.MenuBuilder
import androidx.recyclerview.widget.GridLayoutManager
import com.dinhlam.sharesaver.R
import com.dinhlam.sharesaver.base.BaseListAdapter
import com.dinhlam.sharesaver.base.BaseSpanSizeLookup
import com.dinhlam.sharesaver.base.BaseViewModelActivity
import com.dinhlam.sharesaver.databinding.ActivityMainBinding
import com.dinhlam.sharesaver.extensions.cast
import com.dinhlam.sharesaver.extensions.registerOnBackPressHandler
import com.dinhlam.sharesaver.modelview.FolderModelView
import com.dinhlam.sharesaver.ui.dialog.folder.creator.FolderCreatorDialogFragment
import com.dinhlam.sharesaver.ui.home.modelview.HomeDateModelView
import com.dinhlam.sharesaver.ui.home.modelview.HomeImageModelView
import com.dinhlam.sharesaver.ui.home.modelview.HomeWebLinkModelView
import com.dinhlam.sharesaver.viewholder.LoadingViewHolder
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : BaseViewModelActivity<HomeData, HomeViewModel, ActivityMainBinding>(),
    FolderCreatorDialogFragment.OnFolderCreatorCallback {

    private val modelViewsFactory by lazy { HomeModelViewsFactory(this, viewModel, gson) }

    companion object {
        private const val SPAN_COUNT = 3
    }

    @Inject
    lateinit var gson: Gson

    override val viewModel: HomeViewModel by viewModels()

    private val homeAdapter = BaseListAdapter.createAdapter { layoutRes: Int, view: View ->
        return@createAdapter when (layoutRes) {
            R.layout.model_view_home_share_web_link -> HomeWebLinkModelView.HomeTextViewHolder(
                view
            )
            R.layout.model_view_home_share_image -> HomeImageModelView.HomeImageViewHolder(
                view
            )
            R.layout.model_view_loading -> LoadingViewHolder(view)
            R.layout.model_view_folder -> FolderModelView.FolderViewHolder(view) { position ->
                viewModel.onFolderClick(position)
            }
            R.layout.model_view_home_date -> HomeDateModelView.HomeDateViewHolder(view)
            else -> null
        }
    }

    override fun onCreateViewBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerOnBackPressHandler {
            if (viewModel.handleBackPressed()) {
                return@registerOnBackPressHandler
            }
            finish()
        }

        viewBinding.recyclerView.layoutManager = GridLayoutManager(this, SPAN_COUNT).apply {
            spanSizeLookup = BaseSpanSizeLookup(homeAdapter, this)
        }

        viewBinding.recyclerView.adapter = homeAdapter
        modelViewsFactory.attach(homeAdapter)

        viewBinding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.reload()
            viewBinding.swipeRefreshLayout.isRefreshing = false
        }
    }

    override fun onDataChanged(data: HomeData) {
        modelViewsFactory.requestBuildModelViews()
        val title = data.selectedFolder?.name ?: getString(R.string.app_name)
        supportActionBar?.title = title
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu.cast<MenuBuilder>()?.setOptionalIconsVisible(true)
        return menuInflater.inflate(R.menu.home_menu, menu).let { true }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.item_create_folder) {
            showDialogCreateFolder()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showDialogCreateFolder() {
        val dialog = FolderCreatorDialogFragment()
        dialog.show(supportFragmentManager, "DialogCreateFolder")
    }

    override fun onFolderCreated(folderId: String) {
        viewModel.reload()
    }
}