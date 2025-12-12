package com.platform.platformdelivery.presentation.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.platform.platformdelivery.core.network.Result
import com.platform.platformdelivery.data.models.DriverDetailsResponse
import com.platform.platformdelivery.data.models.User
import com.platform.platformdelivery.data.repositories.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val profileRepository: ProfileRepository = ProfileRepository(),
) : ViewModel() {

    private val _driverDetails = MutableStateFlow<User?>(null)
    val driverDetails: StateFlow<User?> get() = _driverDetails

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> get() = _error

    private val _driverDetailsState = MutableStateFlow<Result<DriverDetailsResponse>>(Result.Idle)
    val driverDetailsState: StateFlow<Result<DriverDetailsResponse>> get() = _driverDetailsState

    fun getDriverDetails() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _driverDetailsState.value = Result.Loading

            try {
                val result = profileRepository.getDriverDetails()
                _driverDetailsState.value = result

                when (result) {
                    is Result.Success -> {
                        _driverDetails.value = result.data.data?.user
                        _error.value = null
                    }
                    is Result.Error -> {
                        _error.value = result.message
                        _driverDetails.value = null
                    }
                    else -> Unit
                }
            } catch (e: Exception) {
                _error.value = e.message
                _driverDetailsState.value = Result.Error(e.message ?: "Unknown error", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadDriverDetailsOnce() {
        if (_driverDetails.value == null && !_isLoading.value) {
            getDriverDetails()
        }
    }
}

