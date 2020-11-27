package com.example.studydemo.activity

import android.os.Bundle
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import com.example.studydemo.R
import kotlinx.android.synthetic.main.activity_coordinator.*

class CoordinatorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coordinator)

        setSupportActionBar(toolbar)

        toolbar_layout.collapsedTitleGravity = Gravity.CENTER
        toolbar_layout.expandedTitleGravity = Gravity.LEFT
    }

}
