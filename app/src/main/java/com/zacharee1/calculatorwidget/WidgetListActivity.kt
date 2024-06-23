package com.zacharee1.calculatorwidget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState

class WidgetListActivity : ComponentActivity() {
    private val appWidgetManager by lazy { AppWidgetManager.getInstance(this) }
    private var currentWidgets by mutableStateOf(listOf<Int>())

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val lifecycleState by LocalLifecycleOwner.current.lifecycle.currentStateAsState()

            LaunchedEffect(key1 = lifecycleState) {
                if (lifecycleState >= Lifecycle.State.RESUMED) {
                    currentWidgets = appWidgetManager.getAppWidgetIds(
                        ComponentName(
                            this@WidgetListActivity,
                            CalcProvider::class.java
                        )
                    ).toList()
                }
            }

            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = {
                            BottomAppBar(
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Spacer(modifier = Modifier.weight(1f))

                                IconButton(
                                    onClick = {
                                        try {
                                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/zacharee/CalculatorWidget/blob/master/PRIVACY.md")))
                                        } catch (_: Throwable) {}
                                    },
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.baseline_privacy_tip_24),
                                        contentDescription = stringResource(R.string.privacy_policy),
                                    )
                                }

                                Spacer(modifier = Modifier.weight(1f))
                            }
                        },
                        content = { padding ->
                            val layoutDirection = LocalLayoutDirection.current
                            val systemBarsPadding = WindowInsets.systemBars.asPaddingValues()

                            Crossfade(
                                modifier = Modifier.fillMaxSize(),
                                targetState = currentWidgets.isEmpty(),
                                label = "WidgetsListFade",
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (it) {
                                        Text(
                                            text = stringResource(R.string.add_widget_hint),
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(
                                                top = systemBarsPadding.calculateTopPadding(),
                                                bottom = padding.calculateBottomPadding(),
                                                start = padding.calculateStartPadding(layoutDirection),
                                                end = padding.calculateEndPadding(layoutDirection),
                                            ),
                                        )
                                    } else {
                                        LazyColumn(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(horizontal = 8.dp),
                                            contentPadding = PaddingValues(
                                                top = systemBarsPadding.calculateTopPadding(),
                                                bottom = padding.calculateBottomPadding(),
                                                start = padding.calculateStartPadding(layoutDirection),
                                                end = padding.calculateEndPadding(layoutDirection),
                                            ),
                                            verticalArrangement = Arrangement.spacedBy(8.dp),
                                        ) {
                                            items(items = currentWidgets, { it }) { widgetId ->
                                                OutlinedCard(
                                                    onClick = {
                                                        startActivity(
                                                            Intent(
                                                                this@WidgetListActivity,
                                                                SettingsActivity::class.java,
                                                            ).apply {
                                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                                putExtra(
                                                                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                                                                    widgetId,
                                                                )
                                                            },
                                                        )
                                                    },
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .heightIn(min = 56.dp),
                                                        contentAlignment = Alignment.Center,
                                                    ) {
                                                        Text(
                                                            text = stringResource(
                                                                R.string.widget_format,
                                                                widgetId,
                                                            )
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        },
                    )
                }
            }
        }
    }
}
