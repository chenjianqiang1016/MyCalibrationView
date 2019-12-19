package com.demo.mycalibrationview

import android.os.Bundle
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_main)

        studySetChToEnSv.setMySvOnClickListener(object : MySwitchView.MySvOnClickListener {
            override fun onClick(isOpen: Boolean) {

                Toast.makeText(this@MainActivity, "$isOpen", Toast.LENGTH_SHORT).show()

            }

        })

    }
}
