package com.garmin.android.apps.connectiq.sample.comm2.model

data class InputData(var HRV: Double = 0.0, var meanX: Double = 0.0, var stdX: Double = 0.0, var magX: Double = 0.0,
                     var meanY: Double = 0.0, var stdY: Double = 0.0, var magY: Double = 0.0, var meanZ: Double = 0.0, var stdZ: Double = 0.0, var magZ: Double = 0.0,
                     var step: Int = 0, var distance: Boolean = false, var home: Boolean = false, var work: Boolean = false, var screenTime: Double = 0.0)