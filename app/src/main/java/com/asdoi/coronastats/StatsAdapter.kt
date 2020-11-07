package com.asdoi.coronastats

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.cardview.widget.CardView
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.asdoi.corona.model.LiveTicker


private const val FADE_IN_DURATION = 300

class StatsAdapter(private val activity: MainActivity, data: List<LiveTicker>) :
        RecyclerView.Adapter<StatsViewHolder>() {
    var data = data
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    private var onAttach = true

    override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
    ): StatsViewHolder {
        val entryView = LayoutInflater.from(parent.context)
                .inflate(R.layout.stats_entry, parent, false) as CardView

        return StatsViewHolder(entryView)
    }

    override fun onBindViewHolder(viewHolder: StatsViewHolder, position: Int) {
        var coronaTicker: LiveTicker? = null

        if (position < data.size) {
            coronaTicker = data[position]
        }

        if (coronaTicker == null) {
            bindMessageView(viewHolder, position)
        } else {
            bindCoronaTicker(viewHolder, coronaTicker)
        }

        setAnimation(viewHolder.itemView, position)
    }

    private fun bindCoronaTicker(viewHolder: StatsViewHolder, coronaTicker: LiveTicker) {
        viewHolder.content.visibility = View.VISIBLE
        viewHolder.expand.visibility = View.VISIBLE
        viewHolder.divider.visibility = View.VISIBLE
        viewHolder.openInBrowserCardView.visibility = View.VISIBLE

        val color = coronaTicker.getColor(activity)
        val textColor = getColorWithBestContrast(color)

        viewHolder.root.setCardBackgroundColor(color)
        viewHolder.content.setTextColor(textColor)
        viewHolder.location.setTextColor(textColor)
        viewHolder.metaData.setTextColor(textColor)
        viewHolder.divider.setBackgroundColor(textColor)
        viewHolder.secondDivider.setBackgroundColor(textColor)
        ImageViewCompat.setImageTintList(viewHolder.expand, ColorStateList.valueOf(textColor))
        ImageViewCompat.setImageTintList(
                viewHolder.openInBrowser,
                ColorStateList.valueOf(textColor)
        )

        viewHolder.location.text = coronaTicker.location
        viewHolder.metaData.text = coronaTicker.metaInformation(activity)
        viewHolder.openInBrowser.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(coronaTicker.linkToVisibleData))
            activity.startActivity(intent)
        }

        viewHolder.expand.setOnClickListener {
            val newText = if (viewHolder.full) {
                coronaTicker.summary(activity)
            } else {
                coronaTicker.details(activity)
            }

            viewHolder.full = !viewHolder.full
            if (viewHolder.full) {
                TransitionManager.beginDelayedTransition(
                        viewHolder.root,
                        ChangeBounds()
                )

                viewHolder.secondDivider.visibility = View.VISIBLE
                viewHolder.metaData.visibility = View.VISIBLE
                viewHolder.openInBrowserCardView.visibility = View.VISIBLE
            } else {
                val transition = ChangeBounds()
                transition.duration = 80

                TransitionManager.beginDelayedTransition(
                        viewHolder.root,
                        transition
                )

                viewHolder.secondDivider.visibility = View.GONE
                viewHolder.metaData.visibility = View.GONE
                viewHolder.openInBrowserCardView.visibility = View.GONE
            }
            viewHolder.content.text = newText
            toggleArrow(viewHolder.expand, viewHolder.full)
        }

        viewHolder.full = !viewHolder.full
        viewHolder.expand.performClick()

        viewHolder.root.setOnLongClickListener {
            val popup = PopupMenu(activity, viewHolder.root)
            popup.setOnMenuItemClickListener { item: MenuItem ->
                when (item.itemId) {
                    R.id.action_delete_city -> activity.removeCity(viewHolder.location.text.toString())
                }
                true
            }
            val inflater = popup.menuInflater
            inflater.inflate(R.menu.menu_stats_entry, popup.menu)
            popup.show()
            true
        }
    }

    private fun bindMessageView(viewHolder: StatsViewHolder, position: Int) {
        viewHolder.expand.visibility = View.VISIBLE
        viewHolder.content.visibility = View.GONE
        viewHolder.divider.visibility = View.GONE
        viewHolder.secondDivider.visibility = View.GONE
        viewHolder.metaData.visibility = View.GONE
        viewHolder.openInBrowserCardView.visibility = View.GONE

        viewHolder.root.setCardBackgroundColor(Color.WHITE)
        viewHolder.location.apply {
            text = activity.getString(R.string.please_add_a_city)
            setTextColor(Color.BLACK)
        }
        viewHolder.expand.setOnClickListener {
            it.rotation = 0f
            it.animate().setDuration(400).rotation(360f)
            activity.addCityDialog()
        }
        ImageViewCompat.setImageTintList(viewHolder.expand, ColorStateList.valueOf(Color.BLACK))
    }

    private fun setAnimation(itemView: View, i: Int) {
        var i = i
        if (!onAttach) {
            i = -1
        }
        val isNotFirstItem = i == -1
        i++
        itemView.alpha = 0f
        val animatorSet = AnimatorSet()
        val animator = ObjectAnimator.ofFloat(itemView, "alpha", 0f, 0.5f, 1.0f)
        ObjectAnimator.ofFloat(itemView, "alpha", 0f).start()
        animator.startDelay =
                (if (isNotFirstItem) FADE_IN_DURATION / 2 else i * FADE_IN_DURATION / 3).toLong()
        animator.duration = FADE_IN_DURATION.toLong()
        animatorSet.play(animator)
        animator.start()
    }

    override fun getItemCount() = if (data.isEmpty()) 1 else data.size

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                onAttach = false
                super.onScrollStateChanged(recyclerView, newState)
            }
        })
        super.onAttachedToRecyclerView(recyclerView)
    }

    companion object {
        fun getColorWithBestContrast(
                bgColor: Int,
                lightColor: Int = Color.WHITE,
                darkColor: Int = Color.BLACK
        ): Int {
            var color = String.format("#%06X", 0xFFFFFF and bgColor)
            color = color.substring(1, 7)
            val r = color.substring(0, 2).toInt(16) // hexToR
            val g = color.substring(2, 4).toInt(16) // hexToG
            val b = color.substring(4, 6).toInt(16) // hexToB
            return if (r * 0.299 + g * 0.587 + b * 0.114 > 186) darkColor else lightColor
        }

        fun toggleArrow(view: View, isExpanded: Boolean): Boolean {
            return if (isExpanded) {
                view.animate().setDuration(200).rotation(180f)
                true
            } else {
                view.animate().setDuration(200).rotation(0f)
                false
            }
        }
    }

}

class StatsViewHolder(val root: CardView) : RecyclerView.ViewHolder(root) {
    val location: TextView = root.findViewById(R.id.location)
    val expand: ImageView = root.findViewById(R.id.expand)
    val content: TextView = root.findViewById(R.id.content)
    val divider: View = root.findViewById(R.id.divider)
    val secondDivider: View = root.findViewById(R.id.secondDivider)
    val metaData: TextView = root.findViewById(R.id.metaData)
    val openInBrowser: ImageView = root.findViewById(R.id.open_in_browser)
    val openInBrowserCardView: CardView = root.findViewById(R.id.open_in_browser_card_view)
    var full = false
}