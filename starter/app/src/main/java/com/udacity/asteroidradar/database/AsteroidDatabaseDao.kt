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
import com.udacity.asteroidradar.api.getTodayFormattedDate

@Dao
interface AsteroidDatabaseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg asteroids: Asteroid)

    @Query("SELECT * FROM asteroid_table ORDER BY close_approach_date DESC")
    fun getWeekly(): LiveData<List<Asteroid>>

    @Query("SELECT * FROM asteroid_table WHERE close_approach_date == :today ORDER BY close_approach_date ASC")
    fun getToday(today: String = getTodayFormattedDate()): LiveData<List<Asteroid>>

    @Query("SELECT * FROM asteroid_table ORDER BY close_approach_date ASC")
    fun getAll(): LiveData<List<Asteroid>>

    @Query("DELETE FROM asteroid_table WHERE close_approach_date <= :today")
    suspend fun deleteAll(today: String = getTodayFormattedDate())
}