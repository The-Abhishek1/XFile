package com.xcloak.xfile.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ProcessedFile::class], version = 1, exportSchema = false)
abstract class XFileDatabase : RoomDatabase() {
    abstract fun fileDao(): FileDao
}
