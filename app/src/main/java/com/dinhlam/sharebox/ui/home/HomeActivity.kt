package com.dinhlam.sharebox.ui.home

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseActivity
import com.dinhlam.sharebox.databinding.ActivityHomeBinding
import com.dinhlam.sharebox.dialog.sharelink.ShareLinkInputDialogFragment
import com.dinhlam.sharebox.dialog.sharetextquote.ShareTextQuoteInputDialogFragment
import com.dinhlam.sharebox.extensions.takeIfGreaterThanZero
import com.dinhlam.sharebox.helper.ShareHelper
import com.dinhlam.sharebox.helper.VideoHelper
import com.dinhlam.sharebox.router.Router
import com.dinhlam.sharebox.services.RealtimeDatabaseService
import com.dinhlam.sharebox.ui.home.bookmark.BookmarkFragment
import com.dinhlam.sharebox.ui.home.general.GeneralFragment
import com.dinhlam.sharebox.ui.home.profile.ProfileFragment
import com.dinhlam.sharebox.ui.home.trending.TrendingFragment
import com.dinhlam.sharebox.utils.Icons
import com.dinhlam.sharebox.utils.LiveEventUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : BaseActivity<ActivityHomeBinding>(),
    ShareTextQuoteInputDialogFragment.OnShareTextQuoteCallback,
    ShareLinkInputDialogFragment.OnShareLinkCallback {

    companion object {
        private const val PAGE_SIZE = 4
    }

    private val realtimeDatabaseServiceIntent by lazy(LazyThreadSafetyMode.NONE) {
        Intent(this, RealtimeDatabaseService::class.java)
    }

    private val shareResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                LiveEventUtils.eventScrollToTopGeneral.postValue(true)
            }
        }

    private val pickImagesResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val clipData = result.data?.clipData ?: return@registerForActivityResult
                val pickCount =
                    clipData.itemCount.takeIfGreaterThanZero() ?: return@registerForActivityResult
                val intent = if (pickCount == 1) {
                    Intent(Intent.ACTION_SEND).apply {
                        type = "image/*"
                        `package` = packageName
                        putExtra(Intent.EXTRA_STREAM, clipData.getItemAt(0).uri)
                    }
                } else {
                    Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                        type = "image/*"
                        `package` = packageName
                        val list = arrayListOf<Uri>()
                        for (i in 0 until pickCount) {
                            list.add(clipData.getItemAt(i).uri)
                        }
                        putParcelableArrayListExtra(Intent.EXTRA_STREAM, list)
                    }
                }
                shareResultLauncher.launch(intent)
            }
        }

    private val pageAdapter = object : FragmentStateAdapter(this) {
        override fun getItemCount(): Int = PAGE_SIZE

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                1 -> TrendingFragment()
                2 -> BookmarkFragment()
                3 -> ProfileFragment()
                else -> GeneralFragment()
            }
        }
    }

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var shareHelper: ShareHelper

    @Inject
    lateinit var videoHelper: VideoHelper

    override fun onCreateViewBinding(): ActivityHomeBinding {
        return ActivityHomeBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ContextCompat.startForegroundService(this, realtimeDatabaseServiceIntent)

        viewBinding.viewPager.isUserInputEnabled = false
        viewBinding.viewPager.adapter = pageAdapter

        viewBinding.viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> viewBinding.bottomNavigationView.selectedItemId = R.id.navigation_general
                    1 -> viewBinding.bottomNavigationView.selectedItemId =
                        R.id.navigation_trending

                    2 -> viewBinding.bottomNavigationView.selectedItemId = R.id.navigation_bookmark
                    3 -> viewBinding.bottomNavigationView.selectedItemId = R.id.navigation_profile
                }
            }
        })

        viewBinding.bottomNavigationView.setOnItemSelectedListener { menuItem ->
            val pos = when (menuItem.itemId) {
                R.id.navigation_trending -> 1
                R.id.navigation_bookmark -> 2
                R.id.navigation_profile -> 3
                else -> 0
            }
            viewBinding.viewPager.setCurrentItem(pos, false)
            return@setOnItemSelectedListener true
        }

        viewBinding.bottomNavigationView.setOnItemReselectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_trending -> LiveEventUtils.eventRefreshVideosMixer.postValue(
                    true
                )

                R.id.navigation_general -> LiveEventUtils.eventScrollToTopGeneral.postValue(true)
                R.id.navigation_profile -> LiveEventUtils.eventScrollToTopProfile.postValue(true)
            }
        }

        viewBinding.bottomNavigationView.menu.getItem(2).isEnabled = false

        viewBinding.buttonArchiveUrl.setImageDrawable(Icons.urlIcon(this))
        viewBinding.buttonArchiveImages.setImageDrawable(Icons.imagesIcon(this))
        viewBinding.buttonArchiveText.setImageDrawable(Icons.quoteLeftIcon(this))

        toggleFabActionView(false)

        viewBinding.buttonAction.setOnClickListener {
            toggleFabActionView(!isFabShown())
        }

        viewBinding.viewFabOverlay.setOnClickListener {
            toggleFabActionView(false)
        }

        viewBinding.buttonArchiveText.setOnClickListener {
            toggleFabActionView(false)
            shareHelper.shareTextQuote(supportFragmentManager)
        }

        viewBinding.buttonArchiveUrl.setOnClickListener {
            toggleFabActionView(false)
            shareHelper.shareLink(supportFragmentManager)
        }

        viewBinding.buttonArchiveImages.setOnClickListener {
            toggleFabActionView(false)
            pickImagesResultLauncher.launch(router.pickImageIntent(true))
        }
    }

    private fun isFabShown() =
        viewBinding.buttonArchiveImages.isShown && viewBinding.buttonArchiveUrl.isShown && viewBinding.buttonArchiveText.isShown

    private fun toggleFabActionView(isVisible: Boolean) {
        if (isVisible) {
            viewBinding.buttonArchiveImages.show()
            viewBinding.buttonArchiveUrl.show()
            viewBinding.buttonArchiveText.show()
        } else {
            viewBinding.buttonArchiveImages.hide()
            viewBinding.buttonArchiveUrl.hide()
            viewBinding.buttonArchiveText.hide()
        }
        viewBinding.viewFabOverlay.isVisible = isVisible
        viewBinding.textArchiveUrl.isVisible = isVisible
        viewBinding.textArchiveImages.isVisible = isVisible
        viewBinding.textArchiveText.isVisible = isVisible
    }

    override fun onShareLink(link: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/*"
            `package` = packageName
            putExtra(Intent.EXTRA_TEXT, link)
        }
        shareResultLauncher.launch(intent)
    }

    override fun onShareTextQuote(text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/*"
            `package` = packageName
            putExtra(Intent.EXTRA_TEXT, text)
        }
        shareResultLauncher.launch(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(realtimeDatabaseServiceIntent)
    }
}
