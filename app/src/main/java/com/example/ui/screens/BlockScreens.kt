package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
<<<<<<< HEAD
=======
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.example.R
>>>>>>> 6e834ed (Update Taqwahub)
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
<<<<<<< HEAD
=======
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDirection
>>>>>>> 6e834ed (Update Taqwahub)
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.EmeraldCard
import com.example.ui.theme.TextGray
import com.example.viewmodel.TaqwaViewModel
import com.example.util.FontHelper
import com.example.viewmodel.AppConfig
import com.example.data.IslamicData
import com.example.data.Dua
import com.example.data.Hadith
import com.example.data.Announcement
import com.example.data.BugReport
import com.example.data.room.AllTimeTaskEntity
import com.example.data.room.UserStatsEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.util.Locale

import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.util.Base64
import androidx.compose.ui.graphics.asImageBitmap
<<<<<<< HEAD
import androidx.compose.ui.layout.ContentScale
=======
>>>>>>> 6e834ed (Update Taqwahub)
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer

// Removed artificial splash screen

@Composable
fun ForceUpdateBlockScreen(downloadUrl: String, onAdminBypass: () -> Unit, onSignOut: () -> Unit) {
    val context = LocalContext.current
    var tapCount by remember { mutableStateOf(0) }
    var lastTapTime by remember { mutableStateOf(0L) }
    var showBypassDialog by remember { mutableStateOf(false) }
    var enteredPasscode by remember { mutableStateOf("") }
    var passcodeError by remember { mutableStateOf(false) }
    var isPasswordVisible by remember { mutableStateOf(false) }

    // Security Rate Limiting
    var failedAttempts by remember { mutableStateOf(0) }
    var cooldownUntil by remember { mutableStateOf(0L) }

    val coroutineScope = rememberCoroutineScope()
    val updateState by com.example.util.AppUpdateManager.updateState.collectAsStateWithLifecycle(initialValue = com.example.util.AppUpdateManager.UpdateState.Idle)

    // Check unknown app install permission status
    var canInstallUnknownApps by remember { mutableStateOf(com.example.util.AppUpdateManager.canRequestPackageInstalls(context)) }

    // Recheck permission when app resumes
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                canInstallUnknownApps = com.example.util.AppUpdateManager.canRequestPackageInstalls(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF011D17)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(Color(0xFFFBBF24).copy(alpha = 0.1f), CircleShape)
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null
                    ) {
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastTapTime < 500) {
                            tapCount++
                        } else {
                            tapCount = 1
                        }
                        lastTapTime = currentTime
                        if (tapCount >= 15) {
                            tapCount = 0
                            showBypassDialog = true
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SystemUpdate,
                    contentDescription = "Update Required",
                    tint = GoldPrimary,
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Update Required",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "To continue utilizing TaqwaHub, please update to the latest version available on the Google Play Store.",
                color = Color(0xFFA7F3D0).copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Primary Update Button (Google Play)
            Button(
                onClick = {
                    com.example.util.AppUpdateManager.openPlayStore(context, downloadUrl)
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("update_google_play_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Shop,
                    contentDescription = null,
                    tint = Color(0xFF021612),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Update on Google Play",
                    color = Color(0xFF021612),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            if (downloadUrl.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Could not open URL", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text(text = "Or Open Web Link ↗", color = GoldPrimary, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onSignOut,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(text = "Sign Out & Try Again", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        if (showBypassDialog) {
            AlertDialog(
                onDismissRequest = {
                    showBypassDialog = false
                    enteredPasscode = ""
                    passcodeError = false
                },
                containerColor = Color(0xFF0A2E24),
                titleContentColor = GoldPrimary,
                textContentColor = Color.White,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = "Security", tint = GoldPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Admin Verification", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                },
                text = {
                    Column {
                        val currentTime = System.currentTimeMillis()
                        val isCooldownActive = currentTime < cooldownUntil
                        val cooldownRemaining = ((cooldownUntil - currentTime) / 1000).coerceAtLeast(0L)

                        if (isCooldownActive) {
                            Text(
                                "Security lockout is active due to multiple invalid passkey attempts.\n\nPlease wait $cooldownRemaining seconds before retrying.",
                                fontSize = 13.sp,
                                color = Color(0xFFF87171),
                                lineHeight = 20.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        } else {
                            Text(
                                "Enter the secret administration query passcode to bypass this download constraint.",
                                fontSize = 13.sp,
                                color = Color(0xFFA7F3D0).copy(alpha = 0.8f),
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = enteredPasscode,
                                onValueChange = {
                                    enteredPasscode = it
                                    passcodeError = false
                                },
                                label = { Text("Administration Key", color = Color.White.copy(alpha = 0.6f)) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GoldPrimary,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                    focusedLabelColor = GoldPrimary,
                                    unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                singleLine = true,
                                visualTransformation = if (isPasswordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                        Icon(
                                            imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = if (isPasswordVisible) "Hide" else "Show",
                                            tint = GoldPrimary
                                        )
                                    }
                                },
                                isError = passcodeError,
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (passcodeError) {
                                val remainingAttempts = (3 - failedAttempts).coerceAtLeast(0)
                                Text(
                                    "Invalid Administration Key ($remainingAttempts attempts remaining)",
                                    color = Color(0xFFEF4444),
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    val currentTime = System.currentTimeMillis()
                    val isCooldownActive = currentTime < cooldownUntil
                    if (!isCooldownActive) {
                        TextButton(
                            onClick = {
                                try {
                                    val md = java.security.MessageDigest.getInstance("SHA-256")
                                    val digest = md.digest(enteredPasscode.trim().toByteArray())
                                    val hashedHex = digest.joinToString("") { "%02x".format(it) }
                                    
                                    // SHA-256 of "Malikofficial032"
                                    val correctHash = "8c0732fbee4e70795754f2ea5a8578ba3f7f923499b6fa5fe2ba75f146b47640"
                                    
                                    if (hashedHex == correctHash) {
                                        showBypassDialog = false
                                        enteredPasscode = ""
                                        passcodeError = false
                                        failedAttempts = 0
                                        onAdminBypass()
                                    } else {
                                        failedAttempts++
                                        passcodeError = true
                                        if (failedAttempts >= 3) {
                                            cooldownUntil = System.currentTimeMillis() + 30000 // 30 seconds penalty
                                            enteredPasscode = ""
                                        }
                                    }
                                } catch (e: Exception) {
                                    passcodeError = true
                                }
                            }
                        ) {
                            Text("Verify", color = GoldPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showBypassDialog = false
                            enteredPasscode = ""
                            passcodeError = false
                        }
                    ) {
                        Text("Cancel", color = Color.White.copy(alpha = 0.6f))
                    }
                }
            )
        }
    }
}

@Composable
fun MaintenanceBlockScreen(message: String, onSignOut: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF011D17)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(Color(0xFFEF4444).copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Engineering,
                    contentDescription = "Maintenance",
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "System Maintenance",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message.ifEmpty { "TaqwaHub is currently undergoing scheduled maintenance. We'll be back shortly to serve your spiritual needs. JazakAllah Khair for your patience." },
                color = Color(0xFFA7F3D0).copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                lineHeight = 22.sp
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onSignOut,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(text = "Sign Out & Try Again", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

<<<<<<< HEAD
=======
@Composable
fun ModuleLockedContentView(
    moduleTitle: String,
    reasonMessage: String = "",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF021B15))
            .padding(horizontal = 24.dp, vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 2D Vector Golden Mosque Artwork Graphic
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.gold_mosque_vector_2d_1787212092065),
                    contentDescription = "2D Golden Mosque Vector Graphic",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(24.dp))
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Main Title: "We're Under"
            Text(
                text = "We're Under",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Main Title: "Maintenance" in Gold
            Text(
                text = "Maintenance",
                color = GoldPrimary,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 8-Pointed Star Motif Divider
            Row(
                modifier = Modifier.fillMaxWidth(0.6f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = GoldPrimary.copy(alpha = 0.35f),
                    thickness = 1.dp
                )
                Text(
                    text = "  ۞  ",
                    color = GoldPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = GoldPrimary.copy(alpha = 0.35f),
                    thickness = 1.dp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Maintenance Reason Message
            Text(
                text = reasonMessage.ifBlank { "TaqwaHub is currently undergoing maintenance to improve your experience." },
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }
    }
}

@Composable
fun ModuleLockScreenModal(
    moduleTitle: String,
    lockCategory: String = "",
    reasonMessage: String = "",
    isAdmin: Boolean = false,
    onReturnHome: () -> Unit,
    onAdminBypass: (() -> Unit)? = null
) {
    ModuleLockedContentView(
        moduleTitle = moduleTitle,
        reasonMessage = reasonMessage
    )
}

@Composable
fun ModuleMatrixItemCard(
    title: String,
    icon: ImageVector,
    isLocked: Boolean,
    onLockChange: (Boolean) -> Unit,
    isHidden: Boolean,
    onHideChange: (Boolean) -> Unit,
    reason: String,
    onReasonChange: (String) -> Unit,
    category: String,
    onCategoryChange: (String) -> Unit,
    onPreviewClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF063B2F)),
        border = BorderStroke(1.5.dp, if (isLocked) Color(0xFFEF4444).copy(alpha = 0.6f) else GoldPrimary.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row: Icon, Title & Status Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (isLocked) Color(0xFFEF4444).copy(alpha = 0.2f) else GoldPrimary.copy(alpha = 0.2f))
                            .border(1.dp, if (isLocked) Color(0xFFEF4444) else GoldPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isLocked) Color(0xFFEF4444) else GoldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = title,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (isLocked) {
                                Surface(
                                    color = Color(0xFFEF4444).copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = Color(0xFFFCA5A5),
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("LOCKED", color = Color(0xFFFCA5A5), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            } else {
                                Surface(
                                    color = Color(0xFF10B981).copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF6EE7B7),
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("ACTIVE", color = Color(0xFF6EE7B7), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            if (isHidden) {
                                Surface(
                                    color = Color(0xFFF59E0B).copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.VisibilityOff,
                                            contentDescription = null,
                                            tint = Color(0xFFFDE68A),
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("HIDDEN", color = Color(0xFFFDE68A), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Toggles Row (Dedicated, Clean, Properly Aligned Box Containers)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Lock Toggle Box
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF03261E),
                    border = BorderStroke(1.dp, if (isLocked) Color(0xFFEF4444).copy(alpha = 0.5f) else GoldPrimary.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = null,
                                tint = if (isLocked) Color(0xFFEF4444) else GoldPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Lock Page",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Switch(
                            checked = isLocked,
                            onCheckedChange = onLockChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFFEF4444),
                                uncheckedThumbColor = Color.White.copy(alpha = 0.6f),
                                uncheckedTrackColor = Color(0xFF07382B)
                            )
                        )
                    }
                }

                // Hide Toggle Box
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF03261E),
                    border = BorderStroke(1.dp, if (isHidden) Color(0xFFF59E0B).copy(alpha = 0.5f) else GoldPrimary.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (isHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null,
                                tint = if (isHidden) Color(0xFFF59E0B) else GoldPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Hide Page",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Switch(
                            checked = isHidden,
                            onCheckedChange = onHideChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFFF59E0B),
                                uncheckedThumbColor = Color.White.copy(alpha = 0.6f),
                                uncheckedTrackColor = Color(0xFF07382B)
                            )
                        )
                    }
                }
            }

            // Category Selector
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Lock Reason Category:",
                    color = GoldPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "server_maintenance" to "Server",
                        "content_update" to "Content",
                        "moderation" to "Review",
                        "coming_soon" to "Upgrade"
                    ).forEach { (catKey, catLabel) ->
                        val isSel = category == catKey
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) GoldPrimary else Color(0xFF03261E))
                                .border(1.dp, if (isSel) GoldPrimary else GoldPrimary.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                                .clickable { onCategoryChange(catKey) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = catLabel,
                                color = if (isSel) Color(0xFF021612) else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Reason Text Input
            OutlinedTextField(
                value = reason,
                onValueChange = onReasonChange,
                label = { Text("Custom Reason Message for Users", color = Color(0xFFA7F3D0).copy(alpha = 0.7f), fontSize = 11.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF03261E),
                    unfocusedContainerColor = Color(0xFF03261E),
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = GoldPrimary.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Quick Preset Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onReasonChange("Scheduled maintenance is underway to improve performance.") },
                    border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Maintenance", color = Color.White.copy(alpha = 0.9f), fontSize = 10.sp, fontWeight = FontWeight.Medium)
                }

                OutlinedButton(
                    onClick = { onReasonChange("Content verification & accuracy check in progress.") },
                    border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FactCheck,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Verification", color = Color.White.copy(alpha = 0.9f), fontSize = 10.sp, fontWeight = FontWeight.Medium)
                }
            }

            // Action Row: Save Button on every card + Live Preview
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onSaveClick,
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1.3f)
                        .height(42.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                        tint = Color(0xFF021612),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Save Page",
                        color = Color(0xFF021612),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                OutlinedButton(
                    onClick = onPreviewClick,
                    border = BorderStroke(1.dp, GoldPrimary),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = GoldPrimary.copy(alpha = 0.12f)),
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Preview",
                        color = GoldPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

>>>>>>> 6e834ed (Update Taqwahub)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(viewModel: TaqwaViewModel) {
    val context = LocalContext.current
    
    // Config values
    var welcomeMsgInput by remember { mutableStateOf(viewModel.appConfig.welcomeBannerMessage) }
    var welcomeBismillahMessageInput by remember { mutableStateOf(viewModel.appConfig.welcomeBismillahMessage) }
    var minVersionInput by remember { mutableStateOf(viewModel.appConfig.forceUpdateMinVersion) }
    var updateDownloadUrlInput by remember { mutableStateOf(viewModel.appConfig.updateDownloadUrl) }
    var donateRedirectUrlInput by remember { mutableStateOf(viewModel.appConfig.donateRedirectUrl) }
    var privacyPolicyUrlInput by remember { mutableStateOf(viewModel.appConfig.privacyPolicyUrl) }
    var termsOfServiceUrlInput by remember { mutableStateOf(viewModel.appConfig.termsOfServiceUrl) }
    var deleteAccountUrlInput by remember { mutableStateOf(viewModel.appConfig.deleteAccountUrl) }
    var maintenanceInput by remember { mutableStateOf(viewModel.appConfig.isUnderMaintenance) }
    var maintenanceMsgInput by remember { mutableStateOf(viewModel.appConfig.message) }
    var isQuranAudioLockedInput by remember { mutableStateOf(viewModel.appConfig.isQuranAudioLocked) }
    var quranAudioMsgInput by remember { mutableStateOf(viewModel.appConfig.quranAudioBlockedMessage) }
    var lockedSurahIdsInput by remember { mutableStateOf(viewModel.appConfig.lockedSurahIds) }
    var surahMsgInput by remember { mutableStateOf(viewModel.appConfig.surahBlockedMessage) }
    var lockedWordSurahIdsInput by remember { mutableStateOf(viewModel.appConfig.lockedWordSurahIds) }
    var wordSurahMsgInput by remember { mutableStateOf(viewModel.appConfig.wordSurahBlockedMessage) }

    // Pages Blocking
    var isQuranPageLockedInput by remember { mutableStateOf(viewModel.appConfig.isQuranPageLocked) }
    var quranPageMsgInput by remember { mutableStateOf(viewModel.appConfig.quranPageBlockedMessage) }
    var isToolsPageLockedInput by remember { mutableStateOf(viewModel.appConfig.isToolsPageLocked) }
    var toolsPageMsgInput by remember { mutableStateOf(viewModel.appConfig.toolsPageBlockedMessage) }
    var isLearnPageLockedInput by remember { mutableStateOf(viewModel.appConfig.isLearnPageLocked) }
    var learnPageMsgInput by remember { mutableStateOf(viewModel.appConfig.learnPageBlockedMessage) }

    // Cards Blocking
    var isPrayerTimesCardLockedInput by remember { mutableStateOf(viewModel.appConfig.isPrayerTimesCardLocked) }
    var prayerTimesMsgInput by remember { mutableStateOf(viewModel.appConfig.prayerTimesBlockedMessage) }
    var isDailyAyahCardLockedInput by remember { mutableStateOf(viewModel.appConfig.isDailyAyahCardLocked) }
    var dailyAyahMsgInput by remember { mutableStateOf(viewModel.appConfig.dailyAyahBlockedMessage) }
    var isTrackerCardLockedInput by remember { mutableStateOf(viewModel.appConfig.isTrackerCardLocked) }
    var trackerMsgInput by remember { mutableStateOf(viewModel.appConfig.trackerBlockedMessage) }

<<<<<<< HEAD
=======
    // Module Control Matrix States (12 Modules)
    var isHadithLockedInput by remember { mutableStateOf(viewModel.appConfig.isHadithLocked) }
    var isHadithHiddenInput by remember { mutableStateOf(viewModel.appConfig.isHadithHidden) }
    var hadithReasonInput by remember { mutableStateOf(viewModel.appConfig.hadithLockReason) }
    var hadithCategoryInput by remember { mutableStateOf(viewModel.appConfig.hadithLockCategory) }

    var isDuaLockedInput by remember { mutableStateOf(viewModel.appConfig.isDuaLocked) }
    var isDuaHiddenInput by remember { mutableStateOf(viewModel.appConfig.isDuaHidden) }
    var duaReasonInput by remember { mutableStateOf(viewModel.appConfig.duaLockReason) }
    var duaCategoryInput by remember { mutableStateOf(viewModel.appConfig.duaLockCategory) }

    var isQuranLockedInput by remember { mutableStateOf(viewModel.appConfig.isQuranLocked) }
    var isQuranHiddenInput by remember { mutableStateOf(viewModel.appConfig.isQuranHidden) }
    var quranReasonInput by remember { mutableStateOf(viewModel.appConfig.quranLockReason) }
    var quranCategoryInput by remember { mutableStateOf(viewModel.appConfig.quranLockCategory) }

    var isLeaderboardLockedInput by remember { mutableStateOf(viewModel.appConfig.isLeaderboardLocked) }
    var isLeaderboardHiddenInput by remember { mutableStateOf(viewModel.appConfig.isLeaderboardHidden) }
    var leaderboardReasonInput by remember { mutableStateOf(viewModel.appConfig.leaderboardLockReason) }
    var leaderboardCategoryInput by remember { mutableStateOf(viewModel.appConfig.leaderboardLockCategory) }

    var isTasksLockedInput by remember { mutableStateOf(viewModel.appConfig.isTasksLocked) }
    var isTasksHiddenInput by remember { mutableStateOf(viewModel.appConfig.isTasksHidden) }
    var tasksReasonInput by remember { mutableStateOf(viewModel.appConfig.tasksLockReason) }
    var tasksCategoryInput by remember { mutableStateOf(viewModel.appConfig.tasksLockCategory) }

    var isTasbeehLockedInput by remember { mutableStateOf(viewModel.appConfig.isTasbeehLocked) }
    var isTasbeehHiddenInput by remember { mutableStateOf(viewModel.appConfig.isTasbeehHidden) }
    var tasbeehReasonInput by remember { mutableStateOf(viewModel.appConfig.tasbeehLockReason) }
    var tasbeehCategoryInput by remember { mutableStateOf(viewModel.appConfig.tasbeehLockCategory) }

    var isNamesLockedInput by remember { mutableStateOf(viewModel.appConfig.isNamesLocked) }
    var isNamesHiddenInput by remember { mutableStateOf(viewModel.appConfig.isNamesHidden) }
    var namesReasonInput by remember { mutableStateOf(viewModel.appConfig.namesLockReason) }
    var namesCategoryInput by remember { mutableStateOf(viewModel.appConfig.namesLockCategory) }

    var isZakatLockedInput by remember { mutableStateOf(viewModel.appConfig.isZakatLocked) }
    var isZakatHiddenInput by remember { mutableStateOf(viewModel.appConfig.isZakatHidden) }
    var zakatReasonInput by remember { mutableStateOf(viewModel.appConfig.zakatLockReason) }
    var zakatCategoryInput by remember { mutableStateOf(viewModel.appConfig.zakatLockCategory) }

    var isQiblaLockedInput by remember { mutableStateOf(viewModel.appConfig.isQiblaLocked) }
    var isQiblaHiddenInput by remember { mutableStateOf(viewModel.appConfig.isQiblaHidden) }
    var qiblaReasonInput by remember { mutableStateOf(viewModel.appConfig.qiblaLockReason) }
    var qiblaCategoryInput by remember { mutableStateOf(viewModel.appConfig.qiblaLockCategory) }

    var isCalendarLockedInput by remember { mutableStateOf(viewModel.appConfig.isCalendarLocked) }
    var isCalendarHiddenInput by remember { mutableStateOf(viewModel.appConfig.isCalendarHidden) }
    var calendarReasonInput by remember { mutableStateOf(viewModel.appConfig.calendarLockReason) }
    var calendarCategoryInput by remember { mutableStateOf(viewModel.appConfig.calendarLockCategory) }

    var isComplaintsLockedInput by remember { mutableStateOf(viewModel.appConfig.isComplaintsLocked) }
    var isComplaintsHiddenInput by remember { mutableStateOf(viewModel.appConfig.isComplaintsHidden) }
    var complaintsReasonInput by remember { mutableStateOf(viewModel.appConfig.complaintsLockReason) }
    var complaintsCategoryInput by remember { mutableStateOf(viewModel.appConfig.complaintsLockCategory) }

    var isDonateLockedInput by remember { mutableStateOf(viewModel.appConfig.isDonateLocked) }
    var isDonateHiddenInput by remember { mutableStateOf(viewModel.appConfig.isDonateHidden) }
    var donateReasonInput by remember { mutableStateOf(viewModel.appConfig.donateLockReason) }
    var donateCategoryInput by remember { mutableStateOf(viewModel.appConfig.donateLockCategory) }

    // Admin Lock Screen Live Preview State
    var previewLockData by remember { mutableStateOf<Triple<String, String, String>?>(null) }

>>>>>>> 6e834ed (Update Taqwahub)
    // Collapsible states
    var isConfigExpanded by remember { mutableStateOf(true) }
    var isPageFeatureBlockersExpanded by remember { mutableStateOf(false) }
    var isAdminsExpanded by remember { mutableStateOf(false) }
    var isUsersExpanded by remember { mutableStateOf(false) }
    var userSearchQuery by remember { mutableStateOf("") }
    var searchedUserUid by remember { mutableStateOf<String?>(null) }
    var searchedUserStats by remember { mutableStateOf<UserStatsEntity?>(null) }
    var isSearchingUser by remember { mutableStateOf(false) }
    var isAnnouncementsExpanded by remember { mutableStateOf(false) }
    var isDuasExpanded by remember { mutableStateOf(false) }
    var isHadithsExpanded by remember { mutableStateOf(false) }
    var isAudioOverridesExpanded by remember { mutableStateOf(false) }
    var isBugReportsExpanded by remember { mutableStateOf(false) }
    var isAiFeedbackExpanded by remember { mutableStateOf(false) }
    var activeFeedbacksTab by remember { mutableStateOf("Stats") }
    var feedbackFilterTag by remember { mutableStateOf("All") }
    var filterType by remember { mutableStateOf("All") }
    var filterStatus by remember { mutableStateOf("All") }
    var zoomedImageBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

    LaunchedEffect(Unit) {
        viewModel.fetchBugReportsFromFirestore()
        viewModel.fetchAiFeedbacksFromFirestore()
    }

    // Audio Override Form States
    var aoWordId by remember { mutableStateOf("") }
    var aoVerseId by remember { mutableStateOf("") }
    var aoUrl by remember { mutableStateOf("") }
    
    // Audio Explorer
    var adminSurahId by remember { mutableStateOf("1") }
    var adminVerseNum by remember { mutableStateOf("1") }
    var adminWordSearch by remember { mutableStateOf("") }
    var clipboardUrl by remember { mutableStateOf("") }

    // Admin email state
    var newAdminEmail by remember { mutableStateOf("") }

    // Add Announcement Form states
    var announceTitle by remember { mutableStateOf("") }
    var announceMessage by remember { mutableStateOf("") }
    var announceType by remember { mutableStateOf("Announcement") } // "Announcement", "Update", "Reminder"

<<<<<<< HEAD
    // Add Dua Form states
    var duaCategory by remember { mutableStateOf("") }
=======
    // Add/Edit Dua Form states
    var editingDuaId by remember { mutableStateOf<String?>(null) }
    var duaCategory by remember { mutableStateOf("") }
    var newCategoryInput by remember { mutableStateOf("") }
    var isAddingNewCategory by remember { mutableStateOf(false) }
>>>>>>> 6e834ed (Update Taqwahub)
    var duaReference by remember { mutableStateOf("") }
    var duaArabic by remember { mutableStateOf("") }
    var duaTransliteration by remember { mutableStateOf("") }
    var duaTranslation by remember { mutableStateOf("") }
    var duaTranslationUrdu by remember { mutableStateOf("") }
<<<<<<< HEAD
=======
    var duaToDeleteId by remember { mutableStateOf<String?>(null) }
>>>>>>> 6e834ed (Update Taqwahub)

    // Add Hadith Form states
    var hadithChapter by remember { mutableStateOf("") }
    var hadithNarrator by remember { mutableStateOf("") }
    var hadithSource by remember { mutableStateOf("") }
    var hadithText by remember { mutableStateOf("") }
    var hadithArabic by remember { mutableStateOf("") }
    var hadithTranslationUrdu by remember { mutableStateOf("") }
    var hadithTransliteration by remember { mutableStateOf("") }

    // Manage Tasks Form states
    var isTasksExpanded by remember { mutableStateOf(false) }
    var newTaskTitle by remember { mutableStateOf("") }
    var newTaskCategory by remember { mutableStateOf("Salah") }
    var newTaskDescription by remember { mutableStateOf("") }
    var newTaskPoints by remember { mutableStateOf("15") }
    var newTaskTag by remember { mutableStateOf("RECOMMENDED") } // "OBLIGATORY", "HOT", "RECOMMENDED", "TIMER", "CUSTOM"
    var newTaskTimerSeconds by remember { mutableStateOf("0") }
    var newTaskActionRoute by remember { mutableStateOf("") }
    var editingTaskId by remember { mutableStateOf<String?>(null) }
    var showTaskDialog by remember { mutableStateOf(false) }
    var taskToDeleteId by remember { mutableStateOf<String?>(null) }

    // TASK DIALOG (FOR CREATING & EDITING TASKS)
    if (showTaskDialog) {
        AlertDialog(
            onDismissRequest = { showTaskDialog = false; editingTaskId = null },
            title = {
                Text(
                    text = if (editingTaskId != null) "Edit Spiritual Challenge" else "Publish Spiritual Challenge",
                    color = GoldPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            containerColor = Color(0xFF021612),
            titleContentColor = GoldPrimary,
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = newTaskTitle,
                        onValueChange = { newTaskTitle = it },
                        label = { Text("Task Title (e.g. Recite Astaghfar)", color = Color.White.copy(alpha = 0.6f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = Color(0xFF1B4E38)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newTaskDescription,
                        onValueChange = { newTaskDescription = it },
                        label = { Text("Description / Short Instructions", color = Color.White.copy(alpha = 0.6f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = Color(0xFF1B4E38)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Category", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Salah", "Quran", "Hadith", "Duas", "Dhikr", "Other").forEach { category ->
                            val isSelected = newTaskCategory == category
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) GoldPrimary else Color(0xFF01241A))
                                    .border(1.dp, if (isSelected) GoldPrimary else Color(0xFF1B4E38), RoundedCornerShape(8.dp))
                                    .clickable { newTaskCategory = category }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = category,
                                    color = if (isSelected) Color(0xFF021612) else Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Text("Scheduling / Status Tag", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("OBLIGATORY", "HOT", "RECOMMENDED", "TIMER", "AUTO", "CUSTOM").forEach { tag ->
                            val isSelected = newTaskTag == tag
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) GoldPrimary else Color(0xFF01241A))
                                    .border(1.dp, if (isSelected) GoldPrimary else Color(0xFF1B4E38), RoundedCornerShape(8.dp))
                                    .clickable { newTaskTag = tag }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tag,
                                    color = if (isSelected) Color(0xFF021612) else Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = newTaskPoints,
                            onValueChange = { newTaskPoints = it },
                            label = { Text("Points (XP)", color = Color.White.copy(alpha = 0.6f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = Color(0xFF1B4E38)
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = newTaskTimerSeconds,
                            onValueChange = { newTaskTimerSeconds = it },
                            label = { Text("Timer (Sec)", color = Color.White.copy(alpha = 0.6f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = Color(0xFF1B4E38)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = newTaskActionRoute,
                        onValueChange = { newTaskActionRoute = it },
                        label = { Text("Action Route / Link (e.g. 'quran' or 'dua')", color = Color.White.copy(alpha = 0.6f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = Color(0xFF1B4E38)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val pts = newTaskPoints.toIntOrNull() ?: 15
                        val sec = newTaskTimerSeconds.toIntOrNull() ?: 0
                        if (newTaskTitle.isBlank()) {
                            Toast.makeText(context, "Title cannot be empty!", Toast.LENGTH_SHORT).show()
                        } else {
                            if (editingTaskId != null) {
                                viewModel.updateAdminTask(
                                    id = editingTaskId!!,
                                    title = newTaskTitle.trim(),
                                    category = newTaskCategory,
                                    description = newTaskDescription.trim().ifEmpty { "Daily checklist routine." },
                                    points = pts,
                                    tag = newTaskTag,
                                    timerSeconds = sec,
                                    actionRoute = newTaskActionRoute.trim()
                                )
                                Toast.makeText(context, "Spiritual task updated! 👑", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.addAdminTask(
                                    title = newTaskTitle.trim(),
                                    category = newTaskCategory,
                                    description = newTaskDescription.trim().ifEmpty { "Daily checklist routine." },
                                    points = pts,
                                    tag = newTaskTag,
                                    timerSeconds = sec,
                                    actionRoute = newTaskActionRoute.trim()
                                )
                                Toast.makeText(context, "Spiritual task created! 👑", Toast.LENGTH_SHORT).show()
                            }
                            showTaskDialog = false
                            editingTaskId = null
                            newTaskTitle = ""
                            newTaskDescription = ""
                            newTaskActionRoute = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    Text("Save", color = Color(0xFF021612), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTaskDialog = false; editingTaskId = null }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }

    // DELETE CONFIRMATION DIALOG
    if (taskToDeleteId != null) {
        AlertDialog(
            onDismissRequest = { taskToDeleteId = null },
            title = { Text("Delete Challenge Task?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete this spiritual challenge from the live database?", color = Color.White.copy(alpha = 0.8f)) },
            containerColor = Color(0xFF021612),
            confirmButton = {
                Button(
                    onClick = {
                        taskToDeleteId?.let { id ->
                            viewModel.deleteTask(id)
                            Toast.makeText(context, "Task deleted!", Toast.LENGTH_SHORT).show()
                        }
                        taskToDeleteId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { taskToDeleteId = null }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }

<<<<<<< HEAD
=======
    // DUA DELETE CONFIRMATION DIALOG
    if (duaToDeleteId != null) {
        AlertDialog(
            onDismissRequest = { duaToDeleteId = null },
            title = { Text("Delete Supplication?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to permanently delete this supplication from the database? This action will sync to all users.", color = Color.White.copy(alpha = 0.8f)) },
            containerColor = Color(0xFF021612),
            confirmButton = {
                Button(
                    onClick = {
                        duaToDeleteId?.let { id ->
                            viewModel.deleteCustomDua(id, {
                                Toast.makeText(context, "Supplication deleted!", Toast.LENGTH_SHORT).show()
                            }, { err ->
                                Toast.makeText(context, "Failed: $err", Toast.LENGTH_SHORT).show()
                            })
                        }
                        duaToDeleteId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { duaToDeleteId = null }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }

    // LIVE PREVIEW LOCK DIALOG FOR ADMIN
    if (previewLockData != null) {
        val (pTitle, pCategory, pReason) = previewLockData!!
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { previewLockData = null },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            ModuleLockScreenModal(
                moduleTitle = pTitle,
                lockCategory = pCategory,
                reasonMessage = pReason,
                isAdmin = true,
                onReturnHome = { previewLockData = null },
                onAdminBypass = {
                    Toast.makeText(context, "👑 Admin Access Simulated: Page Unlocked!", Toast.LENGTH_SHORT).show()
                    previewLockData = null
                }
            )
        }
    }

>>>>>>> 6e834ed (Update Taqwahub)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TaqwaHub Admin Console", color = GoldPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF021612)),
                navigationIcon = {
                    IconButton(onClick = { viewModel.currentView = "dashboard" }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = GoldPrimary)
                    }
                }
            )
        },
        containerColor = Color(0xFF01241A)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Signed in as: ${viewModel.currentUser?.email}",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp
            )

            // Super Admin Only Controls
            if (viewModel.isSuperAdmin) {
                // SECTION 1: SYSTEM STAGE CONFIGURATION
                AdminCollapsibleCard(
                    title = "1. App Config & Maintenance Bypass",
                icon = Icons.Default.Settings,
                isExpanded = isConfigExpanded,
                onHeaderClick = { isConfigExpanded = !isConfigExpanded }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Global App Maintenance & Custom Direct Message Control",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Maintenance Blocker Screen", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("Blocks access for all non-admins", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                        }
                        Switch(
                            checked = maintenanceInput,
                            onCheckedChange = { maintenanceInput = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = GoldPrimary, checkedTrackColor = Color(0xFF1B4E38))
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Lock Quran Audio Recitations", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("Blocks playing verse and word audios globally for non-admin users", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                        }
                        Switch(
                            checked = isQuranAudioLockedInput,
                            onCheckedChange = { isQuranAudioLockedInput = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = GoldPrimary, checkedTrackColor = Color(0xFF1B4E38))
                        )
                    }

                    OutlinedTextField(
                        value = quranAudioMsgInput,
                        onValueChange = { quranAudioMsgInput = it },
                        label = { Text("Global Audio Locked Message", color = Color(0xFFA7F3D0).copy(alpha = 0.6f)) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = maintenanceMsgInput,
                        onValueChange = { maintenanceMsgInput = it },
                        label = { Text("Maintenance Message (If Blocked)", color = Color(0xFFA7F3D0).copy(alpha = 0.6f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = Color(0xFF1B4E38)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = welcomeMsgInput,
                        onValueChange = { welcomeMsgInput = it },
                        label = { Text("General Welcome Banner Notice", color = Color(0xFFA7F3D0).copy(alpha = 0.6f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = Color(0xFF1B4E38)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = welcomeBismillahMessageInput,
                        onValueChange = { welcomeBismillahMessageInput = it },
                        label = { Text("Onboarding Bismillah Welcome Message", color = Color(0xFFA7F3D0).copy(alpha = 0.6f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = Color(0xFF1B4E38)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = { Text("Customized message shown to first-time/new users with a golden Bismillah button.", color = Color.White.copy(alpha = 0.5f)) }
                    )

                    OutlinedTextField(
                        value = minVersionInput,
                        onValueChange = { minVersionInput = it },
                        label = { Text("Minimum Version Required (e.g. 1.0.4 or 5)", color = Color(0xFFA7F3D0).copy(alpha = 0.6f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = Color(0xFF1B4E38)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = updateDownloadUrlInput,
                        onValueChange = { updateDownloadUrlInput = it },
                        label = { Text("Play Store / Website Link (Optional)", color = Color(0xFFA7F3D0).copy(alpha = 0.6f)) },
                        supportingText = { Text("Your Google Play Store URL or website link (e.g. https://play.google.com/store/apps/details?id=com.taqwahub)", color = Color.White.copy(alpha = 0.5f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = Color(0xFF1B4E38)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = lockedSurahIdsInput,
                        onValueChange = { lockedSurahIdsInput = it },
                        label = { Text("Locked Surah IDs (e.g. 36, 55, 67)", color = Color(0xFFA7F3D0).copy(alpha = 0.6f)) },
                        supportingText = { Text("Comma-separated list of Surah IDs whose audios will be locked for non-admins", color = Color.White.copy(alpha = 0.5f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = Color(0xFF1B4E38)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = surahMsgInput,
                        onValueChange = { surahMsgInput = it },
                        label = { Text("Locked Surahs Message", color = Color(0xFFA7F3D0).copy(alpha = 0.6f)) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = lockedWordSurahIdsInput,
                        onValueChange = { lockedWordSurahIdsInput = it },
                        label = { Text("Locked Word-by-Word Surah IDs (e.g. 1, 2, or * for all)", color = Color(0xFFA7F3D0).copy(alpha = 0.6f)) },
                        supportingText = { Text("Only lock word-by-word pronunciation audio for these specific Surahs, or * to lock all", color = Color.White.copy(alpha = 0.5f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = Color(0xFF1B4E38)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = wordSurahMsgInput,
                        onValueChange = { wordSurahMsgInput = it },
                        label = { Text("Locked Word Audio Message", color = Color(0xFFA7F3D0).copy(alpha = 0.6f)) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = donateRedirectUrlInput,
                        onValueChange = { donateRedirectUrlInput = it },
<<<<<<< HEAD
                        label = { Text("Donation Website Redirection URL 💖", color = Color(0xFFA7F3D0).copy(alpha = 0.6f)) },
=======
                        label = { Text("Donation Website Redirection URL", color = Color(0xFFA7F3D0).copy(alpha = 0.6f)) },
>>>>>>> 6e834ed (Update Taqwahub)
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = privacyPolicyUrlInput,
                        onValueChange = { privacyPolicyUrlInput = it },
<<<<<<< HEAD
                        label = { Text("Privacy Policy URL 🛡️", color = Color(0xFFA7F3D0).copy(alpha = 0.6f)) },
=======
                        label = { Text("Privacy Policy URL", color = Color(0xFFA7F3D0).copy(alpha = 0.6f)) },
>>>>>>> 6e834ed (Update Taqwahub)
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = termsOfServiceUrlInput,
                        onValueChange = { termsOfServiceUrlInput = it },
<<<<<<< HEAD
                        label = { Text("Terms of Service URL 📜", color = Color(0xFFA7F3D0).copy(alpha = 0.6f)) },
=======
                        label = { Text("Terms of Service URL", color = Color(0xFFA7F3D0).copy(alpha = 0.6f)) },
>>>>>>> 6e834ed (Update Taqwahub)
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = deleteAccountUrlInput,
                        onValueChange = { deleteAccountUrlInput = it },
<<<<<<< HEAD
                        label = { Text("Web Account Deletion Portal URL ⚠️", color = Color(0xFFA7F3D0).copy(alpha = 0.6f)) },
=======
                        label = { Text("Web Account Deletion Portal URL", color = Color(0xFFA7F3D0).copy(alpha = 0.6f)) },
>>>>>>> 6e834ed (Update Taqwahub)
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            try {
                                val minVer = minVersionInput.trim()
                                val newConfig = viewModel.appConfig.copy(
                                    isUnderMaintenance = maintenanceInput,
                                    message = maintenanceMsgInput,
                                    welcomeBannerMessage = welcomeMsgInput,
                                    forceUpdateMinVersion = minVer,
                                    updateDownloadUrl = updateDownloadUrlInput,
                                    donateRedirectUrl = donateRedirectUrlInput.trim(),
                                    privacyPolicyUrl = privacyPolicyUrlInput.trim(),
                                    termsOfServiceUrl = termsOfServiceUrlInput.trim(),
                                    deleteAccountUrl = deleteAccountUrlInput.trim(),
                                    isQuranAudioLocked = isQuranAudioLockedInput,
                                    quranAudioBlockedMessage = quranAudioMsgInput,
                                    lockedSurahIds = lockedSurahIdsInput,
                                    surahBlockedMessage = surahMsgInput,
                                    lockedWordSurahIds = lockedWordSurahIdsInput,
                                    wordSurahBlockedMessage = wordSurahMsgInput,
                                    welcomeBismillahMessage = welcomeBismillahMessageInput
                                )
                                viewModel.saveAppConfig(newConfig, {
                                    Toast.makeText(context, "System configuration successfully updated!", Toast.LENGTH_SHORT).show()
                                }, { err ->
                                    Toast.makeText(context, "Config save failed: $err", Toast.LENGTH_LONG).show()
                                })
                            } catch (e: Exception) {
                                Toast.makeText(context, "Save Exception: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Save Standard Configurations", color = Color(0xFF021612), fontWeight = FontWeight.Black)
                    }
                }
            }

<<<<<<< HEAD
            // SECTION 1.2: Page & Feature Blockers
            AdminCollapsibleCard(
                title = "1.2. Page & Feature Blockers",
=======
            // SECTION 1.2: Module Security & Visibility Matrix (Lock, Hide, Custom Reasons)
            val saveModuleMatrixSettings = {
                try {
                    val newConfig = viewModel.appConfig.copy(
                        isHadithLocked = isHadithLockedInput,
                        isHadithHidden = isHadithHiddenInput,
                        hadithLockReason = hadithReasonInput,
                        hadithLockCategory = hadithCategoryInput,

                        isDuaLocked = isDuaLockedInput,
                        isDuaHidden = isDuaHiddenInput,
                        duaLockReason = duaReasonInput,
                        duaLockCategory = duaCategoryInput,

                        isQuranLocked = isQuranLockedInput,
                        isQuranHidden = isQuranHiddenInput,
                        quranLockReason = quranReasonInput,
                        quranLockCategory = quranCategoryInput,

                        isLeaderboardLocked = isLeaderboardLockedInput,
                        isLeaderboardHidden = isLeaderboardHiddenInput,
                        leaderboardLockReason = leaderboardReasonInput,
                        leaderboardLockCategory = leaderboardCategoryInput,

                        isTasksLocked = isTasksLockedInput,
                        isTasksHidden = isTasksHiddenInput,
                        tasksLockReason = tasksReasonInput,
                        tasksLockCategory = tasksCategoryInput,

                        isTasbeehLocked = isTasbeehLockedInput,
                        isTasbeehHidden = isTasbeehHiddenInput,
                        tasbeehLockReason = tasbeehReasonInput,
                        tasbeehLockCategory = tasbeehCategoryInput,

                        isNamesLocked = isNamesLockedInput,
                        isNamesHidden = isNamesHiddenInput,
                        namesLockReason = namesReasonInput,
                        namesLockCategory = namesCategoryInput,

                        isZakatLocked = isZakatLockedInput,
                        isZakatHidden = isZakatHiddenInput,
                        zakatLockReason = zakatReasonInput,
                        zakatLockCategory = zakatCategoryInput,

                        isQiblaLocked = isQiblaLockedInput,
                        isQiblaHidden = isQiblaHiddenInput,
                        qiblaLockReason = qiblaReasonInput,
                        qiblaLockCategory = qiblaCategoryInput,

                        isCalendarLocked = isCalendarLockedInput,
                        isCalendarHidden = isCalendarHiddenInput,
                        calendarLockReason = calendarReasonInput,
                        calendarLockCategory = calendarCategoryInput,

                        isComplaintsLocked = isComplaintsLockedInput,
                        isComplaintsHidden = isComplaintsHiddenInput,
                        complaintsLockReason = complaintsReasonInput,
                        complaintsLockCategory = complaintsCategoryInput,

                        isDonateLocked = isDonateLockedInput,
                        isDonateHidden = isDonateHiddenInput,
                        donateLockReason = donateReasonInput,
                        donateLockCategory = donateCategoryInput
                    )
                    viewModel.saveAppConfig(newConfig, {
                        Toast.makeText(context, "Page settings saved live!", Toast.LENGTH_SHORT).show()
                    }, { err ->
                        Toast.makeText(context, "Save failed: $err", Toast.LENGTH_LONG).show()
                    })
                } catch (e: Exception) {
                    Toast.makeText(context, "Save Exception: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            AdminCollapsibleCard(
                title = "1.2. Module Security & Visibility Matrix (12 Pages)",
>>>>>>> 6e834ed (Update Taqwahub)
                icon = Icons.Default.Block,
                isExpanded = isPageFeatureBlockersExpanded,
                onHeaderClick = { isPageFeatureBlockersExpanded = !isPageFeatureBlockersExpanded }
            ) {
<<<<<<< HEAD
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Easily turn off specific app features, blocks, and define custom warning messages globally.",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                    
                    // Quran Page
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Lock Quran Page", color = Color.White, fontWeight = FontWeight.Bold)
                        Switch(checked = isQuranPageLockedInput, onCheckedChange = { isQuranPageLockedInput = it }, colors = SwitchDefaults.colors(checkedThumbColor = GoldPrimary))
                    }
                    OutlinedTextField(
                        value = quranPageMsgInput, onValueChange = { quranPageMsgInput = it },
                        label = { Text("Quran Page Locked Message", color = Color(0xFFA7F3D0).copy(alpha = 0.6f)) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White), modifier = Modifier.fillMaxWidth()
                    )

                    // Tools Page
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Lock Tools Page", color = Color.White, fontWeight = FontWeight.Bold)
                        Switch(checked = isToolsPageLockedInput, onCheckedChange = { isToolsPageLockedInput = it }, colors = SwitchDefaults.colors(checkedThumbColor = GoldPrimary))
                    }
                    OutlinedTextField(
                        value = toolsPageMsgInput, onValueChange = { toolsPageMsgInput = it },
                        label = { Text("Tools Page Locked Message", color = Color(0xFFA7F3D0).copy(alpha = 0.6f)) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White), modifier = Modifier.fillMaxWidth()
                    )

                    // Learn Page
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Lock Learn Page", color = Color.White, fontWeight = FontWeight.Bold)
                        Switch(checked = isLearnPageLockedInput, onCheckedChange = { isLearnPageLockedInput = it }, colors = SwitchDefaults.colors(checkedThumbColor = GoldPrimary))
                    }
                    OutlinedTextField(
                        value = learnPageMsgInput, onValueChange = { learnPageMsgInput = it },
                        label = { Text("Learn Page Locked Message", color = Color(0xFFA7F3D0).copy(alpha = 0.6f)) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White), modifier = Modifier.fillMaxWidth()
                    )

                    // Cards
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Lock Prayer Times Card", color = Color.White, fontWeight = FontWeight.Bold)
                        Switch(checked = isPrayerTimesCardLockedInput, onCheckedChange = { isPrayerTimesCardLockedInput = it }, colors = SwitchDefaults.colors(checkedThumbColor = GoldPrimary))
                    }
                    OutlinedTextField(
                        value = prayerTimesMsgInput, onValueChange = { prayerTimesMsgInput = it },
                        label = { Text("Prayer Times Locked Message", color = Color(0xFFA7F3D0).copy(alpha = 0.6f)) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White), modifier = Modifier.fillMaxWidth()
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Lock Jumu'ah Card", color = Color.White, fontWeight = FontWeight.Bold)
                        Switch(checked = isDailyAyahCardLockedInput, onCheckedChange = { isDailyAyahCardLockedInput = it }, colors = SwitchDefaults.colors(checkedThumbColor = GoldPrimary))
                    }
                    OutlinedTextField(
                        value = dailyAyahMsgInput, onValueChange = { dailyAyahMsgInput = it },
                        label = { Text("Jumu'ah Card Locked Message", color = Color(0xFFA7F3D0).copy(alpha = 0.6f)) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White), modifier = Modifier.fillMaxWidth()
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Lock Tracker Card", color = Color.White, fontWeight = FontWeight.Bold)
                        Switch(checked = isTrackerCardLockedInput, onCheckedChange = { isTrackerCardLockedInput = it }, colors = SwitchDefaults.colors(checkedThumbColor = GoldPrimary))
                    }
                    OutlinedTextField(
                        value = trackerMsgInput, onValueChange = { trackerMsgInput = it },
                        label = { Text("Tracker Locked Message", color = Color(0xFFA7F3D0).copy(alpha = 0.6f)) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White), modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            try {
                                val newConfig = viewModel.appConfig.copy(
                                    isQuranPageLocked = isQuranPageLockedInput,
                                    quranPageBlockedMessage = quranPageMsgInput,
                                    isToolsPageLocked = isToolsPageLockedInput,
                                    toolsPageBlockedMessage = toolsPageMsgInput,
                                    isLearnPageLocked = isLearnPageLockedInput,
                                    learnPageBlockedMessage = learnPageMsgInput,
                                    isPrayerTimesCardLocked = isPrayerTimesCardLockedInput,
                                    prayerTimesBlockedMessage = prayerTimesMsgInput,
                                    isDailyAyahCardLocked = isDailyAyahCardLockedInput,
                                    dailyAyahBlockedMessage = dailyAyahMsgInput,
                                    isTrackerCardLocked = isTrackerCardLockedInput,
                                    trackerBlockedMessage = trackerMsgInput
                                )
                                viewModel.saveAppConfig(newConfig, {
                                    Toast.makeText(context, "Blockers successfully updated!", Toast.LENGTH_SHORT).show()
                                }, { err ->
                                    Toast.makeText(context, "Blockers save failed: $err", Toast.LENGTH_LONG).show()
                                })
                            } catch (e: Exception) {
                                Toast.makeText(context, "Save Exception: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        },
=======
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Granular security matrix: Lock, Hide, set Custom Reason Messages, and select Theme-Matching Artwork for all 12 core app pages.",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )

                    // Quick Emergency Batch Actions
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF03261E)),
                        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Emergency Batch Controls", color = GoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        isHadithLockedInput = false; isDuaLockedInput = false; isQuranLockedInput = false
                                        isLeaderboardLockedInput = false; isTasksLockedInput = false; isTasbeehLockedInput = false
                                        isNamesLockedInput = false; isZakatLockedInput = false; isQiblaLockedInput = false
                                        isCalendarLockedInput = false; isComplaintsLockedInput = false; isDonateLockedInput = false
                                    },
                                    border = BorderStroke(1.dp, Color(0xFF10B981)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LockOpen,
                                        contentDescription = null,
                                        tint = Color(0xFF6EE7B7),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Unlock All", color = Color(0xFF6EE7B7), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        isHadithHiddenInput = false; isDuaHiddenInput = false; isQuranHiddenInput = false
                                        isLeaderboardHiddenInput = false; isTasksHiddenInput = false; isTasbeehHiddenInput = false
                                        isNamesHiddenInput = false; isZakatHiddenInput = false; isQiblaHiddenInput = false
                                        isCalendarHiddenInput = false; isComplaintsHiddenInput = false; isDonateHiddenInput = false
                                    },
                                    border = BorderStroke(1.dp, GoldPrimary),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Visibility,
                                        contentDescription = null,
                                        tint = GoldPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Unhide All", color = GoldPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // 1. Hadith Module
                    ModuleMatrixItemCard(
                        title = "Hadith Collection",
                        icon = Icons.Default.MenuBook,
                        isLocked = isHadithLockedInput, onLockChange = { isHadithLockedInput = it },
                        isHidden = isHadithHiddenInput, onHideChange = { isHadithHiddenInput = it },
                        reason = hadithReasonInput, onReasonChange = { hadithReasonInput = it },
                        category = hadithCategoryInput, onCategoryChange = { hadithCategoryInput = it },
                        onPreviewClick = { previewLockData = Triple("Hadith Collection", hadithCategoryInput, hadithReasonInput) },
                        onSaveClick = saveModuleMatrixSettings
                    )

                    // 2. Dua Module
                    ModuleMatrixItemCard(
                        title = "Dua & Azkar Supplications",
                        icon = Icons.Default.Favorite,
                        isLocked = isDuaLockedInput, onLockChange = { isDuaLockedInput = it },
                        isHidden = isDuaHiddenInput, onHideChange = { isDuaHiddenInput = it },
                        reason = duaReasonInput, onReasonChange = { duaReasonInput = it },
                        category = duaCategoryInput, onCategoryChange = { duaCategoryInput = it },
                        onPreviewClick = { previewLockData = Triple("Dua & Azkar Supplications", duaCategoryInput, duaReasonInput) },
                        onSaveClick = saveModuleMatrixSettings
                    )

                    // 3. Quran Module
                    ModuleMatrixItemCard(
                        title = "Holy Quran & Recitations",
                        icon = Icons.Default.AutoStories,
                        isLocked = isQuranLockedInput, onLockChange = { isQuranLockedInput = it },
                        isHidden = isQuranHiddenInput, onHideChange = { isQuranHiddenInput = it },
                        reason = quranReasonInput, onReasonChange = { quranReasonInput = it },
                        category = quranCategoryInput, onCategoryChange = { quranCategoryInput = it },
                        onPreviewClick = { previewLockData = Triple("Holy Quran & Recitations", quranCategoryInput, quranReasonInput) },
                        onSaveClick = saveModuleMatrixSettings
                    )

                    // 4. Leaderboard Module
                    ModuleMatrixItemCard(
                        title = "Spiritual Leaderboard & Ranks",
                        icon = Icons.Default.EmojiEvents,
                        isLocked = isLeaderboardLockedInput, onLockChange = { isLeaderboardLockedInput = it },
                        isHidden = isLeaderboardHiddenInput, onHideChange = { isLeaderboardHiddenInput = it },
                        reason = leaderboardReasonInput, onReasonChange = { leaderboardReasonInput = it },
                        category = leaderboardCategoryInput, onCategoryChange = { leaderboardCategoryInput = it },
                        onPreviewClick = { previewLockData = Triple("Spiritual Leaderboard & Ranks", leaderboardCategoryInput, leaderboardReasonInput) },
                        onSaveClick = saveModuleMatrixSettings
                    )

                    // 5. Tasks Module
                    ModuleMatrixItemCard(
                        title = "Spiritual Challenges & Tasks",
                        icon = Icons.Default.CheckCircle,
                        isLocked = isTasksLockedInput, onLockChange = { isTasksLockedInput = it },
                        isHidden = isTasksHiddenInput, onHideChange = { isTasksHiddenInput = it },
                        reason = tasksReasonInput, onReasonChange = { tasksReasonInput = it },
                        category = tasksCategoryInput, onCategoryChange = { tasksCategoryInput = it },
                        onPreviewClick = { previewLockData = Triple("Spiritual Challenges & Tasks", tasksCategoryInput, tasksReasonInput) },
                        onSaveClick = saveModuleMatrixSettings
                    )

                    // 6. Tasbeeh Counter Module
                    ModuleMatrixItemCard(
                        title = "Digital Tasbeeh Counter",
                        icon = Icons.Default.TouchApp,
                        isLocked = isTasbeehLockedInput, onLockChange = { isTasbeehLockedInput = it },
                        isHidden = isTasbeehHiddenInput, onHideChange = { isTasbeehHiddenInput = it },
                        reason = tasbeehReasonInput, onReasonChange = { tasbeehReasonInput = it },
                        category = tasbeehCategoryInput, onCategoryChange = { tasbeehCategoryInput = it },
                        onPreviewClick = { previewLockData = Triple("Digital Tasbeeh Counter", tasbeehCategoryInput, tasbeehReasonInput) },
                        onSaveClick = saveModuleMatrixSettings
                    )

                    // 7. 99 Names Module
                    ModuleMatrixItemCard(
                        title = "99 Names of Allah (Asmaul Husna)",
                        icon = Icons.Default.Star,
                        isLocked = isNamesLockedInput, onLockChange = { isNamesLockedInput = it },
                        isHidden = isNamesHiddenInput, onHideChange = { isNamesHiddenInput = it },
                        reason = namesReasonInput, onReasonChange = { namesReasonInput = it },
                        category = namesCategoryInput, onCategoryChange = { namesCategoryInput = it },
                        onPreviewClick = { previewLockData = Triple("99 Names of Allah", namesCategoryInput, namesReasonInput) },
                        onSaveClick = saveModuleMatrixSettings
                    )

                    // 8. Zakat Calculator Module
                    ModuleMatrixItemCard(
                        title = "Zakat Calculator",
                        icon = Icons.Default.Calculate,
                        isLocked = isZakatLockedInput, onLockChange = { isZakatLockedInput = it },
                        isHidden = isZakatHiddenInput, onHideChange = { isZakatHiddenInput = it },
                        reason = zakatReasonInput, onReasonChange = { zakatReasonInput = it },
                        category = zakatCategoryInput, onCategoryChange = { zakatCategoryInput = it },
                        onPreviewClick = { previewLockData = Triple("Zakat Calculator", zakatCategoryInput, zakatReasonInput) },
                        onSaveClick = saveModuleMatrixSettings
                    )

                    // 9. Qibla Finder Module
                    ModuleMatrixItemCard(
                        title = "Qibla Compass Finder",
                        icon = Icons.Default.Explore,
                        isLocked = isQiblaLockedInput, onLockChange = { isQiblaLockedInput = it },
                        isHidden = isQiblaHiddenInput, onHideChange = { isQiblaHiddenInput = it },
                        reason = qiblaReasonInput, onReasonChange = { qiblaReasonInput = it },
                        category = qiblaCategoryInput, onCategoryChange = { qiblaCategoryInput = it },
                        onPreviewClick = { previewLockData = Triple("Qibla Compass Finder", qiblaCategoryInput, qiblaReasonInput) },
                        onSaveClick = saveModuleMatrixSettings
                    )

                    // 10. Hijri Calendar Module
                    ModuleMatrixItemCard(
                        title = "Islamic Hijri Calendar",
                        icon = Icons.Default.CalendarToday,
                        isLocked = isCalendarLockedInput, onLockChange = { isCalendarLockedInput = it },
                        isHidden = isCalendarHiddenInput, onHideChange = { isCalendarHiddenInput = it },
                        reason = calendarReasonInput, onReasonChange = { calendarReasonInput = it },
                        category = calendarCategoryInput, onCategoryChange = { calendarCategoryInput = it },
                        onPreviewClick = { previewLockData = Triple("Islamic Hijri Calendar", calendarCategoryInput, calendarReasonInput) },
                        onSaveClick = saveModuleMatrixSettings
                    )

                    // 11. Complaints & Support Module
                    ModuleMatrixItemCard(
                        title = "User Feedback & Support",
                        icon = Icons.Default.Feedback,
                        isLocked = isComplaintsLockedInput, onLockChange = { isComplaintsLockedInput = it },
                        isHidden = isComplaintsHiddenInput, onHideChange = { isComplaintsHiddenInput = it },
                        reason = complaintsReasonInput, onReasonChange = { complaintsReasonInput = it },
                        category = complaintsCategoryInput, onCategoryChange = { complaintsCategoryInput = it },
                        onPreviewClick = { previewLockData = Triple("User Feedback & Support", complaintsCategoryInput, complaintsReasonInput) },
                        onSaveClick = saveModuleMatrixSettings
                    )

                    // 12. Donate Module
                    ModuleMatrixItemCard(
                        title = "Sadaqah & Donation Portal",
                        icon = Icons.Default.Favorite,
                        isLocked = isDonateLockedInput, onLockChange = { isDonateLockedInput = it },
                        isHidden = isDonateHiddenInput, onHideChange = { isDonateHiddenInput = it },
                        reason = donateReasonInput, onReasonChange = { donateReasonInput = it },
                        category = donateCategoryInput, onCategoryChange = { donateCategoryInput = it },
                        onPreviewClick = { previewLockData = Triple("Sadaqah & Donation Portal", donateCategoryInput, donateReasonInput) },
                        onSaveClick = saveModuleMatrixSettings
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = saveModuleMatrixSettings,
>>>>>>> 6e834ed (Update Taqwahub)
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
<<<<<<< HEAD
                        Text("Save Blockers", color = Color(0xFF021612), fontWeight = FontWeight.Black)
=======
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            tint = Color(0xFF021612),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save All Module Configurations", color = Color(0xFF021612), fontWeight = FontWeight.Black)
>>>>>>> 6e834ed (Update Taqwahub)
                    }
                }
            }

            // SECTION 1.5: MANAGE ADMIN EMAILS (SECURE FIREBASE STORAGE)
            AdminCollapsibleCard(
                title = "1.5. Dynamic Admin Emails & Access Control",
                icon = Icons.Default.Security,
                isExpanded = isAdminsExpanded,
                onHeaderClick = { isAdminsExpanded = !isAdminsExpanded }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Authorized administrative users are retrieved securely in real-time directly from Firebase. Add or remove emails to grant/revoke admin rights dynamically.",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                    
                    Text(
                        text = "Current Database Admins:",
                        color = GoldPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    viewModel.adminEmails.forEach { adminEmail ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF021E15), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = adminEmail,
                                color = Color.White,
                                fontSize = 13.sp,
                                modifier = Modifier.weight(1f)
                            )
                            if (adminEmail != "kb1747038@gmail.com") { // Prevent locking out primary developer
                                IconButton(
                                    onClick = {
                                        val updatedList = viewModel.adminEmails.filter { it != adminEmail }
                                        viewModel.updateAdminEmails(updatedList, {
                                            Toast.makeText(context, "Admin access revoked for $adminEmail", Toast.LENGTH_SHORT).show()
                                        }, { err ->
                                            Toast.makeText(context, "Failed: $err", Toast.LENGTH_SHORT).show()
                                        })
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Remove Admin",
                                        tint = Color.Red.copy(alpha = 0.8f)
                                    )
                                }
                            } else {
                                Text(
                                    text = "Primary Owner",
                                    color = GoldPrimary.copy(alpha = 0.6f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Grant Admin Access",
                        color = GoldPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newAdminEmail,
                            onValueChange = { newAdminEmail = it },
                            placeholder = { Text("new-admin@email.com", color = Color.White.copy(alpha = 0.3f)) },
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 14.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = Color(0xFF1B4E38)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        
                        Button(
                            onClick = {
                                val emailToGrant = newAdminEmail.lowercase().trim()
                                if (emailToGrant.isEmpty() || !emailToGrant.contains("@")) {
                                    Toast.makeText(context, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (viewModel.adminEmails.any { it.lowercase().trim() == emailToGrant }) {
                                    Toast.makeText(context, "User is already an Admin", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                val updatedList = viewModel.adminEmails + emailToGrant
                                viewModel.updateAdminEmails(updatedList, {
                                    Toast.makeText(context, "Admin access granted for $emailToGrant", Toast.LENGTH_SHORT).show()
                                    newAdminEmail = ""
                                }, { err ->
                                    Toast.makeText(context, "Failed: $err", Toast.LENGTH_SHORT).show()
                                })
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Grant", color = Color(0xFF021612), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            }

            // SECTION 2: SYSTEM ACTIVE USERS COUNT
            AdminCollapsibleCard(
                title = "2. System Users Tracking Stats",
                icon = Icons.Default.BarChart,
                isExpanded = isUsersExpanded,
                onHeaderClick = { 
                    isUsersExpanded = !isUsersExpanded 
                    if (isUsersExpanded) viewModel.fetchTotalUsersCount()
                }
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Total Active Registered App Users in Firestore",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF021612)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 40.dp, vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = viewModel.totalUsersCount.toString(),
                                color = GoldPrimary,
                                fontSize = 48.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Registered Accounts",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    IconButton(
                        onClick = { 
                            viewModel.fetchTotalUsersCount()
                            Toast.makeText(context, "Users count refreshed", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.background(GoldPrimary.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh count", tint = GoldPrimary)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFF1B4E38), thickness = 1.dp)
<<<<<<< HEAD
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Admin User Lookup & Remote Control",
                        color = GoldPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = userSearchQuery,
                        onValueChange = { userSearchQuery = it.trim().lowercase() },
                        modifier = Modifier.fillMaxWidth().testTag("admin_user_search_field"),
                        placeholder = { Text("Search by unique username...", color = Color.White.copy(alpha = 0.4f), fontSize = 13.sp) },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        trailingIcon = {
                            if (isSearchingUser) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = GoldPrimary, strokeWidth = 2.dp)
                            } else {
                                IconButton(
                                    onClick = {
                                        val queryStr = userSearchQuery.trim().lowercase()
                                        if (queryStr.isEmpty()) {
                                            Toast.makeText(context, "Please enter a username", Toast.LENGTH_SHORT).show()
                                            return@IconButton
                                        }
                                        isSearchingUser = true
                                        searchedUserStats = null
                                        searchedUserUid = null

                                        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                        db.collection("users")
                                            .whereEqualTo("userStats.username", queryStr)
                                            .get()
                                            .addOnSuccessListener { snapshot ->
                                                isSearchingUser = false
                                                if (snapshot != null && !snapshot.isEmpty) {
                                                    val doc = snapshot.documents.first()
                                                    val uid = doc.id
                                                    val remoteStatsMap = doc.get("userStats") as? Map<String, Any>
                                                    if (remoteStatsMap != null) {
                                                        val stats = UserStatsEntity(
                                                            id = 1,
                                                            totalTasksCompleted = (remoteStatsMap["totalTasksCompleted"] as? Long)?.toInt() ?: 0,
                                                            daysActive = (remoteStatsMap["daysActive"] as? Long)?.toInt() ?: 1,
                                                            quranProgress = (remoteStatsMap["quranProgress"] as? Long)?.toInt() ?: 0,
                                                            lastReadSurah = (remoteStatsMap["lastReadSurah"] as? Long)?.toInt() ?: 1,
                                                            lastReadVerse = (remoteStatsMap["lastReadVerse"] as? Long)?.toInt() ?: 1,
                                                            lastReadVerseKey = remoteStatsMap["lastReadVerseKey"] as? String ?: "1:1",
                                                            tasbeehCount = (remoteStatsMap["tasbeehCount"] as? Long)?.toInt() ?: 0,
                                                            lastResetDate = remoteStatsMap["lastResetDate"] as? String ?: "",
                                                            currentStreak = (remoteStatsMap["currentStreak"] as? Long)?.toInt() ?: 0,
                                                            streakChancesLeft = (remoteStatsMap["streakChancesLeft"] as? Long)?.toInt() ?: 2,
                                                            longestStreak = (remoteStatsMap["longestStreak"] as? Long)?.toInt() ?: 0,
                                                            totalXp = (remoteStatsMap["totalXp"] as? Long)?.toInt() ?: 0,
                                                            weeklyXp = (remoteStatsMap["weeklyXp"] as? Long)?.toInt() ?: 0,
                                                            lastActiveWeekOfYear = (remoteStatsMap["lastActiveWeekOfYear"] as? Long)?.toInt() ?: 0,
                                                            name = remoteStatsMap["name"] as? String ?: "Servant of Allah",
                                                            username = remoteStatsMap["username"] as? String ?: "",
                                                            gender = remoteStatsMap["gender"] as? String ?: "Male",
                                                            sectOrCast = remoteStatsMap["sectOrCast"] as? String ?: "Sunni",
                                                            email = remoteStatsMap["email"] as? String ?: "",
                                                            completedSurahs = remoteStatsMap["completedSurahs"] as? String ?: "",
                                                            firstPlaceCount = (remoteStatsMap["firstPlaceCount"] as? Long)?.toInt() ?: 0,
                                                            secondPlaceCount = (remoteStatsMap["secondPlaceCount"] as? Long)?.toInt() ?: 0,
                                                            thirdPlaceCount = (remoteStatsMap["thirdPlaceCount"] as? Long)?.toInt() ?: 0,
                                                            isBlocked = remoteStatsMap["isBlocked"] as? Boolean ?: false, isVerified = remoteStatsMap["isVerified"] as? Boolean ?: false, profilePictureBase64 = remoteStatsMap["profilePictureBase64"] as? String ?: ""
                                                        )
                                                        searchedUserStats = stats
                                                        searchedUserUid = uid
                                                        Toast.makeText(context, "User found!", Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        Toast.makeText(context, "User stats map is empty", Toast.LENGTH_SHORT).show()
                                                    }
                                                } else {
                                                    Toast.makeText(context, "User not found!", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                            .addOnFailureListener {
                                                isSearchingUser = false
                                                Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_LONG).show()
                                            }
                                    }
                                ) {
                                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = GoldPrimary)
                                }
                            }
                        },
=======
                    // SECTION 1.6: PAGINATED USER DIRECTORY & REMOTE CONTROL
                    Spacer(modifier = Modifier.height(16.dp))

                    LaunchedEffect(Unit) {
                        if (viewModel.adminUserList.isEmpty()) {
                            viewModel.loadAdminUsers(reset = true)
                        }
                    }

                    // Header Row with Title and Live User Count Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.People, contentDescription = "User Directory", tint = GoldPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "User Directory & Remote Control",
                                color = GoldPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Total Users Count Badge
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(Color(0xFF10B981), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                val userCountDisplay = if (viewModel.adminTotalUserCount > 0) viewModel.adminTotalUserCount else viewModel.adminUserList.size
                                Text(
                                    text = "$userCountDisplay Users",
                                    color = Color(0xFF10B981),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Live Search Bar with Instant Filter & Database Search
                    OutlinedTextField(
                        value = viewModel.adminUserSearchQuery,
                        onValueChange = {
                            viewModel.adminUserSearchQuery = it
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_user_search_field"),
                        placeholder = { Text("Search by name, @username, or email...", color = Color.White.copy(alpha = 0.4f), fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = GoldPrimary)
                        },
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (viewModel.adminUserSearchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.adminUserSearchQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.White.copy(alpha = 0.6f))
                                    }
                                }
                                if (viewModel.isAdminUserLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp).padding(end = 4.dp), color = GoldPrimary, strokeWidth = 2.dp)
                                } else if (viewModel.adminUserSearchQuery.trim().isNotEmpty()) {
                                    IconButton(
                                        onClick = {
                                            val queryStr = viewModel.adminUserSearchQuery.trim().lowercase()
                                            viewModel.searchAdminUsersRemote(queryStr) { foundCount ->
                                                Toast.makeText(context, if (foundCount > 0) "Found $foundCount matching users" else "No matching users found in DB", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.ArrowForward, contentDescription = "Search Database", tint = GoldPrimary)
                                    }
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
>>>>>>> 6e834ed (Update Taqwahub)
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = Color(0xFF1B4E38)
                        )
                    )

<<<<<<< HEAD
                    searchedUserStats?.let { stats ->
                        Spacer(modifier = Modifier.height(16.dp))

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF021612)),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
=======
                    Spacer(modifier = Modifier.height(12.dp))

                    // Filtered List Logic
                    val searchQuery = viewModel.adminUserSearchQuery.trim().lowercase()
                    val displayedUsers = if (searchQuery.isBlank()) {
                        viewModel.adminUserList
                    } else {
                        viewModel.adminUserList.filter { (uid, stats) ->
                            stats.username.lowercase().contains(searchQuery) ||
                            stats.name.lowercase().contains(searchQuery) ||
                            stats.email.lowercase().contains(searchQuery) ||
                            uid.lowercase().contains(searchQuery)
                        }
                    }

                    var expandedUserUid by remember { mutableStateOf<String?>(null) }

                    if (displayedUsers.isEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF021612)),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFF1B4E38)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (viewModel.isAdminUserLoading) {
                                    CircularProgressIndicator(color = GoldPrimary, strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("Loading users...", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                                } else {
                                    Icon(Icons.Default.PersonSearch, contentDescription = "No users", tint = GoldPrimary.copy(alpha = 0.5f), modifier = Modifier.size(36.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = if (searchQuery.isNotBlank()) "No users match '$searchQuery'" else "No users loaded yet",
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 13.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    if (searchQuery.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = {
                                                viewModel.searchAdminUsersRemote(searchQuery) { found ->
                                                    if (found == 0) Toast.makeText(context, "No users found in Firestore", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Search Full Firestore DB", color = Color(0xFF021612), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            displayedUsers.forEachIndexed { index, pair ->
                                val (uid, stats) = pair
                                val isExpanded = (expandedUserUid == uid)

                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF021612)),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, if (isExpanded) GoldPrimary else Color(0xFF1B4E38)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        // Compact User Header Row
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { expandedUserUid = if (isExpanded) null else uid },
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                // Avatar Circle
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .background(GoldPrimary.copy(alpha = 0.15f), CircleShape)
                                                        .border(1.dp, GoldPrimary.copy(alpha = 0.4f), CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    val letter = (stats.name.ifBlank { stats.username.ifBlank { stats.email } }).firstOrNull()?.toString()?.uppercase() ?: "U"
                                                    Text(letter, color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                }

                                                Spacer(modifier = Modifier.width(10.dp))

                                                Column {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        val displayName = when {
                                                            stats.name.isNotBlank() -> stats.name
                                                            stats.username.isNotBlank() -> "@${stats.username.removePrefix("@")}"
                                                            else -> "User_${uid.take(6)}"
                                                        }
                                                        Text(displayName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)

                                                        if (stats.isVerified) {
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            Icon(Icons.Default.Verified, contentDescription = "Verified", tint = GoldPrimary, modifier = Modifier.size(14.dp))
                                                        }
                                                    }

                                                    val handle = if (stats.username.isNotBlank()) "@${stats.username.removePrefix("@")}" else ""
                                                    val emailStr = stats.email.ifBlank { uid }
                                                    val subText = if (handle.isNotBlank()) "$handle • $emailStr" else emailStr
                                                    Text(subText, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp, maxLines = 1)
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(8.dp))

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                // Status Tag
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = if (stats.isBlocked) Color(0xFFEF4444).copy(alpha = 0.15f) else Color(0xFF10B981).copy(alpha = 0.15f),
                                                    border = BorderStroke(1.dp, if (stats.isBlocked) Color(0xFFEF4444) else Color(0xFF10B981))
                                                ) {
                                                    Text(
                                                        text = if (stats.isBlocked) "SUSPENDED" else "ACTIVE",
                                                        color = if (stats.isBlocked) Color(0xFFEF4444) else Color(0xFF10B981),
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }

                                                Spacer(modifier = Modifier.width(6.dp))

                                                Icon(
                                                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                                    contentDescription = "Expand controls",
                                                    tint = GoldPrimary
                                                )
                                            }
                                        }

                                        // Stats Quick Badge Bar
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Text("⚡ ${stats.totalXp} XP", color = GoldPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                            Text("🔥 ${stats.currentStreak} Day Streak", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                                            Text("🏆 ${stats.firstPlaceCount} Wins", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                                        }

                                        // Expanded Stats Editor & Remote Controls
                                        if (isExpanded) {
                                            Spacer(modifier = Modifier.height(12.dp))
                                            HorizontalDivider(color = Color(0xFF1B4E38).copy(alpha = 0.5f))
                                            Spacer(modifier = Modifier.height(12.dp))

                                            var editTotalXp by remember(stats) { mutableStateOf(stats.totalXp.toString()) }
                                            var editWeeklyXp by remember(stats) { mutableStateOf(stats.weeklyXp.toString()) }
                                            var editCurrentStreak by remember(stats) { mutableStateOf(stats.currentStreak.toString()) }
                                            var editStreakChances by remember(stats) { mutableStateOf(stats.streakChancesLeft.toString()) }
                                            var editFirstPlace by remember(stats) { mutableStateOf(stats.firstPlaceCount.toString()) }
                                            var editIsVerified by remember(stats) { mutableStateOf(stats.isVerified) }
                                            var isSavingChanges by remember { mutableStateOf(false) }

                                            Text("Modify User Statistics & Controls", color = GoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                                            Spacer(modifier = Modifier.height(8.dp))

                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                OutlinedTextField(
                                                    value = editTotalXp,
                                                    onValueChange = { editTotalXp = it },
                                                    label = { Text("Total XP", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp) },
                                                    modifier = Modifier.weight(1f),
                                                    singleLine = true,
                                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = GoldPrimary, unfocusedBorderColor = Color(0xFF1B4E38))
                                                )
                                                OutlinedTextField(
                                                    value = editWeeklyXp,
                                                    onValueChange = { editWeeklyXp = it },
                                                    label = { Text("Weekly XP", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp) },
                                                    modifier = Modifier.weight(1f),
                                                    singleLine = true,
                                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = GoldPrimary, unfocusedBorderColor = Color(0xFF1B4E38))
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(6.dp))

                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                OutlinedTextField(
                                                    value = editCurrentStreak,
                                                    onValueChange = { editCurrentStreak = it },
                                                    label = { Text("Streak", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp) },
                                                    modifier = Modifier.weight(1f),
                                                    singleLine = true,
                                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = GoldPrimary, unfocusedBorderColor = Color(0xFF1B4E38))
                                                )
                                                OutlinedTextField(
                                                    value = editStreakChances,
                                                    onValueChange = { editStreakChances = it },
                                                    label = { Text("Chances", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp) },
                                                    modifier = Modifier.weight(1f),
                                                    singleLine = true,
                                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = GoldPrimary, unfocusedBorderColor = Color(0xFF1B4E38))
                                                )
                                                OutlinedTextField(
                                                    value = editFirstPlace,
                                                    onValueChange = { editFirstPlace = it },
                                                    label = { Text("1st Place", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp) },
                                                    modifier = Modifier.weight(1f),
                                                    singleLine = true,
                                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = GoldPrimary, unfocusedBorderColor = Color(0xFF1B4E38))
                                                )
                                            }

                                            if (viewModel.isSuperAdmin) {
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text("Verified Servant Badge", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                        Text("Grant golden verified badge to user profile", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                                                    }
                                                    Switch(
                                                        checked = editIsVerified,
                                                        onCheckedChange = { editIsVerified = it },
                                                        colors = SwitchDefaults.colors(checkedThumbColor = GoldPrimary, checkedTrackColor = Color(0xFF1B4E38))
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(10.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Button(
                                                    onClick = {
                                                        isSavingChanges = true
                                                        val targetBlockedStatus = !stats.isBlocked
                                                        val currentWeekCode = viewModel.getLeaderboardWeekCode(viewModel.getSynchronizedTime())
                                                        val parsedWeeklyXp = editWeeklyXp.toIntOrNull()?.coerceAtLeast(0) ?: stats.weeklyXp
                                                        val targetWeekCode = if (parsedWeeklyXp > 0) currentWeekCode else stats.lastActiveWeekOfYear

                                                        val updatedStatsMap = hashMapOf<String, Any>(
                                                            "totalTasksCompleted" to stats.totalTasksCompleted,
                                                            "daysActive" to stats.daysActive,
                                                            "quranProgress" to stats.quranProgress,
                                                            "lastReadSurah" to stats.lastReadSurah,
                                                            "lastReadVerse" to stats.lastReadVerse,
                                                            "lastReadVerseKey" to stats.lastReadVerseKey,
                                                            "tasbeehCount" to stats.tasbeehCount,
                                                            "lastResetDate" to stats.lastResetDate,
                                                            "currentStreak" to (editCurrentStreak.toIntOrNull()?.coerceAtLeast(0) ?: stats.currentStreak) as Any,
                                                            "streakChancesLeft" to (editStreakChances.toIntOrNull()?.coerceAtLeast(0) ?: stats.streakChancesLeft) as Any,
                                                            "longestStreak" to stats.longestStreak,
                                                            "totalXp" to (editTotalXp.toIntOrNull()?.coerceAtLeast(0) ?: stats.totalXp) as Any,
                                                            "weeklyXp" to parsedWeeklyXp as Any,
                                                            "lastActiveWeekOfYear" to targetWeekCode as Any,
                                                            "name" to stats.name,
                                                            "username" to stats.username,
                                                            "gender" to stats.gender,
                                                            "sectOrCast" to stats.sectOrCast,
                                                            "email" to stats.email,
                                                            "completedSurahs" to stats.completedSurahs,
                                                            "firstPlaceCount" to (editFirstPlace.toIntOrNull()?.coerceAtLeast(0) ?: stats.firstPlaceCount) as Any,
                                                            "secondPlaceCount" to stats.secondPlaceCount,
                                                            "thirdPlaceCount" to stats.thirdPlaceCount,
                                                            "isBlocked" to targetBlockedStatus,
                                                            "isVerified" to editIsVerified,
                                                            "profilePictureBase64" to stats.profilePictureBase64,
                                                            "streakShields" to stats.streakShields,
                                                            "maxShields" to stats.maxShields,
                                                            "frozenDates" to stats.frozenDates,
                                                            "activeDates" to stats.activeDates,
                                                            "lastShieldUsedDate" to stats.lastShieldUsedDate,
                                                            "streakRepairsAvailable" to stats.streakRepairsAvailable
                                                        )

                                                        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                                        val nowMillis = System.currentTimeMillis()
                                                        val updatePayload = mapOf(
                                                            "userStats" to updatedStatsMap,
                                                            "lastUpdatedAt" to nowMillis,
                                                            "adminUpdatedTimestamp" to nowMillis
                                                        )
                                                        db.collection("users").document(uid)
                                                            .set(updatePayload, com.google.firebase.firestore.SetOptions.merge())
                                                            .addOnSuccessListener {
                                                                isSavingChanges = false
                                                                val updatedStats = stats.copy(
                                                                    isBlocked = targetBlockedStatus,
                                                                    totalXp = editTotalXp.toIntOrNull()?.coerceAtLeast(0) ?: stats.totalXp,
                                                                    weeklyXp = parsedWeeklyXp,
                                                                    lastActiveWeekOfYear = targetWeekCode,
                                                                    currentStreak = editCurrentStreak.toIntOrNull()?.coerceAtLeast(0) ?: stats.currentStreak,
                                                                    streakChancesLeft = editStreakChances.toIntOrNull()?.coerceAtLeast(0) ?: stats.streakChancesLeft,
                                                                    firstPlaceCount = editFirstPlace.toIntOrNull()?.coerceAtLeast(0) ?: stats.firstPlaceCount,
                                                                    isVerified = editIsVerified
                                                                )
                                                                val idx = viewModel.adminUserList.indexOfFirst { it.first == uid }
                                                                if (idx != -1) {
                                                                    viewModel.adminUserList[idx] = Pair(uid, updatedStats)
                                                                }
                                                                Toast.makeText(context, if (targetBlockedStatus) "User Suspended!" else "User Activated!", Toast.LENGTH_SHORT).show()
                                                            }
                                                            .addOnFailureListener {
                                                                isSavingChanges = false
                                                                Toast.makeText(context, "Failed: ${it.message}", Toast.LENGTH_LONG).show()
                                                            }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = if (stats.isBlocked) Color(0xFF10B981) else Color(0xFFEF4444)
                                                    ),
                                                    modifier = Modifier.weight(1.2f),
                                                    shape = RoundedCornerShape(10.dp)
                                                ) {
                                                    Text(if (stats.isBlocked) "UNSUSPEND" else "SUSPEND", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                                }

                                                Button(
                                                    onClick = {
                                                        isSavingChanges = true
                                                        val currentWeekCode = viewModel.getLeaderboardWeekCode(viewModel.getSynchronizedTime())
                                                        val parsedWeeklyXp = editWeeklyXp.toIntOrNull()?.coerceAtLeast(0) ?: stats.weeklyXp
                                                        val targetWeekCode = if (parsedWeeklyXp > 0) currentWeekCode else stats.lastActiveWeekOfYear

                                                        val updatedStatsMap = hashMapOf<String, Any>(
                                                            "totalTasksCompleted" to stats.totalTasksCompleted,
                                                            "daysActive" to stats.daysActive,
                                                            "quranProgress" to stats.quranProgress,
                                                            "lastReadSurah" to stats.lastReadSurah,
                                                            "lastReadVerse" to stats.lastReadVerse,
                                                            "lastReadVerseKey" to stats.lastReadVerseKey,
                                                            "tasbeehCount" to stats.tasbeehCount,
                                                            "lastResetDate" to stats.lastResetDate,
                                                            "currentStreak" to (editCurrentStreak.toIntOrNull()?.coerceAtLeast(0) ?: stats.currentStreak) as Any,
                                                            "streakChancesLeft" to (editStreakChances.toIntOrNull()?.coerceAtLeast(0) ?: stats.streakChancesLeft) as Any,
                                                            "longestStreak" to stats.longestStreak,
                                                            "totalXp" to (editTotalXp.toIntOrNull()?.coerceAtLeast(0) ?: stats.totalXp) as Any,
                                                            "weeklyXp" to parsedWeeklyXp as Any,
                                                            "lastActiveWeekOfYear" to targetWeekCode as Any,
                                                            "name" to stats.name,
                                                            "username" to stats.username,
                                                            "gender" to stats.gender,
                                                            "sectOrCast" to stats.sectOrCast,
                                                            "email" to stats.email,
                                                            "completedSurahs" to stats.completedSurahs,
                                                            "firstPlaceCount" to (editFirstPlace.toIntOrNull()?.coerceAtLeast(0) ?: stats.firstPlaceCount) as Any,
                                                            "secondPlaceCount" to stats.secondPlaceCount,
                                                            "thirdPlaceCount" to stats.thirdPlaceCount,
                                                            "isBlocked" to stats.isBlocked,
                                                            "isVerified" to editIsVerified,
                                                            "profilePictureBase64" to stats.profilePictureBase64
                                                        )

                                                        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                                        val nowMillis = System.currentTimeMillis()
                                                        val updatePayload = mapOf(
                                                            "userStats" to updatedStatsMap,
                                                            "lastUpdatedAt" to nowMillis,
                                                            "adminUpdatedTimestamp" to nowMillis
                                                        )
                                                        db.collection("users").document(uid)
                                                            .set(updatePayload, com.google.firebase.firestore.SetOptions.merge())
                                                            .addOnSuccessListener {
                                                                isSavingChanges = false
                                                                val updatedStats = stats.copy(
                                                                    totalXp = editTotalXp.toIntOrNull()?.coerceAtLeast(0) ?: stats.totalXp,
                                                                    weeklyXp = parsedWeeklyXp,
                                                                    lastActiveWeekOfYear = targetWeekCode,
                                                                    currentStreak = editCurrentStreak.toIntOrNull()?.coerceAtLeast(0) ?: stats.currentStreak,
                                                                    streakChancesLeft = editStreakChances.toIntOrNull()?.coerceAtLeast(0) ?: stats.streakChancesLeft,
                                                                    firstPlaceCount = editFirstPlace.toIntOrNull()?.coerceAtLeast(0) ?: stats.firstPlaceCount,
                                                                    isVerified = editIsVerified
                                                                )
                                                                val idx = viewModel.adminUserList.indexOfFirst { it.first == uid }
                                                                if (idx != -1) {
                                                                    viewModel.adminUserList[idx] = Pair(uid, updatedStats)
                                                                }
                                                                Toast.makeText(context, "User stats updated remotely!", Toast.LENGTH_SHORT).show()
                                                            }
                                                            .addOnFailureListener {
                                                                isSavingChanges = false
                                                                Toast.makeText(context, "Failed: ${it.message}", Toast.LENGTH_LONG).show()
                                                            }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                                                    modifier = Modifier.weight(1.8f),
                                                    shape = RoundedCornerShape(10.dp),
                                                    enabled = !isSavingChanges
                                                ) {
                                                    Text("SAVE REMOTELY", color = Color(0xFF021612), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Pagination Load More Controls
                            if (searchQuery.isBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
>>>>>>> 6e834ed (Update Taqwahub)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
<<<<<<< HEAD
                                    Column {
                                        Text(stats.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        Text("@${stats.username}", color = GoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                        Text(stats.email, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                                    }
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (stats.isBlocked) Color(0xFF7F1D1D).copy(alpha = 0.15f) else Color(0xFF10B981).copy(alpha = 0.15f)
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, if (stats.isBlocked) Color(0xFFEF4444) else Color(0xFF10B981))
                                    ) {
                                        Text(
                                            text = if (stats.isBlocked) "SUSPENDED" else "ACTIVE",
                                            color = if (stats.isBlocked) Color(0xFFEF4444) else Color(0xFF10B981),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                HorizontalDivider(color = Color(0xFF1B4E38).copy(alpha = 0.5f))

                                var editTotalXp by remember(stats) { mutableStateOf(stats.totalXp.toString()) }
                                var editWeeklyXp by remember(stats) { mutableStateOf(stats.weeklyXp.toString()) }
                                var editCurrentStreak by remember(stats) { mutableStateOf(stats.currentStreak.toString()) }
                                var editStreakChances by remember(stats) { mutableStateOf(stats.streakChancesLeft.toString()) }
                                var editFirstPlace by remember(stats) { mutableStateOf(stats.firstPlaceCount.toString()) }
                                var editIsVerified by remember(stats) { mutableStateOf(stats.isVerified) }

                                Text("Modify User Statistics", color = GoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = editTotalXp,
                                        onValueChange = { editTotalXp = it },
                                        label = { Text("Total XP", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp) },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = GoldPrimary, unfocusedBorderColor = Color(0xFF1B4E38))
                                    )
                                    OutlinedTextField(
                                        value = editWeeklyXp,
                                        onValueChange = { editWeeklyXp = it },
                                        label = { Text("Weekly XP", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp) },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = GoldPrimary, unfocusedBorderColor = Color(0xFF1B4E38))
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = editCurrentStreak,
                                        onValueChange = { editCurrentStreak = it },
                                        label = { Text("Streak", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp) },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = GoldPrimary, unfocusedBorderColor = Color(0xFF1B4E38))
                                    )
                                    OutlinedTextField(
                                        value = editStreakChances,
                                        onValueChange = { editStreakChances = it },
                                        label = { Text("Chances", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp) },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = GoldPrimary, unfocusedBorderColor = Color(0xFF1B4E38))
                                    )
                                    OutlinedTextField(
                                        value = editFirstPlace,
                                        onValueChange = { editFirstPlace = it },
                                        label = { Text("1st Place", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp) },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = GoldPrimary, unfocusedBorderColor = Color(0xFF1B4E38))
                                    )
                                }

                                if (viewModel.isSuperAdmin) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Verified Servant Badge", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                            Text("Grant premium golden verified badge to this user", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                                        }
                                        Switch(
                                            checked = editIsVerified,
                                            onCheckedChange = { editIsVerified = it },
                                            colors = SwitchDefaults.colors(checkedThumbColor = GoldPrimary, checkedTrackColor = Color(0xFF1B4E38))
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                var isSavingChanges by remember { mutableStateOf(false) }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            isSavingChanges = true
                                            val updatedStatsMap = hashMapOf<String, Any>(
                                                "totalTasksCompleted" to stats.totalTasksCompleted,
                                                "daysActive" to stats.daysActive,
                                                "quranProgress" to stats.quranProgress,
                                                "lastReadSurah" to stats.lastReadSurah,
                                                "lastReadVerse" to stats.lastReadVerse,
                                                "lastReadVerseKey" to stats.lastReadVerseKey,
                                                "tasbeehCount" to stats.tasbeehCount,
                                                "lastResetDate" to stats.lastResetDate,
                                                "currentStreak" to (editCurrentStreak.toIntOrNull()?.coerceAtLeast(0) ?: stats.currentStreak) as Any,
                                                "streakChancesLeft" to (editStreakChances.toIntOrNull()?.coerceAtLeast(0) ?: stats.streakChancesLeft) as Any,
                                                "longestStreak" to stats.longestStreak,
                                                "totalXp" to (editTotalXp.toIntOrNull()?.coerceAtLeast(0) ?: stats.totalXp) as Any,
                                                "weeklyXp" to (editWeeklyXp.toIntOrNull()?.coerceAtLeast(0) ?: stats.weeklyXp) as Any,
                                                "lastActiveWeekOfYear" to stats.lastActiveWeekOfYear,
                                                "name" to stats.name,
                                                "username" to stats.username,
                                                "gender" to stats.gender,
                                                "sectOrCast" to stats.sectOrCast,
                                                "email" to stats.email,
                                                "completedSurahs" to stats.completedSurahs,
                                                "firstPlaceCount" to (editFirstPlace.toIntOrNull()?.coerceAtLeast(0) ?: stats.firstPlaceCount) as Any,
                                                "secondPlaceCount" to stats.secondPlaceCount,
                                                "thirdPlaceCount" to stats.thirdPlaceCount,
                                                "isBlocked" to !stats.isBlocked,
                                                "isVerified" to editIsVerified,
                                                "profilePictureBase64" to stats.profilePictureBase64
                                            )

                                            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                            db.collection("users").document(searchedUserUid!!)
                                                .update(
                                                    "userStats", updatedStatsMap,
                                                    "lastUpdatedAt", System.currentTimeMillis()
                                                )
                                                .addOnSuccessListener {
                                                    isSavingChanges = false
                                                    searchedUserStats = stats.copy(
                                                        isBlocked = !stats.isBlocked,
                                                        totalXp = editTotalXp.toIntOrNull()?.coerceAtLeast(0) ?: stats.totalXp,
                                                        weeklyXp = editWeeklyXp.toIntOrNull()?.coerceAtLeast(0) ?: stats.weeklyXp,
                                                        currentStreak = editCurrentStreak.toIntOrNull()?.coerceAtLeast(0) ?: stats.currentStreak,
                                                        streakChancesLeft = editStreakChances.toIntOrNull()?.coerceAtLeast(0) ?: stats.streakChancesLeft,
                                                        firstPlaceCount = editFirstPlace.toIntOrNull()?.coerceAtLeast(0) ?: stats.firstPlaceCount,
                                                        isVerified = editIsVerified
                                                    )
                                                    Toast.makeText(context, if (!stats.isBlocked) "User Suspended!" else "User Activated!", Toast.LENGTH_SHORT).show()
                                                }
                                                .addOnFailureListener {
                                                    isSavingChanges = false
                                                    Toast.makeText(context, "Failed: ${it.message}", Toast.LENGTH_LONG).show()
                                                }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (stats.isBlocked) Color(0xFF10B981) else Color(0xFFEF4444)
                                        ),
                                        modifier = Modifier.weight(1.5f),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(if (stats.isBlocked) "UNSUSPEND" else "SUSPEND", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }

                                    Button(
                                        onClick = {
                                            isSavingChanges = true
                                            val updatedStatsMap = hashMapOf<String, Any>(
                                                "totalTasksCompleted" to stats.totalTasksCompleted,
                                                "daysActive" to stats.daysActive,
                                                "quranProgress" to stats.quranProgress,
                                                "lastReadSurah" to stats.lastReadSurah,
                                                "lastReadVerse" to stats.lastReadVerse,
                                                "lastReadVerseKey" to stats.lastReadVerseKey,
                                                "tasbeehCount" to stats.tasbeehCount,
                                                "lastResetDate" to stats.lastResetDate,
                                                "currentStreak" to (editCurrentStreak.toIntOrNull()?.coerceAtLeast(0) ?: stats.currentStreak) as Any,
                                                "streakChancesLeft" to (editStreakChances.toIntOrNull()?.coerceAtLeast(0) ?: stats.streakChancesLeft) as Any,
                                                "longestStreak" to stats.longestStreak,
                                                "totalXp" to (editTotalXp.toIntOrNull()?.coerceAtLeast(0) ?: stats.totalXp) as Any,
                                                "weeklyXp" to (editWeeklyXp.toIntOrNull()?.coerceAtLeast(0) ?: stats.weeklyXp) as Any,
                                                "lastActiveWeekOfYear" to stats.lastActiveWeekOfYear,
                                                "name" to stats.name,
                                                "username" to stats.username,
                                                "gender" to stats.gender,
                                                "sectOrCast" to stats.sectOrCast,
                                                "email" to stats.email,
                                                "completedSurahs" to stats.completedSurahs,
                                                "firstPlaceCount" to (editFirstPlace.toIntOrNull()?.coerceAtLeast(0) ?: stats.firstPlaceCount) as Any,
                                                "secondPlaceCount" to stats.secondPlaceCount,
                                                "thirdPlaceCount" to stats.thirdPlaceCount,
                                                "isBlocked" to stats.isBlocked,
                                                "isVerified" to editIsVerified,
                                                "profilePictureBase64" to stats.profilePictureBase64
                                            )

                                            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                            db.collection("users").document(searchedUserUid!!)
                                                .update(
                                                    "userStats", updatedStatsMap,
                                                    "lastUpdatedAt", System.currentTimeMillis()
                                                )
                                                .addOnSuccessListener {
                                                    isSavingChanges = false
                                                    searchedUserStats = stats.copy(
                                                        totalXp = editTotalXp.toIntOrNull()?.coerceAtLeast(0) ?: stats.totalXp,
                                                        weeklyXp = editWeeklyXp.toIntOrNull()?.coerceAtLeast(0) ?: stats.weeklyXp,
                                                        currentStreak = editCurrentStreak.toIntOrNull()?.coerceAtLeast(0) ?: stats.currentStreak,
                                                        streakChancesLeft = editStreakChances.toIntOrNull()?.coerceAtLeast(0) ?: stats.streakChancesLeft,
                                                        firstPlaceCount = editFirstPlace.toIntOrNull()?.coerceAtLeast(0) ?: stats.firstPlaceCount,
                                                        isVerified = editIsVerified
                                                    )
                                                    Toast.makeText(context, "User stats updated remote!", Toast.LENGTH_SHORT).show()
                                                }
                                                .addOnFailureListener {
                                                    isSavingChanges = false
                                                    Toast.makeText(context, "Failed: ${it.message}", Toast.LENGTH_LONG).show()
                                                }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                                        modifier = Modifier.weight(2f),
                                        shape = RoundedCornerShape(10.dp),
                                        enabled = !isSavingChanges
                                    ) {
                                        Text("SAVE REMOTELY", color = Color(0xFF021612), fontWeight = FontWeight.Bold, fontSize = 11.sp)
=======
                                    Text(
                                        text = "Showing ${viewModel.adminUserList.size} of ${if (viewModel.adminTotalUserCount > 0) viewModel.adminTotalUserCount else viewModel.adminUserList.size} users",
                                        color = Color.White.copy(alpha = 0.5f),
                                        fontSize = 11.sp
                                    )

                                    if (viewModel.isAdminUserHasMore) {
                                        Button(
                                            onClick = { viewModel.loadAdminUsers(reset = false) },
                                            enabled = !viewModel.isAdminUserLoading,
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B4E38)),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            if (viewModel.isAdminUserLoading) {
                                                CircularProgressIndicator(modifier = Modifier.size(14.dp), color = GoldPrimary, strokeWidth = 2.dp)
                                                Spacer(modifier = Modifier.width(6.dp))
                                            }
                                            Text("Load More Users (+20)", color = GoldPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
>>>>>>> 6e834ed (Update Taqwahub)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // SECTION 2.5: MANAGE SPIRITUAL TASKS & DAILY CHALLENGES
            AdminCollapsibleCard(
                title = "2.5. Manage Spiritual Tasks & Challenges",
                icon = Icons.Default.Assignment,
                isExpanded = isTasksExpanded,
                onHeaderClick = { isTasksExpanded = !isTasksExpanded }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = {
                            editingTaskId = null
                            newTaskTitle = ""
                            newTaskCategory = "Salah"
                            newTaskDescription = ""
                            newTaskPoints = "15"
                            newTaskTag = "RECOMMENDED"
                            newTaskTimerSeconds = "0"
                            newTaskActionRoute = ""
                            showTaskDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Task", tint = Color(0xFF021612))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Publish New Spiritual Challenge", color = Color(0xFF021612), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Existing Spiritual Tasks Checklist", color = GoldPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                    val tasksList by viewModel.tasks.collectAsStateWithLifecycle()
                    if (tasksList.isEmpty()) {
                        Text("No active tasks in database.", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            tasksList.forEach { t ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF021612))
                                        .border(1.dp, Color(0xFF1B4E38), RoundedCornerShape(8.dp))
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(t.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .background(GoldPrimary.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                            ) {
                                                Text(t.tag, color = GoldPrimary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                            }
                                            if (t.isAuto) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .background(Color(0xFF10B981).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                                ) {
                                                    Text("AUTO", color = Color(0xFF10B981), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                        Text(t.description, color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                                        if (t.actionRoute.isNotEmpty()) {
                                            Text("Route: ${t.actionRoute}", color = GoldPrimary.copy(alpha = 0.8f), fontSize = 10.sp, fontWeight = FontWeight.Medium)
                                        }
                                        Text("Reward: ${t.points} XP | Timer: ${t.timerSeconds}s", color = Color(0xFFA7F3D0), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        IconButton(
                                            onClick = {
                                                editingTaskId = t.id
                                                newTaskTitle = t.title
                                                newTaskCategory = t.category
                                                newTaskDescription = t.description
                                                newTaskPoints = t.points.toString()
                                                newTaskTag = t.tag
                                                newTaskTimerSeconds = t.timerSeconds.toString()
                                                newTaskActionRoute = t.actionRoute
                                                showTaskDialog = true
                                            }
                                        ) {
                                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit task", tint = GoldPrimary)
                                        }

                                        IconButton(
                                            onClick = {
                                                taskToDeleteId = t.id
                                            }
                                        ) {
                                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete task", tint = Color(0xFFEF4444))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // SECTION 3: ADD ANNOUNCEMENTS UPDATES & REMINDERS
            AdminCollapsibleCard(
                title = "3. Manage Home Announcements Feed",
                icon = Icons.Default.Campaign,
                isExpanded = isAnnouncementsExpanded,
                onHeaderClick = { isAnnouncementsExpanded = !isAnnouncementsExpanded }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Add Announcements, Updates & Reminders", color = GoldPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                    // Type Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Announcement", "Update", "Reminder").forEach { type ->
                            val isSelected = announceType == type
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) GoldPrimary else Color(0xFF021612))
                                    .border(1.dp, if (isSelected) GoldPrimary else Color(0xFF1B4E38), RoundedCornerShape(8.dp))
                                    .clickable { announceType = type }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = type,
                                    color = if (isSelected) Color(0xFF021612) else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = announceTitle,
                        onValueChange = { announceTitle = it },
                        label = { Text("Title / Summary Header", color = Color(0xFFA7F3D0).copy(alpha = 0.6f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = Color(0xFF1B4E38)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = announceMessage,
                        onValueChange = { announceMessage = it },
                        label = { Text("Detailed Message Body", color = Color(0xFFA7F3D0).copy(alpha = 0.6f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = Color(0xFF1B4E38)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            if (announceMessage.isBlank()) {
                                Toast.makeText(context, "Message content cannot be blank!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val newAnnounce = Announcement(
                                id = java.util.UUID.randomUUID().toString(),
                                title = announceTitle,
                                message = announceMessage,
                                type = announceType,
                                timestamp = System.currentTimeMillis()
                            )
                            viewModel.addAnnouncement(newAnnounce, {
                                Toast.makeText(context, "Announcement successfully published!", Toast.LENGTH_SHORT).show()
                                announceTitle = ""
                                announceMessage = ""
                            }, { err ->
                                Toast.makeText(context, "Failed to publish: $err", Toast.LENGTH_LONG).show()
                            })
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Text("Publish Announcement", color = Color(0xFF021612), fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Currently Active Feed Items (${viewModel.announcementsList.size})", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                    if (viewModel.announcementsList.isEmpty()) {
                        Text("No active announcements published", color = Color.White.copy(alpha = 0.4f), fontSize = 11.sp)
                    } else {
                        viewModel.announcementsList.forEach { announce ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF021612)),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(announce.title.ifEmpty { announce.type }, color = GoldPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Text(announce.message, color = Color.White, fontSize = 11.sp, lineHeight = 15.sp)
                                        Text(announce.type, color = Color(0xFFA7F3D0).copy(alpha = 0.6f), fontSize = 9.sp)
                                    }
                                    IconButton(
                                        onClick = {
                                            viewModel.deleteAnnouncement(announce.id, {
                                                Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
                                            }, { err ->
                                                Toast.makeText(context, "Failed: $err", Toast.LENGTH_SHORT).show()
                                            })
                                        }
                                    ) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // SECTION 4: MANAGE DUAS
            AdminCollapsibleCard(
<<<<<<< HEAD
                title = "4. Manage Dynamic Duas",
=======
                title = "4. Manage Dynamic Duas & Categories",
>>>>>>> 6e834ed (Update Taqwahub)
                icon = Icons.Default.MenuBook,
                isExpanded = isDuasExpanded,
                onHeaderClick = { isDuasExpanded = !isDuasExpanded }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
<<<<<<< HEAD
                    Text("Add Custom Supplication / Dua", color = GoldPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = duaCategory,
                        onValueChange = { duaCategory = it },
                        label = { Text("Category (e.g. Morning, Sleep)", color = Color(0xFFA7F3D0).copy(alpha = 0.6f)) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = GoldPrimary, unfocusedBorderColor = Color(0xFF1B4E38)),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = duaReference,
                        onValueChange = { duaReference = it },
                        label = { Text("Reference (e.g. Surah Al-Baqarah 2:255)", color = Color(0xFFA7F3D0).copy(alpha = 0.6f)) },
=======
                    Text(
                        text = if (editingDuaId != null) "Edit Supplication / Dua" else "Add New Supplication / Dua",
                        color = GoldPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // CATEGORY SELECTION & CREATION
                    val existingCategories = remember(viewModel.dynamicDuaList) {
                        viewModel.dynamicDuaList.map { it.category.trim() }.filter { it.isNotEmpty() }.distinct().sorted()
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Category", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        
                        if (existingCategories.isNotEmpty()) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                            ) {
                                items(existingCategories) { cat ->
                                    val isSelected = !isAddingNewCategory && duaCategory.equals(cat, ignoreCase = true)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) GoldPrimary else Color(0xFF01241A))
                                            .border(1.dp, if (isSelected) GoldPrimary else Color(0xFF1B4E38), RoundedCornerShape(8.dp))
                                            .clickable {
                                                isAddingNewCategory = false
                                                duaCategory = cat
                                            }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = cat,
                                            color = if (isSelected) Color(0xFF021612) else Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = if (isAddingNewCategory) newCategoryInput else duaCategory,
                                onValueChange = {
                                    if (isAddingNewCategory) {
                                        newCategoryInput = it
                                        duaCategory = it
                                    } else {
                                        duaCategory = it
                                    }
                                },
                                label = { Text(if (isAddingNewCategory) "New Category Name (e.g. Forgiveness)" else "Selected Category", color = Color(0xFFA7F3D0).copy(alpha = 0.6f)) },
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = GoldPrimary, unfocusedBorderColor = Color(0xFF1B4E38)),
                                modifier = Modifier.weight(1f)
                            )

                            Button(
                                onClick = {
                                    isAddingNewCategory = !isAddingNewCategory
                                    if (isAddingNewCategory) {
                                        newCategoryInput = ""
                                        duaCategory = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isAddingNewCategory) Color(0xFF10B981) else Color(0xFF1B4E38)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(if (isAddingNewCategory) "Use Existing" else "+ New Cat", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = duaReference,
                        onValueChange = { duaReference = it },
                        label = { Text("Reference (e.g. Sahih Al-Bukhari, Quran 2:255)", color = Color(0xFFA7F3D0).copy(alpha = 0.6f)) },
>>>>>>> 6e834ed (Update Taqwahub)
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = GoldPrimary, unfocusedBorderColor = Color(0xFF1B4E38)),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = duaArabic,
                        onValueChange = { duaArabic = it },
<<<<<<< HEAD
                        label = { Text("Arabic Text", color = Color(0xFFA7F3D0).copy(alpha = 0.6f)) },
=======
                        label = { Text("Arabic Text *", color = Color(0xFFA7F3D0).copy(alpha = 0.6f)) },
>>>>>>> 6e834ed (Update Taqwahub)
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = GoldPrimary, unfocusedBorderColor = Color(0xFF1B4E38)),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = duaTransliteration,
                        onValueChange = { duaTransliteration = it },
<<<<<<< HEAD
                        label = { Text("Transliteration", color = Color(0xFFA7F3D0).copy(alpha = 0.6f)) },
=======
                        label = { Text("Transliteration (Optional)", color = Color(0xFFA7F3D0).copy(alpha = 0.6f)) },
>>>>>>> 6e834ed (Update Taqwahub)
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = GoldPrimary, unfocusedBorderColor = Color(0xFF1B4E38)),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = duaTranslation,
                        onValueChange = { duaTranslation = it },
<<<<<<< HEAD
                        label = { Text("English Translation", color = Color(0xFFA7F3D0).copy(alpha = 0.6f)) },
=======
                        label = { Text("English Translation *", color = Color(0xFFA7F3D0).copy(alpha = 0.6f)) },
>>>>>>> 6e834ed (Update Taqwahub)
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = GoldPrimary, unfocusedBorderColor = Color(0xFF1B4E38)),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = duaTranslationUrdu,
                        onValueChange = { duaTranslationUrdu = it },
<<<<<<< HEAD
                        label = { Text("Urdu Translation", color = Color(0xFFA7F3D0).copy(alpha = 0.6f)) },
=======
                        label = { Text("Urdu Translation (Optional)", color = Color(0xFFA7F3D0).copy(alpha = 0.6f)) },
>>>>>>> 6e834ed (Update Taqwahub)
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = GoldPrimary, unfocusedBorderColor = Color(0xFF1B4E38)),
                        modifier = Modifier.fillMaxWidth()
                    )

<<<<<<< HEAD
                    Button(
                        onClick = {
                            if (duaArabic.isBlank() || duaTranslation.isBlank()) {
                                Toast.makeText(context, "Arabic and English Translation are required!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val newDua = Dua(
                                id = "custom_" + java.util.UUID.randomUUID().toString().take(8),
                                category = duaCategory.ifEmpty { "General" },
                                reference = duaReference,
                                arabic = duaArabic,
                                transliteration = duaTransliteration,
                                translation = duaTranslation,
                                translationUrdu = duaTranslationUrdu
                            )
                            viewModel.addCustomDua(newDua, {
                                Toast.makeText(context, "Dua added and synced!", Toast.LENGTH_SHORT).show()
                                duaCategory = ""
                                duaReference = ""
                                duaArabic = ""
                                duaTransliteration = ""
                                duaTranslation = ""
                                duaTranslationUrdu = ""
                            }, { err ->
                                Toast.makeText(context, "Failed: $err", Toast.LENGTH_LONG).show()
                            })
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Text("Add Supplication", color = Color(0xFF021612), fontWeight = FontWeight.Bold)
=======
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (editingDuaId != null) {
                            OutlinedButton(
                                onClick = {
                                    editingDuaId = null
                                    duaCategory = ""
                                    newCategoryInput = ""
                                    isAddingNewCategory = false
                                    duaReference = ""
                                    duaArabic = ""
                                    duaTransliteration = ""
                                    duaTranslation = ""
                                    duaTranslationUrdu = ""
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).height(44.dp)
                            ) {
                                Text("Cancel", color = Color.White)
                            }
                        }

                        Button(
                            onClick = {
                                if (duaArabic.isBlank() || duaTranslation.isBlank()) {
                                    Toast.makeText(context, "Arabic and English Translation are required!", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                val finalCategory = duaCategory.trim().ifEmpty { "General" }
                                if (editingDuaId != null) {
                                    val updatedDua = Dua(
                                        id = editingDuaId!!,
                                        category = finalCategory,
                                        reference = duaReference.trim(),
                                        arabic = duaArabic.trim(),
                                        transliteration = duaTransliteration.trim(),
                                        translation = duaTranslation.trim(),
                                        translationUrdu = duaTranslationUrdu.trim()
                                    )
                                    viewModel.updateDua(updatedDua, {
                                        Toast.makeText(context, "Supplication updated and synced!", Toast.LENGTH_SHORT).show()
                                        editingDuaId = null
                                        duaCategory = ""
                                        newCategoryInput = ""
                                        isAddingNewCategory = false
                                        duaReference = ""
                                        duaArabic = ""
                                        duaTransliteration = ""
                                        duaTranslation = ""
                                        duaTranslationUrdu = ""
                                    }, { err ->
                                        Toast.makeText(context, "Failed to update: $err", Toast.LENGTH_LONG).show()
                                    })
                                } else {
                                    val newDua = Dua(
                                        id = "dua_" + java.util.UUID.randomUUID().toString().take(8),
                                        category = finalCategory,
                                        reference = duaReference.trim(),
                                        arabic = duaArabic.trim(),
                                        transliteration = duaTransliteration.trim(),
                                        translation = duaTranslation.trim(),
                                        translationUrdu = duaTranslationUrdu.trim()
                                    )
                                    viewModel.addCustomDua(newDua, {
                                        Toast.makeText(context, "Supplication published and synced!", Toast.LENGTH_SHORT).show()
                                        duaCategory = ""
                                        newCategoryInput = ""
                                        isAddingNewCategory = false
                                        duaReference = ""
                                        duaArabic = ""
                                        duaTransliteration = ""
                                        duaTranslation = ""
                                        duaTranslationUrdu = ""
                                    }, { err ->
                                        Toast.makeText(context, "Failed to add: $err", Toast.LENGTH_LONG).show()
                                    })
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(if (editingDuaId != null) 2f else 1f).height(44.dp)
                        ) {
                            Text(
                                text = if (editingDuaId != null) "Update Supplication" else "Publish Supplication",
                                color = Color(0xFF021612),
                                fontWeight = FontWeight.Bold
                            )
                        }
>>>>>>> 6e834ed (Update Taqwahub)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    
<<<<<<< HEAD
                    val customDuasList = viewModel.dynamicDuaList.filter { 
                        it.id !in IslamicData.duas.map { d -> d.id } 
                    }
                    Text("Custom Added Supplications (${customDuasList.size})", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                    if (customDuasList.isEmpty()) {
                        Text("No custom Supplications added yet", color = Color.White.copy(alpha = 0.4f), fontSize = 11.sp)
                    } else {
                        customDuasList.forEach { dua ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF021612)),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
=======
                    val allDuasList = viewModel.dynamicDuaList
                    Text("Published Supplications (${allDuasList.size})", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)

                    if (allDuasList.isEmpty()) {
                        Text("No Supplications in database. Add supplications above to publish them to users.", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                    } else {
                        allDuasList.forEach { dua ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF021612)),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                border = BorderStroke(1.dp, if (editingDuaId == dua.id) GoldPrimary else Color(0xFF1B4E38).copy(alpha = 0.5f))
>>>>>>> 6e834ed (Update Taqwahub)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
<<<<<<< HEAD
                                        Text(dua.category, color = GoldPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text(dua.arabic, color = Color.White, fontSize = 13.sp, maxLines = 1)
                                        Text(dua.translation, color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, maxLines = 1)
                                    }
                                    IconButton(
                                        onClick = {
                                            viewModel.deleteCustomDua(dua.id, {
                                                Toast.makeText(context, "Supplication removed!", Toast.LENGTH_SHORT).show()
                                            }, { err ->
                                                Toast.makeText(context, "Failed: $err", Toast.LENGTH_SHORT).show()
                                            })
                                        }
                                    ) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
=======
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .background(GoldPrimary.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(dua.category, color = GoldPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                            if (dua.reference.isNotEmpty()) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(dua.reference, color = Color.White.copy(alpha = 0.5f), fontSize = 9.sp)
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(dua.arabic, color = Color.White, fontSize = 13.sp, maxLines = 1)
                                        Text(dua.translation, color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, maxLines = 1)
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                        IconButton(
                                            onClick = {
                                                editingDuaId = dua.id
                                                duaCategory = dua.category
                                                duaReference = dua.reference
                                                duaArabic = dua.arabic
                                                duaTransliteration = dua.transliteration
                                                duaTranslation = dua.translation
                                                duaTranslationUrdu = dua.translationUrdu
                                                isAddingNewCategory = false
                                                Toast.makeText(context, "Editing supplication", Toast.LENGTH_SHORT).show()
                                            }
                                        ) {
                                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = GoldPrimary)
                                        }

                                        IconButton(
                                            onClick = {
                                                duaToDeleteId = dua.id
                                            }
                                        ) {
                                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
                                        }
>>>>>>> 6e834ed (Update Taqwahub)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // SECTION 5: MANAGE HADITHS
            AdminCollapsibleCard(
                title = "5. Manage Dynamic Hadiths",
                icon = Icons.Default.AutoStories,
                isExpanded = isHadithsExpanded,
                onHeaderClick = { isHadithsExpanded = !isHadithsExpanded }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Add Custom Hadith", color = GoldPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = hadithChapter,
                        onValueChange = { hadithChapter = it },
                        label = { Text("Chapter (e.g. Belief, Character)", color = Color(0xFFA7F3D0).copy(alpha = 0.6f)) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = GoldPrimary, unfocusedBorderColor = Color(0xFF1B4E38)),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = hadithNarrator,
                        onValueChange = { hadithNarrator = it },
                        label = { Text("Narrator (e.g. Abu Hurairah)", color = Color(0xFFA7F3D0).copy(alpha = 0.6f)) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = GoldPrimary, unfocusedBorderColor = Color(0xFF1B4E38)),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = hadithSource,
                        onValueChange = { hadithSource = it },
                        label = { Text("Source Bukhari / Muslim / Tirmidhi", color = Color(0xFFA7F3D0).copy(alpha = 0.6f)) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = GoldPrimary, unfocusedBorderColor = Color(0xFF1B4E38)),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = hadithText,
                        onValueChange = { hadithText = it },
                        label = { Text("Hadith Text (English Detail)", color = Color(0xFFA7F3D0).copy(alpha = 0.6f)) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = GoldPrimary, unfocusedBorderColor = Color(0xFF1B4E38)),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = hadithArabic,
                        onValueChange = { hadithArabic = it },
                        label = { Text("Hadith Arabic Text", color = Color(0xFFA7F3D0).copy(alpha = 0.6f)) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = GoldPrimary, unfocusedBorderColor = Color(0xFF1B4E38)),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = hadithTranslationUrdu,
                        onValueChange = { hadithTranslationUrdu = it },
                        label = { Text("Hadith Urdu Translation", color = Color(0xFFA7F3D0).copy(alpha = 0.6f)) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = GoldPrimary, unfocusedBorderColor = Color(0xFF1B4E38)),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = hadithTransliteration,
                        onValueChange = { hadithTransliteration = it },
                        label = { Text("Hadith Transliteration", color = Color(0xFFA7F3D0).copy(alpha = 0.6f)) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = GoldPrimary, unfocusedBorderColor = Color(0xFF1B4E38)),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            if (hadithText.isBlank() || hadithArabic.isBlank()) {
                                Toast.makeText(context, "Hadith text and Arabic text are required!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
<<<<<<< HEAD
                            // Generate unique numerical identifier for custom hadiths
                            val numericId = 10000 + (viewModel.dynamicHadithList.filter { it.id >= 10000 }.maxOfOrNull { it.id } ?: 0) + 1
=======
                            // Generate unique collision-free numerical identifier for custom hadiths
                            val baseOffset = 100000 + (System.currentTimeMillis() % 800000).toInt()
                            val randomPadding = (1..99).random()
                            val numericId = baseOffset + randomPadding
>>>>>>> 6e834ed (Update Taqwahub)
                            val newHadith = Hadith(
                                id = numericId,
                                chapter = hadithChapter.ifEmpty { "General" },
                                narrator = hadithNarrator,
                                source = hadithSource,
                                text = hadithText,
                                arabic = hadithArabic,
                                translationUrdu = hadithTranslationUrdu,
                                transliteration = hadithTransliteration
                            )
                            viewModel.addCustomHadith(newHadith, {
                                Toast.makeText(context, "Hadith published and synced successfully!", Toast.LENGTH_SHORT).show()
                                hadithChapter = ""
                                hadithNarrator = ""
                                hadithSource = ""
                                hadithText = ""
                                hadithArabic = ""
                                hadithTranslationUrdu = ""
                                hadithTransliteration = ""
                            }, { err ->
                                Toast.makeText(context, "Publish failed: $err", Toast.LENGTH_LONG).show()
                            })
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Text("Add Custom Hadith", color = Color(0xFF021612), fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val customHadithsList = viewModel.dynamicHadithList.filter { it.id >= 10000 }
                    Text("Custom Added Hadiths (${customHadithsList.size})", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                    if (customHadithsList.isEmpty()) {
                        Text("No custom Hadith added yet", color = Color.White.copy(alpha = 0.4f), fontSize = 11.sp)
                    } else {
                        customHadithsList.forEach { hadith ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF021612)),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(hadith.chapter, color = GoldPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text(hadith.text, color = Color.White, fontSize = 13.sp, maxLines = 1)
                                        Text("By: ${hadith.narrator.ifEmpty { "Unknown" }} (Source: ${hadith.source})", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                                    }
                                    IconButton(
                                        onClick = {
                                            viewModel.deleteCustomHadith(hadith.id, {
                                                Toast.makeText(context, "Hadith successfully deleted!", Toast.LENGTH_SHORT).show()
                                            }, { err ->
                                                Toast.makeText(context, "Failed: $err", Toast.LENGTH_SHORT).show()
                                            })
                                        }
                                    ) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // SECTION 6: AUDIO OVERRIDES
            AdminCollapsibleCard(
                title = "6. Offline Audio Sync Explorer",
                icon = Icons.Default.Audiotrack,
                isExpanded = isAudioOverridesExpanded,
                onHeaderClick = { isAudioOverridesExpanded = !isAudioOverridesExpanded }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Audio Sync Explorer", color = GoldPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Load any Surah and Verse to listen to current audios and override them securely. Changes sync instantly across all devices.", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = adminSurahId,
                            onValueChange = { adminSurahId = it },
                            label = { Text("Surah (1-114)", color = Color(0xFFA7F3D0).copy(alpha = 0.6f)) },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = GoldPrimary, unfocusedBorderColor = Color(0xFF1B4E38)),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = adminVerseNum,
                            onValueChange = { adminVerseNum = it },
                            label = { Text("Verse (e.g. 1)", color = Color(0xFFA7F3D0).copy(alpha = 0.6f)) },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = GoldPrimary, unfocusedBorderColor = Color(0xFF1B4E38)),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = adminWordSearch,
                        onValueChange = { adminWordSearch = it },
                        label = { Text("Quick Find Word by Text or ID (optional)", color = Color(0xFFA7F3D0).copy(alpha = 0.6f)) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = GoldPrimary, unfocusedBorderColor = Color(0xFF1B4E38)),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            val sId = adminSurahId.toIntOrNull()
                            if (sId != null) {
                                viewModel.selectChapter(sId)
                                Toast.makeText(context, "Loading Surah $sId...", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B4E38)),
                        modifier = Modifier.fillMaxWidth().height(40.dp)
                    ) {
                        Text("Load Surah into Explorer", color = Color.White)
                    }

                    if (clipboardUrl.isNotBlank()) {
                        Text("Clipboard: ${clipboardUrl.takeLast(40)}", color=GoldPrimary, fontSize=11.sp, maxLines=1)
                    }

                    if (viewModel.isVersesLoading) {
                        VerseSkeletonCard()
                    } else if (viewModel.selectedSurah?.id?.toString() == adminSurahId && viewModel.activeVerses.isNotEmpty()) {
                        
                        val matchedWords = mutableListOf<Pair<com.example.data.api.QuranWord, String>>()
                        
                        if (adminWordSearch.isNotBlank()) {
                            viewModel.activeVerses.forEach { v ->
                                v.words?.forEach { w ->
                                    if (w.char_type_name == "word" && (w.id.toString() == adminWordSearch || (w.text_uthmani ?: "").contains(adminWordSearch))) {
                                        matchedWords.add(w to v.verse_key)
                                    }
                                }
                            }
                        } else if (adminVerseNum.isNotBlank()) {
                            val targetVerseKey = "$adminSurahId:$adminVerseNum"
                            val v = viewModel.activeVerses.find { it.verse_key == targetVerseKey }
                            if (v != null) {
                                v.words?.filter { it.char_type_name == "word" }?.forEach { w ->
                                    matchedWords.add(w to v.verse_key)
                                }
                            }
                        }

                        // Just Verse part
                        if (adminWordSearch.isBlank() && adminVerseNum.isNotBlank()) {
                            val targetVerseKey = "$adminSurahId:$adminVerseNum"
                            val verseObj = viewModel.activeVerses.find { it.verse_key == targetVerseKey }
                            
                            if (verseObj != null) {
                                val verseAudio = viewModel.audioOverrides["verse_${verseObj.verse_key}"] ?: viewModel.activeVerseAudioUrls[verseObj.verse_key]
                                
                                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF021612)), modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("Verse: ${verseObj.verse_key}", color = GoldPrimary, fontWeight = FontWeight.Bold)
<<<<<<< HEAD
                                        Text(if (viewModel.quranScript == "indopak") (verseObj.text_indopak ?: verseObj.text_uthmani ?: "") else (verseObj.text_uthmani ?: ""), color = Color.White, fontSize = 24.sp, fontFamily = FontHelper.getFontForScript(viewModel.quranScript), textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth())
=======
                                        Text(FontHelper.formatArabicText(if (viewModel.quranScript == "indopak") (verseObj.text_indopak ?: verseObj.text_uthmani ?: "") else (verseObj.text_uthmani ?: "")), color = Color.White, fontSize = 24.sp, fontFamily = FontHelper.getFontForScript(viewModel.quranScript), textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth())
>>>>>>> 6e834ed (Update Taqwahub)
                                        
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(onClick = { if (verseAudio != null) viewModel.audioPlayerHelper.playAudio(verseAudio) }) {
                                                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play Verse", tint = GoldPrimary)
                                            }
                                            Text("Current: ${verseAudio ?: "None"}", color = Color.White.copy(alpha=0.5f), fontSize=10.sp, modifier = Modifier.weight(1f))
                                        }
                                        
                                        var editVerseUrl by remember { mutableStateOf(verseAudio ?: "") }
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                            OutlinedTextField(
                                                value = editVerseUrl,
                                                onValueChange = { editVerseUrl = it },
                                                modifier = Modifier.weight(1f).height(50.dp),
                                                textStyle = androidx.compose.ui.text.TextStyle(fontSize=12.sp, color=Color.White)
                                            )
                                            Button(onClick = { viewModel.saveAudioOverride("verse_${verseObj.verse_key}", editVerseUrl, { }, {}) }) {
                                                Text("Save", fontSize=12.sp)
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier=Modifier.height(8.dp))
                            } else {
                                Text("Verse $targetVerseKey not found.", color=Color.Red)
                            }
                        }

                        if (matchedWords.isNotEmpty()) {
                            Text("Words (${matchedWords.size}) - You can search by text or ID above", color = Color.White, fontSize = 12.sp)
                            
                            LazyRow(Modifier.fillMaxWidth()) {
                                items(matchedWords) { (word, verseKey) ->
                                    val wordAudio = viewModel.audioOverrides["word_${word.id}"] ?: word.audio_url
                                    var editWordUrl by remember { mutableStateOf(wordAudio ?: "") }

                                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1B4E38)), modifier = Modifier.padding(end=8.dp).width(240.dp)) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text("ID: word_${word.id} (v$verseKey)", color = Color.White.copy(alpha=0.5f), fontSize=11.sp)
<<<<<<< HEAD
                                            Text(if (viewModel.quranScript == "indopak") (word.text_indopak ?: word.text_uthmani ?: "") else (word.text_uthmani ?: ""), color = GoldPrimary, fontSize=24.sp, fontFamily = FontHelper.getFontForScript(viewModel.quranScript), textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth().padding(vertical=8.dp))
=======
                                            Text(FontHelper.formatArabicText(if (viewModel.quranScript == "indopak") (word.text_indopak ?: word.text_uthmani ?: "") else (word.text_uthmani ?: "")), color = GoldPrimary, fontSize=24.sp, fontFamily = FontHelper.getFontForScript(viewModel.quranScript), textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth().padding(vertical=8.dp))
>>>>>>> 6e834ed (Update Taqwahub)
                                            
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                                IconButton(onClick = { if (wordAudio != null) viewModel.audioPlayerHelper.playAudio(wordAudio) }, modifier = Modifier.size(36.dp)) {
                                                    Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White)
                                                }
                                                IconButton(onClick = { clipboardUrl = wordAudio ?: ""; Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show() }, modifier = Modifier.size(36.dp)) {
                                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = GoldPrimary)
                                                }
                                                IconButton(onClick = { 
                                                    if (clipboardUrl.isNotBlank()) {
                                                        editWordUrl = clipboardUrl
                                                        Toast.makeText(context, "Pasted!", Toast.LENGTH_SHORT).show()
                                                    }
                                                }, modifier = Modifier.size(36.dp)) {
                                                    Icon(Icons.Default.ContentPaste, contentDescription = "Paste", tint = GoldPrimary)
                                                }
                                            }
                                            Text(wordAudio?.takeLast(30) ?: "None", color=Color.White.copy(alpha=0.5f), fontSize=9.sp, maxLines=1)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            OutlinedTextField(
                                                value = editWordUrl,
                                                onValueChange = { editWordUrl = it },
                                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                                textStyle = androidx.compose.ui.text.TextStyle(fontSize=10.sp, color = Color.White)
                                            )
                                            Button(
                                                onClick = { viewModel.saveAudioOverride("word_${word.id}", editWordUrl, {Toast.makeText(context, "Saved word", Toast.LENGTH_SHORT).show()}, {}) },
                                                modifier = Modifier.fillMaxWidth().height(36.dp).padding(top=8.dp),
                                                contentPadding = PaddingValues(0.dp)
                                            ) {
                                                Text("Save Audio URL", fontSize=11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        } else if (adminWordSearch.isNotBlank()) {
                            Text("No words match '$adminWordSearch'", color=Color.Red)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val overridesList = viewModel.audioOverrides.toList()
                    Text("Active Firebase Audio Overrides (${overridesList.size})", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                    if (overridesList.isEmpty()) {
                        Text("No custom audios overridden.", color = Color.White.copy(alpha = 0.4f), fontSize = 11.sp)
                    } else {
                        overridesList.take(20).forEach { (id, url) ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF021612)),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(id, color = GoldPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text(url, color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp, maxLines = 1)
                                    }
                                    IconButton(
                                        onClick = {
                                            viewModel.saveAudioOverride(id, "", {
                                                Toast.makeText(context, "Audio Override successfully removed!", Toast.LENGTH_SHORT).show()
                                            }, { err ->
                                                Toast.makeText(context, "Failed: $err", Toast.LENGTH_SHORT).show()
                                            })
                                        }
                                    ) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
                                    }
                                }
                            }
                        }
                        if (overridesList.size > 20) {
                             Text("...and ${overridesList.size - 20} more", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                        }
                    }
                }
            }

            // SECTION 7: USER COMPLAINTS & SUGGESTIONS
            AdminCollapsibleCard(
                title = "7. User Complaints & Suggestions",
                icon = Icons.Default.Feedback,
                isExpanded = isBugReportsExpanded,
                onHeaderClick = { isBugReportsExpanded = !isBugReportsExpanded }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("User Feedback & Bug Reports", color = GoldPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Manage, assign statuses, and securely read complaints and feature suggestions submitted by application users.", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)

                    // Refresh Button & Stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val pendingCount = viewModel.bugReportsList.count { it.status == "Pending" }
                        val inProgressCount = viewModel.bugReportsList.count { it.status == "In Progress" }
                        val resolvedCount = viewModel.bugReportsList.count { it.status == "Resolved" }
                        
                        Text(
                            text = "Pending: $pendingCount | Active: $inProgressCount | Done: $resolvedCount",
                            color = Color(0xFFA7F3D0).copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Button(
                            onClick = { 
                                viewModel.fetchBugReportsFromFirestore()
                                Toast.makeText(context, "Refreshed complaints", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B4E38)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("Refresh", fontSize = 10.sp, color = Color.White)
                        }
                    }

                    // Filter row
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Filter by Type:", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf("All", "Bug", "Suggestion", "Other").forEach { type ->
                                val isSel = filterType == type
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSel) GoldPrimary else Color(0xFF021612))
                                        .clickable { filterType = type }
                                        .padding(vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = type, color = if (isSel) Color(0xFF021612) else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        
                        Text("Filter by Status:", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf("All", "Pending", "In Progress", "Resolved").forEach { status ->
                                val isSel = filterStatus == status
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSel) GoldPrimary else Color(0xFF021612))
                                        .clickable { filterStatus = status }
                                        .padding(vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = status, color = if (isSel) Color(0xFF021612) else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    val filteredList = viewModel.bugReportsList.filter {
                        (filterType == "All" || it.type == filterType) &&
                        (filterStatus == "All" || it.status == filterStatus)
                    }

                    if (filteredList.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No complaints or suggestions matching filters.", color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp)
                        }
                    } else {
                        filteredList.forEach { report ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF021612)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color(0xFF1B4E38).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                    .padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    // Row 1: Type badge and Status badge
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val typeColor = when (report.type) {
                                            "Bug" -> Color(0xFFEF4444)
                                            "Suggestion" -> Color(0xFF3B82F6)
                                            else -> Color(0xFF10B981)
                                        }
                                        
                                        val statusColor = when (report.status) {
                                            "Pending" -> Color(0xFFF59E0B)
                                            "In Progress" -> Color(0xFF3B82F6)
                                            else -> Color(0xFF10B981)
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .background(typeColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(report.type.uppercase(), color = typeColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(report.status.uppercase(), color = statusColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        // Timestamp
                                        val dateStr = try {
                                            java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(report.timestamp))
                                        } catch (ex: Exception) {
                                            "Unknown Date"
                                        }
                                        Text(dateStr, color = Color.White.copy(alpha = 0.4f), fontSize = 9.sp)
                                    }

                                    // Content detail
                                    Text(report.subject, color = GoldPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text(report.description, color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp, lineHeight = 16.sp)

                                    if (report.imageUrl.isNotEmpty()) {
                                        val bitmap = remember(report.imageUrl) { base64ToBitmap(report.imageUrl) }
                                        if (bitmap != null) {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Image(
                                                bitmap = bitmap.asImageBitmap(),
                                                contentDescription = "Report Screenshot (Click to zoom)",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(180.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                                    .clickable { zoomedImageBitmap = bitmap },
                                                contentScale = ContentScale.Inside
                                            )
                                        }
                                    }

                                    HorizontalDivider(color = Color(0xFF1B4E38).copy(alpha = 0.2f), thickness = 1.dp)

                                    // User info and Device info
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text("User: ${report.userEmail}", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                                        Text("User UID: ${report.userId}", color = Color.White.copy(alpha = 0.4f), fontSize = 9.sp)
                                        Text("Device: ${report.deviceModel} (v${report.appVersion})", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                                    }

                                    HorizontalDivider(color = Color(0xFF1B4E38).copy(alpha = 0.2f), thickness = 1.dp)

                                    // Reply composer Section
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        if (report.adminReply.isNotEmpty()) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(GoldPrimary.copy(alpha = 0.08f))
                                                    .border(1.dp, GoldPrimary.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                                    .padding(6.dp)
                                            ) {
                                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                    Text("CURRENT ACTIVE REPLY:", color = GoldPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                    Text(report.adminReply, color = Color.White, fontSize = 11.sp, lineHeight = 14.sp)
                                                }
                                            }
                                        }

                                        var replyText by remember(report.id) { mutableStateOf("") }
                                        var isPostingReply by remember { mutableStateOf(false) }

                                        OutlinedTextField(
                                            value = replyText,
                                            onValueChange = { replyText = it },
                                            placeholder = { Text("Write staff response...", color = Color.White.copy(alpha = 0.4f), fontSize = 11.sp) },
                                            modifier = Modifier.fillMaxWidth(),
                                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 11.sp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = GoldPrimary,
                                                unfocusedBorderColor = Color.White.copy(alpha = 0.15f)
                                            ),
                                            maxLines = 3,
                                            singleLine = false
                                        )

                                        Button(
                                            onClick = {
                                                if (replyText.trim().isEmpty()) {
                                                    Toast.makeText(context, "Please enter reply message first", Toast.LENGTH_SHORT).show()
                                                    return@Button
                                                }
                                                isPostingReply = true
                                                viewModel.replyToBugReport(report.id, replyText.trim()) { success, err ->
                                                    isPostingReply = false
                                                    if (success) {
                                                        replyText = ""
                                                        Toast.makeText(context, "Reply posted & status updated to In Progress!", Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        Toast.makeText(context, "Failed to reply: $err", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                                            modifier = Modifier.fillMaxWidth().height(26.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            enabled = !isPostingReply
                                        ) {
                                            if (isPostingReply) {
                                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color(0xFF021612))
                                            } else {
                                                Text(if (report.adminReply.isNotEmpty()) "UPDATE STAFF REPLY" else "POST DIRECT STAFF REPLY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF021612))
                                            }
                                        }
                                    }

                                    HorizontalDivider(color = Color(0xFF1B4E38).copy(alpha = 0.2f), thickness = 1.dp)

                                    // Status and Delete CTA
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            listOf("Pending", "In Progress", "Resolved").forEach { option ->
                                                if (report.status != option) {
                                                    Button(
                                                        onClick = {
                                                            viewModel.updateBugReportStatus(report.id, option)
                                                            Toast.makeText(context, "Status set to $option", Toast.LENGTH_SHORT).show()
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B4E38)),
                                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                        modifier = Modifier.height(26.dp)
                                                    ) {
                                                        Text(option, fontSize = 9.sp, color = Color.White)
                                                    }
                                                }
                                            }
                                        }

                                        IconButton(
                                            onClick = {
                                                viewModel.deleteBugReport(report.id)
                                                Toast.makeText(context, "Report deleted", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete report", tint = Color(0xFFEF4444))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // SECTION 8: AI ASSISTANT FEEDBACK & RATIO ANALYTICS
            AdminCollapsibleCard(
                title = "8. AI Assistant Feedback & Ratios",
                icon = Icons.Default.SmartToy,
                isExpanded = isAiFeedbackExpanded,
                onHeaderClick = { isAiFeedbackExpanded = !isAiFeedbackExpanded }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("AI Assistant Response Suggestions & Logs", color = GoldPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Securely analyze positive vs negative ratios, check helpful suggestions, and read user reports for the AI chat assistant.", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)

                    // Refresh Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Evaluated Responses: ${viewModel.aiFeedbackList.size}",
                            color = Color(0xFFA7F3D0).copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Button(
                            onClick = { 
                                viewModel.fetchAiFeedbacksFromFirestore()
                                Toast.makeText(context, "Refreshed AI feedback reports", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B4E38)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("Refresh", fontSize = 10.sp, color = Color.White)
                        }
                    }

                    // Tabs Selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Stats", "Submissions").forEach { tab ->
                            val isSel = activeFeedbacksTab == tab
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) GoldPrimary else Color(0xFF021612))
                                    .clickable { activeFeedbacksTab = tab }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (tab == "Stats") "Ratio Graphs (Stats)" else "User Log",
                                    color = if (isSel) Color(0xFF021612) else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    if (activeFeedbacksTab == "Stats") {
                        // Ratio graphs calculation
                        val likes = viewModel.aiFeedbackList.count { it.rating == "like" }
                        val dislikes = viewModel.aiFeedbackList.count { it.rating == "dislike" }
                        val totalRated = likes + dislikes
                        val likePct = if (totalRated > 0) (likes * 100f / totalRated) else 0f
                        val dislikePct = if (totalRated > 0) (dislikes * 100f / totalRated) else 0f

                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF021612))
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Like / Dislike Ratio Analysis",
                                color = GoldPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.ThumbUp, contentDescription = "Likes", tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Likes Ratio: $likes (${"%.1f".format(likePct)}%)", color = Color.White, fontSize = 12.sp)
                                }
                                Text("Likes", color = Color(0xFF10B981), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            // Likes Progress Bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.08f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(if (totalRated > 0) likes.toFloat() / totalRated else 0f)
                                        .clip(CircleShape)
                                        .background(Color(0xFF10B981))
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.ThumbDown, contentDescription = "Dislikes", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Dislikes Ratio: $dislikes (${"%.1f".format(dislikePct)}%)", color = Color.White, fontSize = 12.sp)
                                }
                                Text("Dislikes", color = Color(0xFFEF4444), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            // Dislikes Progress Bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.08f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(if (totalRated > 0) dislikes.toFloat() / totalRated else 0f)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEF4444))
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(Color.White.copy(alpha = 0.08f))
                            )

                            Text(
                                text = "Summary: $totalRated Rated Responses. " +
                                       if (likePct >= 80f && totalRated > 0) "The AI feedback ratio is exceptionally high! Users appreciate the responses."
                                       else if (likePct >= 50f && totalRated > 0) "Satisfactory response ratios. Some improvements could be made."
                                       else if (totalRated == 0) "No interactions have been rated yet."
                                       else "Attention required: AI responses are receiving high dislike ratios.",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    } else {
                        // "Submissions" listing log
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Sub-Filters for rating classes
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf("All", "Likes Only", "Dislikes Only", "Reported Corrections").forEach { filter ->
                                    val isSel = feedbackFilterTag == filter
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSel) GoldPrimary else Color(0xFF021612))
                                            .clickable { feedbackFilterTag = filter }
                                            .padding(vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = filter, color = if (isSel) Color(0xFF021612) else Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            val filteredFeedbacks = viewModel.aiFeedbackList.filter {
                                when (feedbackFilterTag) {
                                    "Likes Only" -> it.rating == "like"
                                    "Dislikes Only" -> it.rating == "dislike"
                                    "Reported Corrections" -> it.reportMessage.isNotEmpty()
                                    else -> true
                                }
                            }

                            if (filteredFeedbacks.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No AI feedback logs matching criteria.", color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp)
                                }
                            } else {
                                filteredFeedbacks.forEach { feedback ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF021612)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, Color(0xFF1B4E38).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                                    val ratingColor = when (feedback.rating) {
                                                        "like" -> Color(0xFF10B981)
                                                        "dislike" -> Color(0xFFEF4444)
                                                        else -> Color.White.copy(alpha = 0.4f)
                                                    }
                                                    Box(
                                                        modifier = Modifier
                                                            .background(ratingColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(feedback.rating.uppercase(), color = ratingColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                    }

                                                    if (feedback.reportMessage.isNotEmpty()) {
                                                        Box(
                                                            modifier = Modifier
                                                                .background(GoldPrimary.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                                        ) {
                                                            Text("SUGGESTION/REPORT", color = GoldPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                }

                                                val dateStr = try {
                                                    java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(feedback.timestamp))
                                                } catch (ex: Exception) {
                                                    "Unknown"
                                                }
                                                Text(dateStr, color = Color.White.copy(alpha = 0.4f), fontSize = 9.sp)
                                            }

                                            // User email context
                                            Text("User: ${feedback.userEmail}", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)

                                            // Query
                                            Text(text = "User Question:", color = GoldPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                            Text(text = feedback.query, color = Color.White, fontSize = 12.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)

                                            // Response
                                            Text(text = "AI Response:", color = GoldPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                            Text(text = feedback.response, color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp, lineHeight = 15.sp, maxLines = 4)

                                            if (feedback.reportMessage.isNotEmpty()) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(GoldPrimary.copy(alpha = 0.08f))
                                                        .border(1.dp, GoldPrimary.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                                        .padding(6.dp)
                                                ) {
                                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                        Text("SUGGESTED CORRECTION / INACCURACY:", color = GoldPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                        Text(feedback.reportMessage, color = Color.White, fontSize = 11.sp, lineHeight = 14.sp)
                                                    }
                                                }
                                            }

                                            HorizontalDivider(color = Color(0xFF1B4E38).copy(alpha = 0.2f), thickness = 1.dp)

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.End,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Button(
                                                    onClick = {
                                                        viewModel.deleteAiFeedback(
                                                            feedbackId = feedback.id,
                                                            onSuccess = { Toast.makeText(context, "Feedback entry resolved!", Toast.LENGTH_SHORT).show() },
                                                            onFailure = { err -> Toast.makeText(context, "Error: $err", Toast.LENGTH_SHORT).show() }
                                                        )
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                    modifier = Modifier.height(24.dp)
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                                                        Text("Dismiss & Resolve", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
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
            }

    zoomedImageBitmap?.let { bitmap ->
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { zoomedImageBitmap = null },
            properties = androidx.compose.ui.window.DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.95f))
            ) {
                var scale by remember { mutableStateOf(1f) }
                var offset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }

                // Zoom/Pan Box
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(1f, 5f)
                                offset = if (scale == 1f) {
                                    androidx.compose.ui.geometry.Offset.Zero
                                } else {
                                    offset + pan
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Zoomed Screenshot",
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = offset.x,
                                translationY = offset.y
                            ),
                        contentScale = ContentScale.Fit
                    )
                }

                // Top Panel row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(16.dp)
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { zoomedImageBitmap = null },
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }

                    Text("Full Screen Screenshot", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)

                    IconButton(
                        onClick = {
                            scale = 1f
                            offset = androidx.compose.ui.geometry.Offset.Zero
                        },
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset Zoom", tint = Color.White)
                    }
                }

                // Bottom Zoom action HUD
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(24.dp)
                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            scale = (scale - 0.5f).coerceIn(1f, 5f)
                            if (scale == 1f) {
                                offset = androidx.compose.ui.geometry.Offset.Zero
                            }
                        }
                    ) {
                        Icon(imageVector = Icons.Default.ZoomOut, contentDescription = "Zoom Out", tint = GoldPrimary)
                    }

                    Text(
                        text = "${(scale * 100).toInt()}%",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(60.dp)
                    )

                    IconButton(
                        onClick = {
                            scale = (scale + 0.5f).coerceIn(1f, 5f)
                        }
                    ) {
                        Icon(imageVector = Icons.Default.ZoomIn, contentDescription = "Zoom In", tint = GoldPrimary)
                    }
                }
            }
        }
    }
}
}
}

@Composable
fun AdminCollapsibleCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isExpanded: Boolean,
    onHeaderClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = EmeraldCard),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GoldPrimary.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onHeaderClick() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(imageVector = icon, contentDescription = title, tint = GoldPrimary, modifier = Modifier.size(24.dp))
                    Text(text = title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Black)
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Toggle",
                    tint = GoldPrimary
                )
            }

            if (isExpanded) {
                Divider(color = GoldPrimary.copy(alpha = 0.08f), thickness = 1.dp)
                Box(modifier = Modifier.padding(16.dp)) {
                    content()
                }
            }
        }
    }
}

@Composable
fun AnnouncementBanner(message: String) {
<<<<<<< HEAD
=======
    val isArabicOrUrdu = remember(message) {
        message.any { it in '\u0600'..'\u06FF' || it in '\u0750'..'\u077F' || it in '\uFB50'..'\uFDFF' || it in '\uFE70'..'\uFEFF' }
    }

    val displayText = if (isArabicOrUrdu) {
        FontHelper.formatArabicText(message)
    } else {
        message
    }

>>>>>>> 6e834ed (Update Taqwahub)
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F3624)),
        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Campaign,
                contentDescription = "Announcement",
                tint = GoldPrimary,
                modifier = Modifier.size(24.dp)
            )
<<<<<<< HEAD
            LoopingAnnouncementText(
                text = message,
                modifier = Modifier.weight(1f),
                color = Color.White,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Medium
=======
            Text(
                text = displayText,
                color = Color.White,
                fontSize = if (isArabicOrUrdu) 15.sp else 13.sp,
                lineHeight = if (isArabicOrUrdu) 24.sp else 18.sp,
                fontFamily = if (isArabicOrUrdu) FontHelper.getFontForScript("indopak") else androidx.compose.ui.text.font.FontFamily.Default,
                style = TextStyle(
                    textAlign = if (isArabicOrUrdu) TextAlign.Right else TextAlign.Left,
                    textDirection = if (isArabicOrUrdu) TextDirection.Rtl else TextDirection.Ltr
                ),
                modifier = Modifier.weight(1f)
>>>>>>> 6e834ed (Update Taqwahub)
            )
        }
    }
}

@Composable
fun GoldenActivityGraph(allTimeTasks: List<AllTimeTaskEntity>) {
    val context = LocalContext.current
    val completedOnly = remember(allTimeTasks) {
        allTimeTasks.filter { it.completedAt != "MISSED" }
    }
    val taskCountByDate = remember(completedOnly) {
        completedOnly.groupBy { it.date }
            .mapValues { it.value.size }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = EmeraldCard),
        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.15f)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = "Activity Graph",
                        tint = GoldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Spiritual Devotion Log",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "${completedOnly.size} Total Deeds",
                    color = GoldPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (col in 0 until 5) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        for (row in 0 until 7) {
                            val index = col * 7 + row
                            val cal = java.util.Calendar.getInstance().apply {
                                add(java.util.Calendar.DAY_OF_YEAR, -index)
                            }
                            val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(cal.time)
                            val count = taskCountByDate[dateStr] ?: 0
                            
                            val cellColor = when {
                                count == 0 -> Color(0xFF042F2E).copy(alpha = 0.4f)
                                count == 1 -> GoldPrimary.copy(alpha = 0.3f)
                                count == 2 -> GoldPrimary.copy(alpha = 0.6f)
                                else -> GoldPrimary
                            }

                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(cellColor)
                                    .clickable {
                                        val formattedDate = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.US).format(cal.time)
                                        Toast.makeText(context, "$count deeds completed on $formattedDate", Toast.LENGTH_SHORT).show()
                                    }
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Less", color = TextGray, fontSize = 9.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFF042F2E).copy(alpha = 0.4f)))
                Spacer(modifier = Modifier.width(3.dp))
                Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(GoldPrimary.copy(alpha = 0.3f)))
                Spacer(modifier = Modifier.width(3.dp))
                Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(GoldPrimary.copy(alpha = 0.6f)))
                Spacer(modifier = Modifier.width(3.dp))
                Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(GoldPrimary))
                Spacer(modifier = Modifier.width(4.dp))
                Text("More", color = TextGray, fontSize = 9.sp)
            }
        }
    }
}
