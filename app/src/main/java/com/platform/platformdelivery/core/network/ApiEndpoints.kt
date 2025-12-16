package com.platform.platformdelivery.core.network

object ApiConfig {
    const val version = "v1.15.0"

    //    const val baseUrl = "https://platformdelivery.app/platform/public/api/driver/"
    const val baseUrl = "https://platform.hannastransport.com/platform/public/api/driver/"
    const val apiVersion = "https://platformdelivery.app/platform/public/api/api_version"
}

object AuthEndpoints {
    const val login = "login"
    const val register = "signup"
    const val resendOtp = "resend-otp"
    const val logout = "logout"
    const val forgotPassword = "forgotPassword"
    const val resetPassword = "resetPassword"
    const val verifyOtp = "verify-otp"
}

object RouteEndpoints {
    const val availableRoutes = "available-routes"
    const val acceptRoute = "accept-route"
    const val routeDetails = "route-details"
    const val myRoutes = "my-routes"
    const val routeHistory = "route-history"
    const val cancelRoute = "cancel-route"
    const val skipRoute = "skip-waypoint"
    const val endDelivery = "route-mark-end-delivery-with-time"
    const val completeTrip = "complete-trip-with-time"
    const val tripStartTime = "trip-start-time"
    const val validateLocation = "validate-delivery-location"
    const val locationCheckPickup = "location-check-pickup"
    const val updateCurrentLocation = "update-current-location"
    const val routeDeliveryWithOptions = "route-delivery-with-options"
}

object ProfileEndpoints {
    const val driverDetails = "details"
    const val updateProfile = "update-profile"
    const val deleteProfile = "delete-profile"
    const val getStateList = "state-list"
}

object EarningsEndpoints {
    const val totalEarnings = "total-earnings"
    const val transactionHistory = "get-transaction-history"
    const val instantPayoutEnabled = "instant-payout"
}

object NotificationEndpoints {
    const val allNotifications = "all-notifications"
    const val unreadNotifications = "total-unreadnotifications"
    const val markAllAsRead = "mark-all-as-read"
}

object ChatEndpoints {
    const val sendMessage = "send-msg"
    const val generateToken = "geneate-token"
    const val getConversation = "get-conversation"
    const val checkSupportExist = "check-support-exist"
    const val startConversation = "start-conversation"
    const val endConversation = "end-conversation"
    const val readConversation = "read-conversation"
    const val getMessageCount = "get-msg-count"
}

object ReferralEndpoints {
    const val referralDetails = "get-referral-code"
    const val referralsList = "get-referrals-list"
}

object PaymentEndpoints {
    const val stripeValidation = "geneate-stripe-link"
}

object VehicleEndpoints {
    const val vehicleLoaded = "vehicle-loaded"
}