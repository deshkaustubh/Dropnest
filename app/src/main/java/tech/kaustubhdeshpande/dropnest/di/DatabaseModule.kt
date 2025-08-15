package tech.kaustubhdeshpande.dropnest.di

import android.content.Context
import tech.kaustubhdeshpande.dropnest.data.local.dao.CategoryDao
import tech.kaustubhdeshpande.dropnest.data.local.dao.DropDao
import tech.kaustubhdeshpande.dropnest.data.local.database.DropNestDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDropNestDatabase(@ApplicationContext context: Context): DropNestDatabase {
        return DropNestDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideDropDao(database: DropNestDatabase): DropDao {
        return database.dropDao()
    }

    @Provides
    @Singleton
    fun provideCategoryDao(database: DropNestDatabase): CategoryDao {
        return database.categoryDao()
    }
}