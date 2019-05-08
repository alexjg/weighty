package me.memoryandthought.weighty

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton as MaterialFloatingActionButton

/**
 * List of exercises
 */
class WeightyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val ft = supportFragmentManager.beginTransaction()
        val fragment = NavHostFragment.create(R.navigation.nav_graph)
        ft.add(android.R.id.content, fragment)
        ft.setPrimaryNavigationFragment(fragment)
        ft.commit()
    }

}

