package com.android.mathias.velocity

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.mathias.velocity.db.DBManager
import com.android.mathias.velocity.ext.IRvClickListener
import com.android.mathias.velocity.model.Route
import com.android.mathias.velocity.util.BottomSheetRoutes
import com.android.mathias.velocity.util.RecyclerAdapterRoutes
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import java.util.*

class FragmentRoutes : Fragment(), IRvClickListener {

    private lateinit var mRvRoutes : RecyclerView
    private lateinit var fabCreate: FloatingActionButton
    private lateinit var mAdapter: RecyclerAdapterRoutes
    private lateinit var mToolbar: Toolbar
    private lateinit var mNavBar: BottomNavigationView

    private val mListRoutes = ArrayList<Route>()
    private var mListRoutesBackup: MutableList<Route>? = null
    private var mTempRoute: Route? = null

    private var mSnackbar: Snackbar? = null
    private var mMoveMode = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val routesView = inflater.inflate(R.layout.fragment_routes, container, false)
        setHasOptionsMenu(true)
        initRecyclerView(routesView)
        mToolbar = activity!!.findViewById(R.id.toolbar)
        mNavBar = activity!!.findViewById(R.id.navigation)
        fabCreate = routesView.findViewById(R.id.fab_create_route)
        fabCreate.setOnClickListener { this@FragmentRoutes.handleFabEvent() }
        val routes = DBManager.getRoutes(context!!, null)
        for (r in routes) {
            addRouteCard(r)
        }
        return routesView
    }

    private fun initRecyclerView(routesView: View) {
        mRvRoutes = routesView.findViewById(R.id.list_routes)
        mListRoutes.clear()
        mAdapter = RecyclerAdapterRoutes(mListRoutes, mRvRoutes, this)
        val layoutManager = LinearLayoutManager(activity!!.applicationContext)
        mRvRoutes.layoutManager = layoutManager
        mRvRoutes.adapter = mAdapter
        itemTouchHelper.attachToRecyclerView(mRvRoutes)
    }

    private fun handleFabEvent() {
        startActivityForResult(Intent(activity, ActivityCreateRoute::class.java), REQUEST_ROUTE_DATA)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_ROUTE_DATA) {
            if (resultCode == RESULT_OK) {
                val newRoute = Route()
                val result = data!!.getBundleExtra(ActivityCreateRoute.RESULT_BUNDLE)
                val start = result.getDoubleArray(ActivityCreateRoute.START_LOC)
                val end = result.getDoubleArray(ActivityCreateRoute.END_LOC)
                if (start != null && end != null) {
                    newRoute.name = result.getString(ActivityCreateRoute.ROUTE_NAME)
                    newRoute.startLoc = LatLng(start[0], start[1])
                    newRoute.endLoc = LatLng(end[0], end[1])
                    newRoute.startName = result.getString(ActivityCreateRoute.START_LOC_NAME)
                    newRoute.endName = result.getString(ActivityCreateRoute.END_LOC_NAME)
                    newRoute.pos = mListRoutes.size + 1
                    newRoute.id = DBManager.saveRoute(context!!, newRoute)
                    newRoute.pos = mListRoutes.size
                    addRouteCard(newRoute)
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu!!.clear()
        inflater!!.inflate(R.menu.menu_routes, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.action_settings -> startActivity(Intent(activity, ActivitySettings::class.java))
            R.id.action_delete_routes -> {
                DBManager.deleteAllRoutes(context!!)
                mListRoutes.clear()
                mAdapter.notifyDataSetChanged()
            }
            R.id.action_rearrange -> {
                mListRoutesBackup = ArrayList(mListRoutes)
                toggleRearrangeMode()
            }
            R.id.action_apply -> {
                persistNewOrder()
                toggleRearrangeMode()
            }
            R.id.action_cancel -> {
                restoreOrderBackup()
                toggleRearrangeMode()
            }
            R.id.action_about -> createRoutesDemoData()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun toggleRearrangeMode() {
        mMoveMode = !mMoveMode
        mToolbar.menu.clear()
        val menu = if (mMoveMode) R.menu.menu_apply_changes else R.menu.menu_routes
        val color = resources.getColor(if (mMoveMode) R.color.colorAccent else R.color.colorPrimary, activity!!.theme)
        val title = if (mMoveMode) R.string.title_rearrange else R.string.nav_item_routes
        mToolbar.inflateMenu(menu)
        mToolbar.setBackgroundColor(color)
        mToolbar.setTitle(title)
        if (mMoveMode) fabCreate.hide() else fabCreate.show()
        mNavBar.visibility = if (mMoveMode) View.GONE else View.VISIBLE
        mAdapter.setRearrangeMode(mMoveMode)
    }

    private fun persistNewOrder() {
        for (i in mListRoutes.indices) {
            DBManager.setRoutePos(context!!, mListRoutes[i].id, i)
        }
    }

    private fun restoreOrderBackup() {
        mListRoutes.clear()
        mListRoutes.addAll(mListRoutesBackup!!)
        mAdapter.notifyDataSetChanged()
        mListRoutesBackup!!.clear()
        mListRoutesBackup = null
    }

    override fun onDetach() {
        if (mSnackbar != null) {
            mSnackbar!!.dismiss()
            mSnackbar = null
        }
        super.onDetach()
    }

    override fun itemLongClick(v: View, position: Int) {
        if (!mMoveMode) {
            showBottomSheet(mListRoutes[position])
        }
    }

    private fun showBottomSheet(route: Route) {
        val sheet = BottomSheetRoutes().newInstance(route)
        sheet.show(activity!!.supportFragmentManager, "Bottom Sheet Dialog Fragment")
    }

    private fun addRouteCard(route: Route) {
        mListRoutes.add(route)
        mAdapter.notifyItemInserted(mListRoutes.lastIndex)
    }

    private fun createRoutesDemoData() {
        for (i in 1..5) {
            val startLoc = LatLng((i + 0.002) * Math.PI, (i + 0.001) * Math.PI)
            val endLoc = LatLng(i * Math.PI, i * Math.PI)
            val route = Route("To location $i", startLoc, endLoc, "Somewhere $i", "Nowhere $i")
            route.pos = mListRoutes.size
            route.id = DBManager.saveRoute(context!!, route)
            addRouteCard(route)
        }
    }

    private val sbCallback = object : Snackbar.Callback() {
        override fun onDismissed(sb: Snackbar?, event: Int) {
            if (mSnackbar != null && mTempRoute != null) {
                DBManager.deleteRoute(context!!, mTempRoute!!.id)
                mSnackbar!!.removeCallback(this)
                for (i: Int in mTempRoute!!.pos until mListRoutes.size) {
                    mListRoutes[i].pos--
                    DBManager.setRoutePos(context!!, mListRoutes[i].id, mListRoutes[i].pos)
                }
                mSnackbar = null
                mTempRoute = null
            }
            super.onDismissed(sb, event)
        }
    }

    private val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, 0) {
        override fun onMoved(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                             fromPos: Int, target: RecyclerView.ViewHolder, toPos: Int, x: Int, y: Int) {
            Collections.swap(mListRoutes, fromPos, toPos)
            super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y)
        }

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            mAdapter.notifyItemMoved(viewHolder.adapterPosition, target.adapterPosition)
            return true
        }

        override fun getDragDirs(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
            return if (mMoveMode)
                ItemTouchHelper.UP or ItemTouchHelper.DOWN
            else
                super.getDragDirs(recyclerView, viewHolder)
        }

        override fun getSwipeDirs(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
            return if (!mMoveMode)
                ItemTouchHelper.START or ItemTouchHelper.END
            else
                super.getSwipeDirs(recyclerView, viewHolder)
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            if (mSnackbar != null && mTempRoute != null) {
                DBManager.deleteRoute(context!!, mTempRoute!!.id)
                mSnackbar!!.removeCallback(sbCallback)
                for (i: Int in mTempRoute!!.pos until mListRoutes.size) {
                    mListRoutes[i].pos--
                    DBManager.setRoutePos(context!!, mListRoutes[i].id, mListRoutes[i].pos)
                }
                mSnackbar = null
                mTempRoute = null
            }
            val idx = viewHolder.adapterPosition
            mTempRoute = mListRoutes[idx]
            mListRoutes.removeAt(idx)
            mAdapter.notifyItemRemoved(idx)
            mSnackbar = Snackbar.make(mRvRoutes, "\"${mTempRoute!!.name}\" deleted.", Snackbar.LENGTH_LONG).setAction("UNDO") {
                if (mTempRoute != null) {
                    mSnackbar!!.removeCallback(sbCallback)
                    mListRoutes.add(idx, mTempRoute!!)
                    mAdapter.notifyItemInserted(idx)
                    Snackbar.make(mRvRoutes, "Restored.", Snackbar.LENGTH_SHORT).show()
                    mSnackbar = null
                    mTempRoute = null
                } else {
                    Snackbar.make(mRvRoutes, "Error restoring...", Snackbar.LENGTH_SHORT).show()
                }
            }.addCallback(sbCallback)
            mSnackbar!!.show()
        }
    })

    companion object {
        private const val REQUEST_ROUTE_DATA = 200
    }
}

