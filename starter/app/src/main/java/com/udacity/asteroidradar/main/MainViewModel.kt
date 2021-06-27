package com.udacity.asteroidradar.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.PictureOfDay
import com.udacity.asteroidradar.api.NasaApi
import com.udacity.asteroidradar.api.getNextSevenDaysFormattedDates
import com.udacity.asteroidradar.api.getTodayFormattedDate
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import com.udacity.asteroidradar.database.AsteroidDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val asteroidDao = AsteroidDatabase.getInstance(application).asteroidDatabaseDao
    private val _nearObjects = MutableLiveData<List<Asteroid>>()
    private val _pictureOfDay = MutableLiveData<PictureOfDay?>()
    private val _navigateToAsteroidDetail = MutableLiveData<Asteroid?>()

    val nearObjects: LiveData<List<Asteroid>>
        get() = _nearObjects
    val pictureOfDay: LiveData<PictureOfDay?>
        get() = _pictureOfDay
    val navigateToAsteroidDetail: LiveData<Asteroid?>
        get() = _navigateToAsteroidDetail

    init {
        updateFilter(Constants.NasaApiFilter.SHOW_SAVED)
        getPictureOfDay()
    }

    fun updateFilter(filter: Constants.NasaApiFilter) {

        viewModelScope.launch(Dispatchers.IO) {
            when (filter) {

                Constants.NasaApiFilter.SHOW_WEEK -> {
                    getNearEarthObjects(null, null)?.let {

                        _nearObjects.postValue(
                            parseAsteroidsJsonResult(
                                JSONObject(it),
                                getNextSevenDaysFormattedDates()
                            )
                        )
                    }
                }
                Constants.NasaApiFilter.SHOW_TODAY -> {
                    getNearEarthObjects(getTodayFormattedDate(), getTodayFormattedDate())?.let {

                        _nearObjects.postValue(
                            parseAsteroidsJsonResult(
                                JSONObject(it),
                                listOf(getTodayFormattedDate())
                            )
                        )
                    }
                }
                Constants.NasaApiFilter.SHOW_SAVED -> {
                    _nearObjects.postValue(asteroidDao.getAll())
                }
            }
        }
    }

    fun onAsteroidClicked(asteroid: Asteroid) {
        _navigateToAsteroidDetail.value = asteroid
    }

    fun onAsteroidDetailNavigated() {
        _navigateToAsteroidDetail.value = null
    }

    private suspend fun getNearEarthObjects(startDate: String?, endDate: String?): String? {

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

    private fun getPictureOfDay() {
        viewModelScope.launch(Dispatchers.IO) {
            try {

                val imageOfTheDay = NasaApi.retrofitService.getImageOfTheDay(Constants.API_KEY)
                _pictureOfDay.postValue(
                    when (imageOfTheDay.mediaType) {
                        "image" -> imageOfTheDay
                        else -> null
                    }
                )
            } catch (e: Exception) {
                _pictureOfDay.postValue(null)
            }
        }
    }
}