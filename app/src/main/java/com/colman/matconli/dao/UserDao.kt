package com.colman.matconli.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.colman.matconli.model.User

@Dao
interface UserDao {

    @Query("SELECT * FROM User WHERE id = :id")
    fun getById(id: String): LiveData<User?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: User)
}

