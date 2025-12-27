package io.github.thwisse.kentinsesi.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.thwisse.kentinsesi.data.local.db.AppDatabase
import io.github.thwisse.kentinsesi.data.local.db.FilterPresetDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocalDataModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "kentin_sesi.db"
        ).build()
    }

    @Provides
    fun provideFilterPresetDao(db: AppDatabase): FilterPresetDao = db.filterPresetDao()
}
