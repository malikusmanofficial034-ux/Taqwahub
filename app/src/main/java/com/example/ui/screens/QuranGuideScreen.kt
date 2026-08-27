package com.example.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GuideLanguage
import com.example.data.QuranGuideRepository
import com.example.ui.theme.EmeraldBackground
import com.example.ui.theme.EmeraldCard
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.TextGray
import com.example.util.FontHelper
import com.example.viewmodel.TaqwaViewModel

@Composable
fun QuranGuideScreen(
    viewModel: TaqwaViewModel,
    onBack: () -> Unit
) {
    var selectedLanguage by remember { mutableStateOf(GuideLanguage.ENGLISH) }
    var selectedSubCardIndex by remember { mutableStateOf(0) } // 0: History, 1: Tajweed, 2: Waqf Symbols

    val nuzulGuide = remember(selectedLanguage) { QuranGuideRepository.getNuzulHistory(selectedLanguage) }
    val tajweedGuide = remember(selectedLanguage) { QuranGuideRepository.getTajweedGuide(selectedLanguage) }
    val waqfGuide = remember(selectedLanguage) { QuranGuideRepository.getWaqfGuide(selectedLanguage) }

    val isRtl = selectedLanguage.isRtl

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(EmeraldBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Custom Top Bar with Back Navigation Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(36.dp)
                        .background(GoldPrimary.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = GoldPrimary
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = when (selectedLanguage) {
                            GuideLanguage.ENGLISH -> "Quran Complete Guide"
                            GuideLanguage.URDU -> "مکمل معلوماتِ قرآن"
                            GuideLanguage.ARABIC -> "دليل القرآن الكريم"
                        },
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = when (selectedLanguage) {
                            GuideLanguage.ENGLISH -> "Revelation, Tajweed Rules & Stop Symbols"
                            GuideLanguage.URDU -> "نزول و تدوین، تجوید اور علاماتِ وقف"
                            GuideLanguage.ARABIC -> "تاريخ النزول والتجويد وعلامات الوقف"
                        },
                        color = TextGray,
                        fontSize = 11.sp
                    )
                }
            }

            // Scrollable Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("quran_guide_screen_content"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Multi-Language Toggle Row
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(EmeraldCard, RoundedCornerShape(14.dp))
                            .border(1.dp, GoldPrimary.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GuideLanguage.values().forEach { lang ->
                            val isSelected = (selectedLanguage == lang)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) GoldPrimary else Color.Transparent)
                                    .clickable { selectedLanguage = lang }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = lang.label,
                                    color = if (isSelected) EmeraldCard else Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Sub-Cards Segment Navigation
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SubCardTabChip(
                            title = when (selectedLanguage) {
                                GuideLanguage.ENGLISH -> "Nuzul History"
                                GuideLanguage.URDU -> "تاریخِ نزول"
                                GuideLanguage.ARABIC -> "تاريخ النزول"
                            },
                            icon = Icons.Default.HistoryEdu,
                            isSelected = selectedSubCardIndex == 0,
                            modifier = Modifier.weight(1f)
                        ) { selectedSubCardIndex = 0 }

                        SubCardTabChip(
                            title = when (selectedLanguage) {
                                GuideLanguage.ENGLISH -> "Tajweed Rules"
                                GuideLanguage.URDU -> "قواعدِ تجوید"
                                GuideLanguage.ARABIC -> "أحكام التجويد"
                            },
                            icon = Icons.Default.RecordVoiceOver,
                            isSelected = selectedSubCardIndex == 1,
                            modifier = Modifier.weight(1f)
                        ) { selectedSubCardIndex = 1 }

                        SubCardTabChip(
                            title = when (selectedLanguage) {
                                GuideLanguage.ENGLISH -> "Waqf Symbols"
                                GuideLanguage.URDU -> "علاماتِ وقف"
                                GuideLanguage.ARABIC -> "علامات الوقف"
                            },
                            icon = Icons.Default.Book,
                            isSelected = selectedSubCardIndex == 2,
                            modifier = Modifier.weight(1f)
                        ) { selectedSubCardIndex = 2 }
                    }
                }

                // Sub-Section Meta Description
                item {
                    val detailTitle = when (selectedSubCardIndex) {
                        0 -> nuzulGuide.mainTitle
                        1 -> tajweedGuide.mainTitle
                        else -> waqfGuide.mainTitle
                    }
                    val detailSubtitle = when (selectedSubCardIndex) {
                        0 -> nuzulGuide.subtitle
                        1 -> tajweedGuide.subtitle
                        else -> waqfGuide.subtitle
                    }
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = if (isRtl) Alignment.End else Alignment.Start
                    ) {
                        Text(
                            text = detailTitle,
                            color = GoldPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = if (isRtl) TextAlign.Right else TextAlign.Left,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = detailSubtitle,
                            color = TextGray,
                            fontSize = 12.sp,
                            textAlign = if (isRtl) TextAlign.Right else TextAlign.Left,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = GoldPrimary.copy(alpha = 0.15f))
                    }
                }

                // Items list depending on selected tab
                when (selectedSubCardIndex) {
                    0 -> {
                        items(nuzulGuide.facts) { fact ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = EmeraldCard),
                                border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.15f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = if (isRtl) Arrangement.End else Arrangement.Start,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(GoldPrimary)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = fact.title,
                                            color = GoldPrimary,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = if (isRtl) TextAlign.Right else TextAlign.Left
                                        )
                                    }
                                    Text(
                                        text = fact.description,
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontSize = 13.sp,
                                        lineHeight = 19.sp,
                                        textAlign = if (isRtl) TextAlign.Right else TextAlign.Left,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                    1 -> {
                        items(tajweedGuide.rules) { rule ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = EmeraldCard),
                                border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.2f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = rule.ruleTitle,
                                            color = GoldPrimary,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = if (isRtl) TextAlign.Right else TextAlign.Left
                                        )
                                        Box(
                                            modifier = Modifier
                                                .background(GoldPrimary.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                                .border(1.dp, GoldPrimary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = rule.name,
                                                color = GoldPrimary,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Serif
                                            )
                                        }
                                    }

                                    Text(
                                        text = rule.description,
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontSize = 13.sp,
                                        lineHeight = 19.sp,
                                        textAlign = if (isRtl) TextAlign.Right else TextAlign.Left,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    if (rule.examples != null) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(EmeraldBackground, RoundedCornerShape(10.dp))
                                                .border(1.dp, GoldPrimary.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            contentAlignment = Alignment.CenterEnd
                                        ) {
                                            Text(
                                                text = FontHelper.formatArabicText(rule.examples),
                                                style = FontHelper.getArabicTextStyle(
                                                    fontSize = 18.sp,
                                                    color = GoldPrimary
                                                ),
                                                textAlign = TextAlign.Right,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    2 -> {
                        items(waqfGuide.symbols) { item ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = EmeraldCard),
                                border = BorderStroke(
                                    1.dp,
                                    if (item.isMandatory) Color(0xFFFF6B6B).copy(alpha = 0.4f) else GoldPrimary.copy(alpha = 0.15f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Symbol Badge Pillar
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(
                                                if (item.isMandatory) Color(0xFFFF6B6B).copy(alpha = 0.15f)
                                                else GoldPrimary.copy(alpha = 0.15f)
                                            )
                                            .border(
                                                1.dp,
                                                if (item.isMandatory) Color(0xFFFF6B6B) else GoldPrimary,
                                                RoundedCornerShape(14.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = item.symbol,
                                            color = if (item.isMandatory) Color(0xFFFF6B6B) else GoldPrimary,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Black,
                                            fontFamily = FontFamily.Serif
                                        )
                                    }

                                    // Content Details
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = item.name,
                                                color = GoldPrimary,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = if (isRtl) TextAlign.Right else TextAlign.Left
                                            )
                                            Text(
                                                text = item.instruction,
                                                color = if (item.isMandatory) Color(0xFFFF6B6B) else Color(0xFF4ADE80),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                modifier = Modifier
                                                    .background(
                                                        if (item.isMandatory) Color(0xFFFF6B6B).copy(alpha = 0.1f) else Color(0xFF4ADE80).copy(alpha = 0.1f),
                                                        RoundedCornerShape(6.dp)
                                                    )
                                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }

                                        Text(
                                            text = item.explanation,
                                            color = Color.White.copy(alpha = 0.9f),
                                            fontSize = 12.sp,
                                            lineHeight = 17.sp,
                                            textAlign = if (isRtl) TextAlign.Right else TextAlign.Left,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SubCardTabChip(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) GoldPrimary.copy(alpha = 0.2f) else EmeraldCard,
        border = BorderStroke(1.dp, if (isSelected) GoldPrimary else GoldPrimary.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) GoldPrimary else TextGray,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = title,
                color = if (isSelected) GoldPrimary else Color.White,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}
