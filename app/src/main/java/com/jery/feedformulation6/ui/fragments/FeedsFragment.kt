package com.jery.feedformulation6.ui.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jery.feedformulation6.R
import com.jery.feedformulation6.data.Feed
import com.jery.feedformulation6.databinding.DialogAddNewFeedBinding
import com.jery.feedformulation6.databinding.FragmentFeedsBinding
import com.jery.feedformulation6.ui.adapters.FeedAdapter
import com.jery.feedformulation6.utils.Utils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.properties.Delegates

private const val EXPORT_FEEDS_REQUEST_CODE = 100
private const val IMPORT_FEEDS_REQUEST_CODE = 101

/**
 * A [Fragment] for displaying the list of feeds.
 * Use the [FeedsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FeedsFragment : Fragment() {
    private lateinit var feedsFile: File
    private lateinit var binding: FragmentFeedsBinding
    private lateinit var feedAdapter: FeedAdapter

    companion object {
        private var IS_SELECT_FEEDS_ENABLED: Boolean by Delegates.notNull()

        fun newInstance(isSelectFeedsEnabled: Boolean): FeedsFragment {
            val fragment = FeedsFragment()
            val args = Bundle()
            args.putBoolean("IS_SELECT_FEEDS_ENABLED", isSelectFeedsEnabled)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFeedsBinding.inflate(inflater, container, false)

        binding.addNewFeedsFab.setOnClickListener { addNewFeed() }
        binding.exportFeedsFab.setOnClickListener { exportFeeds() }
        binding.importFeedsFab.setOnClickListener { importFeeds() }
        binding.resetFeedsFab.setOnClickListener  { resetFeeds() }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        feedsFile = File(requireActivity().filesDir, "feeds.json")
        IS_SELECT_FEEDS_ENABLED = arguments?.getBoolean("IS_SELECT_FEEDS_ENABLED") ?: false

        setupRecyclerView()
        initializeFeedsFile()
        loadFeedsList()
    }

    private fun setupRecyclerView() {
        feedAdapter = FeedAdapter(mutableListOf(), feedsFile, this, IS_SELECT_FEEDS_ENABLED)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = feedAdapter
    }

    private fun initializeFeedsFile() {
        if (!feedsFile.exists()) {
            requireActivity().assets.open(feedsFile.name).use { input ->
                FileOutputStream(feedsFile).use { output ->
                    input.copyTo(output)
                }
            }
        }
    }

    private fun loadFeedsList() {
        initializeFeedsFile()
        val feedsJson = Utils.loadJSONFromFile(feedsFile.path)
        val feedType = object : TypeToken<List<Feed>>() {}.type
        val feeds: List<Feed> = Gson().fromJson(feedsJson, feedType)
        val typeSortingWeights = mapOf("R" to 0, "C" to 1, "M" to 2, "" to 3)
        val sortedFeeds = feeds.sortedBy { typeSortingWeights[it.type] }
        feedAdapter.updateFeeds(sortedFeeds)
    }

    private fun addNewFeed() {
        val _v = DialogAddNewFeedBinding.inflate(requireActivity().layoutInflater)
        val typeArray = resources.getStringArray(R.array.type_array)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, typeArray)
        _v.sprType.setAdapter(adapter)
        _v.sprType.onFocusChangeListener = View.OnFocusChangeListener { view, hasFocus ->
            if (hasFocus) Utils.hideKeyboard(view)
            _v.sprType.showDropDown()
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(_v.root)
            .setTitle("Add New Feed #${feedAdapter.itemCount + 1}")
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .setNeutralButton("Help") { _, _ -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://docs.google.com/gview?url=https://www.nddb.coop/sites/default/files/pdfs/Animal-Nutrition-booklet.pdf"))) }
            .setCancelable(false)
            .show()

        val saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        saveButton.isEnabled = false

        val requiredFields = listOf(_v.edtName, _v.edtCost, _v.sprType, _v.edtDM, _v.edtCP, _v.edtTDN, _v.edtMinInclLvl)
        requiredFields.forEach { field ->
            field.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    saveButton.isEnabled = requiredFields.all { it.text.isNotEmpty() }
                }
            })
        }

        saveButton.setOnClickListener {
            val newFeed = Feed(
                name = _v.edtName.text.toString().trim(),
                cost = _v.edtCost.text.toString().toFloat(),
                type = _v.sprType.text.toString().substring(0, 1),
                details = listOf(_v.edtDM, _v.edtCP, _v.edtTDN).map { it.text.toString().toFloat() },
                percentage = mapOf(
                    "cow" to _v.edtMinInclLvl.text.toString().toFloat(),
                    "buffalo" to _v.edtMinInclLvl.text.toString().toFloat()
                ),
                checked = false,
                id = feedAdapter.itemCount + 1
            )
            Feed.addNewFeed(newFeed, feedsFile)
            loadFeedsList()
            dialog.dismiss()
        }
    }

    private fun exportFeeds() {
        val timeStamp = Utils.generateTimestamp()
        val fileName = "feeds_export_$timeStamp.json"

        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "application/json"
        intent.putExtra(Intent.EXTRA_TITLE, fileName)
        startActivityForResult(intent, EXPORT_FEEDS_REQUEST_CODE)
    }

    private fun importFeeds() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "application/json"
        startActivityForResult(intent, IMPORT_FEEDS_REQUEST_CODE)
    }

    private fun resetFeeds() {
        AlertDialog.Builder(requireContext())
            .setTitle("Reset Feeds List")
            .setMessage("Are you sure you want to reset the feeds list? This action cannot be undone.")
            .setPositiveButton("Reset") { _, _ ->
                feedsFile.delete()
                Toast.makeText(requireContext(), "Feeds list reset successfully", Toast.LENGTH_SHORT).show()
                loadFeedsList()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            EXPORT_FEEDS_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    data.data?.let {
                        try {
                            requireActivity().contentResolver.openOutputStream(it)?.use { outputStream ->
                                val feedsJson = Utils.loadJSONFromFile(feedsFile.path)
                                outputStream.write(feedsJson.toByteArray())
                                Toast.makeText(requireContext(), "Feeds list exported successfully", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                            Toast.makeText(requireContext(), "Failed to export feeds list: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            IMPORT_FEEDS_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    data.data?.let {
                        try {
                            requireActivity().contentResolver.openInputStream(it)?.use { inputStream ->
                                FileOutputStream(feedsFile).use { outputStream ->
                                    inputStream.copyTo(outputStream)
                                    Toast.makeText(requireContext(), "Feeds list imported successfully", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                            Toast.makeText(requireContext(), "Failed to import feeds list: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                        loadFeedsList()
                    }
                }
            }
        }
    }

    fun getSelectedFeeds(): List<Feed> {
        return feedAdapter.getFeeds().filter { it.checked }
    }
}