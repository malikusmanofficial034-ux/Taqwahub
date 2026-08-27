package com.example.data.room

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaqwaDao {
    @Query("SELECT * FROM downloaded_hadiths WHERE bookKey = :bookKey ORDER BY hadithNumber ASC")
    suspend fun getHadithsForBook(bookKey: String): List<HadithEntity>

    @Query("SELECT * FROM downloaded_hadiths WHERE bookKey = :bookKey AND chapterNumber = :chapterNumber ORDER BY hadithNumber ASC")
    suspend fun getHadithsForChapter(bookKey: String, chapterNumber: Int): List<HadithEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHadiths(hadiths: List<HadithEntity>)

    @Query("DELETE FROM downloaded_hadiths WHERE bookKey = :bookKey")
    suspend fun clearHadithsForBook(bookKey: String)

    @Query("SELECT COUNT(*) FROM downloaded_hadiths WHERE bookKey = :bookKey")
    suspend fun getHadithCountForBook(bookKey: String): Int

    @Query("SELECT * FROM tasks")
    fun getAllTasksFlow(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM bookmarks")
    fun getAllBookmarksFlow(): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM all_time_tasks")
    fun getAllTimeTasksFlow(): Flow<List<AllTimeTaskEntity>>

    @Query("SELECT * FROM user_stats WHERE id = 1")
    fun getUserStatsFlow(): Flow<UserStatsEntity?>

    @Query("SELECT * FROM tasks")
    suspend fun getAllTasksDirect(): List<TaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTasks(tasks: List<TaskEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: String)

    @Query("DELETE FROM all_time_tasks WHERE id = :id")
    suspend fun deleteAllTimeTaskById(id: String)

    @Query("DELETE FROM tasks")
    suspend fun clearTasks()

    @Query("DELETE FROM tasks WHERE isSystemTask = 1")
    suspend fun clearSystemTasks()

    @Query("DELETE FROM bookmarks")
    suspend fun clearBookmarks()

    @Query("DELETE FROM all_time_tasks")
    suspend fun clearAllTimeTasks()

    @Query("DELETE FROM user_stats")
    suspend fun clearUserStats()

    @Query("SELECT * FROM bookmarks")
    suspend fun getAllBookmarksDirect(): List<BookmarkEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTimeTask(log: AllTimeTaskEntity)

    @Query("SELECT * FROM user_stats WHERE id = 1")
    suspend fun getUserStatsDirect(): UserStatsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserStats(stats: UserStatsEntity)

    @Query("SELECT * FROM all_time_tasks")
    suspend fun getAllTimeTasksDirect(): List<AllTimeTaskEntity>

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteBookmarkById(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)
}
