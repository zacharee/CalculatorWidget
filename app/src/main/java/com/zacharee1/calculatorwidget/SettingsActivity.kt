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
            .beginTransaction()
            .replace(R.id.content,
                Prefs.newInstance(intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)))
            .commit()
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

        private val widgetId by lazy { requireArguments().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID) }
        private val textColor by lazy { findPreference<ColorPreferenceCompat>("text_color") }
        private val borderColor by lazy { findPreference<ColorPreferenceCompat>("border_color") }
        private val backgroundColor by lazy { findPreference<ColorPreferenceCompat>("background_color") }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.prefs_main, rootKey)

            setListeners()
        }

        private fun setListeners() {
            textColor?.saveValue(requireActivity().prefManager.getTextColorForId(widgetId))
            borderColor?.saveValue(requireActivity().prefManager.getBorderColorForId(widgetId))
            backgroundColor?.saveValue(requireActivity().prefManager.getBackgroundColorForId(widgetId))

            textColor?.setOnPreferenceChangeListener { _, newValue ->
                requireActivity().prefManager.setTextColorForId(widgetId, newValue.toString().toInt())
                CalcProvider.update(requireActivity())

                true
            }

            borderColor?.setOnPreferenceChangeListener { _, newValue ->
                requireActivity().prefManager.setBorderColorForId(widgetId, newValue.toString().toInt())
                CalcProvider.update(requireActivity())

                true
            }

            backgroundColor?.setOnPreferenceChangeListener { _, newValue ->
                requireActivity().prefManager.setBackgroundColorForId(widgetId, newValue.toString().toInt())
                CalcProvider.update(requireActivity())

                true
            }
        }
    }
}
