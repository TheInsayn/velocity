package com.android.mathias.velocity

import android.animation.ObjectAnimator
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.preference.PreferenceManager
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListPopupWindow
import android.widget.ProgressBar
import android.widget.TextView

import com.google.android.material.floatingactionbutton.FloatingActionButton

import java.util.ArrayList
import java.util.Date
import java.util.Objects
import java.util.concurrent.TimeUnit
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment

class FragmentCurrent : Fragment() {
    private var mTimeView: TextView? = null
    private var mRouteView: TextView? = null
    private var mTimeState: TimeState? = null
    private var mStartTime: Long = 0
    private var mLastStopTime: Long = 0
    private var mCurrentWalkRoute: Route? = null
    private var mAnimator: ObjectAnimator? = null
    private var mNotificationManager: NotificationManager? = null
    private var mSharedPref: SharedPreferences? = null
    private var mChannel: NotificationChannel? = null
    private var mActivity: Activity? = null

    private var mFab: FloatingActionButton? = null
    private var mBtnR: Button? = null
    private var mHandler: Handler? = null
    private var mProgressBar: ProgressBar? = null

    private val mRunnable = object : Runnable {
        override fun run() {
            val time = SystemClock.elapsedRealtime() - mStartTime
            val sec = TimeUnit.MILLISECONDS.toSeconds(time).toInt() % 60
            mTimeView!!.text = DateFormat.format("mm:ss", Date(time))
            if (mSharedPref!!.getBoolean("enable_notifications", false)) {
                buildNotification()
            } else if (mNotificationManager!!.activeNotifications != null) {
                mNotificationManager!!.cancel(NOTIFICATION_ID)
            }
            setProgressSeconds(sec)
            mHandler!!.postDelayed(this, 1000)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_current, container, false)
        setHasOptionsMenu(true)
        return view
    }

    private fun toggleStopwatch() {
        when (mTimeState) {
            FragmentCurrent.TimeState.STOPPED -> startWalk()
            FragmentCurrent.TimeState.RUNNING -> pauseStopwatch()
            FragmentCurrent.TimeState.PAUSED -> resumeStopwatch()
            else -> {
            }
        }
    }

    private fun startWalk() {
        val routeNames = ArrayList<String>()
        for (r in DBManager.getRoutes(context!!, null)) {
            routeNames.add(r.name!!)
        }
        val defaultRouteName = mSharedPref!!.getString("default_route", "None")
        if (defaultRouteName == "None") {
            mCurrentWalkRoute = Route("No route set")
        } else {
            mCurrentWalkRoute = DBManager.getRoutes(context!!, defaultRouteName)[0]
        }
        if (routeNames.size > 0 && defaultRouteName == "None") {
            val adapter = ArrayAdapter<CharSequence>(Objects.requireNonNull<Context>(context), R.layout.support_simple_spinner_dropdown_item)
            adapter.addAll(routeNames)
            adapter.add("None")
            val lpw = ListPopupWindow(context!!)
            lpw.setAdapter(adapter)
            lpw.anchorView = mFab
            lpw.setContentWidth(((mFab!!.parent as View).width / 2.5).toInt())
            lpw.setOnItemClickListener { adapterView, _, i, l ->
                mCurrentWalkRoute = if (adapterView.getItemAtPosition(i).toString() == "None") {
                    Route("No route set")
                } else {
                    DBManager.getRoutes(context!!, adapterView.getItemAtPosition(i).toString())[0]
                }
                startStopwatch()
                lpw.dismiss()
            }
            lpw.show()
        } else {
            startStopwatch()
        }
    }

    private fun startStopwatch() {
        mStartTime = SystemClock.elapsedRealtime()
        mTimeState = TimeState.RUNNING
        updateUI()
        mHandler = Handler()
        mHandler!!.post(mRunnable)
    }

    internal fun pauseStopwatch() {
        mLastStopTime = SystemClock.elapsedRealtime()
        mTimeState = TimeState.PAUSED
        updateUI()
        mHandler!!.removeCallbacks(mRunnable)
        buildNotification()
    }

    internal fun resumeStopwatch() {
        mStartTime += (SystemClock.elapsedRealtime() - mLastStopTime)
        mTimeState = TimeState.RUNNING
        updateUI()
        mHandler!!.post(mRunnable)
    }

    internal fun stopWalk() {
        val walkTime = SystemClock.elapsedRealtime() - mStartTime
        val walk = Walk(walkTime, Date(), mCurrentWalkRoute!!)
        mLastStopTime = 0
        mCurrentWalkRoute = null
        mTimeState = TimeState.STOPPED
        updateUI()
        DBManager.saveWalk(context!!, walk)
    }

    private fun updateUI() {
        when (mTimeState) {
            FragmentCurrent.TimeState.STOPPED -> {
                mFab!!.setImageResource(android.R.drawable.ic_media_play)
                mBtnR!!.isClickable = false
                mBtnR!!.visibility = View.INVISIBLE
                mRouteView!!.text = ""
                mProgressBar!!.progress = 0
                if (mNotificationManager!!.activeNotifications != null) mNotificationManager!!.cancel(NOTIFICATION_ID)
                if (mHandler != null) mHandler!!.removeCallbacks(mRunnable)
            }
            FragmentCurrent.TimeState.RUNNING -> {
                mFab!!.setImageResource(android.R.drawable.ic_media_pause)
                mBtnR!!.isClickable = true
                mBtnR!!.visibility = View.VISIBLE
                mRouteView!!.text = mCurrentWalkRoute!!.name
            }
            FragmentCurrent.TimeState.PAUSED -> {
                mFab!!.setImageResource(android.R.drawable.ic_media_play)
                mBtnR!!.isClickable = true
                mBtnR!!.visibility = View.VISIBLE
                mTimeView!!.text = DateFormat.format("mm:ss", Date(mLastStopTime - mStartTime))
                mRouteView!!.text = mCurrentWalkRoute!!.name
            }
            else -> {
            }
        }
    }

    private fun buildNotification() {
        val builder = NotificationCompat.Builder(mActivity!!, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_current)
                .setContentTitle(if (mTimeState == TimeState.RUNNING) "Ongoing walk" else "Walk paused")
                .setSubText(mCurrentWalkRoute!!.name)
                .setContentText(DateFormat.format("mm:ss", Date(SystemClock.elapsedRealtime() - mStartTime)))
                .setOngoing(mTimeState == TimeState.RUNNING)
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_app))
                .setAutoCancel(true)
        val notificationIntent = Intent(activity, ActivityMain::class.java)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        builder.setContentIntent(PendingIntent.getActivity(activity, 0, notificationIntent, 0))
        addNotificationActions(builder)
        mNotificationManager!!.notify(NOTIFICATION_ID, builder.build())
    }

    private fun addNotificationActions(builder: NotificationCompat.Builder) {
        when (mTimeState) {
            FragmentCurrent.TimeState.RUNNING -> {
                val pauseIntent = Intent(activity, ActivityMain::class.java)
                pauseIntent.action = getString(R.string.notification_action_pause)
                val pausePendingIntent = PendingIntent.getActivity(activity, 0, pauseIntent, 0)
                val pauseAction = NotificationCompat.Action.Builder(
                        R.drawable.ic_walk, getString(R.string.notification_action_pause), pausePendingIntent).build()
                builder.addAction(pauseAction)
            }
            FragmentCurrent.TimeState.PAUSED -> {
                val resumeIntent = Intent(activity, ActivityMain::class.java)
                resumeIntent.action = getString(R.string.notification_action_resume)
                val resumePendingIntent = PendingIntent.getActivity(activity, 0, resumeIntent, 0)
                val resumeAction = NotificationCompat.Action.Builder(
                        R.drawable.ic_walk, getString(R.string.notification_action_resume), resumePendingIntent).build()
                builder.addAction(resumeAction)
            }
            else -> {
            }
        }
        //no checks as far as TimeState.STOPPED has no notification
        val stopIntent = Intent(activity, ActivityMain::class.java)
        stopIntent.action = getString(R.string.notification_action_stop)
        val stopPendingIntent = PendingIntent.getActivity(activity, 0, stopIntent, 0)
        val stopAction = NotificationCompat.Action.Builder(
                R.drawable.ic_walk, getString(R.string.notification_action_stop), stopPendingIntent).build()
        builder.addAction(stopAction)
    }

    private fun setProgressSeconds(seconds: Int) {
        if (mAnimator != null && mAnimator!!.isRunning) mAnimator!!.cancel()
        mAnimator = ObjectAnimator.ofInt(mProgressBar, "progress", seconds * 100)
        mAnimator!!.duration = 1500
        mAnimator!!.interpolator = DecelerateInterpolator()
        mAnimator!!.start()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu!!.clear()
        inflater!!.inflate(R.menu.menu_current, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.action_settings -> startActivity(Intent(mActivity, ActivitySettings::class.java))
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        //initializations
        if (mActivity == null) mActivity = activity
        mTimeView = Objects.requireNonNull<Activity>(mActivity).findViewById(R.id.timer)
        mRouteView = mActivity!!.findViewById(R.id.txt_current_route)
        mProgressBar = mActivity!!.findViewById(R.id.progressBar)
        mFab = mActivity!!.findViewById(R.id.fab_current_toggle)
        mFab!!.setOnClickListener { toggleStopwatch() }
        mBtnR = mActivity!!.findViewById(R.id.fab_current_stop)
        mBtnR!!.setOnClickListener { stopWalk() }
        if (mTimeState == null) mTimeState = TimeState.STOPPED
        if (mNotificationManager == null) mNotificationManager = activity!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mSharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        //custom actions
        if (mTimeState == TimeState.RUNNING) mHandler!!.post(mRunnable)
        if (mChannel == null) createNotificationChannel()
        updateUI()
    }

    private fun createNotificationChannel() {
        mChannel = NotificationChannel(CHANNEL_ID, getString(R.string.channel_name), NotificationManager.IMPORTANCE_LOW)
        mChannel!!.description = getString(R.string.channel_description)
        mChannel!!.enableLights(mSharedPref!!.getBoolean("notifications_led", false))
        mChannel!!.lightColor = Color.GREEN
        mChannel!!.enableVibration(mSharedPref!!.getBoolean("notifications_vibrate", false))
        mChannel!!.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
        mNotificationManager!!.createNotificationChannel(mChannel!!)
    }

    private enum class TimeState {
        RUNNING, PAUSED, STOPPED
    }

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "channel_walk"
    }
}


