package com.jery.feedformulation6.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import com.jery.feedformulation6.R
import com.jery.feedformulation6.ui.activities.MainActivity

/**
 * A [Fragment] subclass to show additional options and launch settings.
 * Use the [MoreFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MoreFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_more, container, false)
        // TODO: Handle More Fragment

        // Switch between Day and Night themes
        view.findViewById<View>(R.id.switchTheme).setOnClickListener { _: View? ->
            if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            else
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            MainActivity.chpNavBar.setItemSelected(R.id.home, true)   // Select the Home menu after changing App Theme
            MainActivity.prefsEditor.putInt("keyTheme", AppCompatDelegate.getDefaultNightMode()).commit()
        }



        return view
    }
}