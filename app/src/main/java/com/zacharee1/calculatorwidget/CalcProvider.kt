package com.zacharee1.calculatorwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.*
import android.graphics.Color
import android.view.View
import android.widget.RemoteViews
import android.widget.Toast
import androidx.compose.ui.text.intl.Locale
import androidx.core.text.HtmlCompat

/**
 * Implementation of App Widget functionality.
 */
open class CalcProvider : AppWidgetProvider() {
    companion object {
        private const val ACTION_BUTTON_PRESSED = "${BuildConfig.APPLICATION_ID}.action.BUTTON_PRESSED"
        private const val EXTRA_BUTTON = "button"
        private const val EXTRA_ID = "id"

        private const val DIVIDE = '\u00F7'
        private const val MULTIPLY = '\u00D7'
        private const val SUBTRACT = '\u2212'
        private const val NEGATIVE = '-'
        private const val ADD = '\u002B'
        private const val DOT = '\u002E'
        private const val DELETE = '\u007F'
        private const val CLEAR = '\u239A'
        private const val EQUALS = '\u003D'
        private const val INPUT = '\u2402'

        private val borders = hashMapOf(
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
                EQUALS to R.id.equals_border,
                "settings" to R.id.settings_border,
                NEGATIVE to R.id.negative_border,
        )

        private val numbers = hashMapOf(
                '1' to R.id.one,
                '2' to R.id.two,
                '3' to R.id.three,
                '4' to R.id.four,
                '5' to R.id.five,
                '6' to R.id.six,
                '7' to R.id.seven,
                '8' to R.id.eight,
                '9' to R.id.nine,
                '0' to R.id.zero,
        )

        private val functions = hashMapOf(
                DIVIDE to R.id.divide,
                MULTIPLY to R.id.times,
                SUBTRACT to R.id.minus,
                ADD to R.id.plus,
                DOT to R.id.dot,
                DELETE to R.id.delete,
                CLEAR to R.id.clear,
                EQUALS to R.id.equals,
                INPUT to R.id.input_text,
                NEGATIVE to R.id.negative,
        )

        private val all = HashMap(numbers).apply { putAll(HashMap(functions)) }

        private var currentInputText = HashMap<Int, ArrayList<String>?>()
        private var results = HashMap<Int, String>()

        private fun isNotOperator(button: Char?) = button != DIVIDE && button != MULTIPLY && button != SUBTRACT && button != ADD
        private fun isNotOperator(button: String?) = button != null && button.length > 1 || isNotOperator(button?.toCharArray()?.get(0))
        private fun isOperator(button: Char?) = !isNotOperator(button)
        private fun isOperator(button: String?) = !isNotOperator(button)

        private fun addToMapIfNeeded(id: Int): ArrayList<String> {
            return currentInputText[id] ?: arrayListOf<String>().also { currentInputText[id] = it }
        }

        private fun canAddDot(id: Int): Boolean {
            var canAdd = true

            val string = addToMapIfNeeded(id).joinToString("")
            if (string.contains(DOT)) {
                string.forEach {
                    if (it == DOT) canAdd = false
                    else if (!it.isDigit()) canAdd = true
                }
            }

            return canAdd
        }

        private fun canAddNegative(id: Int): Boolean {
            var canAdd = true

            val string = addToMapIfNeeded(id).joinToString("")
            if (string.contains(NEGATIVE)) {
                string.forEach {
                    if (it == NEGATIVE) canAdd = false
                    else if (!it.isDigit()) canAdd = true
                }
            }

            return canAdd
        }

        private fun performOp(first: Double, last: Double, op: Char?): Double {
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
                            val text = addToMapIfNeeded(id)

                            text.forEach {
                                if (isNotOperator(it)) {
                                    temp.add(it)
                                } else {
                                    numbers.add(temp.joinToString(""))
                                    temp.clear()
                                    numbers.add(it)
                                }
                            }

                            if (temp.isNotEmpty()) numbers.add(temp.joinToString(""))

                            var result = Double.MIN_VALUE
                            var prevOp: Char? = null

                            numbers.forEach {
                                if (isNotOperator(it)) {
                                    if (result == Double.MIN_VALUE) result = it.toDouble()
                                    else if (prevOp != null) result = performOp(result, it.toDouble(), prevOp)
                                } else {
                                    prevOp = it[0]
                                }
                            }

                            text.clear()

                            results[id] = result.toString()
                        }

                        DELETE -> {
                            val t = addToMapIfNeeded(id)
                            if (t.isNotEmpty()) t.removeAt(t.lastIndex)
                        }

                        CLEAR -> {
                            addToMapIfNeeded(id).clear()
                            results.remove(id)
                        }

                        INPUT -> {
                            val result = results[id] ?: return
                            val cbm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

                            cbm.setPrimaryClip(ClipData.newPlainText(context.resources.getString(R.string.app_name), result))
                            Toast.makeText(context, context.resources.getString(R.string.copied, result), Toast.LENGTH_SHORT).show()
                        }

                        else -> {
                            val text = addToMapIfNeeded(id)

                            val last = if (text.isNotEmpty()) text[text.lastIndex] else null
                            val oldResult = results[id]

                            val canAddForResult = (isOperator(button) && !oldResult.isNullOrBlank())
                            val canAdd =
                                    (!(isOperator(button) && text.isEmpty())
                                            && !(isOperator(button) && isOperator(last))
                                            && if (button == NEGATIVE) canAddNegative(id) else true
                                            && if (button == DOT) canAddDot(id) else true)
                                            || canAddForResult
                            if (canAdd) {
                                if (canAddForResult) text.add(oldResult)
                                text.add(button.toString())
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
                    PendingIntent.getActivity(context, 100,
                            Intent(context, SettingsActivity::class.java)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, it),
                        PendingIntent.FLAG_IMMUTABLE))
            views.setImageViewResource(R.id.settings, R.drawable.settings)
            views.setInt(R.id.settings, "setColorFilter", tcNoAlpha)
            views.setInt(R.id.settings, "setImageAlpha", textAlpha)

            all.forEach { (key, value) ->
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
            }

            var text = current.joinToString("")
            val result = results[it]
            val isResult = text.isBlank() && result != null

            if (isResult) text = result

            val formatted = if (isResult) try {
                text.format(Locale.current, "%8.8g")
            } catch (_: Exception) {
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

    private fun makeIntent(context: Context, button: Char, id: Int): Intent {
        return Intent(ACTION_BUTTON_PRESSED).apply {
            component = getComponent(context)
            `package` = context.packageName
            putExtra(EXTRA_BUTTON, button)
            putExtra(EXTRA_ID, id)
        }
    }

    private fun makePendingIntent(context: Context, button: Char, id: Int): PendingIntent {
        return PendingIntent.getBroadcast(context, button.hashCode(),
                    makeIntent(context, button, id), PendingIntent.FLAG_IMMUTABLE)
    }
}
