package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.PathPilotDao
import com.example.data.entity.AttemptEntity
import com.example.data.entity.AuditLogEntity
import com.example.data.entity.ChapterEntity
import com.example.data.entity.ExamEntity
import com.example.data.entity.QuestionEntity
import com.example.data.entity.StudentDNAEntity
import com.example.data.entity.SubjectEntity
import com.example.data.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        ExamEntity::class,
        SubjectEntity::class,
        ChapterEntity::class,
        QuestionEntity::class,
        AttemptEntity::class,
        StudentDNAEntity::class,
        AuditLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pathPilotDao(): PathPilotDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "path_pilot_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
