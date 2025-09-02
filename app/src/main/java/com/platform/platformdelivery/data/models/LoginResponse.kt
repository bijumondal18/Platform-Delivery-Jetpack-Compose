package com.platform.platformdelivery.data.models

data class LoginResponse(
    var success: Boolean?,
    var message: String?,
    var data: LoginData
)

data class LoginData(
    var msg: String?,
    var status: Boolean?,
    var token: String,
    val user: User
)

data class User(
    var id: Int,
    var name: String?,
    var first_name: String?,
    var last_name: String?,
    var email: String?,
    var status: String?,
    var phone: String?,
    var driver_id: String?,
    var profile_pic: String?,
    var fcm_token: String?,
    var lat: String?,
    var lng: String?,
    var average_ratting: Int?,
    var activation_count: Int?,
    var agora_id: String?,
    var agora_user_created: Int?,
    var stripe_id: String?,
    var stripe_charges_enabled: Int?,
    var instant_payout_enabled: Int?,
    var stripe_extarnal_bank_account_id: String?,
    var stripe_payout_interval: String?,
    var stripe_payout_anchor: String?,
    var barcode_status: String?,
    var is_map_active: String?,
    var is_location_active: String?,
    var is_sig_img_active: String?,
    var is_twilio_active: String?,
    var ssn: String?,
    var street: String?,
    var city: String?,
    var state: String?,
    var base_location: String?,
    var base_location_lat: String?,
    var base_location_lng: String?,
    var zip: String?,
    var ifsc: String?,
    var account_no: String?,
)