package com.example.kotlin_weather_app.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    //isLoading state with initial value true
    private val _isLoading = MutableStateFlow(true);
    val isLoading = _isLoading.asStateFlow();

    init {
        viewModelScope.launch {
            //Delay to simulate some background processing like fetching data
            delay(3000)
            //After task is done set isLoading to false to hide splash screen
            _isLoading.value = false
        }
    }
}