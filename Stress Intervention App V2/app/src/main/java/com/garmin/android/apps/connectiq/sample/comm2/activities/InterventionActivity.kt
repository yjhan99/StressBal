package com.garmin.android.apps.connectiq.sample.comm2.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.garmin.android.apps.connectiq.sample.comm2.R
import org.w3c.dom.Text
import java.util.*

class InterventionActivity : AppCompatActivity() {
    private lateinit var toolbar: Toolbar

    private lateinit var numberText: TextView
    private var count = 9

    private var timer = Timer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intervention)

        toolbar = findViewById(R.id.intervention_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        updateUI()
    }

    private fun updateUI() {
        numberText = findViewById(R.id.number)
        val intent = Intent(this, InterventionActivity2::class.java)

        timer = kotlin.concurrent.timer(period = 1000) {
            if(count == 1) {
                startActivity(intent)
                finish()
            }
            runOnUiThread {
                numberText.text = count.toString()
            }
            count -= 1
        }
    }
}