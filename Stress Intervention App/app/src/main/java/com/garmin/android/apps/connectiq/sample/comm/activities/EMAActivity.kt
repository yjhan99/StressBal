package com.garmin.android.apps.connectiq.sample.comm.activities

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
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

    private lateinit var r_group: RadioGroup
    private var emaResult = 2

    private lateinit var submit_btn: Button

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

        r_group = findViewById(R.id.radioGroup)
        submit_btn = findViewById(R.id.subimit_btn)

        DBhelper = AppDatabase.getInstance(this)

        r_group.setOnCheckedChangeListener{ group, checkedId ->
            when(checkedId) {
                R.id.rg_btn1 -> {
                    emaResult = 0
                    r_btn1.setTextColor(Color.parseColor("#ff7575"))
                    r_btn2.setTextColor(Color.parseColor("#454545"))
                    r_btn3.setTextColor(Color.parseColor("#454545"))
                    r_btn4.setTextColor(Color.parseColor("#454545"))
                    r_btn5.setTextColor(Color.parseColor("#454545"))

                }
                R.id.rg_btn2 -> {
                    emaResult = 0
                    r_btn2.setTextColor(Color.parseColor("#ff7575"))
                    r_btn1.setTextColor(Color.parseColor("#454545"))
                    r_btn3.setTextColor(Color.parseColor("#454545"))
                    r_btn4.setTextColor(Color.parseColor("#454545"))
                    r_btn5.setTextColor(Color.parseColor("#454545"))

                }
                R.id.rg_btn3 -> {
                    emaResult = 0
                    r_btn3.setTextColor(Color.parseColor("#ff7575"))
                    r_btn1.setTextColor(Color.parseColor("#454545"))
                    r_btn2.setTextColor(Color.parseColor("#454545"))
                    r_btn4.setTextColor(Color.parseColor("#454545"))
                    r_btn5.setTextColor(Color.parseColor("#454545"))

                }
                R.id.rg_btn4 -> {
                    emaResult = 1
                    r_btn4.setTextColor(Color.parseColor("#ff7575"))
                    r_btn1.setTextColor(Color.parseColor("#454545"))
                    r_btn2.setTextColor(Color.parseColor("#454545"))
                    r_btn3.setTextColor(Color.parseColor("#454545"))
                    r_btn5.setTextColor(Color.parseColor("#454545"))

                }
                R.id.rg_btn5 -> {
                    emaResult = 1
                    r_btn5.setTextColor(Color.parseColor("#ff7575"))
                    r_btn1.setTextColor(Color.parseColor("#454545"))
                    r_btn2.setTextColor(Color.parseColor("#454545"))
                    r_btn3.setTextColor(Color.parseColor("#454545"))
                    r_btn4.setTextColor(Color.parseColor("#454545"))
                }
            }
        }

        submit_btn.setOnClickListener {
            r_group.clearCheck()
            r_btn1.setTextColor(Color.parseColor("#454545"))
            r_btn2.setTextColor(Color.parseColor("#454545"))
            r_btn3.setTextColor(Color.parseColor("#454545"))
            r_btn4.setTextColor(Color.parseColor("#454545"))
            r_btn5.setTextColor(Color.parseColor("#454545"))

            val addRunnable = Runnable {
                DBhelper.labelDAO().insertLabelData(System.currentTimeMillis(), emaResult)
            }
            val thread = Thread(addRunnable)
            thread.start()
            Toast.makeText(applicationContext, "Your answer is stored...", Toast.LENGTH_SHORT).show()
        }
    }
}