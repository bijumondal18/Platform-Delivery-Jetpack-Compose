package com.platform.platformdelivery.presentation.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.platform.platformdelivery.data.models.BaseResponse
import com.platform.platformdelivery.data.models.LoginResponse
import com.platform.platformdelivery.data.repositories.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.platform.platformdelivery.core.network.Result
import com.platform.platformdelivery.data.local.TokenManager
import com.platform.platformdelivery.data.remote.RetrofitClient
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
) : ViewModel() {

    private val _loginState = MutableStateFlow<Result<LoginResponse>>(Result.Idle)
    val loginState: StateFlow<Result<LoginResponse>> = _loginState

    private val _forgotPasswordState = MutableStateFlow<Result<BaseResponse>>(Result.Idle)
    val forgotPasswordState: StateFlow<Result<BaseResponse>> = _forgotPasswordState

    private val _verifyOtpState = MutableStateFlow<Result<BaseResponse>>(Result.Idle)
    val verifyOtpState: StateFlow<Result<BaseResponse>> = _verifyOtpState

    private val _resendOtpState = MutableStateFlow<Result<BaseResponse>>(Result.Idle)
    val resendOtpState: StateFlow<Result<BaseResponse>> = _resendOtpState

    private val _resetPasswordState = MutableStateFlow<Result<BaseResponse>>(Result.Idle)
    val resetPasswordState: StateFlow<Result<BaseResponse>> = _resetPasswordState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = Result.Loading
            val result = authRepository.login(email, password)
            _loginState.value = result
            if (result is Result.Success) {
                result.data?.data?.token?.let {
                    (RetrofitClient.tokenProvider as TokenManager).saveAccessToken(it)
                }
                result.data?.data?.user?.id?.let {
                    (RetrofitClient.tokenProvider as TokenManager).saveUserId(it)
                }
                result.data?.data?.user?.name?.let {
                    (RetrofitClient.tokenProvider as TokenManager).saveName(it)
                }
                result.data?.data?.user?.email?.let {
                    (RetrofitClient.tokenProvider as TokenManager).saveEmail(it)
                }
                result.data?.data?.user?.profilePic?.let {
                    (RetrofitClient.tokenProvider as TokenManager).saveProfilePic(it)
                }

                (RetrofitClient.tokenProvider as TokenManager).setIsLoggedIn(true)
            }
        }
    }

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            _forgotPasswordState.value = Result.Loading
            val result = authRepository.forgotPassword(email)
            _forgotPasswordState.value = result
        }
    }

    fun resetForgotPasswordState() {
        _forgotPasswordState.value = Result.Idle
    }

    fun verifyOtp(userId: String, otp: String) {
        viewModelScope.launch {
            _verifyOtpState.value = Result.Loading
            val result = authRepository.verifyOtp(userId, otp)
            _verifyOtpState.value = result
        }
    }

    fun resetVerifyOtpState() {
        _verifyOtpState.value = Result.Idle
    }

    fun resendOtp(email: String) {
        viewModelScope.launch {
            _resendOtpState.value = Result.Loading
            val result = authRepository.resendOtp(email)
            _resendOtpState.value = result
        }
    }

    fun resetResendOtpState() {
        _resendOtpState.value = Result.Idle
    }

    fun resetPassword(token: String, password: String) {
        viewModelScope.launch {
            _resetPasswordState.value = Result.Loading
            val result = authRepository.resetPassword(token, password)
            _resetPasswordState.value = result
        }
    }

    fun resetResetPasswordState() {
        _resetPasswordState.value = Result.Idle
    }

}