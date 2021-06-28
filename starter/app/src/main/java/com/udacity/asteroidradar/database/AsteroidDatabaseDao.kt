/*
 * Copyright (c) 2021 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 16/6/2021
 */

package com.udacity.asteroidradar.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.udacity.asteroidradar.Asteroid

@Dao
interface AsteroidDatabaseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg asteroids: Asteroid)

    @Query("SELECT * FROM asteroid_table ORDER BY close_approach_date DESC")
    suspend fun getAll(): List<Asteroid>

    @Query("DELETE FROM asteroid_table")
    suspend fun deleteAll()
}