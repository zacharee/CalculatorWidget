package com.zacharee1.calculatorwidget

import android.appwidget.AppWidgetManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import com.huawei.hms.analytics.HiAnalytics
import com.huawei.hms.analytics.HiAnalyticsTools
import com.jaredrummler.android.colorpicker.ColorPreferenceCompat

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        HiAnalyticsTools.enableLog()

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
        private val textColor by lazy { findPreference<ColorPreferenceCompat>("text_color") as ColorPreferenceCompat }
        private val borderColor by lazy { findPreference<ColorPreferenceCompat>("border_color") as ColorPreferenceCompat }
        private val backgroundColor by lazy { findPreference<ColorPreferenceCompat>("background_color") as ColorPreferenceCompat }

        val instance by lazy { HiAnalytics.getInstance(requireContext()) }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.prefs_main, rootKey)

            val bundle = Bundle()
            bundle.putString("preferences_created", "prefs_main")

            instance.onEvent("PreferenceCreation", bundle)

            setListeners()
        }

        private fun setListeners() {
            textColor.saveValue(requireActivity().prefManager.getTextColorForId(widgetId))
            borderColor.saveValue(requireActivity().prefManager.getBorderColorForId(widgetId))
            backgroundColor.saveValue(requireActivity().prefManager.getBackgroundColorForId(widgetId))

            textColor.setOnPreferenceChangeListener { _, newValue ->
                requireActivity().prefManager.setTextColorForId(widgetId, newValue.toString().toInt())
                CalcProvider.update(requireActivity())

                val bundle = Bundle()
                bundle.putString("text_color", newValue.toString())

                instance.onEvent("SetTextColor", bundle)
                instance.setUserProfile("current_text_color", newValue.toString())

                true
            }

            borderColor.setOnPreferenceChangeListener { _, newValue ->
                requireActivity().prefManager.setBorderColorForId(widgetId, newValue.toString().toInt())
                CalcProvider.update(requireActivity())

                val bundle = Bundle()
                bundle.putString("border_color", newValue.toString())

                instance.onEvent("SetBorderColor", bundle)
                instance.setUserProfile("current_border_color", newValue.toString())

                true
            }

            backgroundColor.setOnPreferenceChangeListener { _, newValue ->
                requireActivity().prefManager.setBackgroundColorForId(widgetId, newValue.toString().toInt())
                CalcProvider.update(requireActivity())

                val bundle = Bundle()
                bundle.putString("background_color", newValue.toString())

                instance.onEvent("SetBackgroundColor", bundle)
                instance.setUserProfile("current_background_color", newValue.toString())

                true
            }
        }
    }
}
