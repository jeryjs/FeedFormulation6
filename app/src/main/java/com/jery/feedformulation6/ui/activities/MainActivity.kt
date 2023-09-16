package com.jery.feedformulation6.ui.activities

import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.Animatable
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE
import androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN
import com.ismaeldivita.chipnavigation.ChipNavigationBar
import com.jery.feedformulation6.R
import com.jery.feedformulation6.ui.fragments.*


class MainActivity : AppCompatActivity(), ChipNavigationBar.OnItemSelectedListener {

    companion object {
        lateinit var chpNavBar: ChipNavigationBar
        lateinit var sharedPreferences: SharedPreferences
        lateinit var prefsEditor: SharedPreferences.Editor
        lateinit var mainActivity: MainActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        chpNavBar = findViewById(R.id.bottom_nav)!!
        chpNavBar.setOnItemSelectedListener(this)
        chpNavBar.setItemSelected(R.id.home, true)

        // assign valus to some lateinit vars
        sharedPreferences = getSharedPreferences("preferences", Context.MODE_PRIVATE)
        prefsEditor = sharedPreferences.edit()
        mainActivity = this

        // restore the last set theme
        AppCompatDelegate.setDefaultNightMode(sharedPreferences.getInt("keyTheme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM))
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onItemSelected(id: Int) {
        var selectedFragment: Fragment? = null
        when (id) {
            R.id.home -> selectedFragment = HomeFragment()
            R.id.feeds -> selectedFragment = FeedsFragment()
            R.id.info -> selectedFragment = InfoFragment()
            R.id.more -> selectedFragment = MoreFragment()
        }
//        chpNavBar.showBadge(id, id)


        if (selectedFragment != null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, selectedFragment)
                .setTransition(TRANSIT_FRAGMENT_FADE)
                .addToBackStack(null)
                .commit()
        }
    }
}
