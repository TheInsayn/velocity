package com.android.mathias.velocity


import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.preference.*
import android.text.TextUtils
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class ActivitySettings : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setSupportActionBar(findViewById(R.id.toolbar))
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }
        fragmentManager.beginTransaction().replace(R.id.frame_preferences, FragmentPreferences()).commit()
    }

    class FragmentPreferences : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.preferences)
            setHasOptionsMenu(true)

            val routePref = findPreference("default_route") as ListPreference
            val routes = DBManager.getRoutes(context, null)
            val routeNames = ArrayList<String>()
            for (r in routes) {
                routeNames.add(r.name!!)
            }
            routeNames.add("None")
            routePref.entries = routeNames.toTypedArray<CharSequence>()
            routePref.entryValues = routeNames.toTypedArray<CharSequence>()
            routePref.setDefaultValue("None")

            bindPreferenceSummaryToValue(routePref)
            bindPreferenceSummaryToValue(findPreference("display_name"))
            bindPreferenceSummaryToValue(findPreference("notifications_sound"))
            bindPreferenceSummaryToValue(findPreference("sync_frequency"))
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            val id = item.itemId
            if (id == android.R.id.home) {
                activity.finish()
                return true
            }
            return super.onOptionsItemSelected(item)
        }
    }

    companion object {
        private val sBindPreferenceSummaryToValueListener = Preference.OnPreferenceChangeListener { preference: Preference, value: Any ->
            val stringValue = value.toString()
            if (preference is ListPreference) {
                val index = preference.findIndexOfValue(stringValue)
                preference.setSummary(if (index >= 0) preference.entries[index] else null)
            } else if (preference is RingtonePreference) {
                if (TextUtils.isEmpty(stringValue)) {
                    preference.setSummary(R.string.pref_sound_none)
                } else {
                    val ringtone = RingtoneManager.getRingtone(preference.getContext(), Uri.parse(stringValue))
                    if (ringtone == null) {
                        preference.setSummary(null)
                    } else {
                        val name = ringtone.getTitle(preference.getContext())
                        preference.setSummary(name)
                    }
                }
            } else {
                preference.summary = stringValue
            }
            //bindPreferenceSummaryToValue(preference);
            true
        }

        private fun bindPreferenceSummaryToValue(preference: Preference) {
            preference.onPreferenceChangeListener = sBindPreferenceSummaryToValueListener
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, PreferenceManager.getDefaultSharedPreferences(preference.context).getString(preference.key, ""))
        }
    }
}
