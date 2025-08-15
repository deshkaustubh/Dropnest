package tech.kaustubhdeshpande.dropnest.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import tech.kaustubhdeshpande.dropnest.data.local.dao.CategoryDao
import tech.kaustubhdeshpande.dropnest.data.local.dao.DropDao
import tech.kaustubhdeshpande.dropnest.data.local.entity.CategoryEntity
import tech.kaustubhdeshpande.dropnest.data.local.entity.DropEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [DropEntity::class, CategoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class DropNestDatabase : RoomDatabase() {

    abstract fun dropDao(): DropDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        private const val DATABASE_NAME = "dropnest_db"

        @Volatile
        private var INSTANCE: DropNestDatabase? = null

        fun getDatabase(context: Context): DropNestDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DropNestDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance

                // Initialize default categories
                CoroutineScope(Dispatchers.IO).launch {
                    initializeDefaultCategories(instance)
                }

                instance
            }
        }

        private suspend fun initializeDefaultCategories(database: DropNestDatabase) {
            val defaultCategories = listOf(
                CategoryEntity(
                    id = "links",
                    name = "Saved Links",
                    emoji = "🔖",
                    colorHex = "#26E07F", // Vault Green
                    isDefault = true,
                    timestamp = System.currentTimeMillis()
                ),
                CategoryEntity(
                    id = "notes",
                    name = "Notes",
                    emoji = "📝",
                    colorHex = "#D6ABFF", // Purple
                    isDefault = true,
                    timestamp = System.currentTimeMillis()
                ),
                CategoryEntity(
                    id = "images",
                    name = "Images",
                    emoji = "🖼️",
                    colorHex = "#7A92C2", // Blue
                    isDefault = true,
                    timestamp = System.currentTimeMillis()
                ),
                CategoryEntity(
                    id = "pdfs",
                    name = "PDFs",
                    emoji = "📄",
                    colorHex = "#FFE6A6", // Yellow
                    isDefault = true,
                    timestamp = System.currentTimeMillis()
                )
            )

            database.categoryDao().insertCategories(defaultCategories)
        }
    }
}