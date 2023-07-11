package com.dinhlam.sharebox.ui.home

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
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
import com.dinhlam.sharebox.router.AppRouter
import com.dinhlam.sharebox.ui.home.bookmark.BookmarkFragment
import com.dinhlam.sharebox.ui.home.community.CommunityFragment
import com.dinhlam.sharebox.ui.home.profile.ProfileFragment
import com.dinhlam.sharebox.ui.home.videomixer.VideoMixerFragment
import com.dinhlam.sharebox.utils.IconUtils
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
                startActivity(intent)
            }
        }

    private val pageAdapter = object : FragmentStateAdapter(this) {
        override fun getItemCount(): Int = PAGE_SIZE

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                1 -> VideoMixerFragment()
                2 -> BookmarkFragment()
                3 -> ProfileFragment()
                else -> CommunityFragment()
            }
        }
    }

    @Inject
    lateinit var appRouter: AppRouter

    @Inject
    lateinit var shareHelper: ShareHelper

    @Inject
    lateinit var videoHelper: VideoHelper

    override fun onCreateViewBinding(): ActivityHomeBinding {
        return ActivityHomeBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding.viewPager.isUserInputEnabled = false
        viewBinding.viewPager.adapter = pageAdapter

        viewBinding.viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> viewBinding.bottomNavigationView.selectedItemId = R.id.navigation_community
                    1 -> viewBinding.bottomNavigationView.selectedItemId =
                        R.id.navigation_video_mixer

                    2 -> viewBinding.bottomNavigationView.selectedItemId = R.id.navigation_bookmark
                    3 -> viewBinding.bottomNavigationView.selectedItemId = R.id.navigation_profile
                }
            }
        })

        viewBinding.bottomNavigationView.setOnItemSelectedListener { menuItem ->
            val pos = when (menuItem.itemId) {
                R.id.navigation_video_mixer -> 1
                R.id.navigation_bookmark -> 2
                R.id.navigation_profile -> 3
                else -> 0
            }
            viewBinding.viewPager.setCurrentItem(pos, false)
            return@setOnItemSelectedListener true
        }

        viewBinding.bottomNavigationView.setOnItemReselectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_video_mixer -> LiveEventUtils.eventRefreshVideosMixer.postValue(
                    true
                )

                R.id.navigation_community -> LiveEventUtils.eventScrollToTopCommunity.postValue(true)
                R.id.navigation_profile -> LiveEventUtils.eventScrollToTopProfile.postValue(true)
            }
        }

        viewBinding.bottomNavigationView.menu.getItem(2).isEnabled = false

        viewBinding.buttonShareUrl.setImageDrawable(IconUtils.urlIcon(this))
        viewBinding.buttonShareImages.setImageDrawable(IconUtils.imagesIcon(this))
        viewBinding.buttonShareText.setImageDrawable(IconUtils.quoteLeftIcon(this))

        toggleFabActionView(false)

        viewBinding.buttonAction.setOnClickListener {
            toggleFabActionView(!isFabShown())
        }

        viewBinding.viewFabOverlay.setOnClickListener {
            toggleFabActionView(false)
        }

        viewBinding.buttonShareText.setOnClickListener {
            toggleFabActionView(false)
            shareHelper.shareTextQuote(supportFragmentManager)
        }

        viewBinding.buttonShareUrl.setOnClickListener {
            toggleFabActionView(false)
            shareHelper.shareLink(supportFragmentManager)
        }

        viewBinding.buttonShareImages.setOnClickListener {
            toggleFabActionView(false)
            pickImagesResultLauncher.launch(appRouter.pickImageIntent(true))
        }
    }

    private fun isFabShown() =
        viewBinding.buttonShareImages.isShown && viewBinding.buttonShareUrl.isShown && viewBinding.buttonShareText.isShown

    private fun toggleFabActionView(isVisible: Boolean) {
        if (isVisible) {
            viewBinding.buttonShareImages.show()
            viewBinding.buttonShareUrl.show()
            viewBinding.buttonShareText.show()
        } else {
            viewBinding.buttonShareImages.hide()
            viewBinding.buttonShareUrl.hide()
            viewBinding.buttonShareText.hide()
        }
        viewBinding.viewFabOverlay.isVisible = isVisible
        viewBinding.textShareUrl.isVisible = isVisible
        viewBinding.textShareImages.isVisible = isVisible
        viewBinding.textShareText.isVisible = isVisible
    }

    override fun onShareLink(link: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/*"
            `package` = packageName
            putExtra(Intent.EXTRA_TEXT, link)
        }
        startActivity(intent)
    }

    override fun onShareTextQuote(text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/*"
            `package` = packageName
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(intent)
    }
}
