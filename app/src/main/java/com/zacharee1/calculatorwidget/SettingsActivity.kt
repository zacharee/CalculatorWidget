package com.zacharee1.calculatorwidget

import android.appwidget.AppWidgetManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import com.jaredrummler.android.colorpicker.ColorPreferenceCompat

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        supportFragmentManager
                ?.beginTransaction()
                ?.replace(R.id.content,
                        Prefs.newInstance(intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)))
                ?.commit()
    }

    class Prefs private constructor(): PreferenceFragmentCompat() {
        companion object {
            fun newInstance(widgetId: Int): Prefs {
                val instance = Prefs()
                val args = Bundle()

                args.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                instance.arguments = args

                return instance
            }
        }

        private val widgetId by lazy { arguments!!.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID) }
        private val textColor by lazy { findPreference("text_color") as ColorPreferenceCompat }
        private val borderColor by lazy { findPreference("border_color") as ColorPreferenceCompat }
        private val backgroundColor by lazy { findPreference("background_color") as ColorPreferenceCompat }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.prefs_main, rootKey)

            setListeners()
        }

        private fun setListeners() {
            textColor.saveValue(activity!!.prefManager.getTextColorForId(widgetId))
            borderColor.saveValue(activity!!.prefManager.getBorderColorForId(widgetId))
            backgroundColor.saveValue(activity!!.prefManager.getBackgroundColorForId(widgetId))

            textColor.setOnPreferenceChangeListener { _, newValue ->
                activity!!.prefManager.setTextColorForId(widgetId, newValue.toString().toInt())
                CalcProvider.update(activity!!)

                true
            }

            borderColor.setOnPreferenceChangeListener { _, newValue ->
                activity!!.prefManager.setBorderColorForId(widgetId, newValue.toString().toInt())
                CalcProvider.update(activity!!)

                true
            }

            backgroundColor.setOnPreferenceChangeListener { _, newValue ->
                activity!!.prefManager.setBackgroundColorForId(widgetId, newValue.toString().toInt())
                CalcProvider.update(activity!!)

                true
            }
        }
    }
}
