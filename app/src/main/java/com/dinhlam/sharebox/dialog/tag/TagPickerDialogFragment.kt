package com.dinhlam.sharebox.dialog.tag

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseViewModelDialogFragment
import com.dinhlam.sharebox.base.CommonLazyGrid
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.data.local.entity.Tag
import com.dinhlam.sharebox.databinding.DialogFragmentTagPickerBinding
import com.dinhlam.sharebox.extensions.asColorInt
import com.dinhlam.sharebox.extensions.showToast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TagPickerDialogFragment :
    BaseViewModelDialogFragment<TagPickerState, TagPickerViewModel, DialogFragmentTagPickerBinding>() {

    companion object {
        @JvmStatic
        fun showDialog(fragmentManager: FragmentManager, shareId: String): TagPickerDialogFragment {
            return TagPickerDialogFragment().apply {
                arguments = bundleOf(AppExtras.EXTRA_SHARE_ID to shareId)
                show(fragmentManager, "dialog_tag_picker")
            }
        }
    }

    override val isUseMaterialDialog: Boolean
        get() = false

    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogFragmentTagPickerBinding {
        return DialogFragmentTagPickerBinding.inflate(inflater, container, false)
    }

    override val viewModel: TagPickerViewModel by viewModels()

    private var composeState by mutableStateOf<TagPickerState?>(null)

    override fun onStateChanged(state: TagPickerState) {
        composeState = state
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        composeState = viewModel.currentState
        binding.tagGrid.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        binding.tagGrid.setContent {
            MaterialTheme {
                val state = composeState ?: return@MaterialTheme
                TagGrid(
                    tags = state.tags,
                    selectedTagId = state.tagIdPicked,
                    onTagClick = viewModel::setSelectedTag,
                )
            }
        }

        onAsyncChange(TagPickerState::asyncLoadSaveTag, onFail = { error ->
            showToast(error.message)
        }) {
            showToast(R.string.saved)
            dismiss()
        }

        binding.buttonSave.setOnClickListener {
            viewModel.saveShareTag()
        }
    }
}

@Composable
private fun TagGrid(
    tags: List<Tag>,
    selectedTagId: Int?,
    onTagClick: (Int) -> Unit,
) {
    CommonLazyGrid(
        items = tags,
        columns = GridCells.Fixed(5),
        key = { it.id },
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(3.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) { tag ->
        val selected = tag.id == selectedTagId
        Card(
            modifier = Modifier
                .aspectRatio(1f)
                .clickable { onTagClick(tag.id) },
            shape = CircleShape,
            colors = CardDefaults.cardColors(
                containerColor = Color(tag.tagColor.asColorInt()),
            ),
            border = if (selected) BorderStroke(3.dp, Color.Gray) else null,
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                if (selected) {
                    Text(text = "✓", color = Color.White)
                }
            }
        }
    }
}
