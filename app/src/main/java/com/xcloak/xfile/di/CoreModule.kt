package com.xcloak.xfile.di

import android.content.Context
import androidx.room.Room
import com.xcloak.xfile.core.ads.AdManager
import com.xcloak.xfile.core.billing.BillingManager
import com.xcloak.xfile.core.database.FileDao
import com.xcloak.xfile.core.database.XFileDatabase
import com.xcloak.xfile.core.image.ImageProcessor
import com.xcloak.xfile.core.pdf.PdfProcessor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoreModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): XFileDatabase = Room.databaseBuilder(
        context,
        XFileDatabase::class.java,
        "xfile_db"
    ).build()

    @Provides
    fun provideFileDao(db: XFileDatabase): FileDao = db.fileDao()

    @Provides
    @Singleton
    fun providePdfProcessor(
        @ApplicationContext context: Context
    ): PdfProcessor = PdfProcessor(context)

    @Provides
    @Singleton
    fun provideImageProcessor(
        @ApplicationContext context: Context
    ): ImageProcessor = ImageProcessor(context)

    @Provides
    @Singleton
    fun provideAdManager(
        @ApplicationContext context: Context
    ): AdManager = AdManager(context)

    @Provides
    @Singleton
    fun provideBillingManager(
        @ApplicationContext context: Context
    ): BillingManager = BillingManager(context)
}
