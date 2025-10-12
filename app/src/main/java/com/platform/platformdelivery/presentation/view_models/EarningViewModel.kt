package com.platform.platformdelivery.presentation.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.platform.platformdelivery.core.network.Result
import com.platform.platformdelivery.data.models.EarningResponse
import com.platform.platformdelivery.data.repositories.EarningRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EarningViewModel(
    private val earningRepository: EarningRepository = EarningRepository(),
) : ViewModel() {

    private val _earningDetails = MutableStateFlow<EarningResponse?>(null)
    val earningDetails: StateFlow<EarningResponse?> get() = _earningDetails

    private val _isEarningDetailsLoading = MutableStateFlow(false)
    val isEarningDetailsLoading: StateFlow<Boolean> get() = _isEarningDetailsLoading

    private val _earningDetailsError = MutableStateFlow<String?>(null)
    val earningDetailsError: StateFlow<String?> get() = _earningDetailsError

    var hasLoadedEarningDetails = false


    fun loadEarningDetailsOnce() {
        if (!hasLoadedEarningDetails) {
            getEarningDetails()
            hasLoadedEarningDetails = true
        }
    }

    fun getEarningDetails() {
        viewModelScope.launch {
            _isEarningDetailsLoading.value = true
            _earningDetailsError.value = null
            _earningDetails.value = null

            try {
                val result = earningRepository.getEarningDetails()

                when (result) {
                    is com.platform.platformdelivery.core.network.Result.Success -> {
                        _earningDetails.value = result.data
                    }

                    is com.platform.platformdelivery.core.network.Result.Error -> {
                        _earningDetailsError.value = result.message
                    }

                    com.platform.platformdelivery.core.network.Result.Idle -> Unit
                    Result.Loading -> _isEarningDetailsLoading.value = true
                }
            } catch (e: Exception) {
                _earningDetailsError.value = e.message
            } finally {
                _isEarningDetailsLoading.value = false
            }
        }
    }

}