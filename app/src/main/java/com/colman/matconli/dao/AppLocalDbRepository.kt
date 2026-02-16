package com.colman.matconli.dao

import androidx.room.Database
import androidx.room.RoomDatabase
import com.colman.matconli.model.Recipe
import com.colman.matconli.model.User

@Database(entities = [Recipe::class, User::class], version = 3, exportSchema = false)
abstract class AppLocalDbRepository : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun userDao(): UserDao
}