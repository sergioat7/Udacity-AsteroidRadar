/*
 * Copyright (c) 2021 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 16/6/2021
 */

package com.udacity.asteroidradar.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.udacity.asteroidradar.Asteroid

@Dao
interface AsteroidDatabaseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(asteroids: List<Asteroid>)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(asteroid: Asteroid)

    @Query("SELECT * FROM asteroid_table ORDER BY close_approach_date DESC")
    fun getAll(): LiveData<List<Asteroid>>

    @Delete
    suspend fun delete(asteroids: List<Asteroid>)
}