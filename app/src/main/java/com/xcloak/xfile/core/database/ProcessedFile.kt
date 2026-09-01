package com.xcloak.xfile.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "processed_files")
data class ProcessedFile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val timestamp: Long,
    val size: Long,
    val type: String, // "PDF" or "IMAGE"
    val tool: String, // "MERGE", "RESIZE", etc.
    val uri: String
)
