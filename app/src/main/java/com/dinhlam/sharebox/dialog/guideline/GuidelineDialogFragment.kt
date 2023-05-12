package com.dinhlam.sharebox.dialog.guideline

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseDialogFragment
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.DialogGuidelineBinding
import com.dinhlam.sharebox.databinding.ModelViewGuidelineImageBinding
import com.dinhlam.sharebox.extensions.screenHeight

class GuidelineDialogFragment : BaseDialogFragment<DialogGuidelineBinding>() {

    private val adapter by lazy {
        BaseListAdapter.createAdapter({
            repeat(4) { number ->
                add(GuidelineImageModelView(number + 1))
            }
        }) {
            withViewType(R.layout.model_view_guideline_image) {
                GuidelineImageModelView.GuidelineImageViewHolder(
                    ModelViewGuidelineImageBinding.bind(
                        this
                    )
                )
            }
        }
    }

    override fun onCreateViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): DialogGuidelineBinding {
        return DialogGuidelineBinding.inflate(inflater, container, false)
    }

    override fun onViewDidLoad(view: View, savedInstanceState: Bundle?) {
        viewBinding.container.updateLayoutParams {
            height = screenHeight().times(0.8f).toInt()
        }
        viewBinding.viewPager.adapter = adapter
        adapter.requestBuildModelViews()

        viewBinding.viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                viewBinding.imageViewBack.isEnabled = position > 0
                viewBinding.imageViewForward.isEnabled = position < 3
                updateText(position)
            }
        })

        viewBinding.imageViewBack.setOnClickListener {
            viewBinding.viewPager.currentItem =
                viewBinding.viewPager.currentItem.minus(1).coerceAtLeast(0)
        }

        viewBinding.imageViewForward.setOnClickListener {
            viewBinding.viewPager.currentItem =
                viewBinding.viewPager.currentItem.plus(1).coerceAtMost(3)
        }
    }

    private fun updateText(position: Int) {
        viewBinding.textView.text = when (position) {
            1 -> getString(R.string.guideline_2)
            2 -> getString(R.string.guideline_3)
            3 -> getString(R.string.guideline_4)
            else -> getString(R.string.guideline_1)
        }
    }

    override fun getSpacing(): Int {
        return 32
    }
}