package com.example.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.R

import androidx.glance.appwidget.appWidgetBackground

private val CLICK_COUNT_KEY = intPreferencesKey("click_count")

class HadithWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val count = currentState(key = CLICK_COUNT_KEY) ?: 0
            val texts = listOf(
                "إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ", // Arabic
                "Actions are according to intentions.", // English
                "اعمال کا دارومدار نیتوں پر ہے۔" // Urdu
            )

            val labels = listOf("Arabic", "English", "Urdu")
            val currentIndex = count % 3

            // Responsive size adaptation with increased size constraints
            val size = LocalSize.current
            val isSmall = size.width < 140.dp || size.height < 140.dp
            
            val titleSize = if (isSmall) 12.sp else 15.sp
            val baseTextSize = if (currentIndex == 0 || currentIndex == 2) {
                if (isSmall) 14.sp else 17.sp
            } else {
                if (isSmall) 12.sp else 15.sp
            }
            val containerPadding = if (isSmall) 8.dp else 14.dp
            val innerPadding = if (isSmall) 10.dp else 14.dp
            val glassMarginHorizontal = if (isSmall) 12.dp else 22.dp

            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .appWidgetBackground()
                    .background(ImageProvider(R.drawable.widget_background))
                    .padding(containerPadding)
                    .clickable(actionRunCallback<HadithCycleAction>()),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = GlanceModifier.fillMaxWidth()
                ) {
                    Text(
                        text = "📜 Daily Hadith (${labels[currentIndex]})",
                        style = TextStyle(
                            color = ColorProvider(Color(0xFFFBBF24)), // GoldPrimary
                            fontSize = titleSize,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    )
                    Spacer(GlanceModifier.height(if (isSmall) 8.dp else 12.dp))
                    
                    // Liquid Glass Card with outer horizontal margins to prevent boundary touching
                    Box(
                        modifier = GlanceModifier
                            .padding(horizontal = glassMarginHorizontal)
                            .background(ImageProvider(R.drawable.widget_glass_card))
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = texts[currentIndex],
                            style = TextStyle(
                                color = ColorProvider(Color.White),
                                fontSize = baseTextSize,
                                textAlign = if (currentIndex == 0 || currentIndex == 2) TextAlign.Right else TextAlign.Center
                            ),
                            modifier = GlanceModifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

class HadithCycleAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        updateAppWidgetState(context, glanceId) { prefs ->
            val current = prefs[CLICK_COUNT_KEY] ?: 0
            prefs[CLICK_COUNT_KEY] = current + 1
        }
        HadithWidget().update(context, glanceId)
    }
}

class HadithWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = HadithWidget()
}
