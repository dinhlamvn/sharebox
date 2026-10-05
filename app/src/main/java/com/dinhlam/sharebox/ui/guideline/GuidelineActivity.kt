package com.dinhlam.sharebox.ui.guideline

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseActivity
import com.dinhlam.sharebox.base.BaseFragment
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.databinding.ActivityGuidelineBinding
import com.dinhlam.sharebox.databinding.FragmentGuidelineBinding
import com.dinhlam.sharebox.extensions.getParcelableExtraCompat
import com.dinhlam.sharebox.imageloader.ImageLoader
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.parcelize.Parcelize

@AndroidEntryPoint
class GuidelineActivity : BaseActivity<ActivityGuidelineBinding>() {

    @Parcelize
    data class Guideline(
        @DrawableRes val image: Int,
        @StringRes val title: Int,
        @StringRes val subtitle: Int
    ) : Parcelable

    override fun onCreateViewBinding(): ActivityGuidelineBinding {
        return ActivityGuidelineBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.viewPager.adapter = PageAdapter(this)
        binding.pageProgress.max = guidelines.size
        binding.viewPager.registerOnPageChangeCallback(pageChangeCallback)
        updateNavigation(binding.viewPager.currentItem)
        binding.buttonNext.setOnClickListener {
            if (binding.viewPager.currentItem == guidelines.lastIndex) finish()
            else binding.viewPager.currentItem += 1
        }
        binding.buttonBack.setOnClickListener {
            binding.viewPager.currentItem = (binding.viewPager.currentItem - 1).coerceAtLeast(0)
        }
        binding.buttonSkip.setOnClickListener { finish() }
    }

    private val pageChangeCallback = object : OnPageChangeCallback() {
        override fun onPageSelected(position: Int) = updateNavigation(position)
    }

    private fun updateNavigation(position: Int) {
        val isLastPage = position == guidelines.lastIndex
        binding.textPage.text = getString(R.string.guideline_step, position + 1, guidelines.size)
        binding.pageProgress.setProgressCompat(position + 1, true)
        binding.buttonBack.visibility = if (position == 0) View.INVISIBLE else View.VISIBLE
        binding.buttonSkip.visibility = if (isLastPage) View.INVISIBLE else View.VISIBLE
        binding.buttonNext.setText(if (isLastPage) R.string.done else R.string.next)
    }

    override fun onDestroy() {
        binding.viewPager.unregisterOnPageChangeCallback(pageChangeCallback)
        super.onDestroy()
    }

    private class PageAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount() = guidelines.size

        override fun createFragment(position: Int): Fragment = GuidelineFragment().apply {
            arguments = bundleOf(AppExtras.EXTRA_DATA to guidelines[position])
        }
    }

    private companion object {
        val guidelines = listOf(
            Guideline(R.drawable.guideline_1, R.string.guideline_title_1, R.string.guideline_subtitle_1),
            Guideline(R.drawable.guideline_2, R.string.guideline_title_2, R.string.guideline_subtitle_2),
            Guideline(R.drawable.guideline_3, R.string.guideline_title_3, R.string.guideline_subtitle_3),
            Guideline(R.drawable.guideline_4, R.string.guideline_title_4, R.string.guideline_subtitle_4),
            Guideline(R.drawable.guideline_5, R.string.guideline_title_5, R.string.guideline_subtitle_5),
            Guideline(R.drawable.guideline_6, R.string.guideline_title_6, R.string.guideline_subtitle_6),
            Guideline(R.drawable.guideline_7, R.string.guideline_title_7, R.string.guideline_subtitle_7),
        )
    }

    @AndroidEntryPoint
    class GuidelineFragment : BaseFragment<FragmentGuidelineBinding>() {

        private val guideline by lazy { arguments?.getParcelableExtraCompat<Guideline>(AppExtras.EXTRA_DATA)!! }

        override fun onCreateViewBinding(
            inflater: LayoutInflater,
            container: ViewGroup?
        ): FragmentGuidelineBinding {
            return FragmentGuidelineBinding.inflate(layoutInflater, container, false)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            ImageLoader.INSTANCE.load(requireContext(), guideline.image, binding.image)
            binding.title.setText(guideline.title)
            binding.subtitle.setText(guideline.subtitle)
        }
    }
}