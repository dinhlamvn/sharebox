package com.dinhlam.sharebox.dialog.guideline

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseDialogFragment
import com.dinhlam.sharebox.base.BaseListAdapter
import com.dinhlam.sharebox.databinding.DialogGuidelineBinding
import com.dinhlam.sharebox.extensions.dp
import com.dinhlam.sharebox.extensions.heightPercentage
import com.dinhlam.sharebox.imageloader.config.ImageLoadScaleType
import com.dinhlam.sharebox.modelview.ResourceImageModelView
import com.dinhlam.sharebox.modelview.SizedBoxModelView
import com.dinhlam.sharebox.modelview.TextModelView

class GuidelineDialogFragment : BaseDialogFragment<DialogGuidelineBinding>() {

    override fun onCreateViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): DialogGuidelineBinding {
        return DialogGuidelineBinding.inflate(inflater, container, false)
    }

    private val guidelineAdapter = BaseListAdapter.createAdapter {
        add(
            TextModelView(
                "title",
                getString(R.string.title_guideline),
                height = ViewGroup.LayoutParams.WRAP_CONTENT,
                textAppearance = R.style.TextAppearance_MaterialComponents_Headline6
            )
        )

        add(
            TextModelView(
                "title_other_apps",
                getString(R.string.title_guideline_other_app),
                height = ViewGroup.LayoutParams.WRAP_CONTENT,
                gravity = Gravity.START,
                textAppearance = R.style.TextAppearance_MaterialComponents_Subtitle2
            )
        )

        add(
            TextModelView(
                "title_other_apps_step_1",
                getString(R.string.title_guideline_other_app_step_1),
                height = ViewGroup.LayoutParams.WRAP_CONTENT,
                gravity = Gravity.START,
                textAppearance = R.style.TextAppearance_MaterialComponents_Body1
            )
        )

        add(
            SizedBoxModelView(
                "spacing_1", height = 8.dp(), backgroundColor = android.R.color.transparent
            )
        )
        add(ResourceImageModelView(R.drawable.guidline_1, height = 100.dp()))

        add(
            SizedBoxModelView(
                "spacing_2", height = 8.dp(), backgroundColor = android.R.color.transparent
            )
        )
        add(ResourceImageModelView(R.drawable.guidline_2, height = 100.dp()))

        add(
            SizedBoxModelView(
                "spacing_3", height = 8.dp(), backgroundColor = android.R.color.transparent
            )
        )
        add(
            TextModelView(
                "title_other_apps_step_2",
                getString(R.string.title_guideline_other_app_step_2),
                height = ViewGroup.LayoutParams.WRAP_CONTENT,
                gravity = Gravity.START,
                textAppearance = R.style.TextAppearance_MaterialComponents_Body1
            )
        )
        add(
            SizedBoxModelView(
                "spacing_4", height = 8.dp(), backgroundColor = android.R.color.transparent
            )
        )
        add(ResourceImageModelView(R.drawable.guidline_3, height = 100.dp()))

        add(
            SizedBoxModelView(
                "spacing_5", height = 32.dp(), backgroundColor = android.R.color.transparent
            )
        )
        add(
            TextModelView(
                "title_in_app",
                getString(R.string.title_guideline_in_app),
                height = ViewGroup.LayoutParams.WRAP_CONTENT,
                gravity = Gravity.START,
                textAppearance = R.style.TextAppearance_MaterialComponents_Subtitle2
            )
        )

        add(
            TextModelView(
                "title_in_app_step_1",
                getString(R.string.title_guideline_in_app_step_1),
                height = ViewGroup.LayoutParams.WRAP_CONTENT,
                gravity = Gravity.START,
                textAppearance = R.style.TextAppearance_MaterialComponents_Body1
            )
        )

        add(
            SizedBoxModelView(
                "spacing_6", height = 8.dp(), backgroundColor = android.R.color.transparent
            )
        )
        add(
            ResourceImageModelView(
                R.drawable.guidline_4, height = 200.dp(), scaleType = ImageLoadScaleType.FitCenter
            )
        )

        add(
            SizedBoxModelView(
                "spacing_7", height = 8.dp(), backgroundColor = android.R.color.transparent
            )
        )
        add(
            TextModelView(
                "title_in_app_step_2",
                getString(R.string.title_guideline_in_app_step_2),
                height = ViewGroup.LayoutParams.WRAP_CONTENT,
                gravity = Gravity.START,
                textAppearance = R.style.TextAppearance_MaterialComponents_Body1
            )
        )
        add(
            SizedBoxModelView(
                "spacing_8", height = 8.dp(), backgroundColor = android.R.color.transparent
            )
        )
        add(
            ResourceImageModelView(
                R.drawable.guidline_5, height = 200.dp(), scaleType = ImageLoadScaleType.FitCenter
            )
        )

        add(
            SizedBoxModelView(
                "spacing_9", height = 8.dp(), backgroundColor = android.R.color.transparent
            )
        )
        add(
            TextModelView(
                "title_done",
                getString(R.string.title_guideline_done),
                height = ViewGroup.LayoutParams.WRAP_CONTENT,
                gravity = Gravity.START,
                textAppearance = R.style.TextAppearance_MaterialComponents_Subtitle2
            )
        )

        add(
            SizedBoxModelView(
                "spacing_10", height = 12.dp(), backgroundColor = android.R.color.transparent
            )
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding.recyclerView.updateLayoutParams {
            height = heightPercentage(80)
        }

        viewBinding.recyclerView.adapter = guidelineAdapter
        guidelineAdapter.requestBuildModelViews()
    }
}
