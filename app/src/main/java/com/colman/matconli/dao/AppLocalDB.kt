package com.colman.matconli.dao

import androidx.room.Room
import com.colman.matconli.base.MyApplication

object AppLocalDB {

    val db: AppLocalDbRepository = run {
        val context = MyApplication.Globals.appContext
            ?: throw IllegalStateException("Application context not available")
        
        Room.databaseBuilder(
            context,
            AppLocalDbRepository::class.java,
            "matkonli_db"
        )
            .fallbackToDestructiveMigration(true)
            .build()
    }
}

