package com.dinhlam.sharebox.ui.home

import android.content.Context
import com.dinhlam.sharebox.extensions.castNonNull
import com.dinhlam.sharebox.helper.ShareHelper
import com.dinhlam.sharebox.router.Router
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped

@Module
@InstallIn(value = [ActivityComponent::class])
object HomeModule {
    @Provides
    @ActivityScoped
    fun provideAdapter(
        @ActivityContext context: Context,
        shareHelper: ShareHelper,
        router: Router,
    ): HomeAdapter {
        val activity: HomeActivity = context.castNonNull()
        return HomeAdapter(activity, activity.viewModel, shareHelper, router)
    }
}