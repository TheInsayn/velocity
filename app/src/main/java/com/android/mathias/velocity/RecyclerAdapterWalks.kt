package com.android.mathias.velocity

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.format.DateFormat
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.*

internal class RecyclerAdapterWalks(private val mWalkList: List<Walk>, private val mRecyclerView: RecyclerView) : RecyclerView.Adapter<RecyclerAdapterWalks.WalkCardHolder>() {
    private var mExpandedPosition = -1
    private var mPreviousExpandedPosition = -1

    internal inner class WalkCardHolder(view: View) : RecyclerView.ViewHolder(view) {
        val mWalkRoute: TextView = view.findViewById(R.id.txt_walk_route)
        val mWalkDuration: TextView = view.findViewById(R.id.txt_walk_duration)
        val mWalkDate: TextView = view.findViewById(R.id.txt_walk_date)
        val mWalkWeekday: TextView = view.findViewById(R.id.txt_walk_weekday)
        val mExpansion: RelativeLayout = view.findViewById(R.id.walk_card_expansion)
        val mAverageTime: TextView = view.findViewById(R.id.txt_walk_average)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WalkCardHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_walk, parent, false)
        return WalkCardHolder(view)
    }

    override fun onBindViewHolder(holder: WalkCardHolder, position: Int) {
        val walk = mWalkList[position]
        holder.mWalkRoute.text = walk.route!!.name
        holder.mWalkDuration.text = DateFormat.format("mm:ss", Date(walk.duration))
        holder.mWalkDate.text = DateFormat.format("dd.MM.yyyy", walk.date)
        holder.mWalkWeekday.text = DateFormat.format("EEEE", walk.date)
        //handle expansion in list
        val isExpanded = position == mExpandedPosition
        holder.mExpansion.visibility = if (isExpanded) View.VISIBLE else View.GONE
        holder.itemView.isActivated = isExpanded
        if (isExpanded) mPreviousExpandedPosition = holder.adapterPosition
        holder.itemView.setOnClickListener { v ->
            mExpandedPosition = if (isExpanded) -1 else holder.adapterPosition
            notifyItemChanged(mPreviousExpandedPosition)
            notifyItemChanged(holder.adapterPosition)
        }
        if (isExpanded) {
            val avg = Date(walk.route!!.getAverageWalkTime(mRecyclerView.context))
            val min = DateFormat.format("m", avg)
            val sec = DateFormat.format("s", avg)
            val boldText = walk.route!!.name
            var timeStr = "average walk duration for $boldText: "
            if (min != "0") timeStr += min.toString() + "m and "
            timeStr += sec.toString() + "s"
            val idx = timeStr.indexOf(boldText!!)
            val str = SpannableString(timeStr)
            str.setSpan(StyleSpan(Typeface.BOLD), idx, idx + boldText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            holder.mAverageTime.text = str
        }
    }

    override fun getItemCount(): Int {
        return mWalkList.size
    }
}
