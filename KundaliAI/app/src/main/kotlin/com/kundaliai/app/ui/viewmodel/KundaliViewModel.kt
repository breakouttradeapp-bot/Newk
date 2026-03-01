package com.kundaliai.app.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kundaliai.app.data.models.ApiState
import com.kundaliai.app.data.models.KundaliRequest
import com.kundaliai.app.data.models.KundaliResult
import com.kundaliai.app.data.repository.KundaliRepository
import kotlinx.coroutines.launch

class KundaliViewModel : ViewModel() {

    private val repository = KundaliRepository()

    private val _kundaliState = MutableLiveData<ApiState<KundaliResult>>()
    val kundaliState: LiveData<ApiState<KundaliResult>> = _kundaliState

    private val _fullPredictionState = MutableLiveData<ApiState<KundaliResult>>()
    val fullPredictionState: LiveData<ApiState<KundaliResult>> = _fullPredictionState

    private var _currentResult: KundaliResult? = null
    val currentResult: KundaliResult? get() = _currentResult

    fun generateKundali(request: KundaliRequest) {
        viewModelScope.launch {
            _kundaliState.value = ApiState.Loading
            val result = repository.generateKundali(request)
            if (result is ApiState.Success) {
                _currentResult = result.data
            }
            _kundaliState.value = result
        }
    }

    fun generateFullPrediction(result: KundaliResult) {
        viewModelScope.launch {
            _fullPredictionState.value = ApiState.Loading
            val fullResult = repository.generateFullPrediction(result)
            if (fullResult is ApiState.Success) {
                _currentResult = fullResult.data
            }
            _fullPredictionState.value = fullResult
        }
    }

    fun validateForm(name: String, dob: String, tob: String, pob: String): String? {
        if (name.isBlank()) return "Please enter your name"
        if (name.length < 2) return "Name must be at least 2 characters"
        if (dob.isBlank()) return "Please select your date of birth"
        if (tob.isBlank()) return "Please enter your time of birth"
        if (pob.isBlank()) return "Please enter your place of birth"
        return null
    }
}
