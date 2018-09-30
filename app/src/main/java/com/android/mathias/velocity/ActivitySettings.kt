package com.android.mathias.velocity


import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.android.mathias.velocity.db.DBManager

class ActivitySettings : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportFragmentManager.beginTransaction()
                .replace(R.id.frame_preferences, FragmentPreferences())
                .commit()
    }

    class FragmentPreferences : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)
            val routePref = findPreference("default_route") as ListPreference
            val routes = DBManager.getRoutes(context!!, null)
            val routeNames = ArrayList<String>()
            for (r in routes) routeNames.add(r.name!!)
            routeNames.add("None")
            routePref.entries = routeNames.toTypedArray<CharSequence>()
            routePref.entryValues = routeNames.toTypedArray<CharSequence>()
            routePref.setDefaultValue("None")
            bindSummaryToValue(routePref)
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            val id = item.itemId
            if (id == android.R.id.home) {
                activity!!.finish()
                return true
            }
            return super.onOptionsItemSelected(item)
        }
    }

    companion object {
        private val valueListener =
                Preference.OnPreferenceChangeListener { preference: Preference, value: Any ->
                    val stringValue = value.toString()
                    when (preference) {
                        is ListPreference -> {
                            val index = preference.findIndexOfValue(stringValue)
                            preference.setSummary(if (index >= 0) preference.entries[index] else null)
                        }
                        else -> preference.summary = stringValue
                    }
                    true
                }

        private fun bindSummaryToValue(preference: Preference) {
            preference.onPreferenceChangeListener = valueListener
            valueListener.onPreferenceChange(preference, PreferenceManager
                    .getDefaultSharedPreferences(preference.context)
                    .getString(preference.key, ""))
        }
    }
}
