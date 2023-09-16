package com.jery.feedformulation6.ui.activities

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.chaquo.python.PyException
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.jery.feedformulation6.R
import com.jery.feedformulation6.data.Optimize
import com.jery.feedformulation6.databinding.ActivityFeedsSelectionBinding
import com.jery.feedformulation6.ui.fragments.FeedsFragment

private const val EXPORT_FEEDS_REQUEST_CODE = 100
private const val IMPORT_FEEDS_REQUEST_CODE = 101

class FeedsSelection : AppCompatActivity() {
    private lateinit var binding: ActivityFeedsSelectionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedsSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Create instance of FeedsFragment
        val feedsFragment = FeedsFragment.newInstance(true)

        // Add the fragment to the fragment container
        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, feedsFragment)
            .commit()

        // start python
        if (! Python.isStarted()) {
            Python.start(AndroidPlatform(this));
        }

        // Set click listener for FAB
        binding.fabSelectFeeds.setOnClickListener {
            // Retrieve selected feeds from the FeedsFragment
            val selectedFeeds = feedsFragment.getSelectedFeeds()

            binding.fragmentContainer.visibility = View.GONE
            binding.tvLog.visibility = View.VISIBLE

            val obj = mutableListOf<Float>()    //cost
            val cp = mutableListOf<Float>()     //cp
            val tdn = mutableListOf<Float>()    //tdn
            val ca = mutableListOf<Float>()
            val ph = mutableListOf<Float>()
            val per = mutableListOf<Float>()

            var string = ""
            for ((index, feed) in selectedFeeds.withIndex()) {
                Log.d("FeedSelectionActivity", "Selected Feed: ${feed.name}")
                obj.add(feed.cost)
                cp.add(feed.details[1])
                tdn.add(feed.details[2])
                ca.add(feed.details[3])
                ph.add(feed.details[4])
                per.add(feed.percentage["cow"]!!)
                string += "x$index:\t\t${feed.name}\n"
            }
            binding.tvLog.text = string

            val data = mapOf("obj" to obj, "cp"  to cp, "tdn" to tdn, "ca"  to ca, "ph"  to ph, "per" to per)
            val dataa = Optimize(obj, cp, tdn, ca, ph, per)

            val py = Python.getInstance()
//            val module = py.getModule("plot")
            val solver = py.getModule("optimization")

            try {
//                val bytes = module.callAttr("plot", "5", "10")
//                    .toJava(ByteArray::class.java)
//                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
//                binding.imageview.setImageBitmap(bitmap)
                val pyobj = solver.callAttr("optimization", data)
                val result = pyobj.asList().map { it.toFloat() }

                string += "\n\nRESULT:\n"
                for ((index, value) in result.withIndex()) {
                    string += "x$index:\t\t$value\n"
                    binding.tvLog.text = string
                }
            } catch (e: PyException) {
                e.printStackTrace()
                string += "\n\nERROR: $e"
                Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
            }

            string += "\n\nDATA:\n\n"
            string += "OBJ:\t\t${data["obj"]}\n\n"
            string += "CP:\t\t${data["cp"]}\n\n"
            string += "TDN:\t\t${data["tdn"]}\n\n"
            string += "CA:\t\t${data["obj"]}\n\n"
            string += "PH:\t\t${data["obj"]}\n\n"
            binding.tvLog.text = string
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (binding.fragmentContainer.isVisible) super.onBackPressed()
        else {
            binding.fragmentContainer.visibility = View.VISIBLE
            binding.tvLog.visibility = View.GONE
        }
    }
}