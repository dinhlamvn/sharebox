package com.dinhlam.sharebox.ui.discover.unsplash

import android.app.Activity
import androidx.activity.result.contract.ActivityResultContracts
import com.dinhlam.sharebox.common.AppExtras
import com.dinhlam.sharebox.extensions.showToast
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.dinhlam.sharebox.extensions.trimmedString
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import coil.compose.AsyncImage
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.base.BaseViewModel
import com.dinhlam.sharebox.base.BaseViewModelFragment
import com.dinhlam.sharebox.components.AppCardView
import com.dinhlam.sharebox.components.AppLazyStaggeredGrid
import com.dinhlam.sharebox.databinding.FragmentUnsplashDiscoverBinding
import com.dinhlam.sharebox.model.UnsplashPhoto
import com.dinhlam.sharebox.router.Router
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class UnsplashDiscoverFragment :
    BaseViewModelFragment<UnsplashDiscoverState, UnsplashDiscoverViewModel, FragmentUnsplashDiscoverBinding>() {

    private val createBoxResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.getStringExtra(AppExtras.EXTRA_BOX_ID)?.let { boxId ->
                    viewModel.setCurrentBoxId(boxId)
                }
            }
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

    override val viewModel: UnsplashDiscoverViewModel by viewModels()
    private var composeState by mutableStateOf(UnsplashDiscoverState())

    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): FragmentUnsplashDiscoverBinding {
        return FragmentUnsplashDiscoverBinding.inflate(inflater, container, false)
    }

    override fun onStateChanged(state: UnsplashDiscoverState) {
        composeState = state
        binding.searchButton.isEnabled = state.asyncSearch !is BaseViewModel.AsyncLoad.Loading
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        composeState = viewModel.currentState
        binding.searchInput.setText(composeState.query)
        binding.searchButton.isEnabled = composeState.asyncSearch !is BaseViewModel.AsyncLoad.Loading
        binding.searchButton.setOnClickListener { submitSearch() }
        binding.searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                submitSearch()
                true
            } else {
                false
            }
        }
        binding.composeView.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        binding.composeView.setContent {
            MaterialTheme {
                UnsplashDiscoverContent(
                    state = composeState,
                    onSave = { photo -> onArchive(photo.photoUrl, "Photo by ${photo.photographerName}") },
                    onLoadMore = viewModel::loadMore,
                    onRetry = viewModel::retry,
                    onPreview = { photo ->
                        startActivity(
                            router.imageViewer(requireContext(), listOf(photo.previewUrl.toUri()))
                        )
                    },
                    onOpenUrl = { url ->
                        router.moveToChromeCustomTab(requireContext(), url, null, null)
                    },
                )
            }
        }
        binding.boxSectionButton.setOnClickListener {
            chooseBoxLauncher.launch(router.boxList(requireContext(), null))
        }

        binding.boxSectionButton.setOnAddIconClickListener {
            createBoxResultLauncher.launch(router.boxForm(requireContext(), null))
        }

        onChange(UnsplashDiscoverState::asyncLoadArchive) { asyncLoad ->
            if (asyncLoad.success) {
                showToast(getString(R.string.archive_url_success, asyncLoad.data))
            }
        }

        onChange(UnsplashDiscoverState::currentBox) { currentBox ->
            val boxName = currentBox?.boxName
            val isLock = currentBox?.passcode?.isNotBlank() ?: false
            binding.boxSectionButton.setBoxName(boxName)
            binding.boxSectionButton.showLock(isLock)
        }
    }

    private fun submitSearch() {
        if (viewModel.currentState.asyncSearch is BaseViewModel.AsyncLoad.Loading) return
        val query = binding.searchInput.text.trimmedString()
        binding.searchInputLayout.error = if (query.isBlank()) {
            getString(R.string.search_text_required)
        } else {
            null
        }
        if (query.isNotBlank()) {
            ViewCompat.getWindowInsetsController(binding.searchInput)?.hide(WindowInsetsCompat.Type.ime())
            binding.searchInput.clearFocus()
            viewModel.search(query)
        }
    }

    private fun onArchive(url: String, note: String?) = getState(viewModel) {
        val box = getState(viewModel, UnsplashDiscoverState::currentBox)
        if (box == null) {
            showToast(R.string.please_choose_box)
            binding.boxSectionButton.playZoomAnimation()
            return@getState
        }
        viewModel.archiveLink(url, note, box.boxId)
    }

}

@Composable
private fun UnsplashDiscoverContent(
    state: UnsplashDiscoverState,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    onPreview: (UnsplashPhoto) -> Unit,
    onOpenUrl: (String) -> Unit,
    onSave: (UnsplashPhoto) -> Unit,
) {
    val gridState = rememberLazyStaggeredGridState()
    var displayedSearchRequestId by rememberSaveable { mutableStateOf(state.searchRequestId) }
    val shouldLoadMore by remember(state, gridState) {
        derivedStateOf {
            val lastVisible = gridState.layoutInfo.visibleItemsInfo.maxOfOrNull { it.index } ?: -1
            state.shouldLoadMore(lastVisible)
        }
    }

    LaunchedEffect(shouldLoadMore, state.page, state.searchRequestId) {
        if (shouldLoadMore) onLoadMore()
    }

    LaunchedEffect(state.searchRequestId) {
        if (displayedSearchRequestId != state.searchRequestId) {
            gridState.scrollToItem(0)
            displayedSearchRequestId = state.searchRequestId
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            AppLazyStaggeredGrid(
                items = state.photos,
                columns = StaggeredGridCells.Adaptive(160.dp),
                key = UnsplashPhoto::id,
                state = gridState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp, 0.dp, 8.dp, 72.dp),
                verticalItemSpacing = 8.dp,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) { photo ->
                UnsplashPhotoCard(
                    photo = photo,
                    onSave = onSave,
                    onPreview = onPreview,
                    onOpenUrl = onOpenUrl,
                )
            }

            when {
                state.asyncSearch is BaseViewModel.AsyncLoad.Loading && state.photos.isEmpty() -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }

                state.asyncSearch is BaseViewModel.AsyncLoad.Failed && state.photos.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = state.asyncSearch.error.message ?: "Unable to load Unsplash photos",
                            color = MaterialTheme.colorScheme.error,
                        )
                        Button(onClick = onRetry) { Text("Retry") }
                    }
                }

                state.asyncSearch.completed && state.photos.isEmpty() -> {
                    Text("No photos found", Modifier.align(Alignment.Center))
                }
            }

            if (state.isLoadingMore) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                )
            }
        }
        if (state.loadMoreError != null) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = state.loadMoreError,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
                TextButton(onClick = onLoadMore) { Text("Retry") }
            }
        }
    }
}

@Composable
private fun UnsplashPhotoCard(
    photo: UnsplashPhoto,
    onPreview: (UnsplashPhoto) -> Unit,
    onOpenUrl: (String) -> Unit,
    onSave: (UnsplashPhoto) -> Unit,
) {
    AppCardView(modifier = Modifier.fillMaxWidth()) {
        AsyncImage(
            model = photo.imageUrl,
            contentDescription = "Photo by ${photo.photographerName}",
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio((photo.width.toFloat() / photo.height.coerceAtLeast(1)).coerceIn(0.75f, 1.5f))
                .clickable { onOpenUrl(photo.photoUrl) },
            contentScale = ContentScale.Crop,
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "Photo by ${photo.photographerName}",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenUrl(photo.photographerUrl) },
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
            )
            TextButton(
                onClick = { onSave(photo) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(androidx.compose.ui.res.stringResource(R.string.save))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    onClick = { onPreview(photo) },
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp),
                ) {
                    Text("Preview", style = MaterialTheme.typography.labelMedium)
                }
                TextButton(
                    onClick = {
                        onOpenUrl("https://unsplash.com/?utm_source=sharebox&utm_medium=referral")
                    },
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp),
                ) {
                    Text("Unsplash", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
