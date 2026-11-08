package com.example.data.db

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

typealias Projects = ProjectEntity
typealias JDKInstallations = JdkInstallationEntity
typealias LearningProgress = LearningProgressEntity

@Entity(tableName = "jdk_installations")
data class JdkInstallationEntity(
    @PrimaryKey val versionId: String, // e.g., "JDK_1_2", "JDK_8", "JDK_17", "JDK_21", "JDK_25"
    val displayName: String,
    val majorNumber: Int, // 0 for 1.0, 1 for 1.1, 2 for 1.2, 3 for 1.3, 4 for 1.4, 5..25+
    val versionString: String, // e.g. "1.2.2_017", "1.8.0_412", "21.0.4", "25.0.1"
    val vendor: String, // Sun Microsystems, Oracle, Eclipse Adoptium, OpenJDK
    val javaHome: String,
    val compilerPath: String,
    val runtimePath: String,
    val architecture: String, // "32-bit x86 (Legacy VM)" / "64-bit x86_64 / ARM64"
    val bytecodeVersion: String, // e.g. "46.0 (0x2E)", "52.0 (0x34)", "69.0 (0x45)"
    val status: String, // "Native Runtime Detected", "Configured Toolchain", "Legacy VM Container", "Unavailable / Needs Binary"
    val compatibilityMode: String, // Explicit distinction of Language / API / Bytecode / Actual Compiler / Actual Runtime
    val supportedCompilerFlags: String, // e.g. "-target 1.2 -bootclasspath rt.jar -classpath -d" or "--release 21 -classpath -d"
    val releaseYear: Int,
    val isDefault: Boolean = false,
    val isCustomAdded: Boolean = false
)

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val jdkVersionId: String, // Project-specific JDK!
    val packageName: String,
    val mainClass: String,
    val templateType: String,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "source_files")
data class SourceFileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val fileName: String,
    val packageName: String,
    val content: String,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "learning_progress")
data class LearningProgressEntity(
    @PrimaryKey val itemId: String,
    val moduleType: String = "TUTORIAL", // "TUTORIAL", "PRACTICE_LAB", "QUIZ"
    val chapterOrCategory: String,
    val targetJdkVersion: String = "JDK_1_2",
    val isCompleted: Boolean = false,
    val isBookmarked: Boolean = false,
    val masteryScore: Int = 100,
    val timeSpentMinutes: Int = 15,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "lesson_progress")
data class LessonProgressEntity(
    @PrimaryKey val lessonId: String,
    val chapterId: String,
    val isCompleted: Boolean = false,
    val isBookmarked: Boolean = false,
    val timeSpentMinutes: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "quiz_results")
data class QuizResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val quizId: String,
    val quizTitle: String,
    val targetCategory: String,
    val score: Int,
    val totalQuestions: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "execution_history")
data class ExecutionHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectName: String,
    val fileName: String,
    val jdkVersionId: String,
    val status: String, // "COMPILED & EXECUTED", "COMPILATION FAILED", "RUNTIME EXCEPTION", "COMPILED"
    val compilerCommand: String,
    val compilerOutput: String,
    val runtimeOutput: String,
    val bytecodeVersion: String,
    val executionTimeMs: Long,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "offline_resources")
data class OfflineResourceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String, // "HTML Tutorial", "API Docs", "PDF Reference", "Code Snippet", "Exercise Pack"
    val targetJdkVersion: String,
    val resourcePath: String,
    val status: String, // "Verified Offline", "Imported Bundle"
    val attribution: String,
    val importedAt: Long = System.currentTimeMillis()
)

@Dao
interface StudioDao {
    // JDK Installations
    @Query("SELECT * FROM jdk_installations ORDER BY majorNumber ASC")
    fun getAllJdks(): Flow<List<JdkInstallationEntity>>

    @Query("SELECT * FROM jdk_installations ORDER BY majorNumber ASC")
    suspend fun getAllJdksOnce(): List<JdkInstallationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJdks(jdks: List<JdkInstallationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJdk(jdk: JdkInstallationEntity)

    @Query("DELETE FROM jdk_installations WHERE versionId = :versionId")
    suspend fun deleteJdk(versionId: String)

    @Query("UPDATE jdk_installations SET isDefault = CASE WHEN versionId = :versionId THEN 1 ELSE 0 END")
    suspend fun setDefaultJdk(versionId: String)

    // Projects
    @Query("SELECT * FROM projects ORDER BY updatedAt DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects ORDER BY updatedAt DESC")
    suspend fun getAllProjectsOnce(): List<ProjectEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity): Long

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Query("UPDATE projects SET jdkVersionId = :jdkVersionId, updatedAt = :timestamp WHERE id = :projectId")
    suspend fun updateProjectJdk(projectId: Long, jdkVersionId: String, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM projects WHERE id = :projectId")
    suspend fun deleteProject(projectId: Long)

    // Source Files
    @Query("SELECT * FROM source_files WHERE projectId = :projectId ORDER BY fileName ASC")
    fun getFilesForProject(projectId: Long): Flow<List<SourceFileEntity>>

    @Query("SELECT * FROM source_files WHERE projectId = :projectId ORDER BY fileName ASC")
    suspend fun getFilesForProjectOnce(projectId: Long): List<SourceFileEntity>

    @Query("SELECT * FROM source_files ORDER BY updatedAt DESC")
    fun getAllSourceFiles(): Flow<List<SourceFileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSourceFile(file: SourceFileEntity): Long

    @Update
    suspend fun updateSourceFile(file: SourceFileEntity)

    @Query("DELETE FROM source_files WHERE id = :fileId")
    suspend fun deleteSourceFile(fileId: Long)

    @Query("DELETE FROM source_files WHERE projectId = :projectId")
    suspend fun deleteFilesForProject(projectId: Long)

    // LearningProgress & LessonProgress
    @Query("SELECT * FROM learning_progress ORDER BY updatedAt DESC")
    fun getAllLearningProgress(): Flow<List<LearningProgressEntity>>

    @Query("SELECT * FROM learning_progress ORDER BY updatedAt DESC")
    suspend fun getAllLearningProgressOnce(): List<LearningProgressEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLearningProgress(progress: LearningProgressEntity)

    @Query("SELECT * FROM lesson_progress")
    fun getAllLessonProgress(): Flow<List<LessonProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLessonProgress(progress: LessonProgressEntity)

    // Quiz Results
    @Query("SELECT * FROM quiz_results ORDER BY timestamp DESC")
    fun getAllQuizResults(): Flow<List<QuizResultEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizResult(result: QuizResultEntity)

    // Execution History
    @Query("SELECT * FROM execution_history ORDER BY timestamp DESC LIMIT 40")
    fun getRecentExecutions(): Flow<List<ExecutionHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExecution(entry: ExecutionHistoryEntity)

    @Query("DELETE FROM execution_history")
    suspend fun clearExecutionHistory()

    // Offline Resources
    @Query("SELECT * FROM offline_resources ORDER BY importedAt DESC")
    fun getAllResources(): Flow<List<OfflineResourceEntity>>

    @Query("SELECT * FROM offline_resources ORDER BY importedAt DESC")
    suspend fun getAllResourcesOnce(): List<OfflineResourceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResources(resources: List<OfflineResourceEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResource(resource: OfflineResourceEntity)

    @Query("DELETE FROM offline_resources WHERE id = :id")
    suspend fun deleteResource(id: Long)
}

@Database(
    entities = [
        JdkInstallationEntity::class,
        ProjectEntity::class,
        SourceFileEntity::class,
        LearningProgressEntity::class,
        LessonProgressEntity::class,
        QuizResultEntity::class,
        ExecutionHistoryEntity::class,
        OfflineResourceEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class StudioDatabase : RoomDatabase() {
    abstract fun studioDao(): StudioDao

    companion object {
        @Volatile
        private var INSTANCE: StudioDatabase? = null

        fun getInstance(context: Context): StudioDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StudioDatabase::class.java,
                    "universal_java_studio.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
