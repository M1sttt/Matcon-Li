package com.colman.matconli.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.colman.matconli.model.Recipe

@Dao
interface RecipeDao {

    @Query("SELECT * FROM Recipe")
    fun getAll(): LiveData<MutableList<Recipe>>

    @Query("SELECT * FROM Recipe WHERE id = :id")
    fun getById(id: String): Recipe?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(recipe: Recipe)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(recipes: List<Recipe>)

    @Update
    fun update(recipe: Recipe)

    @Delete
    fun delete(recipe: Recipe)
}

