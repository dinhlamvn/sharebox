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

        binding.textPage.text = "1/7"
        val adapter = PageAdapter(this)
        binding.viewPager.adapter = adapter

        binding.viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.textPage.text = "%d/7".format(position + 1)
            }
        })

        binding.buttonNext.setOnClickListener {
            if (binding.viewPager.currentItem == 6) {
                finish()
            } else {
                binding.viewPager.currentItem += 1
            }
        }

        binding.buttonSkip.setOnClickListener {
            finish()
        }
    }

    private class PageAdapter(private val fragmentActivity: FragmentActivity) :
        FragmentStateAdapter(fragmentActivity) {
        override fun getItemCount(): Int {
            return 7
        }

        override fun createFragment(position: Int): Fragment {
            return GuidelineFragment().apply {
                arguments = bundleOf(AppExtras.EXTRA_DATA to buildGuideline(position))
            }
        }

        private fun buildGuideline(position: Int): Guideline {
            return when (position) {
                0 -> Guideline(
                    R.drawable.guideline_1,
                    R.string.guideline_title_1,
                    R.string.guideline_subtitle_1
                )

                1 -> Guideline(
                    R.drawable.guideline_2,
                    R.string.guideline_title_2,
                    R.string.guideline_subtitle_2
                )

                2 -> Guideline(
                    R.drawable.guideline_3,
                    R.string.guideline_title_3,
                    R.string.guideline_subtitle_3
                )

                3 -> Guideline(
                    R.drawable.guideline_4,
                    R.string.guideline_title_4,
                    R.string.guideline_subtitle_4
                )

                4 -> Guideline(
                    R.drawable.guideline_5,
                    R.string.guideline_title_5,
                    R.string.guideline_subtitle_5
                )

                5 -> Guideline(
                    R.drawable.guideline_6,
                    R.string.guideline_title_6,
                    R.string.guideline_subtitle_6
                )

                6 -> Guideline(
                    R.drawable.guideline_7,
                    R.string.guideline_title_7,
                    R.string.guideline_subtitle_7
                )

                else -> error("No Guideline Data For Position: $position")
            }
        }
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
            binding.image.setImageResource(guideline.image)
            binding.title.setText(guideline.title)
            binding.subtitle.setText(guideline.subtitle)
        }
    }
}