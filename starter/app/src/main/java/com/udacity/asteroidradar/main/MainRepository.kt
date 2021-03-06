/*
 * Copyright (c) 2021 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 27/6/2021
 */

package com.udacity.asteroidradar.main

import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.PictureOfDay
import com.udacity.asteroidradar.api.NasaApi
import com.udacity.asteroidradar.api.getNextSevenDaysFormattedDates
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import com.udacity.asteroidradar.database.AsteroidDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class MainRepository(private val database: AsteroidDatabase) {

    suspend fun getPictureOfDay(): PictureOfDay? {
        return withContext(Dispatchers.IO) {
            try {

                val imageOfTheDay = NasaApi.retrofitService.getImageOfTheDay(Constants.API_KEY)
                when (imageOfTheDay.mediaType) {
                    "image" -> imageOfTheDay
                    else -> null
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    fun getWeeklyAsteroids() = database.asteroidDatabaseDao.getWeekly()

    fun getTodayAsteroids() = database.asteroidDatabaseDao.getToday()

    fun getSavedAsteroids() = database.asteroidDatabaseDao.getAll()

    suspend fun refreshTodayAsteroids() {
        withContext(Dispatchers.IO) {

            val objects = NasaApi.retrofitService.getNearEarthObjects(
                null,
                null,
                Constants.API_KEY
            )
            val asteroids = parseAsteroidsJsonResult(
                JSONObject(objects),
                getNextSevenDaysFormattedDates()
            )
            database.asteroidDatabaseDao.deleteAll()
            database.asteroidDatabaseDao.insertAll(*asteroids.toTypedArray())
        }
    }
}