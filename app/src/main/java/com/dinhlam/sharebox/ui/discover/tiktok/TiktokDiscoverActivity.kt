package com.dinhlam.sharebox.ui.discover.tiktok

import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.base.BaseViewModelActivity
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.databinding.ActivityTiktokDiscoverBinding
import com.dinhlam.sharebox.extensions.setDrawableCompat
import com.dinhlam.sharebox.extensions.showToast
import com.dinhlam.sharebox.listmodel.TiktokDiscoverListModel
import com.dinhlam.sharebox.router.Router
import com.dinhlam.sharebox.utils.Icons
import com.dinhlam.sharebox.utils.WorkerUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TiktokDiscoverActivity :
    BaseViewModelActivity<TiktokDiscoverState, TiktokDiscoverViewModel, ActivityTiktokDiscoverBinding>() {

    override fun onCreateViewBinding(): ActivityTiktokDiscoverBinding {
        return ActivityTiktokDiscoverBinding.inflate(layoutInflater)
    }

    private val chooseBoxLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data ?: return@registerForActivityResult
                val boxId =
                    data.getStringExtra(AppExtras.EXTRA_BOX_ID) ?: return@registerForActivityResult
                viewModel.setCurrentBoxId(boxId)
            }
        }

    @Inject
    lateinit var router: Router

    private val adapter = BaseListAdapter.create {
        getState(viewModel) { state ->
            state.tiktokDiscoverList.forEach { tiktokDiscover ->
                TiktokDiscoverListModel(
                    tiktokDiscover.id,
                    tiktokDiscover.url,
                    tiktokDiscover.desc,
                    tiktokDiscover.playCount,
                    BaseListAdapter.NoHashProp(View.OnClickListener {
                        startActivity(router.viewIntent(tiktokDiscover.url))
                    }),
                    BaseListAdapter.NoHashProp(View.OnClickListener {
                        onArchive(tiktokDiscover.url)
                    }),
                    BaseListAdapter.NoHashProp(View.OnClickListener {
                        WorkerUtils.enqueueJobDownloadTiktokVideo(
                            this@TiktokDiscoverActivity,
                            tiktokDiscover.hashCode(),
                            tiktokDiscover.url
                        )
                    }),
                ).attachTo(this)
            }
        }
    }

    override val viewModel: TiktokDiscoverViewModel by viewModels()

    override fun onStateChanged(state: TiktokDiscoverState) {
        binding.loading.toggle(state.asyncLoadTiktokDiscover.loading)
        val boxName = state.currentBox?.boxName
        val isLock = state.currentBox?.passcode?.isNotBlank() ?: false
        binding.textShareBox.text = boxName
        binding.textShareBox.setDrawableCompat(
            start = Icons.boxIcon(this) { copy(sizeDp = 20) },
            end = if (isLock) Icons.lockIcon(this) { copy(sizeDp = 16) } else null,
        )
        adapter.requestBuildListModels()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)

        adapter.attachTo(binding.recyclerView, this)

        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.swipeRefreshLayout.isRefreshing = false
            viewModel.getTiktokTrending()
        }

        binding.containerShareBox.setOnClickListener {
            chooseBoxLauncher.launch(router.boxList(this, null))
        }

        viewModel.onChange(TiktokDiscoverState::asyncLoadArchive, this) { asyncLoad ->
            if (asyncLoad.success) {
                showToast(getString(R.string.archive_url_success, asyncLoad.data))
            }
        }
    }

    private fun onArchive(url: String) = getState(viewModel) { state ->
        val box = getState(viewModel, TiktokDiscoverState::currentBox) ?: return@getState showToast(
            R.string.please_choose_box
        )
        viewModel.archiveLink(url, box.boxId)
    }
}