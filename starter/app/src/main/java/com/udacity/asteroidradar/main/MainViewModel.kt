package com.udacity.asteroidradar.main

import android.app.Application
import androidx.lifecycle.*
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.PictureOfDay
import com.udacity.asteroidradar.api.NasaApi
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import com.udacity.asteroidradar.database.AsteroidDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val asteroidDao = AsteroidDatabase.getInstance(application).asteroidDatabaseDao
    private val _pictureOfDay = MutableLiveData<PictureOfDay?>()

    var nearObjects: LiveData<List<Asteroid>> = Transformations.map(asteroidDao.getAll()) {
        it
    }
    val pictureOfDay: LiveData<PictureOfDay?>
        get() = _pictureOfDay

    init {
        getNearEarthObjects()
        getPictureOfDay()
    }

    private fun getNearEarthObjects() {
        viewModelScope.launch(Dispatchers.IO) {
            try {

                val objects = NasaApi.retrofitService.getNearEarthObjects(Constants.API_KEY)
                val asteroids = parseAsteroidsJsonResult(JSONObject(objects))
                asteroidDao.insertAll(asteroids)
            } catch (e: Exception) {
                e.printStackTrace()
            }
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