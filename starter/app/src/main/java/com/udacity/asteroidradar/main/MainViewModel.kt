package com.udacity.asteroidradar.main

import android.app.Application
import androidx.lifecycle.*
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.PictureOfDay
import com.udacity.asteroidradar.database.AsteroidDatabase
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val mainRepository = MainRepository(AsteroidDatabase.getInstance(application))
    private val _asteroids = MutableLiveData<List<Asteroid>>()
    private val asteroidsObserver = Observer<List<Asteroid>> {
        _asteroids.value = it
    }
    private lateinit var asteroidsLiveData: LiveData<List<Asteroid>>
    private val _pictureOfDay = MutableLiveData<PictureOfDay?>()
    private val _loading = MutableLiveData(false)
    private val _navigateToAsteroidDetail = MutableLiveData<Asteroid?>()

    val asteroids: LiveData<List<Asteroid>>
        get() = _asteroids
    val pictureOfDay: LiveData<PictureOfDay?>
        get() = _pictureOfDay
    val loading: LiveData<Boolean>
        get() = _loading
    val navigateToAsteroidDetail: LiveData<Asteroid?>
        get() = _navigateToAsteroidDetail

    init {

        updateFilter(Constants.NasaApiFilter.SHOW_SAVED)
        viewModelScope.launch {
            _pictureOfDay.postValue(mainRepository.getPictureOfDay())
        }
    }

    override fun onCleared() {
        super.onCleared()
        asteroidsLiveData.removeObserver(asteroidsObserver)
    }

    fun updateFilter(filter: Constants.NasaApiFilter) {

        asteroidsLiveData = getSelection(filter)
        asteroidsLiveData.observeForever(asteroidsObserver)
    }

    private fun getSelection(filter: Constants.NasaApiFilter): LiveData<List<Asteroid>> {
        return when (filter) {

            Constants.NasaApiFilter.SHOW_WEEK -> {
                Transformations.map(mainRepository.getWeeklyAsteroids()) {
                    it
                }
            }
            Constants.NasaApiFilter.SHOW_TODAY -> {
                Transformations.map(mainRepository.getTodayAsteroids()) {
                    it
                }
            }
            Constants.NasaApiFilter.SHOW_SAVED -> {
                Transformations.map(mainRepository.getSavedAsteroids()) {
                    it
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