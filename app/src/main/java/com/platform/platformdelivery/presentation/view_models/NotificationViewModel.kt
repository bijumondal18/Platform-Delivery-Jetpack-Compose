package com.platform.platformdelivery.presentation.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.platform.platformdelivery.core.network.Result
import com.platform.platformdelivery.data.models.Notification
import com.platform.platformdelivery.data.repositories.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NotificationViewModel(
    private val notificationRepository: NotificationRepository = NotificationRepository(),
) : ViewModel() {

    // All notifications state flows
    private val _allNotifications = MutableStateFlow<List<Notification>>(emptyList())
    val allNotifications: StateFlow<List<Notification>> get() = _allNotifications

    private val _isAllNotificationsLoading = MutableStateFlow(false)
    val isAllNotificationsLoading: StateFlow<Boolean> get() = _isAllNotificationsLoading

    private val _allNotificationsEmpty = MutableStateFlow(false)
    val allNotificationsEmpty: StateFlow<Boolean> get() = _allNotificationsEmpty

    private val _noMoreAllNotificationsAvailable = MutableStateFlow(false)
    val noMoreAllNotificationsAvailable: StateFlow<Boolean> get() = _noMoreAllNotificationsAvailable

    private val _allNotificationsError = MutableStateFlow<String?>(null)
    val allNotificationsError: StateFlow<String?> get() = _allNotificationsError

    // Unread notifications state flows
    private val _unreadNotifications = MutableStateFlow<List<Notification>>(emptyList())
    val unreadNotifications: StateFlow<List<Notification>> get() = _unreadNotifications

    private val _isUnreadNotificationsLoading = MutableStateFlow(false)
    val isUnreadNotificationsLoading: StateFlow<Boolean> get() = _isUnreadNotificationsLoading

    private val _unreadNotificationsEmpty = MutableStateFlow(false)
    val unreadNotificationsEmpty: StateFlow<Boolean> get() = _unreadNotificationsEmpty

    private val _noMoreUnreadNotificationsAvailable = MutableStateFlow(false)
    val noMoreUnreadNotificationsAvailable: StateFlow<Boolean> get() = _noMoreUnreadNotificationsAvailable

    private val _unreadNotificationsError = MutableStateFlow<String?>(null)
    val unreadNotificationsError: StateFlow<String?> get() = _unreadNotificationsError

    // Pagination state
    private var currentAllNotificationsPage = 1
    private var currentUnreadNotificationsPage = 1
    private val perPage = 10

    var hasLoadedAllNotifications = false
    var hasLoadedUnreadNotifications = false

    fun loadAllNotificationsOnce() {
        if (!hasLoadedAllNotifications) {
            getAllNotifications(1)
            hasLoadedAllNotifications = true
        }
    }

    fun getAllNotifications(page: Int = 1) {
        viewModelScope.launch {
            if (page == 1) {
                _isAllNotificationsLoading.value = true
                _allNotifications.value = emptyList()
                _noMoreAllNotificationsAvailable.value = false
                _allNotificationsEmpty.value = false
                _allNotificationsError.value = null
            }

            try {
                val result = notificationRepository.getAllNotifications(page, perPage)

                when (result) {
                    is Result.Success -> {
                        val newNotifications = result.data.data?.data?.notifications ?: emptyList()
                        if (newNotifications.isEmpty()) {
                            if (page == 1) {
                                _allNotificationsEmpty.value = true
                            } else {
                                _noMoreAllNotificationsAvailable.value = true
                            }
                        } else {
                            if (page == 1) {
                                _allNotifications.value = newNotifications
                            } else {
                                _allNotifications.value = _allNotifications.value + newNotifications
                            }
                            _allNotificationsEmpty.value = false
                        }
                    }

                    is Result.Error -> {
                        _allNotificationsError.value = result.message
                    }

                    Result.Idle -> Unit
                    Result.Loading -> _isAllNotificationsLoading.value = true
                }
            } catch (e: Exception) {
                _allNotificationsError.value = e.message
            } finally {
                _isAllNotificationsLoading.value = false
                currentAllNotificationsPage = page
            }
        }
    }

    fun loadNextAllNotificationsPage() {
        if (!_noMoreAllNotificationsAvailable.value && !_isAllNotificationsLoading.value) {
            getAllNotifications(currentAllNotificationsPage + 1)
        }
    }

    fun loadUnreadNotificationsOnce() {
        if (!hasLoadedUnreadNotifications) {
            getUnreadNotifications(1)
            hasLoadedUnreadNotifications = true
        }
    }

    fun getUnreadNotifications(page: Int = 1) {
        viewModelScope.launch {
            if (page == 1) {
                _isUnreadNotificationsLoading.value = true
                _unreadNotifications.value = emptyList()
                _noMoreUnreadNotificationsAvailable.value = false
                _unreadNotificationsEmpty.value = false
                _unreadNotificationsError.value = null
            }

            try {
                val result = notificationRepository.getUnreadNotifications(page, perPage)

                when (result) {
                    is Result.Success -> {
                        val newNotifications = result.data.data?.data?.notifications ?: emptyList()
                        if (newNotifications.isEmpty()) {
                            if (page == 1) {
                                _unreadNotificationsEmpty.value = true
                            } else {
                                _noMoreUnreadNotificationsAvailable.value = true
                            }
                        } else {
                            if (page == 1) {
                                _unreadNotifications.value = newNotifications
                            } else {
                                _unreadNotifications.value = _unreadNotifications.value + newNotifications
                            }
                            _unreadNotificationsEmpty.value = false
                        }
                    }

                    is Result.Error -> {
                        _unreadNotificationsError.value = result.message
                    }

                    Result.Idle -> Unit
                    Result.Loading -> _isUnreadNotificationsLoading.value = true
                }
            } catch (e: Exception) {
                _unreadNotificationsError.value = e.message
            } finally {
                _isUnreadNotificationsLoading.value = false
                currentUnreadNotificationsPage = page
            }
        }
    }

    fun loadNextUnreadNotificationsPage() {
        if (!_noMoreUnreadNotificationsAvailable.value && !_isUnreadNotificationsLoading.value) {
            getUnreadNotifications(currentUnreadNotificationsPage + 1)
        }
    }

    fun resetAllNotificationsFlag() {
        hasLoadedAllNotifications = false
    }

    fun resetUnreadNotificationsFlag() {
        hasLoadedUnreadNotifications = false
    }

    fun markNotificationAsRead(notificationId: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val result = notificationRepository.markNotificationAsRead(notificationId)
                when (result) {
                    is Result.Success -> {
                        // Update local state - mark notification as read in both lists
                        val currentTime = java.text.SimpleDateFormat(
                            "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'",
                            java.util.Locale.getDefault()
                        ).format(java.util.Date())
                        
                        // Update in all notifications list (using notifiableId)
                        _allNotifications.value = _allNotifications.value.map { notification ->
                            if (notification.notifiableId?.toString() == notificationId) {
                                notification.copy(readAt = currentTime)
                            } else {
                                notification
                            }
                        }
                        
                        // Remove from unread notifications list if present (using notifiableId)
                        _unreadNotifications.value = _unreadNotifications.value.filter { 
                            it.notifiableId?.toString() != notificationId 
                        }
                        
                        // Update empty state for unread if needed
                        if (_unreadNotifications.value.isEmpty()) {
                            _unreadNotificationsEmpty.value = true
                        }
                        
                        onSuccess()
                    }
                    is Result.Error -> {
                        // Handle error silently or show toast
                    }
                    else -> Unit
                }
            } catch (e: Exception) {
                // Handle exception silently
            }
        }
    }
}

