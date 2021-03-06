package com.android.mathias.velocity

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.mathias.velocity.db.DBManager
import com.android.mathias.velocity.model.Route
import com.android.mathias.velocity.model.Walk
import com.android.mathias.velocity.util.RecyclerAdapterWalks
import com.google.android.material.snackbar.Snackbar
import java.util.*

class FragmentHistory : Fragment() {

    private lateinit var mRvHistory: RecyclerView
    private lateinit var mAdapter: RecyclerAdapterWalks
    private val mListWalks = ArrayList<Walk>()
    private var mTempWalk: Walk? = null
    private var mSnackbar: Snackbar? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val historyView = inflater.inflate(R.layout.fragment_history, container, false)
        setHasOptionsMenu(true)
        initRecyclerView(historyView)
        val walks = DBManager.getWalks(context!!, null)
        for (w in walks) {
            addWalkCard(w)
        }
        return historyView
    }

    private fun initRecyclerView(historyView: View) {
        mRvHistory = historyView.findViewById(R.id.list_walks)
        mListWalks.clear()
        mAdapter = RecyclerAdapterWalks(mListWalks, mRvHistory)
        val layoutManager = LinearLayoutManager(context)
        layoutManager.isItemPrefetchEnabled = true
        mRvHistory.layoutManager = layoutManager
        mRvHistory.adapter = mAdapter
        itemTouchHelper.attachToRecyclerView(mRvHistory)
    }

    private fun addWalkCard(walk: Walk) {
        mListWalks.add(walk)
        mAdapter.notifyItemInserted(mListWalks.size - 1)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu!!.clear()
        inflater!!.inflate(R.menu.menu_history, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.action_settings -> startActivity(Intent(activity, ActivitySettings::class.java))
            R.id.action_delete_walks -> {
                DBManager.deleteAllWalks(context!!)
                mListWalks.clear()
                mAdapter.notifyDataSetChanged()
            }
            R.id.action_about -> createHistoryDemoData()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDetach() {
        if (mSnackbar != null) {
            mSnackbar!!.dismiss()
            mSnackbar = null
        }
        super.onDetach()
    }

    private fun createHistoryDemoData() {
        for (i in 0..4) {
            val walk = Walk((i * 5000).toLong(), Date(SystemClock.currentThreadTimeMillis()), Route("Route $i"))
            walk.id = DBManager.saveWalk(context!!, walk)
            addWalkCard(walk)
        }
    }

    private val sbCallback = object : Snackbar.Callback() {
        override fun onDismissed(sb: Snackbar?, event: Int) {
            if (mSnackbar != null && mTempWalk != null) {
                DBManager.deleteWalk(context!!, mTempWalk!!.id)
                mSnackbar!!.removeCallback(this)
                mSnackbar = null
                mTempWalk = null
            }
            super.onDismissed(sb, event)
        }
    }
    private val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            if (mSnackbar != null && mTempWalk != null) {
                DBManager.deleteWalk(context!!, mTempWalk!!.id)
                mSnackbar!!.removeCallback(sbCallback)
                mSnackbar = null
                mTempWalk = null
            }
            val idx = viewHolder.adapterPosition
            mTempWalk = mListWalks[idx]
            mListWalks.removeAt(idx)
            mAdapter.notifyItemRemoved(idx)
            mSnackbar = Snackbar.make(mRvHistory, "Walk deleted.", Snackbar.LENGTH_LONG).setAction("UNDO") {
                if (mTempWalk != null) {
                    mSnackbar!!.removeCallback(sbCallback)
                    mListWalks.add(idx, mTempWalk!!)
                    mAdapter.notifyItemInserted(idx)
                    Snackbar.make(mRvHistory, "Restored.", Snackbar.LENGTH_SHORT).show()
                    mSnackbar = null
                    mTempWalk = null
                } else {
                    Snackbar.make(mRvHistory, "Error restoring...", Snackbar.LENGTH_SHORT).show()
                }
            }.addCallback(sbCallback)
            mSnackbar!!.show()
        }
    })
}
