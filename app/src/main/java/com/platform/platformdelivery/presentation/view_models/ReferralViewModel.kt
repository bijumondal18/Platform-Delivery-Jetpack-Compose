package com.platform.platformdelivery.presentation.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.platform.platformdelivery.core.network.Result
import com.platform.platformdelivery.data.models.ReferralDetailsResponse
import com.platform.platformdelivery.data.repositories.ReferralRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ReferralViewModel(
    private val referralRepository: ReferralRepository = ReferralRepository()
) : ViewModel() {

    private val _referralCode = MutableStateFlow<String?>(null)
    val referralCode: StateFlow<String?> get() = _referralCode

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> get() = _error

    private val _referralDetailsState = MutableStateFlow<Result<ReferralDetailsResponse>>(Result.Idle)
    val referralDetailsState: StateFlow<Result<ReferralDetailsResponse>> get() = _referralDetailsState

    fun getReferralDetails() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _referralDetailsState.value = Result.Loading

            try {
                val result = referralRepository.getReferralDetails()
                _referralDetailsState.value = result

                when (result) {
                    is Result.Success -> {
                        _referralCode.value = result.data.data?.referralData?.code
                        _error.value = null
                    }
                    is Result.Error -> {
                        _error.value = result.message
                        _referralCode.value = null
                    }
                    else -> Unit
                }
            } catch (e: Exception) {
                _error.value = e.message
                _referralDetailsState.value = Result.Error(e.message ?: "Unknown error", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadReferralDetailsOnce() {
        if (_referralCode.value == null && !_isLoading.value) {
            getReferralDetails()
        }
    }
}

