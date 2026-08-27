package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.draw.scale

// Activity Results & Media imports
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Typeface
import java.io.ByteArrayOutputStream
import android.util.Base64
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.util.Locale
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.TaqwaViewModel
import com.example.util.SecurityManager

fun base64ToBitmap(base64Str: String): Bitmap? {
    if (base64Str.isEmpty()) return null
    return try {
        val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: Exception) {
        null
    }
}

fun compressUriToBase64(context: Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()
        if (originalBitmap == null) return null
        
        val maxDimension = 600
        val width = originalBitmap.width
        val height = originalBitmap.height
        val scaledBitmap = if (width > maxDimension || height > maxDimension) {
            val ratio = width.toFloat() / height.toFloat()
            val (newWidth, newHeight) = if (ratio > 1) {
                Pair(maxDimension, (maxDimension / ratio).toInt())
            } else {
                Pair((maxDimension * ratio).toInt(), maxDimension)
            }
            Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
        } else {
            originalBitmap
        }
        
        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
        val compressedBytes = outputStream.toByteArray()
        Base64.encodeToString(compressedBytes, Base64.NO_WRAP)
    } catch (e: Exception) {
        Log.e("SettingsScreen", "Failed to compress/encode picture", e)
        null
    }
}

fun createMockScreenshotBitmap(title: String, detail: String, errorColor: Int): Bitmap {
    val bitmap = Bitmap.createBitmap(400, 700, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    
    val paintBg = Paint().apply {
        color = 0xFF0B1E19.toInt()
        style = Paint.Style.FILL
    }
    canvas.drawRect(0f, 0f, 400f, 700f, paintBg)
    
    paintBg.color = 0xFF021612.toInt()
    canvas.drawRect(0f, 0f, 400f, 60f, paintBg)
    
    val paintText = Paint().apply {
        color = 0xFFFFD700.toInt()
        textSize = 14f
        isAntiAlias = true
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
    }
    canvas.drawText("TAQWAHUB MOCK EMULATOR", 20f, 38f, paintText)
    
    paintText.color = 0xFFFFFFFF.toInt()
    paintText.textSize = 11f
    canvas.drawText("14:24 ⚡ 100%", 310f, 36f, paintText)
    
    val paintCard = Paint().apply {
        color = errorColor
        style = Paint.Style.FILL
    }
    canvas.drawRect(20f, 100f, 380f, 420f, paintCard)
    
    val paintCardText = Paint().apply {
        color = 0xFFFFFFFF.toInt()
        textSize = 15f
        isAntiAlias = true
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
    }
    canvas.drawText(title, 40f, 150f, paintCardText)
    
    paintCardText.textSize = 12f
    paintCardText.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
    val words = detail.split(" ")
    var x = 40f
    var y = 200f
    var line = StringBuilder()
    for (word in words) {
        if (line.length + word.length > 25) {
            canvas.drawText(line.toString(), x, y, paintCardText)
            line = StringBuilder(word).append(" ")
            y += 22f
        } else {
            line.append(word).append(" ")
        }
    }
    if (line.isNotEmpty()) {
        canvas.drawText(line.toString(), x, y, paintCardText)
    }
    
    paintCard.color = 0x22FFFFFF
    canvas.drawRect(40f, 340f, 360f, 390f, paintCard)
    paintCardText.color = 0xFFFFD700.toInt()
    paintCardText.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
    canvas.drawText("TAP TO RETRY CONNECTION", 84f, 370f, paintCardText)
    
    paintBg.color = 0x55FFFFFF
    canvas.drawRect(140f, 685f, 260f, 690f, paintBg)
    
    return bitmap
}

fun bitmapToBase64(bitmap: Bitmap): String {
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
    val bytes = outputStream.toByteArray()
    return Base64.encodeToString(bytes, Base64.NO_WRAP)
}@Composable
fun SettingsCategoryCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isExpanded: Boolean,
    onHeaderClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = EmeraldCard),
        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onHeaderClick() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(GoldPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = GoldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subtitle,
                        color = TextGray,
                        fontSize = 11.sp
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = GoldPrimary
                )
            }
            
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Divider(color = Color.White.copy(alpha = 0.08f), thickness = 1.dp)
                    content()
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: TaqwaViewModel) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    // Privacy Policy Expand state
    var isPrivacyExpanded by remember { mutableStateOf(false) }
    
    // Categories Expansion States (folded/collapsed by default)
    var isPrayerCategoryExpanded by remember { mutableStateOf(false) }
    var isSecurityCategoryExpanded by remember { mutableStateOf(false) }
    var isSupportCategoryExpanded by remember { mutableStateOf(false) }
    var isAboutCategoryExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_screen_scroll"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App settings header
        item {
            Text(
                text = "App Settings",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "Configure prayer alarms, manage system guards, and read privacy details.",
                color = TextGray,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Category 1: Prayer and Quran Preferences (Folded by default)
        item {
            SettingsCategoryCard(
                title = "Prayer and Quran",
                subtitle = "Alarms, notifications, and script styles",
                icon = Icons.Default.MenuBook,
                isExpanded = isPrayerCategoryExpanded,
                onHeaderClick = { isPrayerCategoryExpanded = !isPrayerCategoryExpanded }
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Prayer Alarms System Card content
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(EmeraldMedium.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Alarm Icon",
                                tint = GoldPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Prayer Alarms System",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Adhan notification and vibration",
                                    color = TextGray,
                                    fontSize = 11.sp
                                )
                            }
                            Switch(
                                checked = viewModel.isPrayerAlarmEnabled,
                                onCheckedChange = { viewModel.updatePrayerAlarmSetting(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = OnGoldText,
                                    checkedTrackColor = GoldPrimary,
                                    uncheckedThumbColor = TextGray,
                                    uncheckedTrackColor = EmeraldMedium.copy(alpha = 0.4f)
                                ),
                                modifier = Modifier.testTag("master_alarm_switch")
                            )
                        }

                        AnimatedVisibility(
                            visible = viewModel.isPrayerAlarmEnabled,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.padding(top = 12.dp)
                            ) {
                                Divider(color = Color.White.copy(alpha = 0.08f))
                                
                                val prayers = listOf(
                                    Triple("Fajr", "Morning prayer", viewModel.isFajrAlarmEnabled),
                                    Triple("Dhuhr", "Noon prayer", viewModel.isDhuhrAlarmEnabled),
                                    Triple("Asr", "Afternoon prayer", viewModel.isAsrAlarmEnabled),
                                    Triple("Maghrib", "Sunset prayer", viewModel.isMaghribAlarmEnabled),
                                    Triple("Isha", "Night prayer", viewModel.isIshaAlarmEnabled)
                                )
                                
                                prayers.forEach { (name, desc, isEnabled) ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(EmeraldMedium.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsNone,
                                            contentDescription = name,
                                            tint = if (isEnabled) GoldPrimary else TextGray,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                            Text(desc, color = TextGray, fontSize = 11.sp)
                                        }
                                        Switch(
                                            checked = isEnabled,
                                            onCheckedChange = { viewModel.updateIndividualAlarmSetting(name, it) },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = OnGoldText,
                                                checkedTrackColor = GoldPrimary,
                                                uncheckedThumbColor = TextGray,
                                                uncheckedTrackColor = EmeraldBackground
                                            ),
                                            modifier = Modifier
                                                .scale(0.85f)
                                                .testTag("switch_${name.lowercase()}")
                                        )
                                    }
                                }

                                OutlinedButton(
                                    onClick = {
                                        try {
                                            val intent = Intent(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            try {
                                                val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                                    data = Uri.parse("package:${context.packageName}")
                                                }
                                                context.startActivity(intent)
                                            } catch (ex: Exception) {
                                                Toast.makeText(context, "Open system settings to adjust battery optimization", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldPrimary),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                                ) {
                                    Icon(Icons.Default.BatteryAlert, contentDescription = "Battery Settings", tint = GoldPrimary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Battery Optimization Settings", color = GoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }

                        if (!viewModel.isPrayerAlarmEnabled) {
                            Text(
                                text = "Prayer alarms are currently disabled. You will not receive any prayer alerts.",
                                color = Color(0xFFFCA5A5),
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            )
                        }
                    }

                    // Quran Script Setup
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(EmeraldMedium.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldBackground),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.MenuBook, contentDescription = "Quran Script", tint = GoldPrimary, modifier = Modifier.size(18.dp))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Quran Reading Script", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Text("Set preference for Indo-Pak or Uthmani", color = TextGray, fontSize = 11.sp)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            val isIndoPak = viewModel.quranScript == "indopak"
                            Card(
                                onClick = { viewModel.updateQuranScript("indopak") },
                                colors = CardDefaults.cardColors(containerColor = if (isIndoPak) GoldPrimary.copy(alpha=0.15f) else EmeraldBackground),
                                border = BorderStroke(1.dp, if (isIndoPak) GoldPrimary else Color.Transparent),
                                modifier = Modifier.weight(1f).testTag("script_indopak")
                            ) {
                                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Indo-Pak", color = if (isIndoPak) GoldPrimary else Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Best for people from South Asia, India, and Pakistan.", textAlign = TextAlign.Center, color = TextGray, fontSize = 10.sp, lineHeight = 14.sp)
                                }
                            }
                            
                            val isUthmani = viewModel.quranScript == "uthmani"
                            Card(
                                onClick = { viewModel.updateQuranScript("uthmani") },
                                colors = CardDefaults.cardColors(containerColor = if (isUthmani) GoldPrimary.copy(alpha=0.15f) else EmeraldBackground),
                                border = BorderStroke(1.dp, if (isUthmani) GoldPrimary else Color.Transparent),
                                modifier = Modifier.weight(1f).testTag("script_uthmani")
                            ) {
                                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Uthmani", color = if (isUthmani) GoldPrimary else Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Standard globally, best for Arabic readers worldwide.", textAlign = TextAlign.Center, color = TextGray, fontSize = 10.sp, lineHeight = 14.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Category 2: Security and System Guards (Folded by default)
        item {
            val signatureResult = viewModel.signatureCheckResult
            val rootResult = viewModel.rootCheckResult

            SettingsCategoryCard(
                title = "Security and System Guards",
                subtitle = "Anti-tamper, anti-screenshot, and greetings",
                icon = Icons.Default.Security,
                isExpanded = isSecurityCategoryExpanded,
                onHeaderClick = { isSecurityCategoryExpanded = !isSecurityCategoryExpanded }
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Security Shield Header Status Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(EmeraldMedium.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "App Integrity Shield",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Continuous environment auditing",
                                color = TextGray,
                                fontSize = 11.sp
                            )
                        }
                        
                        val isSafe = (signatureResult?.isMatch ?: true) && !(rootResult?.isRooted ?: false)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSafe) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFF59E0B).copy(alpha = 0.15f))
                                .border(0.5.dp, if (isSafe) Color(0xFF10B981) else Color(0xFFF59E0B), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isSafe) "SECURE" else "WARNING",
                                color = if (isSafe) Color(0xFF10B981) else Color(0xFFF59E0B),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // System Guard Options List
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Anti-Spy Switch
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(EmeraldMedium.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Anti-Screenshot Guard",
                                tint = if (viewModel.isSecurityAntiSpyEnabled) GoldPrimary else TextGray,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Anti-Screenshot Guard", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("Prevents taking snapshots and screen recording", color = TextGray, fontSize = 11.sp)
                            }
                            Switch(
                                checked = viewModel.isSecurityAntiSpyEnabled,
                                onCheckedChange = { viewModel.updateSecurityAntiSpy(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = OnGoldText,
                                    checkedTrackColor = GoldPrimary,
                                    uncheckedThumbColor = TextGray,
                                    uncheckedTrackColor = EmeraldBackground
                                ),
                                modifier = Modifier.scale(0.85f).testTag("anti_spy_switch")
                            )
                        }

                        // Strict Environment Guard Switch
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(EmeraldMedium.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = "Strict Environment Guard",
                                tint = if (viewModel.isSecurityStrictEnvBlockEnabled) GoldPrimary else TextGray,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Strict Environment Guard", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("Locks advanced AI chat on roots and emulators", color = TextGray, fontSize = 11.sp)
                            }
                            Switch(
                                checked = viewModel.isSecurityStrictEnvBlockEnabled,
                                onCheckedChange = { viewModel.updateSecurityStrictEnvBlock(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = OnGoldText,
                                    checkedTrackColor = GoldPrimary,
                                    uncheckedThumbColor = TextGray,
                                    uncheckedTrackColor = EmeraldBackground
                                ),
                                modifier = Modifier.scale(0.85f).testTag("strict_env_switch")
                            )
                        }

                        // Multi-language Greetings Switch
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(EmeraldMedium.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "Multi-language Greetings",
                                tint = if (viewModel.isMultiLingualGreetingEnabled) GoldPrimary else TextGray,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Multi-language Greetings", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("Cycles Assalamualaikum through multiple Muslim languages", color = TextGray, fontSize = 11.sp)
                            }
                            Switch(
                                checked = viewModel.isMultiLingualGreetingEnabled,
                                onCheckedChange = { viewModel.updateMultiLingualGreetingEnabled(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = OnGoldText,
                                    checkedTrackColor = GoldPrimary,
                                    uncheckedThumbColor = TextGray,
                                    uncheckedTrackColor = EmeraldBackground
                                ),
                                modifier = Modifier.scale(0.85f).testTag("multi_language_greeting_switch")
                            )
                        }
                    }
                }
            }
        }

        // Category 3: Bug Reports and Support (Folded by default)
        item {
            SettingsCategoryCard(
                title = "Help & Support Chat",
                subtitle = "Contact support, submit bug reports, or give feedback",
                icon = Icons.Default.SupportAgent,
                isExpanded = isSupportCategoryExpanded,
                onHeaderClick = { isSupportCategoryExpanded = !isSupportCategoryExpanded }
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Have a question, suggestion, or encountered an issue? Chat directly with our Support Team in real-time.",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Button(
                        onClick = { viewModel.currentView = "user_complaints" },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("open_support_chat_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = null,
                                tint = OnGoldText,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Open Help & Support Chat",
                                color = OnGoldText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // Category 4: App Information and Privacy (Folded by default)
        item {
            SettingsCategoryCard(
                title = "App Information and Privacy",
                subtitle = "TaqwaHub version and data handling terms",
                icon = Icons.Default.Info,
                isExpanded = isAboutCategoryExpanded,
                onHeaderClick = { isAboutCategoryExpanded = !isAboutCategoryExpanded }
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Privacy Policy Expandable Row
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isPrivacyExpanded = !isPrivacyExpanded }
                                .padding(vertical = 4.dp)
                                .testTag("privacy_policy_expand_row")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(GoldPrimary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = "Privacy Policy Icon",
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Privacy Policy",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "TaqwaHub data handling terms",
                                    color = TextGray,
                                    fontSize = 11.sp
                                )
                            }
                            
                            Icon(
                                imageVector = if (isPrivacyExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Expand",
                                tint = GoldPrimary
                            )
                        }
                        
                        AnimatedVisibility(
                            visible = isPrivacyExpanded,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(
                                modifier = Modifier.padding(top = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "Your privacy is our utmost priority. TaqwaHub is built upon offline-first core mechanics to respect your personal boundaries.\n\n" +
                                           "• Location Data: We process your fine/coarse GPS coordinates locally on your device to calculate accurate Islamic prayer times and the precise Qibla compass angle. Your location is NEVER sent to outer servers or shared with commercial entities.\n\n" +
                                           "• Personal Notes and Tasks: All your spiritual progress logs, namaz checklists, tasbeeh counters, and bookmark data exist exclusively inside your local database.\n\n" +
                                           "• Cloud Synchronizations: If you choose to log in, we safely synchronize your progress state with secure database instances, keeping your spiritual history unified when you switch mobile devices.",
                                    color = TextGray,
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp,
                                    modifier = Modifier
                                        .background(EmeraldMedium.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                                        .padding(12.dp)
                                )

                                // Web Legal Action Buttons
                                Button(
                                    onClick = {
                                        val url = viewModel.appConfig.privacyPolicyUrl.ifBlank { "https://taqwahub.vercel.app/privacy.html" }
                                        try {
                                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Opening: $url", Toast.LENGTH_LONG).show()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(42.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary.copy(alpha = 0.15f)),
                                    border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.3f))
                                ) {
                                    Icon(Icons.Default.Policy, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("READ PRIVACY POLICY (WEB)", color = GoldPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        val url = viewModel.appConfig.termsOfServiceUrl.ifBlank { "https://taqwahub.vercel.app/terms.html" }
                                        try {
                                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Opening: $url", Toast.LENGTH_LONG).show()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(42.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary.copy(alpha = 0.15f)),
                                    border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.3f))
                                ) {
                                    Icon(Icons.Default.Description, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("READ TERMS OF SERVICE (WEB)", color = GoldPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        val url = viewModel.appConfig.deleteAccountUrl.ifBlank { "https://taqwahub.vercel.app/delete-account.html" }
                                        try {
                                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Opening: $url", Toast.LENGTH_LONG).show()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(42.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
                                ) {
                                    Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("WEB ACCOUNT DELETION PORTAL", color = Color(0xFFEF4444), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Divider(color = Color.White.copy(alpha = 0.08f))

                    // App Version Information
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(GoldPrimary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "App Version Icon",
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            
                            Column {
                                Text(
                                    text = "App Version",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Version ${com.example.util.AppUpdateManager.getCurrentVersionName(context)} (Build ${com.example.util.AppUpdateManager.getCurrentVersionCode(context)})",
                                    color = TextGray,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Dynamic badge showing if there is an update pending
                        val hasUpdateAvailable = com.example.util.AppUpdateManager.isVersionLower(context, viewModel.appConfig.forceUpdateMinVersion)
                        
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (hasUpdateAvailable) Color(0xFFEF4444).copy(alpha = 0.15f)
                                    else GoldPrimary.copy(alpha = 0.15f)
                                )
                                .border(
                                    1.dp,
                                    if (hasUpdateAvailable) Color(0xFFEF4444).copy(alpha = 0.3f)
                                    else GoldPrimary.copy(alpha = 0.3f),
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (hasUpdateAvailable) "UPDATE PENDING" else "UP TO DATE",
                                color = if (hasUpdateAvailable) Color(0xFFF87171) else GoldPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Google Play Store Rate App card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF021F19)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Rate TaqwaHub",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Support our work by giving us a 5-star rating on Google Play Store.",
                                    color = TextGray,
                                    fontSize = 11.sp
                                )
                            }
                            Button(
                                onClick = {
                                    com.example.util.InAppReviewManager.launchReviewFlow(context)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(32.dp).testTag("rate_app_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFF021612),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Rate Us", color = Color(0xFF021612), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Google Play Store updater card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF021F19)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF1B4E38))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Google Play Store Updates",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Check for official app updates directly on Play Store.",
                                    color = TextGray,
                                    fontSize = 11.sp
                                )
                            }
                            Button(
                                onClick = {
                                    com.example.util.AppUpdateManager.openPlayStore(context, null)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shop,
                                    contentDescription = null,
                                    tint = Color(0xFF021612),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Play Store", color = Color(0xFF021612), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
        
<<<<<<< HEAD
=======
        if (viewModel.isAdmin) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = GoldPrimary.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Admin Console 👑",
                                color = GoldPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "System controls, locks, user stats and administration.",
                                color = TextGray,
                                fontSize = 11.sp
                            )
                        }
                        Button(
                            onClick = { viewModel.currentView = "admin_dashboard" },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Open", color = Color(0xFF021612), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

>>>>>>> 6e834ed (Update Taqwahub)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Made with Taqwa © 2026",
                    color = TextGray.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun AuditItem(
    title: String,
    status: String,
    detail: String,
    isOk: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(EmeraldMedium.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val circleColor = if (isOk) Color(0xFF10B981) else Color(0xFFEF4444)
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(circleColor)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(
                    status, 
                    color = if (isOk) Color(0xFF10B981) else Color(0xFFF87171), 
                    fontSize = 11.sp, 
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(detail, color = TextGray, fontSize = 10.sp)
        }
    }
}
