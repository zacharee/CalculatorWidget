package com.zacharee1.calculatorwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.*
import android.graphics.Color
import android.text.TextUtils
import android.view.View
import android.widget.RemoteViews
import android.widget.Toast
import androidx.core.text.HtmlCompat
import java.text.DecimalFormat

/**
 * Implementation of App Widget functionality.
 */
open class CalcProvider : AppWidgetProvider() {
    companion object {
        const val ACTION_BUTTON_PRESSED = "com.zacharee1.calculatorwidget.action.BUTTON_PRESSED"
        const val EXTRA_BUTTON = "button"
        const val EXTRA_ID = "id"

        const val DIVIDE = '\u00F7'
        const val MULTIPLY = '\u00D7'
        const val SUBTRACT = '\u2212'
        const val ADD = '\u002B'
        const val DOT = '\u002E'
        const val DELETE = '\u007F'
        const val CLEAR = '\u239A'
        const val EQUALS = '\u003D'
        const val INPUT = '\u2402'
        
        val borders = hashMapOf(
                '1' to R.id.one_border,
                '2' to R.id.two_border,
                '3' to R.id.three_border,
                '4' to R.id.four_border,
                '5' to R.id.five_border,
                '6' to R.id.six_border,
                '7' to R.id.seven_border,
                '8' to R.id.eight_border,
                '9' to R.id.nine_border,
                '0' to R.id.zero_border,
                INPUT to R.id.input_border,
                DIVIDE to R.id.divide_border,
                MULTIPLY to R.id.times_border,
                SUBTRACT to R.id.minus_border,
                ADD to R.id.plus_border,
                DOT to R.id.dot_border,
                DELETE to R.id.delete_border,
                CLEAR to R.id.clear_border,
                EQUALS to R.id.equals_border
        )

        val numbers = hashMapOf(
                '1' to R.id.one,
                '2' to R.id.two,
                '3' to R.id.three,
                '4' to R.id.four,
                '5' to R.id.five,
                '6' to R.id.six,
                '7' to R.id.seven,
                '8' to R.id.eight,
                '9' to R.id.nine,
                '0' to R.id.zero
        )

        val functions = hashMapOf(
                DIVIDE to R.id.divide,
                MULTIPLY to R.id.times,
                SUBTRACT to R.id.minus,
                ADD to R.id.plus,
                DOT to R.id.dot,
                DELETE to R.id.delete,
                CLEAR to R.id.clear,
                EQUALS to R.id.equals,
                INPUT to R.id.input_text
        )

        val all = HashMap(numbers).apply { putAll(HashMap(functions)) }

        var currentInputText = HashMap<Int, ArrayList<String>?>()
        var results = HashMap<Int, String>()

        fun isNotOperator(button: Char?) = button != DIVIDE && button != MULTIPLY && button != SUBTRACT && button != ADD
        fun isNotOperator(button: String?) = button != null && button.length > 1 || isNotOperator(button?.toCharArray()?.get(0))
        fun isOperator(button: Char?) = !isNotOperator(button)
        fun isOperator(button: String?) = !isNotOperator(button)

        fun canAddDot(id: Int): Boolean {
            var canAdd = true

            val string = TextUtils.join("", currentInputText[id]!!)
            if (string.contains(DOT)) {
                string.forEach {
                    if (it == DOT) canAdd = false
                    else if (!it.isDigit()) canAdd = true
                }
            }

            return canAdd
        }

        fun performOp(first: Double, last: Double, op: Char?): Double {
            when (op) {
                DIVIDE -> return first / last
                MULTIPLY -> return first * last
                SUBTRACT -> return first - last
                ADD -> return first + last
            }

            return Double.MIN_VALUE
        }

        fun update(context: Context) {
            val manager = AppWidgetManager.getInstance(context)

            val ids = manager.getAppWidgetIds(getComponent(context))
            val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
            intent.`package` = context.packageName
            intent.component = getComponent(context)
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)

            context.sendBroadcast(intent)
        }

        private fun getComponent(context: Context) = ComponentName(context, CalcProvider::class.java)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            ACTION_BUTTON_PRESSED -> {
                if (intent.hasExtra(EXTRA_BUTTON) && intent.hasExtra(EXTRA_ID)) {
                    val button = intent.getCharExtra(EXTRA_BUTTON, Char.MAX_HIGH_SURROGATE)
                    val id = intent.getIntExtra(EXTRA_ID, -1)

                    when (button) {
                        EQUALS -> {
                            val numbers = ArrayList<String>()
                            val temp = ArrayList<String>()

                            currentInputText[id]?.forEach {
                                if (isNotOperator(it)) {
                                    temp.add(it)
                                } else {
                                    numbers.add(TextUtils.join("", temp))
                                    temp.clear()
                                    numbers.add(it)
                                }
                            }

                            if (temp.isNotEmpty()) numbers.add(TextUtils.join("", temp))

                            var result = Double.MIN_VALUE
                            var prevOp: Char? = null

                            numbers.forEach {
                                if (isNotOperator(it)) {
                                    if (result == Double.MIN_VALUE) result = it.toDouble()
                                    else if (prevOp != null) result = performOp(result, it.toDouble(), prevOp)
                                } else {
                                    prevOp = it.toCharArray()[0]
                                }
                            }

                            currentInputText[id]?.clear()

                            results[id] = result.toString()
                        }

                        DELETE -> {
                            val t = currentInputText[id] ?: return
                            if (t.size > 0) t.removeAt(t.lastIndex)
                        }

                        CLEAR -> {
                            currentInputText[id]?.clear()
                            results.remove(id)
                        }

                        INPUT -> {
                            val result = results[id] ?: return
                            val cbm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

                            cbm.primaryClip = ClipData.newPlainText(context.resources.getString(R.string.app_name), result)
                            Toast.makeText(context, context.resources.getString(R.string.copied, result), Toast.LENGTH_SHORT).show()
                        }

                        else -> {
                            val text = currentInputText[id] ?: addToMapIfNeeded(id)

                            val last = if (text.size > 0) text[text.lastIndex] else null
                            val oldResult = results[id]

                            val canAddForResult = (isOperator(button) && oldResult != null && !oldResult.isBlank())
                            val canAdd =
                                    (!(isOperator(button) && text.size < 1)
                                            && !(isOperator(button) && isOperator(last))
                                            && if (button == DOT) canAddDot(id) else true)
                                            || canAddForResult
                            if (canAdd) {
                                if (canAddForResult) text.add(oldResult!!)
                                text.add(Character.toString(button))
                            }
                        }
                    }

                    update(context)
                }
            }
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach {
            val current = addToMapIfNeeded(it)
            val views = RemoteViews(context.packageName, R.layout.calc_provider)

            views.setViewVisibility(R.id.loader, View.GONE)

            views.setInt(R.id.content, "setBackgroundColor",
                    context.prefManager.getBackgroundColorForId(it))

            val textColor = context.prefManager.getTextColorForId(it)
            val textAlpha = Color.alpha(textColor)
            val tcNoAlpha = Color.argb(255, Color.red(textColor),
                    Color.green(textColor), Color.blue(textColor))

            views.setOnClickPendingIntent(R.id.settings,
                    PendingIntent.getActivity(context, 's'.hashCode() + it.hashCode() + Math.random().hashCode(),
                            Intent(context, SettingsActivity::class.java)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, it), 0))
            views.setImageViewResource(R.id.settings, R.drawable.settings)
            views.setInt(R.id.settings, "setColorFilter", tcNoAlpha)
            views.setInt(R.id.settings, "setImageAlpha", textAlpha)

            all.keys.forEach { key ->
                val value = all[key]!!

                views.setOnClickPendingIntent(value, makePendingIntent(context, key, it))
                views.setTextColor(value, textColor)
            }

            val borderColor = context.prefManager.getBorderColorForId(it)
            val borderAlpha = Color.alpha(borderColor)
            val bcNoAlpha = Color.argb(255, Color.red(borderColor),
                    Color.green(borderColor), Color.blue(borderColor))

            borders.values.forEach { id ->
                views.setImageViewResource(id, R.drawable.border)
                views.setInt(id, "setColorFilter", bcNoAlpha)
                views.setInt(id, "setImageAlpha", borderAlpha)
                appWidgetManager.updateAppWidget(it, views)
                appWidgetManager.notifyAppWidgetViewDataChanged(it, id)
            }

            val format = DecimalFormat("0.########")
            var text = TextUtils.join("", current)
            val isResult = text.isBlank() && results[it] != null

            if (isResult) text = results[it]

            val formatted = if (isResult) try {
                format.format(text.toDouble())
            } catch (e: Exception) {
                text
            } else text

            views.setTextViewText(R.id.input_text, HtmlCompat.fromHtml(formatted, 0))
            appWidgetManager.updateAppWidget(it, views)
        }
    }

    override fun onEnabled(context: Context) {}

    override fun onDisabled(context: Context) {}

    override fun onDeleted(context: Context?, appWidgetIds: IntArray) {
        appWidgetIds.forEach { currentInputText.remove(it) }
    }


    private fun addToMapIfNeeded(id: Int): ArrayList<String> {
        if (!currentInputText.containsKey(id)) currentInputText[id] = ArrayList()
        return currentInputText[id]!!
    }

    private fun makeIntent(context: Context, button: Char, id: Int): Intent {
        return Intent(ACTION_BUTTON_PRESSED).apply {
            component = getComponent(context)
            `package` = component!!.packageName
            putExtra(EXTRA_BUTTON, button)
            putExtra(EXTRA_ID, id)
        }
    }

    private fun makePendingIntent(context: Context, button: Char, id: Int): PendingIntent {
        return PendingIntent.getBroadcast(context, button.hashCode() + id.hashCode() + Math.random().hashCode(),
                    makeIntent(context, button, id), 0)
    }
}

