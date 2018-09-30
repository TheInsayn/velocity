package com.android.mathias.velocity.util

import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.mathias.velocity.ext.IRvClickListener
import com.android.mathias.velocity.R
import com.android.mathias.velocity.model.Route
import java.util.*

internal class RecyclerAdapterRoutes(
        private val mRouteList: List<Route>,
        private val mRecyclerView: RecyclerView,
        private val mRvClickListener: IRvClickListener)
    : RecyclerView.Adapter<RecyclerAdapterRoutes.RouteCardHolder>() {

    private var mExpandedPosition = -1
    private var mPreviousExpandedPosition = -1
    private var mRearrangeMode = false

    internal inner class RouteCardHolder(view: View) : RecyclerView.ViewHolder(view) {
        val mRouteName: TextView = view.findViewById(R.id.txt_route_name)
        val mRouteStartPoint: TextView = view.findViewById(R.id.txt_route_start_point)
        val mRouteEndPoint: TextView = view.findViewById(R.id.txt_route_end_point)
        val mRouteDistance: TextView = view.findViewById(R.id.txt_route_distance)
        val mExpansion: RelativeLayout = view.findViewById(R.id.route_card_expansion)
        val mAverageTime: TextView = view.findViewById(R.id.txt_route_average)
        val mDragPoint: ImageView = view.findViewById(R.id.btn_drag_route)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteCardHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_route, parent, false)
        return RouteCardHolder(view)
    }

    override fun onBindViewHolder(holder: RouteCardHolder, position: Int) {
        val route = mRouteList[position]
        // set max width for TextViews (for making ellipsis in case)
        val halfWidth = mRecyclerView.width / 2 - 60
        holder.mRouteName.maxWidth = halfWidth
        holder.mRouteDistance.maxWidth = halfWidth
        holder.mRouteStartPoint.maxWidth = halfWidth
        holder.mRouteEndPoint.maxWidth = halfWidth
        // fill TextViews
        holder.mRouteName.text = route.name
        holder.mRouteStartPoint.text = route.startName
        holder.mRouteEndPoint.text = route.endName
        holder.mRouteDistance.text = String.format(Locale.getDefault(), "Distance: %.1fm", route.approximateDistance)
        //handle expansion in list
        val isExpanded = position == mExpandedPosition
        holder.mDragPoint.visibility = if (mRearrangeMode) View.VISIBLE else View.INVISIBLE
        holder.mExpansion.visibility = if (isExpanded) View.VISIBLE else View.GONE
        holder.itemView.isActivated = isExpanded
        if (isExpanded) mPreviousExpandedPosition = holder.adapterPosition
        holder.itemView.setOnClickListener {
            if (!mRearrangeMode) {
                mExpandedPosition = if (isExpanded) -1 else holder.adapterPosition
                notifyItemChanged(mPreviousExpandedPosition)
                notifyItemChanged(holder.adapterPosition)
            }
        }
        if (isExpanded) {
            var timeStr: String
            val time = route.getAverageWalkTime(mRecyclerView.context)
            if (time == 0L) {
                timeStr = "no walk data yet, not able to calculate average."
            } else {
                val avg = Date(time)
                val min = DateFormat.format("m", avg)
                val sec = DateFormat.format("s", avg)
                timeStr = "average walk duration: "
                if (min != "0") timeStr += min.toString() + "m and "
                timeStr += sec.toString() + "s"
            }
            holder.mAverageTime.text = timeStr
        }
        holder.itemView.setOnLongClickListener { v ->
            mRvClickListener.itemLongClick(v, position)
            true
        }
    }

    override fun getItemCount(): Int {
        return mRouteList.size
    }

    fun setRearrangeMode(enabled: Boolean) {
        mRearrangeMode = enabled
        mExpandedPosition = -1
        mPreviousExpandedPosition = -1
        notifyDataSetChanged()
    }
}
