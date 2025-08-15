package tech.kaustubhdeshpande.dropnest.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tech.kaustubhdeshpande.dropnest.domain.repository.CategoryRepository
import tech.kaustubhdeshpande.dropnest.domain.repository.DropRepository
import tech.kaustubhdeshpande.dropnest.domain.usecase.category.*
import tech.kaustubhdeshpande.dropnest.domain.usecase.drop.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    // Drop use cases
    @Provides
    @Singleton
    fun provideCreateDropUseCase(dropRepository: DropRepository): CreateDropUseCase {
        return CreateDropUseCase(dropRepository)
    }

    @Provides
    @Singleton
    fun provideGetDropsUseCase(dropRepository: DropRepository): GetDropsUseCase {
        return GetDropsUseCase(dropRepository)
    }

    @Provides
    @Singleton
    fun provideGetDropsByCategoryUseCase(dropRepository: DropRepository): GetDropsByCategoryUseCase {
        return GetDropsByCategoryUseCase(dropRepository)
    }

    @Provides
    @Singleton
    fun provideDeleteDropUseCase(dropRepository: DropRepository): DeleteDropUseCase {
        return DeleteDropUseCase(dropRepository)
    }

    @Provides
    @Singleton
    fun provideSearchDropsUseCase(dropRepository: DropRepository): SearchDropsUseCase {
        return SearchDropsUseCase(dropRepository)
    }

    // Category use cases
    @Provides
    @Singleton
    fun provideGetCategoriesUseCase(categoryRepository: CategoryRepository): GetCategoriesUseCase {
        return GetCategoriesUseCase(categoryRepository)
    }

    @Provides
    @Singleton
    fun provideCreateCategoryUseCase(categoryRepository: CategoryRepository): CreateCategoryUseCase {
        return CreateCategoryUseCase(categoryRepository)
    }

    @Provides
    @Singleton
    fun provideDeleteCategoryUseCase(categoryRepository: CategoryRepository): DeleteCategoryUseCase {
        return DeleteCategoryUseCase(categoryRepository)
    }
}