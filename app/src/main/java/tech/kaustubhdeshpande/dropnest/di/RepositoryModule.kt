package tech.kaustubhdeshpande.dropnest.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tech.kaustubhdeshpande.dropnest.data.repository.CategoryRepositoryImpl
import tech.kaustubhdeshpande.dropnest.data.repository.DropRepositoryImpl
import tech.kaustubhdeshpande.dropnest.domain.repository.CategoryRepository
import tech.kaustubhdeshpande.dropnest.domain.repository.DropRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(
        categoryRepositoryImpl: CategoryRepositoryImpl
    ): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindDropRepository(
        dropRepositoryImpl: DropRepositoryImpl
    ): DropRepository
}