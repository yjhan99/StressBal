package com.garmin.android.apps.connectiq.sample.comm.activities

import android.os.Bundle
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.garmin.android.apps.connectiq.sample.comm.R
import com.garmin.android.apps.connectiq.sample.comm.roomdb.AppDatabase

class EMAActivity: AppCompatActivity() {
    private lateinit var toolbar: Toolbar
    private lateinit var r_btn1: RadioButton
    private lateinit var r_btn2: RadioButton
    private lateinit var r_btn3: RadioButton
    private lateinit var r_btn4: RadioButton
    private lateinit var r_btn5: RadioButton

    private var emaResult = 2

    private lateinit var DBhelper: AppDatabase

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

        DBhelper = AppDatabase.getInstance(this)

        var currentTime = System.currentTimeMillis()

        if (r_btn1.isChecked() || r_btn2.isChecked() || r_btn3.isChecked()) {
            //Not stressed
            emaResult = 0
            val addRunnable = Runnable {
                DBhelper.labelDAO().insertLabelData(System.currentTimeMillis(), emaResult)
            }
            val thread = Thread(addRunnable)
            thread.start()
            Toast.makeText(applicationContext, "Your answer is stored...", Toast.LENGTH_SHORT).show()
        }
        else {
            //Stressed
            emaResult = 1
            val addRunnable = Runnable {
                DBhelper.labelDAO().insertLabelData(System.currentTimeMillis(), emaResult)
            }
            val thread = Thread(addRunnable)
            thread.start()
            Toast.makeText(applicationContext, "Your answer is stored...", Toast.LENGTH_SHORT).show()
        }
    }
}