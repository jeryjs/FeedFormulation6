package com.jery.feedformulation6.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Animatable
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.google.gson.GsonBuilder
import com.jery.feedformulation6.R
import com.jery.feedformulation6.data.Feed
import com.jery.feedformulation6.databinding.LayoutFeedItemBinding
import com.jery.feedformulation6.ui.fragments.FeedsFragment
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.NullPointerException

class FeedAdapter(
    private val feeds: MutableList<Feed>,
    private val feedsFile: File,
    private val feedsFragment: FeedsFragment,
    private val IS_SELECT_FEEDS_ENABLED: Boolean
) : RecyclerView.Adapter<FeedAdapter.FeedViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_feed_item, parent, false)
        return FeedViewHolder(view, feedsFragment)
    }

    override fun onBindViewHolder(holder: FeedViewHolder, position: Int) {
        val feed = feeds[position]
        holder.bind(feed, IS_SELECT_FEEDS_ENABLED)
        holder.itemView.setOnLongClickListener {
            holder.showDeleteFeedDialog(feed)
            true
        }
    }

    override fun getItemCount() = feeds.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateFeeds(updatedFeeds: List<Feed>) {
        feeds.clear()
        feeds.addAll(updatedFeeds)
        notifyDataSetChanged()
    }

    fun getFeeds(): List<Feed> {
        return feeds
    }

    inner class FeedViewHolder(itemView: View, private val _FF: FeedsFragment) : RecyclerView.ViewHolder(itemView) {
        private val feedBinding = LayoutFeedItemBinding.bind(itemView)
        private var isExpanded: Boolean = false

        fun bind(feed: Feed, isSelectFeedsEnabled: Boolean) {
            setFeedSelection(feed, isSelectFeedsEnabled)
            updateCategoryTextView(feed)
            updateFeedDetails(feed)
            enableCostEditing(feed)
        }

        private fun setFeedSelection(feed: Feed, status: Boolean) {
            if (status) {
                feedBinding.checkBox.visibility = View.VISIBLE
                feedBinding.checkBox.isChecked = feed.checked
                itemView.setOnClickListener {selectFeed(feed)}
            } else {
                feedBinding.imageView.visibility = View.VISIBLE
                itemView.setOnClickListener {expandFeed()}
            }
        }

        private fun updateCategoryTextView(feed: Feed) {
            val category = feed.getCategoryText()
            val bap = bindingAdapterPosition
            if (bap == 0 || feeds[bap - 1].type != feeds[bap].type) {
                feedBinding.categoryTextView.visibility = View.VISIBLE
                feedBinding.categoryTextView.text = category
            } else {
                feedBinding.categoryTextView.visibility = View.GONE
            }
        }

        private fun updateFeedDetails(feed: Feed) {
            try {
                feedBinding.feedNameTextView.text = feed.name
                feedBinding.feedCostTextView.text = "₹ ${feed.cost}"
                val detailsText =
                    "DM: ${feed.details[0]}% \t CP: ${feed.details[1]}% \t TDN: ${feed.details[2]}%"
                feedBinding.detailsTextView.text = detailsText
            } catch (e: NullPointerException) {
                corruptedFeedsList(itemView.context)
            }
        }

        private fun enableCostEditing(feed: Feed) {
            feedBinding.feedCostTextView.setOnClickListener {
                showCostEditDialog(feed)
            }
        }

        private fun showCostEditDialog(feed: Feed) {
            val costEditText = EditText(itemView.context)
            costEditText.hint = feed.cost.toString()
            costEditText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL

            val dialog = AlertDialog.Builder(itemView.context)
                .setTitle("Set new cost")
                .setView(costEditText)
                .setPositiveButton("Save") { _, _ ->
                    val newCost = costEditText.text.toString().toFloat()
                    updateCost(newCost)
                }
                .setNegativeButton("Cancel", null)
                .create()

            dialog.show()
            costEditText.requestFocus()
            costEditText.setSelection(costEditText.text.length)
            dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }

        private fun updateCost(newCost: Float) {
            val position = bindingAdapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val feed = feeds[position]
                feed.cost = newCost
                notifyItemChanged(position)
                saveUpdatedFeedsToJson()
            }
        }

        private fun expandFeed() {
            isExpanded = !isExpanded
            TransitionManager.beginDelayedTransition(itemView.parent as ViewGroup, ChangeBounds())
            (feedBinding.imageView.drawable as? Animatable)?.start()
            if (isExpanded) {
                feedBinding.detailsLayout.visibility = View.VISIBLE
                feedBinding.imageView.rotation = 180F
            } else {
                feedBinding.detailsLayout.visibility = View.GONE
                feedBinding.imageView.rotation = 0F
            }
        }

        private fun selectFeed(feed: Feed) {
            feedBinding.checkBox.isChecked = (!feedBinding.checkBox.isChecked)
            feed.checked = feedBinding.checkBox.isChecked
        }

        fun showDeleteFeedDialog(feed: Feed) {
            AlertDialog.Builder(itemView.context)
                .setTitle("Delete Feed")
                .setMessage("Are you sure you want to delete this feed?")
                .setPositiveButton("Delete") { _, _ ->
                    deleteFeed(feed)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        private fun deleteFeed(feed: Feed) {
            val position = feeds.indexOf(feed)
            if (position != -1) {
                feeds.removeAt(position)
                notifyItemRemoved(position)
                saveUpdatedFeedsToJson()
                Toast.makeText(itemView.context, "Feed deleted", Toast.LENGTH_SHORT).show()
            }
        }

        private fun saveUpdatedFeedsToJson() {
            val updatedFeedsJson = GsonBuilder().setPrettyPrinting().create().toJson(feeds)
            try {
                FileOutputStream(feedsFile).use { outputStream ->
                    outputStream.write(updatedFeedsJson.toByteArray())
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        private fun corruptedFeedsList(context: Context) {
            AlertDialog.Builder(context)
                .setTitle("Feeds List Corrupted")
                .setMessage("The feeds list seems to be corrupted. You need to reset to proceed. This cannot be undone.")
                .setPositiveButton("Reset") { _, _ ->
                    feedsFile.delete()
                    Toast.makeText(context, "Feeds list reset successfully", Toast.LENGTH_SHORT).show()
//                    _FF.finish()
                }
                .setNegativeButton("Cancel") { _, _ -> /*_FF.finish()*/}
                .setCancelable(false)
                .show()
        }
    }
}
