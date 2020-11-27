package com.example.studydemo.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.studydemo.R
import kotlinx.android.synthetic.main.activity_lottie.*

/**
 *
 * Description: lottie 动画
 * Author: glp
 * CreateDate: 2020-06-02
 */
class LottieActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lottie)

        lottie.setAnimation("short_login_video.json")
    }
}