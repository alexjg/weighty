package me.memoryandthought.weighty

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.fragment.NavHostFragment

/**
 * List of exercises
 */
class WeightyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true

        val ft = supportFragmentManager.beginTransaction()
        val fragment = NavHostFragment.create(R.navigation.nav_graph)
        ft.add(android.R.id.content, fragment)
        ft.setPrimaryNavigationFragment(fragment)
        ft.commit()
    }

}
