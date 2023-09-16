package com.jery.feedformulation6.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.jery.feedformulation6.*
import com.jery.feedformulation6.ui.activities.FeedsSelection

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        val btn = view.findViewById<Button>(R.id.start_button)
        btn.setOnClickListener {
            val intent = Intent(activity, FeedsSelection::class.java)
            startActivity(intent)
        }
        btn.performClick()
        return view
    }
}