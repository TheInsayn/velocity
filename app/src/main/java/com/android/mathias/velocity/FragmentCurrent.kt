package com.android.mathias.velocity

import android.animation.ObjectAnimator
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
import android.view.*
import android.view.animation.DecelerateInterpolator
import android.widget.*
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import com.android.mathias.velocity.FragmentCurrent.TimeState.*
import com.android.mathias.velocity.db.DBManager
import com.android.mathias.velocity.model.Route
import com.android.mathias.velocity.model.Walk
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*
import java.util.concurrent.TimeUnit

class FragmentCurrent : Fragment() {
    private lateinit var mTimeView: TextView
    private lateinit var mRouteView: TextView
    private lateinit var mFab: FloatingActionButton
    private lateinit var mBtnR: Button
    private lateinit var mProgressBar: ProgressBar

    private lateinit var mNotificationManager: NotificationManager
    private lateinit var mSharedPref: SharedPreferences
    private lateinit var mChannel: NotificationChannel

    private var mTimeState: TimeState? = null
    private var mStartTime: Long = 0
    private var mLastStopTime: Long = 0
    private var mCurrentWalkRoute: Route? = null
    private var mAnimator: ObjectAnimator? = null
    private var mHandler: Handler? = null

    private val mRunnable = object : Runnable {
        override fun run() {
            val time = SystemClock.elapsedRealtime() - mStartTime
            val sec = TimeUnit.MILLISECONDS.toSeconds(time).toInt() % 60
            mTimeView.text = DateFormat.format("mm:ss", Date(time))
            if (mSharedPref.getBoolean("enable_notifications", false)) {
                buildNotification()
            } else if (mNotificationManager.activeNotifications != null) {
                mNotificationManager.cancel(NOTIFICATION_ID)
            }
            setProgressSeconds(sec)
            mHandler!!.postDelayed(this, 1000)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_current, container, false)
        setHasOptionsMenu(true)
        mTimeView = view.findViewById(R.id.timer)
        mRouteView = view.findViewById(R.id.txt_current_route)
        mProgressBar = view.findViewById(R.id.progressBar)
        mFab = view.findViewById(R.id.fab_current_toggle)
        mBtnR = view.findViewById(R.id.fab_current_stop)
        mFab.setOnClickListener { toggleStopwatch() }
        mBtnR.setOnClickListener { stopWalk() }
        mSharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        createNotificationChannel()
        return view
    }

    private fun toggleStopwatch() {
        when (mTimeState) {
            STOPPED -> startWalk()
            RUNNING -> pauseStopwatch()
            PAUSED -> resumeStopwatch()
        }
    }

    private fun startWalk() {
        val routeNames = ArrayList<String>()
        for (r in DBManager.getRoutes(context!!, null)) {
            routeNames.add(r.name!!)
        }
        val defaultRouteName = mSharedPref.getString("default_route", "None")
        mCurrentWalkRoute = if (defaultRouteName == "None") {
            Route("No route set")
        } else {
            DBManager.getRoutes(context!!, defaultRouteName)[0]
        }
        if (routeNames.size > 0 && defaultRouteName == "None") {
            val adapter = ArrayAdapter<CharSequence>(context!!, R.layout.support_simple_spinner_dropdown_item)
            adapter.addAll(routeNames)
            adapter.add("None")
            val lpw = ListPopupWindow(context!!)
            lpw.setAdapter(adapter)
            lpw.anchorView = mFab
            lpw.setContentWidth(((mFab.parent as View).width / 2.5).toInt())
            lpw.setOnItemClickListener { adapterView, _, i, _ ->
                val selectedName = adapterView.getItemAtPosition(i).toString()
                if (selectedName != "None") {
                    mCurrentWalkRoute = DBManager.getRoutes(context!!, selectedName)[0]
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
        mTimeState = RUNNING
        updateUI()
        mHandler = Handler()
        mHandler!!.post(mRunnable)
    }

    internal fun pauseStopwatch() {
        mLastStopTime = SystemClock.elapsedRealtime()
        mTimeState = PAUSED
        updateUI()
        mHandler!!.removeCallbacks(mRunnable)
        buildNotification()
    }

    internal fun resumeStopwatch() {
        mStartTime += (SystemClock.elapsedRealtime() - mLastStopTime)
        mTimeState = RUNNING
        updateUI()
        mHandler!!.post(mRunnable)
    }

    internal fun stopWalk() {
        val walkTime = SystemClock.elapsedRealtime() - mStartTime
        val walk = Walk(walkTime, Date(), mCurrentWalkRoute!!)
        mLastStopTime = 0
        mCurrentWalkRoute = null
        mTimeState = STOPPED
        updateUI()
        DBManager.saveWalk(context!!, walk)
    }

    private fun updateUI() {
        when (mTimeState) {
            STOPPED -> {
                mFab.setImageResource(android.R.drawable.ic_media_play)
                mBtnR.isClickable = false
                mBtnR.visibility = View.INVISIBLE
                mRouteView.text = ""
                mProgressBar.progress = 0
                if (mNotificationManager.activeNotifications != null) mNotificationManager.cancel(NOTIFICATION_ID)
                if (mHandler != null) mHandler!!.removeCallbacks(mRunnable)
            }
            RUNNING -> {
                mFab.setImageResource(android.R.drawable.ic_media_pause)
                mBtnR.isClickable = true
                mBtnR.visibility = View.VISIBLE
                mRouteView.text = mCurrentWalkRoute!!.name
            }
            PAUSED -> {
                mFab.setImageResource(android.R.drawable.ic_media_play)
                mBtnR.isClickable = true
                mBtnR.visibility = View.VISIBLE
                mTimeView.text = DateFormat.format("mm:ss", Date(mLastStopTime - mStartTime))
                mRouteView.text = mCurrentWalkRoute!!.name
            }
        }
    }

    private fun buildNotification() {
        val intent = Intent(activity, ActivityMain::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(activity, 0, intent, 0)
        val builder = NotificationCompat.Builder(activity!!, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_current)
                .setContentTitle(if (mTimeState == RUNNING) "Ongoing walk" else "Walk paused")
                .setSubText(mCurrentWalkRoute!!.name)
                .setContentText(DateFormat.format("mm:ss", Date(SystemClock.elapsedRealtime() - mStartTime)))
                .setOngoing(mTimeState == RUNNING)
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_app))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
        addNotificationActions(builder)
        mNotificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    private fun addNotificationActions(builder: NotificationCompat.Builder) {
        val btn1Intent = Intent(activity, ActivityMain::class.java)
        val btn1PendingIntent = PendingIntent.getActivity(activity, 0, btn1Intent, 0)
        when (mTimeState) {
            RUNNING -> {
                btn1Intent.action = getString(R.string.notification_action_pause)
                builder.addAction(NotificationCompat.Action.Builder(
                        R.drawable.ic_walk,
                        getString(R.string.notification_action_pause),
                        btn1PendingIntent).build())
            }
            PAUSED -> {
                btn1Intent.action = getString(R.string.notification_action_resume)
                builder.addAction(NotificationCompat.Action.Builder(
                        R.drawable.ic_walk,
                        getString(R.string.notification_action_resume),
                        btn1PendingIntent).build())
            }
            else -> {
            }
        }
        //no checks as far as TimeState.STOPPED has no notification
        val stopIntent = Intent(activity, ActivityMain::class.java)
        stopIntent.action = getString(R.string.notification_action_stop)
        val stopPendingIntent = PendingIntent.getActivity(activity, 0, stopIntent, 0)
        builder.addAction(NotificationCompat.Action.Builder(
                R.drawable.ic_walk,
                getString(R.string.notification_action_stop),
                stopPendingIntent).build())
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
            R.id.action_settings -> startActivity(Intent(activity, ActivitySettings::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        if (mTimeState == null) mTimeState = STOPPED
        else if (mTimeState == RUNNING) mHandler!!.post(mRunnable)
        updateUI()
    }

    private fun createNotificationChannel() {
        mNotificationManager = activity!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mChannel = NotificationChannel(CHANNEL_ID, getString(R.string.channel_name), NotificationManager.IMPORTANCE_LOW)
        mChannel.description = getString(R.string.channel_description)
        mChannel.enableLights(mSharedPref.getBoolean("notifications_led", false))
        mChannel.lightColor = Color.GREEN
        mChannel.enableVibration(mSharedPref.getBoolean("notifications_vibrate", false))
        mChannel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
        mNotificationManager.createNotificationChannel(mChannel)
    }

    private enum class TimeState {
        RUNNING, PAUSED, STOPPED
    }

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "channel_walk"
    }
}


