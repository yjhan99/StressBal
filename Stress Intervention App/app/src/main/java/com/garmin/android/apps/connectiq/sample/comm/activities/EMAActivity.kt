package com.garmin.android.apps.connectiq.sample.comm.activities

import android.os.Bundle
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.garmin.android.apps.connectiq.sample.comm.R

class EMAActivity: AppCompatActivity() {
    private lateinit var toolbar: Toolbar
    private lateinit var r_btn1: RadioButton
    private lateinit var r_btn2: RadioButton
    private lateinit var r_btn3: RadioButton
    private lateinit var r_btn4: RadioButton
    private lateinit var r_btn5: RadioButton

    private var emaResult = 2


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ema)

        toolbar = findViewById(R.id.esm_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        r_btn1 = findViewById(R.id.rg_btn1)
        r_btn2 = findViewById(R.id.rg_btn2)
        r_btn3 = findViewById(R.id.rg_btn3)
        r_btn4 = findViewById(R.id.rg_btn4)
        r_btn5 = findViewById(R.id.rg_btn5)

        var currentTime = System.currentTimeMillis()
        var minus10Time = currentTime - 10*60*1000
        var plus10Time = currentTime + 10*60*1000
        //TODO 생각해보니 10분후의 데이터는 넣지 못함

        if (r_btn1.isChecked() || r_btn2.isChecked() || r_btn3.isChecked()) {
            emaResult = 0
            //Not stressed
        }
        else {
            emaResult = 1
            //stressed
        }
    }
}