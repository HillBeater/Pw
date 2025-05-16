package com.hillbeater.pwcontactpull.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.hillbeater.pwcontactpull.R
import com.hillbeater.pwcontactpull.fragment.ContactFragment
import com.hillbeater.pwcontactpull.fragment.FavouriteFragment
import com.hillbeater.pwcontactpull.fragment.RecentFragment

class HomeActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        openFragment(ContactFragment())
        bottomNavigationView.selectedItemId = R.id.menu_contact

        bottomNavigationView.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.menu_contact -> {
                    openFragment(ContactFragment())
                    true
                }
                else -> {
                    false
                }
            }
        }

    }

    private fun openFragment(fragment: androidx.fragment.app.Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
