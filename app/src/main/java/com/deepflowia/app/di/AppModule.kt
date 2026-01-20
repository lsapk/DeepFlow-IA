package com.deepflowia.app.di

import android.content.Context
import com.deepflowia.app.data.*
import com.deepflowia.app.data.SupabaseManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.auth.GoTrue
import io.github.jan.supabase.postgrest.Postgrest
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSupabasePostgrest(): Postgrest {
        return SupabaseManager.client.postgrest
    }

    @Provides
    @Singleton
    fun provideSupabaseAuth(): GoTrue {
        return SupabaseManager.client.auth
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Provides
    fun provideTaskDao(appDatabase: AppDatabase): TaskDao = appDatabase.taskDao()

    @Provides
    fun provideHabitDao(appDatabase: AppDatabase): HabitDao = appDatabase.habitDao()

    @Provides
    fun provideGoalDao(appDatabase: AppDatabase): GoalDao = appDatabase.goalDao()

    @Provides
    fun provideJournalEntryDao(appDatabase: AppDatabase): JournalEntryDao = appDatabase.journalEntryDao()

    @Provides
    fun provideDailyReflectionDao(appDatabase: AppDatabase): DailyReflectionDao = appDatabase.dailyReflectionDao()

    @Provides
    fun provideFocusSessionDao(appDatabase: AppDatabase): FocusSessionDao = appDatabase.focusSessionDao()

    @Provides
    fun provideHabitCompletionDao(appDatabase: AppDatabase): HabitCompletionDao = appDatabase.habitCompletionDao()

    @Provides
    fun provideSubtaskDao(appDatabase: AppDatabase): SubtaskDao = appDatabase.subtaskDao()

    @Provides
    fun provideSubobjectiveDao(appDatabase: AppDatabase): SubobjectiveDao = appDatabase.subobjectiveDao()

    @Provides
    @Singleton
    fun provideTaskRepository(taskDao: TaskDao, subtaskDao: SubtaskDao, postgrest: Postgrest): TaskRepository {
        return TaskRepository(taskDao, subtaskDao, postgrest)
    }

    @Provides
    @Singleton
    fun provideHabitRepository(habitDao: HabitDao, habitCompletionDao: HabitCompletionDao, postgrest: Postgrest): HabitRepository {
        return HabitRepository(habitDao, habitCompletionDao, postgrest)
    }

    @Provides
    @Singleton
    fun provideGoalRepository(goalDao: GoalDao, subobjectiveDao: SubobjectiveDao, postgrest: Postgrest): GoalRepository {
        return GoalRepository(goalDao, subobjectiveDao, postgrest)
    }

    @Provides
    @Singleton
    fun provideJournalRepository(journalEntryDao: JournalEntryDao, dailyReflectionDao: DailyReflectionDao, postgrest: Postgrest): JournalRepository {
        return JournalRepository(journalEntryDao, dailyReflectionDao, postgrest)
    }

    @Provides
    @Singleton
    fun provideFocusRepository(focusSessionDao: FocusSessionDao, postgrest: Postgrest): FocusRepository {
        return FocusRepository(focusSessionDao, postgrest)
    }
}
