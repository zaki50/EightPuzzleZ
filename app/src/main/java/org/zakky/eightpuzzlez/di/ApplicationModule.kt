package org.zakky.eightpuzzlez.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import org.zakky.eightpuzzlez.GameRepository
import org.zakky.eightpuzzlez.RankingRepository
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
class ApplicationModule {

    @Provides
    @Singleton
    fun provideRankingRepository(@ApplicationContext context: Context): RankingRepository =
        RankingRepository(
            context.getSharedPreferences(
                RankingRepository.PREF_NAME,
                Context.MODE_PRIVATE
            )
        )

    @Provides
    @Singleton
    fun provideGameRepository(@ApplicationContext context: Context): GameRepository =
        GameRepository(
            context.getSharedPreferences(
                GameRepository.PREF_NAME, Context.MODE_PRIVATE
            )
        )

}