package com.platform.platformdelivery.presentation.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.platform.platformdelivery.core.network.Result
import com.platform.platformdelivery.data.repositories.ApiVersionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ApiVersionViewModel(
    private val repository: ApiVersionRepository = ApiVersionRepository()
) : ViewModel() {

    private val _apiVersionState = MutableStateFlow<Result<String>>(Result.Idle)
    val apiVersionState: StateFlow<Result<String>> = _apiVersionState.asStateFlow()

    fun getApiVersion() {
        viewModelScope.launch {
            _apiVersionState.value = Result.Loading
            try {
                val response = repository.getApiVersion()
                if (response.isSuccessful && response.body() != null) {
                    val baseUrl = response.body()!!.data?.baseUrl
                    if (!baseUrl.isNullOrEmpty()) {
                        _apiVersionState.value = Result.Success(baseUrl)
                    } else {
                        _apiVersionState.value = Result.Error("Base URL not found in response")
                    }
                } else {
                    _apiVersionState.value = Result.Error(response.message() ?: "Failed to get API version")
                }
            } catch (e: Exception) {
                _apiVersionState.value = Result.Error(e.message ?: "Unknown error occurred")
            }
        }
    }
}


