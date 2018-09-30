package com.android.mathias.velocity

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.mathias.velocity.db.DBManager
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import java.util.*

class FragmentRoutes : Fragment(), IClickInterface {

    private var mAdapter: RecyclerAdapterRoutes? = null
    private val mListRoutes = ArrayList<Route>()
    private var mListRoutesBackup: MutableList<Route>? = null
    private var fabCreate: FloatingActionButton? = null

    private var mTempRoute: Route? = null
    private var mSnackbar: Snackbar? = null
    private var mMoveMode = false

    private var mToolbar: Toolbar? = null
    private var mNavBar: BottomNavigationView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val routesView = inflater.inflate(R.layout.fragment_routes, container, false)
        initRecyclerView(routesView)
        setHasOptionsMenu(true)
        mToolbar = Objects.requireNonNull<FragmentActivity>(activity).findViewById(R.id.toolbar)
        mNavBar = Objects.requireNonNull<FragmentActivity>(activity).findViewById(R.id.navigation)
        fabCreate = routesView.findViewById(R.id.fab_create_route)
        fabCreate!!.setOnClickListener { v -> this@FragmentRoutes.handleFabEvent() }
        val routes = DBManager.getRoutes(context!!, null)
        for (r in routes) {
            addRouteCard(r)
        }
        return routesView
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
                    DBManager.saveRoute(context!!, newRoute)
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
                DBManager.deleteAllRoutes(activity!!)
                mListRoutes.clear()
                mAdapter!!.notifyDataSetChanged()
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
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("RestrictedApi")
    private fun toggleRearrangeMode() {
        mMoveMode = !mMoveMode
        mToolbar!!.menu.clear()
        val menu = if (mMoveMode) R.menu.menu_apply_changes else R.menu.menu_routes
        val color = resources.getColor(if (mMoveMode) R.color.colorAccent else R.color.colorPrimary, Objects.requireNonNull<FragmentActivity>(activity).getTheme())
        val title = if (mMoveMode) R.string.title_rearrange else R.string.nav_item_routes
        mToolbar!!.inflateMenu(menu)
        mToolbar!!.setBackgroundColor(color)
        mToolbar!!.setTitle(title)
        fabCreate!!.visibility = if (mMoveMode) View.GONE else View.VISIBLE
        mNavBar!!.visibility = if (mMoveMode) View.GONE else View.VISIBLE
        mAdapter!!.setRearrangeMode(mMoveMode)
    }

    private fun persistNewOrder() {
        for (i in mListRoutes.indices) {
            DBManager.setRoutePos(context!!, mListRoutes[i].id, i)
        }
    }

    private fun restoreOrderBackup() {
        mListRoutes.clear()
        mListRoutes.addAll(mListRoutesBackup!!)
        mAdapter!!.notifyDataSetChanged()
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
        val bundle = Bundle()
        val sheet = BottomSheetRoutes()
        sheet.arguments = bundle
        sheet.show(Objects.requireNonNull<FragmentActivity>(activity).supportFragmentManager, "Bottom Sheet Dialog Fragment")
    }

    private fun addRouteCard(route: Route) {
        mListRoutes.add(route)
        mAdapter!!.notifyItemInserted(mListRoutes.size - 1)
    }

    private fun initRecyclerView(routesView: View) {
        val rvRoutes = routesView.findViewById<RecyclerView>(R.id.list_routes)
        mListRoutes.clear()
        mAdapter = RecyclerAdapterRoutes(mListRoutes, rvRoutes, this)
        val layoutManager = LinearLayoutManager(activity!!.applicationContext)
        rvRoutes.layoutManager = layoutManager
        rvRoutes.adapter = mAdapter
        val sbCallback = object : Snackbar.Callback() {
            override fun onDismissed(sb: Snackbar?, event: Int) {
                if (mSnackbar != null && mTempRoute != null) {
                    DBManager.deleteRoute(context!!, mTempRoute!!.id)
                    mSnackbar!!.removeCallback(this)
                    mSnackbar = null
                    mTempRoute = null
                }
                super.onDismissed(sb, event)
            }
        }
        val ithCallback = object : ItemTouchHelper.SimpleCallback(0, 0) {
            override fun onMoved(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, fromPos: Int, target: RecyclerView.ViewHolder, toPos: Int, x: Int, y: Int) {
                Collections.swap(mListRoutes, fromPos, toPos)
                super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y)
            }

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                mAdapter!!.notifyItemMoved(viewHolder.adapterPosition, target.adapterPosition)
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
                    mSnackbar = null
                    mTempRoute = null
                }
                val idx = viewHolder.adapterPosition
                mTempRoute = mListRoutes[idx]
                mListRoutes.removeAt(idx)
                mAdapter!!.notifyItemRemoved(idx)
                mSnackbar = Snackbar.make(rvRoutes, "\"" + mTempRoute!!.name + "\" deleted.", Snackbar.LENGTH_LONG).setAction("UNDO") { view ->
                    if (mTempRoute != null) {
                        mSnackbar!!.removeCallback(sbCallback)
                        mListRoutes.add(idx, mTempRoute!!)
                        mAdapter!!.notifyItemInserted(idx)
                        Snackbar.make(rvRoutes, "Restored.", Snackbar.LENGTH_SHORT).show()
                        mSnackbar = null
                        mTempRoute = null
                    } else {
                        Snackbar.make(rvRoutes, "Error restoring...", Snackbar.LENGTH_SHORT).show()
                    }
                }.addCallback(sbCallback)
                mSnackbar!!.show()
            }
        }
        val ith = ItemTouchHelper(ithCallback)
        ith.attachToRecyclerView(rvRoutes)
    }

    private fun createRoutesDemoData() {
        for (i in 1..5) {
            val startLoc = LatLng((i + 0.002) * Math.PI, (i + 0.001) * Math.PI)
            val endLoc = LatLng(i * Math.PI, i * Math.PI)
            val route = Route("To location $i", startLoc, endLoc, "Somewhere $i", "Nowhere $i")
            DBManager.saveRoute(context!!, route)
            addRouteCard(route)
        }
    }

    companion object {
        private const val REQUEST_ROUTE_DATA = 200
    }
}

internal interface IClickInterface {
    fun itemLongClick(v: View, position: Int)
}
