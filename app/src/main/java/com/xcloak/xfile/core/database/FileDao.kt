package com.xcloak.xfile.core.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FileDao {
    @Query("SELECT * FROM processed_files ORDER BY timestamp DESC")
    fun getAllProcessedFiles(): Flow<List<ProcessedFile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: ProcessedFile)

    @Delete
    suspend fun deleteFile(file: ProcessedFile)

    @Query("DELETE FROM processed_files")
    suspend fun clearHistory()
}
