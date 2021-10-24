package com.example.newcommand2.utils

import android.util.Log


fun log(msg : String){
    Log.i("ccc","ccc ${msg}")
}

class ConstantsCalculations {

    companion object{

        const val BASE_URL = "https://api.openweathermap.org/"
        const val API_KEY = "ac79fea59e9d15377b787a610a29b784"
        const val API_KEY_RESERVE1 = "1ae4e8cac37ab81430d67c851f915a1e"

        const val DUAL_INDICATOR_SIZEOFFACTORS = 32
        const val SOLAR_PANEL_AREA_FOR_1W =0.00648F

        var CURRENT_TIME_OF_DAY = DayPeriod(TypeOfSky.DAY,true)

        @kotlin.jvm.JvmField
        var switcherMap: Boolean = true

        @JvmField
        var bitmapMain : Bitmap? = null
        //var switcherMap: Boolean? = true
        val WIDTH_CUSTOM_VIEW: Float by lazy { WeatherAnim.WIDTH }
        val HEIGHT_CUSTOM_VIEW: Float by lazy { WeatherAnim.HEIGHT }

        var is_TYPE_ROTATION_VECTOR_SELECTED = PreferenceMaestro.isTypeRotationSensor
        var is_COMPASS_WORKING_FINE = false
        val is_LIGHT_MODE = PreferenceMaestro.isLightMode

        var is_PREMIUM = PreferenceMaestro.isPremiumStatus
        // for Notifications
        const val CHANNEL_ID = "SPXNotifChannel_IMPORTANT"
        const val CHANNEL_ID2 = "SPXNotifChannel2_REGULAR"
        const val CHANNEL_IDNum = 1
        const val CHANNEL_ID2Num = 2



    }
}