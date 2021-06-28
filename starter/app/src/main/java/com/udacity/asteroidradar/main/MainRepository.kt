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

    suspend fun getNearEarthObjects(startDate: String?, endDate: String?): String? {
        return try {

            NasaApi.retrofitService.getNearEarthObjects(
                startDate,
                endDate,
                Constants.API_KEY
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getSavedAsteroids() =
        withContext(Dispatchers.IO) { database.asteroidDatabaseDao.getAll() }

    suspend fun refreshTodayAsteroids() {
        withContext(Dispatchers.IO) {

            val todayObjects = NasaApi.retrofitService.getNearEarthObjects(
                null,
                null,
                Constants.API_KEY
            )
            val todayAsteroids = parseAsteroidsJsonResult(
                JSONObject(todayObjects),
                getNextSevenDaysFormattedDates()
            )
            database.asteroidDatabaseDao.deleteAll()
            database.asteroidDatabaseDao.insertAll(*todayAsteroids.toTypedArray())
        }
    }
}