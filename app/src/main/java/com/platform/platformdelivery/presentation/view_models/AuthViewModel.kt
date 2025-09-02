package com.platform.platformdelivery.presentation.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.platform.platformdelivery.data.models.LoginResponse
import com.platform.platformdelivery.data.repositories.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.platform.platformdelivery.core.network.Result
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {


    private val _loginState = MutableStateFlow<Result<LoginResponse>>(Result.Idle)
    val loginState: StateFlow<Result<LoginResponse>> = _loginState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = Result.Loading
            _loginState.value = authRepository.login(email, password)
        }
    }


}