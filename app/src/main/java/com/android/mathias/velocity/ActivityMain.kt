package com.android.mathias.velocity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class ActivityMain : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    private var mFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        val navigationView = findViewById<BottomNavigationView>(R.id.navigation)
        navigationView.setOnNavigationItemSelectedListener(this)
        mFragment = showFragment(FragmentCurrent::class.java)
    }

    override fun onBackPressed() {
        this.moveTaskToBack(true)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        var fragment: Class<*>? = null
        when (item.itemId) {
            R.id.nav_current -> fragment = FragmentCurrent::class.java
            R.id.nav_history -> fragment = FragmentHistory::class.java
            R.id.nav_routes -> fragment = FragmentRoutes::class.java
            else -> {}
        }
        if (fragment != null && !item.isChecked) {
            showFragment(fragment)
            item.isChecked = true
            title = item.title
        }
        return true
    }

    private fun showFragment(fragmentClass: Class<*>): Fragment? {
        val manager = supportFragmentManager
        val backStateName = fragmentClass.name
        var fragment: Fragment? = null
        val fragmentPopped = manager.popBackStackImmediate(backStateName, 0)
        if (!fragmentPopped) {
            val ft = manager.beginTransaction()
            try {
                fragment = fragmentClass.newInstance() as Fragment
                ft.replace(R.id.frame_content, fragment)
                ft.addToBackStack(backStateName)
                ft.commit()
            } catch (e: InstantiationException) {
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            }

        }
        return fragment
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (mFragment != null && mFragment is FragmentCurrent) {
            val fragment = mFragment as FragmentCurrent?
            val action = intent.action
            if (action != null && action != "") {
                when (action) {
                    getString(R.string.notification_action_pause) -> fragment!!.pauseStopwatch()
                    getString(R.string.notification_action_resume) -> fragment!!.resumeStopwatch()
                    getString(R.string.notification_action_stop) -> fragment!!.stopWalk()
                }
            }
        }
    }
}
