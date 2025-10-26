package me.memoryandthought.weighty

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat

/**
 * List of exercises
 */
class WeightyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_weighty)

        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true
    }

}
