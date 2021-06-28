package com.udacity.asteroidradar.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.PictureOfDay
import com.udacity.asteroidradar.api.getNextSevenDaysFormattedDates
import com.udacity.asteroidradar.api.getTodayFormattedDate
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import com.udacity.asteroidradar.database.AsteroidDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val mainRepository = MainRepository(AsteroidDatabase.getInstance(application))
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
        viewModelScope.launch {
            _pictureOfDay.postValue(mainRepository.getPictureOfDay())
        }
    }

    fun updateFilter(filter: Constants.NasaApiFilter) {

        viewModelScope.launch(Dispatchers.IO) {
            when (filter) {

                Constants.NasaApiFilter.SHOW_WEEK -> {
                    mainRepository.getNearEarthObjects(null, null)?.let {

                        _nearObjects.postValue(
                            parseAsteroidsJsonResult(
                                JSONObject(it),
                                getNextSevenDaysFormattedDates()
                            )
                        )
                    } ?: run {
                        _nearObjects.postValue(listOf())
                    }
                }
                Constants.NasaApiFilter.SHOW_TODAY -> {
                    mainRepository.getNearEarthObjects(
                        getTodayFormattedDate(),
                        getTodayFormattedDate()
                    )?.let {

                        _nearObjects.postValue(
                            parseAsteroidsJsonResult(
                                JSONObject(it),
                                listOf(getTodayFormattedDate())
                            )
                        )
                    } ?: run {
                        _nearObjects.postValue(listOf())
                    }
                }
                Constants.NasaApiFilter.SHOW_SAVED -> {
                    _nearObjects.postValue(mainRepository.getSavedAsteroids())
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
}