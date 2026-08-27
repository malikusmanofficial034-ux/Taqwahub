package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.ui.screens.DonateScreen
import com.example.ui.screens.OfflineDownloadDialog
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.os.Build
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import kotlinx.coroutines.launch
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
<<<<<<< HEAD
=======
import com.example.util.FontHelper
>>>>>>> 6e834ed (Update Taqwahub)
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.EmeraldBackground
import com.example.ui.theme.EmeraldCard
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.OnGoldText
import com.example.ui.theme.TextGray
import com.example.ui.theme.OnEmeraldText
import com.example.ui.theme.AlertRed
import com.example.data.room.UserStatsEntity
import com.example.ui.screens.*
import com.example.viewmodel.TaqwaViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.viewmodel.TaqwaNetworkType
import com.example.viewmodel.TaqwaNetworkCondition
import com.example.viewmodel.TaqwaNetworkStatusInfo
import com.example.viewmodel.*
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.focus.onFocusChanged
class MainActivity : ComponentActivity() {

    private val viewModel: TaqwaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Handle splash screen
        installSplashScreen()

        // Set explicit bar colors for the system status bar and navigation bar with fallback handling to eliminate crashes on older devices
        try {
            enableEdgeToEdge(
                statusBarStyle = androidx.activity.SystemBarStyle.dark(
                    android.graphics.Color.TRANSPARENT
                ),
                navigationBarStyle = androidx.activity.SystemBarStyle.dark(
                    android.graphics.Color.TRANSPARENT
                )
            )
            window.navigationBarColor = android.graphics.Color.parseColor("#0A4636")
            window.statusBarColor = android.graphics.Color.TRANSPARENT
        } catch (e: Throwable) {
            try {
                enableEdgeToEdge()
            } catch (ex: Throwable) {
                // Absolute fallback when system UI components behave unexpectedly
            }
        }
        
        setContent {
            // Safety Layer: Dynamic screenshot & screen recording prevention
            val isAntiSpyEnabled = viewModel.isSecurityAntiSpyEnabled
            LaunchedEffect(isAntiSpyEnabled) {
                if (isAntiSpyEnabled) {
                    window.addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
                } else {
                    window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
                }
            }

            MyApplicationTheme {
                MainAppLayout(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppLayout(viewModel: TaqwaViewModel) {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, "Notification permission enabled!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Notifications disabled. You can enable them in system settings.", Toast.LENGTH_LONG).show()
        }
    }
    val sharedPrefs = remember { com.example.util.SecurePreferences.getSecurePrefs(context) }
    
    // Auth state logic
    val firebaseUser = viewModel.currentUser
    val userStats by viewModel.stats.collectAsState()

    val networkStatus by viewModel.networkStatus.collectAsState()
    var showNetworkDetailPopup by remember { mutableStateOf(false) }
    var popupAnimateVisible by remember { mutableStateOf(false) }
    val composeScope = rememberCoroutineScope()

    val dismissNetworkPopup = {
        popupAnimateVisible = false
    }

    val toggleNetworkPopup = {
        if (!showNetworkDetailPopup) {
            showNetworkDetailPopup = true
            popupAnimateVisible = true
        } else {
            popupAnimateVisible = false
        }
    }

    LaunchedEffect(popupAnimateVisible) {
        if (!popupAnimateVisible && showNetworkDetailPopup) {
            kotlinx.coroutines.delay(180L) // wait for exit animation to finish
            showNetworkDetailPopup = false
        }
    }

    LaunchedEffect(showNetworkDetailPopup) {
        if (showNetworkDetailPopup) {
            kotlinx.coroutines.delay(5000L)
            popupAnimateVisible = false
        }
    }

    LaunchedEffect(Unit) {
        viewModel.smartTaskCompletedFlow.collect { completedTask ->
            val pointsStr = if (completedTask.points > 0) " (+${completedTask.points} XP)" else ""
            Toast.makeText(context, "🎉 Activity Detected! Task Completed:\n${completedTask.title}$pointsStr", Toast.LENGTH_LONG).show()
        }
    }

    // Authentication local state fields
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf("") }
    var signUpUsername by remember { mutableStateOf("") }
    var isCheckingSignUpUsername by remember { mutableStateOf(false) }
    var signUpUsernameError by remember { mutableStateOf<String?>(null) }
    var isSignUpUsernameAvailable by remember { mutableStateOf(false) }

    LaunchedEffect(signUpUsername) {
        val trimmed = signUpUsername.trim().lowercase()
        if (trimmed.isEmpty()) {
            signUpUsernameError = null
            isSignUpUsernameAvailable = false
            return@LaunchedEffect
        }
        val regex = Regex("^[a-zA-Z0-9_]{3,20}$")
        if (!regex.matches(trimmed)) {
            signUpUsernameError = "3-20 chars, letters, numbers & underscores only"
            isSignUpUsernameAvailable = false
            return@LaunchedEffect
        }

        isCheckingSignUpUsername = true
        signUpUsernameError = null
        viewModel.checkUsernameAvailability(trimmed, null) { available ->
            isCheckingSignUpUsername = false
            isSignUpUsernameAvailable = available
            if (!available) {
                signUpUsernameError = "Username is already taken"
            }
        }
    }

    var isSignUpMode by remember { mutableStateOf(false) }
    var isPrivacyAccepted by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var isMale by remember { mutableStateOf(true) }
    var sectCast by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isAuthenticating by remember { mutableStateOf(false) }
    val authScrollState = rememberScrollState()

    val profileCompletedKey = "profile_completed_${firebaseUser?.uid}"
    var profileCompletedState by remember(firebaseUser?.uid) {
<<<<<<< HEAD
        mutableStateOf(sharedPrefs.getBoolean(profileCompletedKey, false))
    }

    var isCheckingProfile by remember(firebaseUser?.uid) {
        mutableStateOf(firebaseUser != null && !sharedPrefs.getBoolean(profileCompletedKey, false))
=======
        mutableStateOf(sharedPrefs.getBoolean(profileCompletedKey, false) || userStats.name.isNotBlank() || userStats.username.isNotBlank())
    }

    var isCheckingProfile by remember(firebaseUser?.uid) {
        mutableStateOf(firebaseUser != null && !sharedPrefs.getBoolean(profileCompletedKey, false) && userStats.name.isBlank() && userStats.username.isBlank())
>>>>>>> 6e834ed (Update Taqwahub)
    }

    var justCompletedSetup by remember(firebaseUser?.uid) {
        mutableStateOf(false)
    }

    androidx.compose.runtime.DisposableEffect(firebaseUser?.uid) {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == profileCompletedKey) {
                profileCompletedState = sharedPrefs.getBoolean(profileCompletedKey, false)
            }
        }
        sharedPrefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            sharedPrefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

<<<<<<< HEAD
    LaunchedEffect(firebaseUser) {
        val user = firebaseUser
        if (user != null && !profileCompletedState) {
            isCheckingProfile = true
            try {
                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                db.collection("users").document(user.uid).get()
                    .addOnCompleteListener { task ->
                        isCheckingProfile = false
                        if (task.isSuccessful) {
                            val document = task.result
                            if (document != null && document.exists()) {
                                val userStats = document.get("userStats")
                                if (userStats != null) {
                                    sharedPrefs.edit().putBoolean(profileCompletedKey, true).apply()
                                    profileCompletedState = true
                                    Log.d("MainActivity", "User profile detected in Firestore! Bypassing setup screen.")
                                }
                            }
                        }
                    }
            } catch (e: Exception) {
                isCheckingProfile = false
                Log.e("MainActivity", "Firestore offline", e)
=======
    LaunchedEffect(firebaseUser, userStats.username, userStats.name) {
        val user = firebaseUser
        if (user != null) {
            val hasLocalProfile = userStats.name.isNotBlank() || userStats.username.isNotBlank()
            if (hasLocalProfile) {
                sharedPrefs.edit().putBoolean(profileCompletedKey, true).apply()
                profileCompletedState = true
                isCheckingProfile = false
            } else if (!profileCompletedState) {
                isCheckingProfile = true
                try {
                    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    db.collection("users").document(user.uid).get()
                        .addOnCompleteListener { task ->
                            isCheckingProfile = false
                            if (task.isSuccessful) {
                                val document = task.result
                                if (document != null && document.exists()) {
                                    val rStats = document.get("userStats") as? Map<String, Any>
                                    val rName = (rStats?.get("name") as? String) ?: document.getString("name") ?: ""
                                    val rUsername = (rStats?.get("username") as? String) ?: document.getString("username") ?: ""
                                    if (rStats != null || rName.isNotBlank() || rUsername.isNotBlank()) {
                                        sharedPrefs.edit().putBoolean(profileCompletedKey, true).apply()
                                        profileCompletedState = true
                                        viewModel.triggerFirebaseSync(forcePull = true)
                                        Log.d("MainActivity", "User profile detected in Firestore! Bypassing setup screen and pulling data.")
                                    }
                                }
                            }
                        }
                } catch (e: Exception) {
                    isCheckingProfile = false
                    Log.e("MainActivity", "Firestore offline", e)
                }
            } else {
                isCheckingProfile = false
>>>>>>> 6e834ed (Update Taqwahub)
            }
        } else {
            isCheckingProfile = false
        }
    }

    var showUsernameOverlay by remember { mutableStateOf(false) }
    var isUsernameCheckGracePeriodPassed by remember(firebaseUser?.uid) { mutableStateOf(false) }

    LaunchedEffect(firebaseUser?.uid) {
        if (firebaseUser != null) {
            // Wait 15 seconds after sign-in before allowing background username overlay to trigger
            kotlinx.coroutines.delay(15000L)
            isUsernameCheckGracePeriodPassed = true
        } else {
            isUsernameCheckGracePeriodPassed = false
        }
    }

    val shouldShowUsernameOverlay = firebaseUser != null && 
            profileCompletedState && 
            !isCheckingProfile && 
            !viewModel.isSyncingData && 
            viewModel.hasCompletedInitialSync && 
            !justCompletedSetup && 
            userStats.email.isNotEmpty() && 
            userStats.username.isEmpty() &&
            isUsernameCheckGracePeriodPassed

    LaunchedEffect(shouldShowUsernameOverlay) {
        if (shouldShowUsernameOverlay) {
            showUsernameOverlay = true
        } else {
            showUsernameOverlay = false
        }
    }
    var showDownloadDialog by remember { mutableStateOf(false) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val currentAppVersionCode = remember(context) {
        try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                pInfo.longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                pInfo.versionCode
            }
        } catch (e: Throwable) {
            1
        }
    }
    val appConfig = viewModel.appConfig
    val isAdmin = viewModel.isAdmin

    // Secure Admin Update Bypass state
    var isUpdateBypassedByAdminCode by remember { mutableStateOf(false) }

    // 1. Force Update Interceptor Screen
    val isUserLoggedIn = (firebaseUser != null)

    if (isUserLoggedIn && com.example.util.AppUpdateManager.isVersionLower(context, appConfig.forceUpdateMinVersion) && !isAdmin && !isUpdateBypassedByAdminCode) {
        ForceUpdateBlockScreen(
            downloadUrl = appConfig.updateDownloadUrl,
            onAdminBypass = {
                isUpdateBypassedByAdminCode = true
            },
            onSignOut = {
                FirebaseAuth.getInstance().signOut()
            }
        )
        return
    }

    if (isUserLoggedIn) {
        // Maintenance Interceptor Screen (Bypassed for standard admin management)
        if (appConfig.isUnderMaintenance && !isAdmin) {
            MaintenanceBlockScreen(
                message = appConfig.message,
                onSignOut = {
                    try {
                        FirebaseAuth.getInstance().signOut()
                    } catch (e: Throwable) {
                        Log.e("MainActivity", "Firebase signout failed: ${e.message}")
                    }
                    viewModel.disableGuestMode()
                    viewModel.clearLocalDataAndPreferences()
                    viewModel.currentUser = null
                    viewModel.currentView = "dashboard"
                }
            )
            return
        }

        BackHandler(enabled = viewModel.currentView != "dashboard") {
            viewModel.navigateBack()
        }
    }

    LaunchedEffect(Unit) {
        // Request Notification Permission dynamically for Android 13+ (API 33)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasNotification = androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (!hasNotification) {
                permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        try {
            val hasFine = androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
            val hasCoarse = androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
            
            if (hasFine || hasCoarse) {
                viewModel.hasLocationPermission = true
                try {
                    val fusedClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context)
                    fusedClient.lastLocation.addOnSuccessListener { loc ->
                        loc?.let {
                            viewModel.updateCoordinates(it.latitude, it.longitude)
                        }
<<<<<<< HEAD
=======
                    }.addOnFailureListener { e ->
                        Log.w("MainActivity", "FusedLocationProvider lastLocation task failed gracefully: ${e.message}")
>>>>>>> 6e834ed (Update Taqwahub)
                    }
                } catch (e: Throwable) {
                    Log.e("MainActivity", "FusedLocationProvider client access failed: ${e.message}")
                }
            }
        } catch (e: Throwable) {
            Log.e("MainActivity", "Permission checking or location initialization failed: ${e.message}")
        }
        try {
            viewModel.fetchPrayerTimes()
        } catch (e: Throwable) {
            Log.e("MainActivity", "Failed to fetch prayer times: ${e.message}")
        }
    }

    if (!isUserLoggedIn) {
        // Main Auth Wrapper
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(EmeraldBackground)
                .safeDrawingPadding()
                .imePadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(authScrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                // Logo & Title
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(bottom = 8.dp)) {
                    Box(modifier = Modifier.size(64.dp).clip(androidx.compose.foundation.shape.CircleShape).border(1.dp, GoldPrimary, androidx.compose.foundation.shape.CircleShape).background(EmeraldCard), contentAlignment = Alignment.Center) {
                        TaqwaLogo(modifier = Modifier.size(40.dp), noText = true)
                    }
                }
                Text(
                    text = "TAQWAHUB",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 3.sp
                )
                Text(
                    text = "SPIRITUAL COMPASS & DEVOTION",
                    color = GoldPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Input Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = EmeraldCard),
                    border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.05f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Shield Icon
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(GoldPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = "Security",
                                modifier = Modifier.size(28.dp),
                                tint = EmeraldCard
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = if (isSignUpMode) "Join TaqwaHub" else "Welcome Back",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = if (isSignUpMode) "CREATE YOUR CLOUD-SYNC PROFILE" else "SYNC YOUR SPIRITUAL JOURNEY IN REAL-TIME",
                            color = TextGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(top = 6.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Toggle switch (SIGN IN / SIGN UP) - Persistent Selected Fill Section!
                        Row(
                            modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .border(1.dp, GoldPrimary.copy(alpha = 0.15f), RoundedCornerShape(16.dp)),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp, topEnd = if (!isSignUpMode) 16.dp else 0.dp, bottomEnd = if (!isSignUpMode) 16.dp else 0.dp))
                                            .background(if (!isSignUpMode) GoldPrimary else Color.Transparent)
                                            .clickable { isSignUpMode = false },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("SIGN IN", color = if (!isSignUpMode) EmeraldCard else TextGray, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Box(
                                modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp, topStart = if (isSignUpMode) 16.dp else 0.dp, bottomStart = if (isSignUpMode) 16.dp else 0.dp))
                                            .background(if (isSignUpMode) GoldPrimary else Color.Transparent)
                                            .clickable { isSignUpMode = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("SIGN UP", color = if (isSignUpMode) EmeraldCard else TextGray, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }

                    // Scrollable section for fields and buttons
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))

                        val fieldColors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedBorderColor = Color(0xFF1B4E38),
                                unfocusedBorderColor = Color(0xFF1B4E38),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )

                        AnimatedVisibility(
                            visible = isSignUpMode,
                            enter = fadeIn(animationSpec = tween(300)) + expandVertically(animationSpec = tween(300)),
                            exit = fadeOut(animationSpec = tween(300)) + shrinkVertically(animationSpec = tween(300))
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text("FULL NAME (OPTIONAL)", color = TextGray, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                                OutlinedTextField(
                                    value = nameInput,
                                    onValueChange = { nameInput = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("signup_name_field"),
                                    placeholder = { Text("Enter your name", color = TextGray.copy(alpha=0.5f)) },
                                    leadingIcon = { Icon(Icons.Default.Check, "Name", tint = TextGray) },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = fieldColors
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("GENDER", color = TextGray, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(56.dp)
                                                .border(1.dp, Color(0xFF1B4E38), RoundedCornerShape(16.dp)),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp, topEnd = if (isMale) 16.dp else 0.dp, bottomEnd = if (isMale) 16.dp else 0.dp)).background(if (isMale) Color(0xFF154430) else Color.Transparent).clickable { isMale = true }, contentAlignment = Alignment.Center) {
                                                Text("MALE", color = if (isMale) GoldPrimary else TextGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Box(modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp, topStart = if (!isMale) 16.dp else 0.dp, bottomStart = if (!isMale) 16.dp else 0.dp)).background(if (!isMale) Color(0xFF154430) else Color.Transparent).clickable { isMale = false }, contentAlignment = Alignment.Center) {
                                                Text("FEMALE", color = if (!isMale) GoldPrimary else TextGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("SECT / CAST (OPTIONAL)", color = TextGray, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                                        OutlinedTextField(
                                            value = sectCast,
                                            onValueChange = { sectCast = it },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("signup_sect_field"),
                                            placeholder = { Text("e.g. Sunni", color = TextGray.copy(alpha=0.5f), fontSize=13.sp) },
                                            shape = RoundedCornerShape(16.dp),
                                            colors = fieldColors
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("UNIQUE USERNAME", color = TextGray, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                                OutlinedTextField(
                                    value = signUpUsername,
                                    onValueChange = { input ->
                                        signUpUsername = input.filter { it.isLetterOrDigit() || it == '_' }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("signup_username_field"),
                                    placeholder = { Text("Enter username", color = TextGray.copy(alpha=0.5f), fontSize = 13.sp) },
                                    leadingIcon = {
                                        Text(
                                            text = "@",
                                            color = Color.White.copy(alpha = 0.4f),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(start = 12.dp)
                                        )
                                    },
                                    trailingIcon = {
                                        IconButton(
                                            onClick = {
                                                signUpUsername = suggestThemeUsername()
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Casino,
                                                contentDescription = "Suggest Username",
                                                tint = GoldPrimary
                                            )
                                        }
                                    },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = fieldColors,
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isCheckingSignUpUsername) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(12.dp),
                                            color = GoldPrimary,
                                            strokeWidth = 1.5.dp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Checking availability...", color = GoldPrimary.copy(alpha = 0.8f), fontSize = 11.sp)
                                    } else if (signUpUsernameError != null) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = "Error",
                                            tint = AlertRed,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(signUpUsernameError!!, color = AlertRed, fontSize = 11.sp)
                                    } else if (isSignUpUsernameAvailable) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Available",
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Username is available!", color = Color(0xFF10B981), fontSize = 11.sp)
                                    } else {
                                        Text("Letters, numbers, or underscores (3-20 chars)", color = TextGray.copy(alpha = 0.6f), fontSize = 11.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("EMAIL ADDRESS", color = TextGray, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("login_email_field"),
                                placeholder = { Text("name@domain.com", color = TextGray.copy(alpha=0.5f)) },
                                leadingIcon = { Icon(Icons.Default.Email, "Email", tint = TextGray) },
                                shape = RoundedCornerShape(16.dp),
                                colors = fieldColors
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("PASSWORD", color = TextGray, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AutoAwesome, "Secure", tint = GoldPrimary, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("SECURE ASSIST", color = GoldPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                                }
                            }
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("login_password_field"),
                                placeholder = { Text("•••••••••", color = TextGray.copy(alpha=0.5f)) },
                                visualTransformation = if (isPasswordVisible) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                leadingIcon = { Icon(Icons.Default.Lock, "Password", tint = TextGray) },
                                trailingIcon = {
                                    Icon(
                                        imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle visibility",
                                        tint = TextGray,
                                        modifier = Modifier.clickable { isPasswordVisible = !isPasswordVisible }
                                    )
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = fieldColors
                            )
                        }

                        if (isSignUpMode) {
                            PasswordStrengthIndicator(password = password)
                        }

                        if (!isSignUpMode) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Text(
                                    text = "Forgot Password?",
                                    color = GoldPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clickable {
                                            if (email.trim().isEmpty()) {
                                                Toast.makeText(context, "Please enter your email address above first.", Toast.LENGTH_LONG).show()
                                            } else {
                                                isAuthenticating = true
                                                FirebaseAuth.getInstance().sendPasswordResetEmail(email.trim())
                                                    .addOnSuccessListener {
                                                        isAuthenticating = false
                                                        Toast.makeText(context, "Password reset link sent to $email. Please check your inbox.", Toast.LENGTH_LONG).show()
                                                    }
                                                    .addOnFailureListener {
                                                        isAuthenticating = false
                                                        Toast.makeText(context, "Error: ${getFriendlyAuthErrorMessage(it)}", Toast.LENGTH_LONG).show()
                                                    }
                                            }
                                        }
                                        .padding(vertical = 4.dp, horizontal = 8.dp)
                                )
                            }
                        }

                        if (isSignUpMode) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isPrivacyAccepted,
                                    onCheckedChange = { isPrivacyAccepted = it },
                                    colors = CheckboxDefaults.colors(checkedColor = GoldPrimary, uncheckedColor = Color(0xFF1B4E38))
                                )
                                val annotatedLegalString = androidx.compose.ui.text.buildAnnotatedString {
                                    append("Before signing up, you explicitly agree that you accept our ")
                                    pushStringAnnotation(tag = "PRIVACY", annotation = "privacy")
                                    withStyle(
                                        androidx.compose.ui.text.SpanStyle(
                                            color = GoldPrimary,
                                            textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline,
                                            fontWeight = FontWeight.Bold
                                        )
                                    ) {
                                        append("Privacy Policy")
                                    }
                                    pop()
                                    append(" and ")
                                    pushStringAnnotation(tag = "TERMS", annotation = "terms")
                                    withStyle(
                                        androidx.compose.ui.text.SpanStyle(
                                            color = GoldPrimary,
                                            textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline,
                                            fontWeight = FontWeight.Bold
                                        )
                                    ) {
                                        append("Terms of Use")
                                    }
                                    pop()
                                    append(".")
                                }

                                androidx.compose.foundation.text.ClickableText(
                                    text = annotatedLegalString,
                                    style = androidx.compose.ui.text.TextStyle(color = TextGray, fontSize = 12.sp),
                                    modifier = Modifier.padding(start = 4.dp),
                                    onClick = { offset ->
                                        annotatedLegalString.getStringAnnotations(tag = "PRIVACY", start = offset, end = offset)
                                            .firstOrNull()?.let {
                                                val url = viewModel.appConfig.privacyPolicyUrl.ifBlank { "https://taqwahub.vercel.app/privacy.html" }
                                                try {
                                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                                } catch (e: Exception) {
                                                    Toast.makeText(context, "Opening: $url", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        annotatedLegalString.getStringAnnotations(tag = "TERMS", start = offset, end = offset)
                                            .firstOrNull()?.let {
                                                val url = viewModel.appConfig.termsOfServiceUrl.ifBlank { "https://taqwahub.vercel.app/terms.html" }
                                                try {
                                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                                } catch (e: Exception) {
                                                    Toast.makeText(context, "Opening: $url", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        val isFormFilled = if (isSignUpMode) {
                            email.trim().isNotEmpty() &&
                            password.trim().isNotEmpty() &&
                            signUpUsername.trim().isNotEmpty() &&
                            isSignUpUsernameAvailable &&
                            signUpUsernameError == null &&
                            isPrivacyAccepted
                        } else {
                            email.trim().isNotEmpty() && password.trim().isNotEmpty()
                        }

                        Button(
                            onClick = {
                                if (isAuthenticating) return@Button
                                if (email.isEmpty() || password.isEmpty()) {
                                    Toast.makeText(context, "Fill all fields", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (isSignUpMode && signUpUsername.trim().isEmpty()) {
                                    Toast.makeText(context, "Please choose a username", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (isSignUpMode && (!isSignUpUsernameAvailable || signUpUsernameError != null)) {
                                    Toast.makeText(context, "Please choose an available username", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (isSignUpMode && !isPrivacyAccepted) {
                                    Toast.makeText(context, "Please accept Privacy Policy", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (isSignUpMode && isTempEmail(email)) {
                                    Toast.makeText(context, "Disposable/temporary email addresses are restricted. Please register using a valid personal email account.", Toast.LENGTH_LONG).show()
                                    return@Button
                                }
                                isAuthenticating = true
                                try {
                                    val auth = FirebaseAuth.getInstance()
                                    if (isSignUpMode) {
                                        auth.createUserWithEmailAndPassword(email, password)
                                            .addOnSuccessListener { result ->
                                                val user = result.user
                                                viewModel.currentUser = user
                                                viewModel.setStats(
                                                    UserStatsEntity(
<<<<<<< HEAD
                                                        name = nameInput.ifEmpty { "Servant of Allah" },
                                                        username = signUpUsername.trim().lowercase(),
                                                        email = email,
                                                        gender = if (isMale) "Male" else "Female",
                                                        sectOrCast = sectCast.ifEmpty { "Sunni" }
=======
                                                        name = nameInput.trim(),
                                                        username = signUpUsername.trim().lowercase(),
                                                        email = email,
                                                        gender = if (isMale) "Male" else "Female",
                                                        sectOrCast = sectCast.trim()
>>>>>>> 6e834ed (Update Taqwahub)
                                                    )
                                                )
                                                sharedPrefs.edit().putBoolean("profile_completed_${user?.uid}", true).apply()
                                                sharedPrefs.edit().putBoolean("has_seen_bismillah_welcome", false).apply()
                                                profileCompletedState = true
                                                isAuthenticating = false
                                                Toast.makeText(context, "Registration successful!", Toast.LENGTH_SHORT).show()
                                            }
                                            .addOnFailureListener {
                                                isAuthenticating = false
                                                Toast.makeText(context, getFriendlyAuthErrorMessage(it), Toast.LENGTH_LONG).show()
                                            }
                                    } else {
                                        auth.signInWithEmailAndPassword(email, password)
                                            .addOnSuccessListener { result ->
                                                val user = result.user
                                                viewModel.currentUser = user
                                                sharedPrefs.edit().putBoolean("profile_completed_${user?.uid}", true).apply()
                                                sharedPrefs.edit().putBoolean("has_seen_bismillah_welcome", true).apply()
                                                profileCompletedState = true
                                                isAuthenticating = false
                                                Toast.makeText(context, "Welcome back!", Toast.LENGTH_SHORT).show()
                                            }
                                            .addOnFailureListener {
                                                isAuthenticating = false
                                                Toast.makeText(context, getFriendlyAuthErrorMessage(it), Toast.LENGTH_LONG).show()
                                            }
                                    }
                                } catch (e: Throwable) {
                                    isAuthenticating = false
                                    Log.e("MainActivity", "Firebase integration unavailable: ${e.message}")
                                    Toast.makeText(context, "Authentication Service Unavailable.\nPlease check connection.", Toast.LENGTH_LONG).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp).testTag("auth_action_button"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isFormFilled) GoldPrimary else Color(0xFF0F3624)
                            )
                        ) {
                            if (isAuthenticating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = if (isFormFilled) EmeraldCard else GoldPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = if (isSignUpMode) "SIGN UP" else "SIGN IN",
                                    color = if (isFormFilled) EmeraldCard else TextGray,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    letterSpacing = 1.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    } else if (firebaseUser != null && isCheckingProfile) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(EmeraldBackground),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    color = GoldPrimary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Verifying profile...",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    } else if (firebaseUser != null && profileCompletedState && !isCheckingProfile && userStats.isBlocked) {
        BlockedScreen(
            message = "Your account has been suspended by the administrator for violating platform policies. Please contact admin@taqwahub.com."
        )
<<<<<<< HEAD
    } else if (firebaseUser != null && !profileCompletedState && !isCheckingProfile) {
=======
    } else if (firebaseUser != null && !profileCompletedState && !isCheckingProfile && userStats.name.isBlank() && userStats.username.isBlank()) {
>>>>>>> 6e834ed (Update Taqwahub)
        CompleteProfileSetupScreen(
            initialName = if (userStats.name.isNotEmpty()) userStats.name else (if (nameInput.isEmpty()) { firebaseUser.displayName ?: "" } else nameInput),
            initialEmail = if (userStats.email.isNotEmpty()) userStats.email else (if (email.isEmpty()) { firebaseUser.email ?: "" } else email),
            initialGender = if (userStats.gender.isNotEmpty()) userStats.gender else (if (isMale) "Male" else "Female"),
            initialSectOrCast = if (userStats.sectOrCast.isNotEmpty()) userStats.sectOrCast else sectCast,
            initialUsername = if (userStats.username.isNotEmpty()) userStats.username else signUpUsername,
            checkUsernameAvailability = { usernameStr, callback ->
                viewModel.checkUsernameAvailability(usernameStr, firebaseUser.uid, callback)
            },
            onSignOut = {
                try {
                    FirebaseAuth.getInstance().signOut()
                } catch (e: Throwable) {
                    Log.e("MainActivity", "Firebase signout failed: ${e.message}")
                }
                viewModel.disableGuestMode()
                viewModel.clearLocalDataAndPreferences()
                viewModel.currentUser = null
                viewModel.currentView = "dashboard"
            },
            onComplete = { correctedName: String, usernameVal: String, gender: String, sect: String ->
                justCompletedSetup = true
                viewModel.setStats(
                    UserStatsEntity(
<<<<<<< HEAD
                        name = if (correctedName.trim().isEmpty()) "Servant of Allah" else correctedName.trim(),
                        username = usernameVal.trim().lowercase(),
                        email = firebaseUser.email ?: "",
                        gender = gender,
                        sectOrCast = if (sect.trim().isEmpty()) "Sunni" else sect.trim()
=======
                        name = correctedName.trim(),
                        username = usernameVal.trim().lowercase(),
                        email = firebaseUser.email ?: "",
                        gender = gender,
                        sectOrCast = sect.trim()
>>>>>>> 6e834ed (Update Taqwahub)
                    )
                )
                sharedPrefs.edit().putBoolean(profileCompletedKey, true).apply()
                profileCompletedState = true
                Toast.makeText(context, "Welcome to TaqwaHub!", Toast.LENGTH_SHORT).show()
            }
        )
    } else {
        var hasSeenBismillahWelcome by remember {
            mutableStateOf(sharedPrefs.getBoolean("has_seen_bismillah_welcome", false))
        }

        Box(modifier = Modifier.fillMaxSize().background(EmeraldBackground)) {
<<<<<<< HEAD
=======
            // ----------------- Graceful Fallback Ad Player Overlay -----------------
            val showAdSimulation by viewModel.showAdSimulation.collectAsState()
            if (showAdSimulation) {
                androidx.compose.ui.window.Dialog(
                    onDismissRequest = { /* No dismissing via outside tap */ },
                    properties = androidx.compose.ui.window.DialogProperties(
                        dismissOnBackPress = false,
                        dismissOnClickOutside = false,
                        usePlatformDefaultWidth = false
                    )
                ) {
                    var secondsRemaining by remember { mutableStateOf(8) }
                    var adProgress by remember { mutableStateOf(1f) }

                    LaunchedEffect(Unit) {
                        while (secondsRemaining > 0) {
                            kotlinx.coroutines.delay(1000L)
                            secondsRemaining--
                            adProgress = secondsRemaining.toFloat() / 8f
                        }
                        // Ad simulation finished successfully! Trigger reward callback and hide
                        viewModel.adSimulationCallback?.invoke()
                        viewModel.showAdSimulation.value = false
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF0F172A)) // Sleek dark slate cinema theme
                    ) {
                        // Decorative Abstract Background Elements
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp)
                        ) {
                            Column(
                                modifier = Modifier.align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                // Developer Icon and Brand Accent
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .background(GoldPrimary.copy(alpha = 0.15f), CircleShape)
                                        .border(1.5.dp, GoldPrimary, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Favorite,
                                        contentDescription = null,
                                        tint = GoldPrimary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }

                                Text(
                                    text = "Taqwa Premium Sponsor",
                                    color = GoldPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif
                                )

                                Text(
                                    text = "Thank you for supporting Taqwa's free ad-free core experience. Your daily bonus is being processed.",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 24.dp),
                                    lineHeight = 18.sp
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Beautiful Circular Progress countdown timer
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.size(90.dp)
                                ) {
                                    CircularProgressIndicator(
                                        progress = adProgress,
                                        color = GoldPrimary,
                                        strokeWidth = 5.dp,
                                        trackColor = Color.White.copy(alpha = 0.1f),
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Text(
                                        text = "$secondsRemaining",
                                        color = Color.White,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Text(
                                    text = "Sponsor Message: ${secondsRemaining}s remaining...",
                                    color = TextGray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Bottom developer support message
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "TaqwaHub Infrastructure",
                                    color = Color.White.copy(alpha = 0.4f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "Keeping servers running for over 100k+ global Muslims free.",
                                    color = Color.White.copy(alpha = 0.3f),
                                    fontSize = 9.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

>>>>>>> 6e834ed (Update Taqwahub)
            // Main Application Scaffold Layout
            ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                androidx.compose.material3.ModalDrawerSheet(
                    drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
                    drawerContainerColor = EmeraldBackground,
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(320.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                    // Header Section with Gradient & Brand Logo
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF022C22),
                                        EmeraldBackground
                                    )
                                )
                            )
                            .padding(24.dp)
                            .statusBarsPadding()
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Unified high-contrast brand logo
                                TaqwaLogo(
                                    modifier = Modifier.size(56.dp),
                                    noText = true
                                )
                                Column {
                                    Text(
                                        text = "TaqwaHub",
                                        color = GoldPrimary,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Serif
                                    )
                                    Text(
                                        text = "Your Islamic Companion",
                                        color = TextGray,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    Divider(
                        color = GoldPrimary.copy(alpha = 0.2f),
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                    )

                    // Navigation Items Scrollable Column
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        val tools = mutableListOf(
                            Triple("Global Leaderboard", "leaderboard", Icons.Default.Leaderboard),
                            Triple("Qibla Finder", "qibla", Icons.Default.Explore),
                            Triple("Dua Library", "dua", Icons.Default.AutoStories),
                            Triple("Hadith Explorer", "hadith", Icons.Default.MenuBook),
                            Triple("Names of Allah", "names", Icons.Default.Bookmarks),
                            Triple("Zakat Calculator", "zakat", Icons.Default.Calculate),
                            Triple("Islamic Calendar", "calendar", Icons.Default.DateRange),
                            Triple("Tasbeeh Counter", "tasbeeh", Icons.Default.Adjust),
                            Triple("Help & Complaints", "user_complaints", Icons.Default.Feedback),
                            Triple("Support & Donate", "donate", Icons.Default.Favorite),
                            Triple("Settings & Privacy", "settings", Icons.Default.Settings)
                        )
                        if (viewModel.isAdmin) {
                            tools.add(Triple("Admin Complaints 👑", "admin_complaints", Icons.Default.RateReview))
                            tools.add(Triple("Admin Dashboard 👑", "admin_dashboard", Icons.Default.Settings))
                        }
                        
                        tools.forEach { tool ->
                            val isSelected = viewModel.currentView == tool.second
                            NavigationDrawerItem(
                                label = {
                                    Text(
                                        text = tool.first,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 14.sp
                                    )
                                },
                                icon = {
<<<<<<< HEAD
                                    Icon(
                                        imageVector = tool.third,
                                        contentDescription = tool.first,
                                        tint = if (isSelected) OnGoldText else GoldPrimary.copy(alpha = 0.8f)
                                    )
=======
                                    val showBadge = (tool.second == "leaderboard" && viewModel.hasLeaderboardUpdate) ||
                                            (tool.second == "user_complaints" && viewModel.hasUnreadSupportReply)
                                    if (showBadge) {
                                        BadgedBox(
                                            badge = {
                                                Badge(containerColor = GoldPrimary)
                                            }
                                        ) {
                                            Icon(
                                                imageVector = tool.third,
                                                contentDescription = tool.first,
                                                tint = if (isSelected) OnGoldText else GoldPrimary.copy(alpha = 0.8f)
                                            )
                                        }
                                    } else {
                                        Icon(
                                            imageVector = tool.third,
                                            contentDescription = tool.first,
                                            tint = if (isSelected) OnGoldText else GoldPrimary.copy(alpha = 0.8f)
                                        )
                                    }
>>>>>>> 6e834ed (Update Taqwahub)
                                },
                                selected = isSelected,
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = GoldPrimary,
                                    selectedTextColor = OnGoldText,
                                    selectedIconColor = OnGoldText,
                                    unselectedContainerColor = Color.Transparent,
                                    unselectedTextColor = Color.White,
                                    unselectedIconColor = GoldPrimary.copy(alpha = 0.8f)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.padding(vertical = 4.dp),
                                onClick = {
                                    viewModel.navigateToView(tool.second)
                                    scope.launch { drawerState.close() }
                                }
                            )
                        }

                        Divider(
                            color = GoldPrimary.copy(alpha = 0.15f),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                    }

                    // Bottom Profile / Session Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                            .navigationBarsPadding()
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = EmeraldCard),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.15f)),
                            modifier = Modifier.fillMaxWidth().clickable { 
                                viewModel.navigateToView("profile")
                                scope.launch { drawerState.close() }
                            }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(GoldPrimary.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "User Account",
                                        tint = GoldPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "Verified Account",
                                        color = GoldPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = firebaseUser?.email ?: "User",
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                    }
                }
            }
        ) {
            Scaffold(
                containerColor = EmeraldBackground,
                topBar = {
                    Surface(
                        color = Color(0xFF022C22),
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(width = 0.5.dp, color = GoldPrimary.copy(alpha = 0.1f))
                    ) {
<<<<<<< HEAD
                        Row(
=======
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
>>>>>>> 6e834ed (Update Taqwahub)
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .height(64.dp)
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
<<<<<<< HEAD
                            // Left Side: Hamburg Menu & Branding
=======
                            // Left Side: Brand Identity (Logo + Glow + Typography)
>>>>>>> 6e834ed (Update Taqwahub)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
<<<<<<< HEAD
                                // Mobile Responsive Hamburger Toggle
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF064E3B).copy(alpha = 0.4f))
                                        .border(1.dp, GoldPrimary.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                        .clickable {
                                            scope.launch { drawerState.open() }
                                        }
                                        .testTag("drawer_open_button"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = "Open Drawer",
                                        tint = GoldPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                // Core Brand Identity (Logo + Glow + Typography)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    TaqwaLogo(
                                        modifier = Modifier.size(36.dp),
                                        noText = true
                                    )

                                    Text(
                                        text = "TaqwaHub",
                                        color = GoldPrimary,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = (-0.5).sp
                                    )
                                }
=======
                                TaqwaLogo(
                                    modifier = Modifier.size(38.dp),
                                    noText = true
                                )

                                Text(
                                    text = "TaqwaHub",
                                    color = GoldPrimary,
                                    fontSize = 20.sp,
                                    fontFamily = FontHelper.cinzelFontFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp
                                )
>>>>>>> 6e834ed (Update Taqwahub)
                            }

                            // Right Side: Network Diagnostic Pill Capsule (With AI integrated trigger button)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .background(Color(0xFF064E3B).copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                                    .border(1.dp, GoldPrimary.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                val activeConditionColor = when (networkStatus.condition) {
                                    TaqwaNetworkCondition.EXCELLENT, TaqwaNetworkCondition.GOOD -> Color(0xFF10B981) // Green
                                    TaqwaNetworkCondition.MEDIUM -> Color(0xFFFBBF24) // Yellow
                                    TaqwaNetworkCondition.BAD -> Color(0xFFEF4444) // Red
                                }

                                Box(
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF022C22).copy(alpha = 0.8f))
                                            .clickable {
                                                toggleNetworkPopup()
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        NetworkSignalIcon(
                                            status = networkStatus,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    if (showNetworkDetailPopup) {
										val density = androidx.compose.ui.platform.LocalDensity.current; val yOffsetPx = with(density) { 36.dp.roundToPx() }; Popup(
											offset = androidx.compose.ui.unit.IntOffset(0, yOffsetPx),
											alignment = Alignment.BottomCenter,
											onDismissRequest = {
												dismissNetworkPopup()
											},
											properties = PopupProperties(
												focusable = true,
												dismissOnClickOutside = true,
												dismissOnBackPress = true
											)
										) {
											androidx.compose.animation.AnimatedVisibility(
												visible = popupAnimateVisible,
												enter = fadeIn(animationSpec = tween(180)) + scaleIn(initialScale = 0.85f, animationSpec = tween(180)),
												exit = fadeOut(animationSpec = tween(150)) + scaleOut(targetScale = 0.85f, animationSpec = tween(150))
											) {
												Column(
													horizontalAlignment = Alignment.CenterHorizontally,
												) {
													// Pointer Triangle
													UpwardTriangle(
														color = Color(0xFF022C22),
														modifier = Modifier.size(12.dp, 6.dp)
													)
													// Pill Tooltip
													Row(
														modifier = Modifier
															.background(Color(0xFF022C22), RoundedCornerShape(20.dp))
															.border(1.dp, activeConditionColor.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
															.padding(horizontal = 14.dp, vertical = 7.dp),
														verticalAlignment = Alignment.CenterVertically,
														horizontalArrangement = Arrangement.spacedBy(6.dp)
													) {
														// Condition Dot
														Box(
															modifier = Modifier
																.size(6.dp)
																.background(activeConditionColor, CircleShape)
														)

														val transportText = when (networkStatus.type) {
															TaqwaNetworkType.WIFI -> "WIFI"
															TaqwaNetworkType.CELLULAR -> "DATA"
															TaqwaNetworkType.AIRPLANE -> "AIRPLANE MODE"
															TaqwaNetworkType.NONE -> "OFFLINE"
														}

														val conditionText = when (networkStatus.condition) {
															TaqwaNetworkCondition.EXCELLENT -> "EXCELLENT"
															TaqwaNetworkCondition.GOOD -> "GOOD"
															TaqwaNetworkCondition.MEDIUM -> "MEDIUM"
															TaqwaNetworkCondition.BAD -> "BAD"
														}

														val displayText = if (networkStatus.type == TaqwaNetworkType.AIRPLANE) "AIRPLANE MODE"
																		  else if (networkStatus.type == TaqwaNetworkType.NONE) "OFFLINE"
																		  else "$transportText: $conditionText"

														Text(
															text = displayText,
															color = activeConditionColor,
															fontSize = 10.sp,
															fontWeight = FontWeight.Black,
															letterSpacing = 0.5.sp
														)
													}
												}
											}
										}
									}
                                }

                                Spacer(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(14.dp)
                                        .background(Color.White.copy(alpha = 0.15f))
                                )

<<<<<<< HEAD
=======
                                // Streak Pill Indicator in TopBar
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFFF59E0B).copy(alpha = 0.18f))
                                        .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                                        .clickable { viewModel.openStreakModal() }
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                        .testTag("topbar_streak_badge")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalFireDepartment,
                                        contentDescription = "Streak",
                                        tint = Color(0xFFFACC15),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "${userStats.currentStreak}",
                                        color = Color(0xFFFDE68A),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    if (userStats.streakShields > 0) {
                                        Icon(
                                            imageVector = Icons.Default.Shield,
                                            contentDescription = "Shield Active",
                                            tint = Color(0xFF60A5FA),
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }

                                Spacer(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(14.dp)
                                        .background(Color.White.copy(alpha = 0.15f))
                                )

>>>>>>> 6e834ed (Update Taqwahub)
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            brush = Brush.linearGradient(
                                                colors = listOf(Color(0xFFFBBF24), Color(0xFFD4AF37))
                                            )
                                        )
                                        .clickable { showDownloadDialog = true }
                                        .testTag("download_header_button"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = "Offline Quran Manager",
                                        tint = Color(0xFF042F2E),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
<<<<<<< HEAD
=======
                            }
                            
                            // Visual Sync Progress Bar
                            androidx.compose.animation.AnimatedVisibility(
                                visible = viewModel.isSyncingData && viewModel.currentView != "tasbeeh",
                                enter = androidx.compose.animation.expandVertically() + androidx.compose.animation.fadeIn(),
                                exit = androidx.compose.animation.shrinkVertically() + androidx.compose.animation.fadeOut()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF022C22))
                                        .padding(bottom = 2.dp)
                                ) {
                                    LinearProgressIndicator(
                                        color = GoldPrimary,
                                        trackColor = Color(0xFF064E3B),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(3.dp)
                                    )
                                }
                            }
>>>>>>> 6e834ed (Update Taqwahub)
                        }
                    }
                },
                bottomBar = {
                    Surface(
                        color = EmeraldCard,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        NavigationBar(
                            containerColor = EmeraldCard,
                            tonalElevation = 0.dp,
                            modifier = Modifier
                                .navigationBarsPadding()
                                .testTag("app_bottom_bar")
                        ) {
                            val navItems = listOf(
                                Triple("dashboard", "Home", Icons.Default.Home),
                                Triple("quran", "Quran", Icons.Default.Book),
                                Triple("hadith", "Hadith", Icons.Default.MenuBook),
                                Triple("tasks", "Tasks", Icons.Default.CheckCircle),
                                Triple("menu", "Menu", Icons.Default.Menu)
                            )

                            navItems.forEach { item ->
                                NavigationBarItem(
                                    selected = if (item.first == "menu") false else viewModel.currentView == item.first,
                                    onClick = {
                                        if (item.first == "menu") {
                                            scope.launch { drawerState.open() }
                                        } else {
                                            viewModel.navigateToView(item.first)
                                        }
                                    },
                                    label = { Text(item.second, fontSize = 9.sp, fontWeight = FontWeight.Bold) },
<<<<<<< HEAD
                                    icon = { Icon(item.third, contentDescription = item.second) },
=======
                                    icon = {
                                        if (item.first == "menu" && (viewModel.hasLeaderboardUpdate || viewModel.hasUnreadSupportReply)) {
                                            BadgedBox(
                                                badge = {
                                                    Badge(containerColor = GoldPrimary)
                                                }
                                            ) {
                                                Icon(item.third, contentDescription = item.second)
                                            }
                                        } else {
                                            Icon(item.third, contentDescription = item.second)
                                        }
                                    },
>>>>>>> 6e834ed (Update Taqwahub)
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = OnGoldText,
                                        selectedTextColor = GoldPrimary,
                                        indicatorColor = GoldPrimary,
                                        unselectedIconColor = TextGray,
                                        unselectedTextColor = TextGray
                                    )
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(EmeraldBackground)
                        .padding(innerPadding)
                ) {
                    AnimatedContent(
                        targetState = viewModel.currentView,
                        transitionSpec = {
                            (fadeIn(animationSpec = tween(280, easing = FastOutSlowInEasing)) +
                                slideInHorizontally(animationSpec = tween(280, easing = FastOutSlowInEasing)) { width -> width / 5 } +
                                scaleIn(initialScale = 0.97f, animationSpec = tween(280, easing = FastOutSlowInEasing)))
                                .togetherWith(
                                    fadeOut(animationSpec = tween(200, easing = FastOutSlowInEasing)) +
                                        slideOutHorizontally(animationSpec = tween(200, easing = FastOutSlowInEasing)) { width -> -width / 5 }
                                )
                        },
                        label = "screenTransition",
                        modifier = Modifier.fillMaxSize()
                    ) { currentView ->
                        when (currentView) {
                            "dashboard" -> DashboardScreen(viewModel) { destination ->
                                viewModel.navigateToView(destination)
                            }
<<<<<<< HEAD
                            "leaderboard" -> com.example.ui.screens.LeaderboardScreen(viewModel) {
                                viewModel.navigateToView("dashboard")
                            }
                            "quran" -> {
                                if (appConfig.isQuranPageLocked && !isAdmin) {
                                    BlockedScreen(appConfig.quranPageBlockedMessage)
=======
                            "leaderboard" -> {
                                if (viewModel.isModuleLocked("leaderboard") && !isAdmin) {
                                    ModuleLockScreenModal(
                                        moduleTitle = viewModel.getModuleTitle("leaderboard"),
                                        lockCategory = viewModel.getModuleLockCategory("leaderboard"),
                                        reasonMessage = viewModel.getModuleLockReason("leaderboard"),
                                        isAdmin = isAdmin,
                                        onReturnHome = { viewModel.navigateToView("dashboard") }
                                    )
                                } else {
                                    com.example.ui.screens.LeaderboardScreen(viewModel) {
                                        viewModel.navigateToView("dashboard")
                                    }
                                }
                            }
                            "quran" -> {
                                if (viewModel.isModuleLocked("quran") && !isAdmin) {
                                    ModuleLockScreenModal(
                                        moduleTitle = viewModel.getModuleTitle("quran"),
                                        lockCategory = viewModel.getModuleLockCategory("quran"),
                                        reasonMessage = viewModel.getModuleLockReason("quran"),
                                        isAdmin = isAdmin,
                                        onReturnHome = { viewModel.navigateToView("dashboard") }
                                    )
>>>>>>> 6e834ed (Update Taqwahub)
                                } else {
                                    QuranReaderScreen(viewModel) { destination ->
                                        viewModel.navigateToView(destination)
                                    }
                                }
                            }
<<<<<<< HEAD
                            "dua" -> {
                                if (appConfig.isLearnPageLocked && !isAdmin) {
                                    BlockedScreen(appConfig.learnPageBlockedMessage)
=======
                            "quran_guide" -> {
                                com.example.ui.screens.QuranGuideScreen(viewModel) {
                                    viewModel.navigateBack()
                                }
                            }
                            "dua" -> {
                                if (viewModel.isModuleLocked("dua") && !isAdmin) {
                                    ModuleLockScreenModal(
                                        moduleTitle = viewModel.getModuleTitle("dua"),
                                        lockCategory = viewModel.getModuleLockCategory("dua"),
                                        reasonMessage = viewModel.getModuleLockReason("dua"),
                                        isAdmin = isAdmin,
                                        onReturnHome = { viewModel.navigateToView("dashboard") }
                                    )
>>>>>>> 6e834ed (Update Taqwahub)
                                } else {
                                    DuaLibraryScreen(viewModel)
                                }
                            }
                            "tasks" -> {
<<<<<<< HEAD
                                if (appConfig.isToolsPageLocked && !isAdmin) {
                                    BlockedScreen(appConfig.toolsPageBlockedMessage)
=======
                                if (viewModel.isModuleLocked("tasks") && !isAdmin) {
                                    ModuleLockScreenModal(
                                        moduleTitle = viewModel.getModuleTitle("tasks"),
                                        lockCategory = viewModel.getModuleLockCategory("tasks"),
                                        reasonMessage = viewModel.getModuleLockReason("tasks"),
                                        isAdmin = isAdmin,
                                        onReturnHome = { viewModel.navigateToView("dashboard") }
                                    )
>>>>>>> 6e834ed (Update Taqwahub)
                                } else {
                                    TaskTrackerScreen(viewModel) { destination ->
                                        viewModel.navigateToView(destination)
                                    }
                                }
                            }
                            "hadith" -> {
<<<<<<< HEAD
                                if (appConfig.isLearnPageLocked && !isAdmin) {
                                    BlockedScreen(appConfig.learnPageBlockedMessage)
=======
                                if (viewModel.isModuleLocked("hadith") && !isAdmin) {
                                    ModuleLockScreenModal(
                                        moduleTitle = viewModel.getModuleTitle("hadith"),
                                        lockCategory = viewModel.getModuleLockCategory("hadith"),
                                        reasonMessage = viewModel.getModuleLockReason("hadith"),
                                        isAdmin = isAdmin,
                                        onReturnHome = { viewModel.navigateToView("dashboard") }
                                    )
>>>>>>> 6e834ed (Update Taqwahub)
                                } else {
                                    HadithExplorerScreen(viewModel)
                                }
                            }
                            "settings" -> com.example.ui.screens.SettingsScreen(viewModel)
                            "tasbeeh" -> {
<<<<<<< HEAD
                                if (appConfig.isToolsPageLocked && !isAdmin) {
                                    BlockedScreen(appConfig.toolsPageBlockedMessage)
=======
                                if (viewModel.isModuleLocked("tasbeeh") && !isAdmin) {
                                    ModuleLockScreenModal(
                                        moduleTitle = viewModel.getModuleTitle("tasbeeh"),
                                        lockCategory = viewModel.getModuleLockCategory("tasbeeh"),
                                        reasonMessage = viewModel.getModuleLockReason("tasbeeh"),
                                        isAdmin = isAdmin,
                                        onReturnHome = { viewModel.navigateToView("dashboard") }
                                    )
>>>>>>> 6e834ed (Update Taqwahub)
                                } else {
                                    TasbeehCounterScreen(viewModel)
                                }
                            }
                            "names" -> {
<<<<<<< HEAD
                                if (appConfig.isLearnPageLocked && !isAdmin) {
                                    BlockedScreen(appConfig.learnPageBlockedMessage)
=======
                                if (viewModel.isModuleLocked("names") && !isAdmin) {
                                    ModuleLockScreenModal(
                                        moduleTitle = viewModel.getModuleTitle("names"),
                                        lockCategory = viewModel.getModuleLockCategory("names"),
                                        reasonMessage = viewModel.getModuleLockReason("names"),
                                        isAdmin = isAdmin,
                                        onReturnHome = { viewModel.navigateToView("dashboard") }
                                    )
>>>>>>> 6e834ed (Update Taqwahub)
                                } else {
                                    NamesOfAllahScreen(viewModel)
                                }
                            }
                            "zakat" -> {
<<<<<<< HEAD
                                if (appConfig.isToolsPageLocked && !isAdmin) {
                                    BlockedScreen(appConfig.toolsPageBlockedMessage)
=======
                                if (viewModel.isModuleLocked("zakat") && !isAdmin) {
                                    ModuleLockScreenModal(
                                        moduleTitle = viewModel.getModuleTitle("zakat"),
                                        lockCategory = viewModel.getModuleLockCategory("zakat"),
                                        reasonMessage = viewModel.getModuleLockReason("zakat"),
                                        isAdmin = isAdmin,
                                        onReturnHome = { viewModel.navigateToView("dashboard") }
                                    )
>>>>>>> 6e834ed (Update Taqwahub)
                                } else {
                                    ZakatCalculatorScreen(viewModel)
                                }
                            }
                            "qibla" -> {
<<<<<<< HEAD
                                if (appConfig.isToolsPageLocked && !isAdmin) {
                                    BlockedScreen(appConfig.toolsPageBlockedMessage)
=======
                                if (viewModel.isModuleLocked("qibla") && !isAdmin) {
                                    ModuleLockScreenModal(
                                        moduleTitle = viewModel.getModuleTitle("qibla"),
                                        lockCategory = viewModel.getModuleLockCategory("qibla"),
                                        reasonMessage = viewModel.getModuleLockReason("qibla"),
                                        isAdmin = isAdmin,
                                        onReturnHome = { viewModel.navigateToView("dashboard") }
                                    )
>>>>>>> 6e834ed (Update Taqwahub)
                                } else {
                                    QiblaFinderScreen(viewModel)
                                }
                            }
                            "calendar" -> {
<<<<<<< HEAD
                                if (appConfig.isToolsPageLocked && !isAdmin) {
                                    BlockedScreen(appConfig.toolsPageBlockedMessage)
=======
                                if (viewModel.isModuleLocked("calendar") && !isAdmin) {
                                    ModuleLockScreenModal(
                                        moduleTitle = viewModel.getModuleTitle("calendar"),
                                        lockCategory = viewModel.getModuleLockCategory("calendar"),
                                        reasonMessage = viewModel.getModuleLockReason("calendar"),
                                        isAdmin = isAdmin,
                                        onReturnHome = { viewModel.navigateToView("dashboard") }
                                    )
>>>>>>> 6e834ed (Update Taqwahub)
                                } else {
                                    IslamicCalendarScreen(viewModel)
                                }
                            }
<<<<<<< HEAD
                             "donate" -> com.example.ui.screens.DonateScreen(viewModel)
                            "user_complaints" -> UserComplaintsScreen(viewModel)
=======
                            "donate" -> {
                                if (viewModel.isModuleLocked("donate") && !isAdmin) {
                                    ModuleLockScreenModal(
                                        moduleTitle = viewModel.getModuleTitle("donate"),
                                        lockCategory = viewModel.getModuleLockCategory("donate"),
                                        reasonMessage = viewModel.getModuleLockReason("donate"),
                                        isAdmin = isAdmin,
                                        onReturnHome = { viewModel.navigateToView("dashboard") }
                                    )
                                } else {
                                    com.example.ui.screens.DonateScreen(viewModel)
                                }
                            }
                            "user_complaints" -> {
                                if (viewModel.isModuleLocked("complaints") && !isAdmin) {
                                    ModuleLockScreenModal(
                                        moduleTitle = viewModel.getModuleTitle("complaints"),
                                        lockCategory = viewModel.getModuleLockCategory("complaints"),
                                        reasonMessage = viewModel.getModuleLockReason("complaints"),
                                        isAdmin = isAdmin,
                                        onReturnHome = { viewModel.navigateToView("dashboard") }
                                    )
                                } else {
                                    UserComplaintsScreen(viewModel)
                                }
                            }
>>>>>>> 6e834ed (Update Taqwahub)
                            "admin_complaints" -> {
                                if (viewModel.isAdmin) {
                                    AdminComplaintsListScreen(viewModel)
                                } else {
                                    viewModel.currentView = "dashboard"
                                }
                            }
                            "admin_dashboard" -> {
                                if (viewModel.isAdmin) {
                                    AdminDashboardScreen(viewModel)
                                } else {
                                    viewModel.currentView = "dashboard"
                                }
                            }
                            "profile" -> ProfileScreen(viewModel) {
                                // Handle sign out
                                try {
                                    FirebaseAuth.getInstance().signOut()
                                } catch (e: Throwable) {
                                    Log.e("MainActivity", "Firebase signout failed: ${e.message}")
                                }
                                viewModel.disableGuestMode()
                                viewModel.clearLocalDataAndPreferences()
                                viewModel.currentUser = null
                                viewModel.currentView = "dashboard"
                            }
                        }
                    }

                    if (showUsernameOverlay) {
                        UsernameMandatorySetupOverlay(
                            currentStats = userStats,
                            checkUsernameAvailability = { usernameStr, callback ->
                                viewModel.checkUsernameAvailability(usernameStr, firebaseUser.uid, callback)
                            },
                            onSaveUsername = { chosenUsername ->
                                val updated = userStats.copy(username = chosenUsername)
                                viewModel.setStats(updated)
                                Toast.makeText(context, "Username set successfully!", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }

            // Offline Download Dialog Overlay
            if (showDownloadDialog) {
                com.example.ui.screens.OfflineDownloadDialog(viewModel = viewModel) {
                    showDownloadDialog = false
                }
            }
<<<<<<< HEAD
=======

            // Duolingo-Grade Streak Details & Shields Dialog Overlay
            if (viewModel.showStreakModal) {
                com.example.ui.screens.StreakDetailsDialog(
                    viewModel = viewModel,
                    stats = userStats,
                    onDismiss = { viewModel.closeStreakModal() }
                )
            }

            // Streak Shield Saved Celebration Dialog Overlay
            if (viewModel.showShieldActivatedCelebration) {
                com.example.ui.screens.StreakShieldActivatedCelebrationDialog(
                    shieldSavedDate = viewModel.shieldSavedDateStr,
                    shieldsRemaining = userStats.streakShields,
                    onDismiss = { viewModel.dismissShieldCelebration() }
                )
            }
>>>>>>> 6e834ed (Update Taqwahub)
        }

        if (!hasSeenBismillahWelcome) {
            com.example.ui.screens.NewUserBismillahWelcomeScreen(
                bismillahMessage = appConfig.welcomeBismillahMessage,
                onEnter = {
                    sharedPrefs.edit().putBoolean("has_seen_bismillah_welcome", true).apply()
                    hasSeenBismillahWelcome = true
                }
            )
        }
    }
}
}

@Composable
fun UpwardTriangle(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(size.width / 2f, 0f)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(path = path, color = color)
    }
}

@Composable
fun NetworkSignalIcon(status: TaqwaNetworkStatusInfo, modifier: Modifier = Modifier) {
    val color = when (status.condition) {
        TaqwaNetworkCondition.EXCELLENT, TaqwaNetworkCondition.GOOD -> Color(0xFF10B981) // Green
        TaqwaNetworkCondition.MEDIUM -> Color(0xFFFBBF24) // Yellow
        TaqwaNetworkCondition.BAD -> Color(0xFFEF4444) // Red
    }

    when (status.type) {
        TaqwaNetworkType.AIRPLANE -> {
            Icon(
                imageVector = Icons.Default.AirplanemodeActive,
                contentDescription = "Airplane Mode",
                tint = Color(0xFFD4AF37), // yellow-gold
                modifier = modifier
            )
        }
        TaqwaNetworkType.NONE -> {
            Icon(
                imageVector = Icons.Default.WifiOff,
                contentDescription = "No Network",
                tint = Color(0xFFEF4444),
                modifier = modifier
            )
        }
        TaqwaNetworkType.WIFI -> {
            Canvas(modifier = modifier.size(18.dp)) {
                val width = size.width
                val height = size.height
                val center = Offset(width / 2f, height - 2.dp.toPx())
                
                // Draw 4 levels (dot + 3 arcs)
                val strokeWidth = 2.dp.toPx()
                val activeLevel = status.signalLevel
                
                // Dot
                drawCircle(
                    color = color,
                    radius = 2.dp.toPx(),
                    center = center
                )
                
                val maxRadius = width / 2f
                for (i in 1..3) {
                    val radius = (maxRadius * (i / 3f))
                    val isArcActive = activeLevel >= (i + 1)
                    val arcColor = if (isArcActive) color else color.copy(alpha = 0.25f)
                    
                    drawArc(
                        color = arcColor,
                        startAngle = 220f,
                        sweepAngle = 100f,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = strokeWidth,
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    )
                }
            }
        }
        TaqwaNetworkType.CELLULAR -> {
            Canvas(modifier = modifier.size(18.dp)) {
                val width = size.width
                val height = size.height
                
                val barCount = 4
                val spacing = 2.dp.toPx()
                val totalSpacing = spacing * (barCount - 1)
                val barWidth = (width - totalSpacing) / barCount
                val activeLevel = status.signalLevel
                
                for (i in 0 until barCount) {
                    val isBarActive = activeLevel >= (i + 1)
                    val barColor = if (isBarActive) color else color.copy(alpha = 0.25f)
                    
                    val barHeight = height * ((i + 1) / barCount.toFloat())
                    val left = i * (barWidth + spacing)
                    val top = height - barHeight
                    
                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(left, top),
                        size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx())
                    )
                }
            }
        }
    }
}

fun isTempEmail(email: String): Boolean {
    val tempDomains = listOf(
        "mailinator.com", "yopmail.com", "tempmail.com", "guerrillamail.com",
        "dispostable.com", "getairmail.com", "throwawaymail.com", "temp-mail.org",
        "tempmailaddress.com", "sharklasers.com", "guerrillamailblock.com", "dispostable.com",
        "10minutemail.com", "maildrop.cc", "trashmail.com", "fakeinbox.com", "disposable.com",
        "tempemail.co", "disposable.org", "disposable.net", "disposable.co"
    )
    val domain = email.substringAfter("@").trim().lowercase()
    return tempDomains.any { domain == it || domain.endsWith(".$it") }
}

fun getFriendlyAuthErrorMessage(e: Throwable): String {
    val message = e.message ?: ""
    return when {
        message.contains("WEAK_PASSWORD", ignoreCase = true) || message.contains("password is weak", ignoreCase = true) ->
            "Your password is too simple. Please choose a password with at least 8 characters including mixed characters."
        message.contains("EMAIL_ALREADY_IN_USE", ignoreCase = true) || message.contains("already in use", ignoreCase = true) ->
            "This email is already registered. Please go back to SIGN IN or choose another email."
        message.contains("INVALID_EMAIL", ignoreCase = true) || message.contains("invalid email", ignoreCase = true) ->
            "The email format seems incorrect. Please clarify your email name@domain.com."
        message.contains("USER_NOT_FOUND", ignoreCase = true) || message.contains("no user record", ignoreCase = true) ->
            "No account found with this email. Please check spelling or register via SIGN UP first."
<<<<<<< HEAD
        message.contains("WRONG_PASSWORD", ignoreCase = true) || message.contains("wrong password", ignoreCase = true) ->
            "Incorrect password. If you forgot your password, please tap on 'Forgot Password?' to reset."
=======
        message.contains("WRONG_PASSWORD", ignoreCase = true) || message.contains("wrong password", ignoreCase = true) ||
        message.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) || message.contains("supplied auth credential is incorrect", ignoreCase = true) ||
        message.contains("credential is incorrect", ignoreCase = true) || message.contains("INVALID_CREDENTIAL", ignoreCase = true) ->
            "Incorrect email or password. Please verify your credentials or tap 'Forgot Password' to reset."
>>>>>>> 6e834ed (Update Taqwahub)
        message.contains("network", ignoreCase = true) || message.contains("timed out", ignoreCase = true) || message.contains("connection", ignoreCase = true) ->
            "Spiritual link disrupted! Please verify your internet or network connection."
        message.contains("API key", ignoreCase = true) ->
            "Devotion Sync mode active! Firebase setup missing - please continue safely with Offline Guest Access."
        else -> "Spiritual Connection Notice: ${e.localizedMessage ?: "Please verify your input fields or use Guest Offline Access."}"
    }
}

@Composable
fun PasswordStrengthIndicator(password: String) {
    val strength = remember(password) {
        if (password.isEmpty()) 0
        else {
            var score = 1
            if (password.length >= 8) score++
            if (password.any { it.isUpperCase() } && password.any { it.isLowerCase() }) score++
            if (password.any { it.isDigit() }) score++
            if (password.any { !it.isLetterOrDigit() }) score++
            score.coerceAtMost(4)
        }
    }
    
    val (label, color) = when (strength) {
        0 -> "" to Color.Transparent
        1 -> "WEAK" to Color(0xFFEF4444)
        2 -> "FAIR" to Color(0xFFF59E0B)
        3 -> "GOOD" to Color(0xFF10B981)
        else -> "STRONG" to GoldPrimary
    }
    
    if (password.isNotEmpty()) {
        Spacer(modifier = Modifier.height(12.dp))
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PASSWORD STRENGTH",
                    color = TextGray.copy(alpha = 0.5f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = label,
                    color = color,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (i in 1..4) {
                    val barColor = if (i <= strength) color else Color(0xFF1B4E38).copy(alpha = 0.4f)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(barColor)
                    )
                }
            }
        }
    }
}

fun suggestThemeUsername(): String {
    val prefixes = listOf(
        "servant_of_allah",
        "taqwa_seeker",
        "quran_soul",
        "mumin_heart",
        "sincere_servant",
        "nur_soul",
        "jannah_aspirant",
        "dhikr_soul",
        "spirit_of_sabr",
        "all_for_islam",
        "deen_follower",
        "servant_heart",
        "humble_servant",
        "guided_soul"
    )
    val randomNum = (100..999).random()
    return "${prefixes.random()}_$randomNum"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompleteProfileSetupScreen(
    initialName: String,
    initialEmail: String,
    initialGender: String,
    initialSectOrCast: String,
    initialUsername: String = "",
    checkUsernameAvailability: (String, (Boolean) -> Unit) -> Unit,
    onSignOut: () -> Unit,
    onComplete: (correctedName: String, username: String, gender: String, sect: String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var username by remember { mutableStateOf(initialUsername) }
    var sect by remember { mutableStateOf(initialSectOrCast) }
    var isMale by remember { mutableStateOf(initialGender.equals("Male", ignoreCase = true)) }

    // Real-time username check state
    var isCheckingUsername by remember { mutableStateOf(false) }
    var usernameError by remember { mutableStateOf<String?>(null) }
    var isUsernameAvailable by remember { mutableStateOf(false) }

    // Validate and check availability
    LaunchedEffect(username) {
        val trimmed = username.trim().lowercase()
        if (trimmed.isEmpty()) {
            usernameError = null
            isUsernameAvailable = false
            return@LaunchedEffect
        }
        val regex = Regex("^[a-zA-Z0-9_]{3,20}$")
        if (!regex.matches(trimmed)) {
            usernameError = "3-20 chars, letters, numbers & underscores only"
            isUsernameAvailable = false
            return@LaunchedEffect
        }
        
        isCheckingUsername = true
        usernameError = null
        checkUsernameAvailability(trimmed) { available ->
            isCheckingUsername = false
            isUsernameAvailable = available
            if (!available) {
                usernameError = "Username is already taken"
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(EmeraldBackground)
            .safeDrawingPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            // Unified high-contrast icon container
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(Color(0xFF154430))
                    .border(1.dp, GoldPrimary, androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profile Setup",
                    tint = GoldPrimary,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "COMPLETE YOUR PROFILE",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            
            Text(
                text = "We fetched your authentication link. Please customize your identity stats for tailored prayer alerts.",
                color = TextGray,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = EmeraldCard),
                border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.1f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    val fieldColors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedBorderColor = Color(0xFF1B4E38),
                        unfocusedBorderColor = Color(0xFF1B4E38),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )

                    Text("YOUR NAME (FROM GMAIL / SSO)", color = TextGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth().testTag("profile_setup_name_field"),
                        placeholder = { Text("Display Name", color = TextGray.copy(alpha=0.5f)) },
                        shape = RoundedCornerShape(16.dp),
                        colors = fieldColors
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text("UNIQUE USERNAME", color = TextGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = username,
                        onValueChange = { input ->
                            username = input.filter { it.isLetterOrDigit() || it == '_' }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("profile_setup_username_field"),
                        placeholder = { Text("e.g. servant_of_allah", color = TextGray.copy(alpha=0.5f)) },
                        leadingIcon = {
                            Text(
                                text = "@",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    username = suggestThemeUsername()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Casino,
                                    contentDescription = "Suggest Username",
                                    tint = GoldPrimary
                                )
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = fieldColors,
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isCheckingUsername) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                color = GoldPrimary,
                                strokeWidth = 1.5.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Checking availability...", color = GoldPrimary.copy(alpha = 0.8f), fontSize = 11.sp)
                        } else if (usernameError != null) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Error",
                                tint = AlertRed,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(usernameError!!, color = AlertRed, fontSize = 11.sp)
                        } else if (isUsernameAvailable) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Available",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Username is available!", color = Color(0xFF10B981), fontSize = 11.sp)
                        } else {
                            Text("Choose a unique tag for the Leaderboard & Admin features.", color = TextGray.copy(alpha = 0.6f), fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text("GENDER", color = TextGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .border(1.dp, Color(0xFF1B4E38), RoundedCornerShape(16.dp)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp, topEnd = if (isMale) 16.dp else 0.dp, bottomEnd = if (isMale) 16.dp else 0.dp))
                                .background(if (isMale) Color(0xFF154430) else Color.Transparent)
                                .clickable { isMale = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("MALE", color = if (isMale) GoldPrimary else TextGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp, topStart = if (!isMale) 16.dp else 0.dp, bottomStart = if (!isMale) 16.dp else 0.dp))
                                .background(if (!isMale) Color(0xFF154430) else Color.Transparent)
                                .clickable { isMale = false },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("FEMALE", color = if (!isMale) GoldPrimary else TextGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text("SECT / CAST", color = TextGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = sect,
                        onValueChange = { sect = it },
                        modifier = Modifier.fillMaxWidth().testTag("profile_setup_sect_field"),
                        placeholder = { Text("e.g. Sunni, Shia", color = TextGray.copy(alpha=0.5f)) },
                        shape = RoundedCornerShape(16.dp),
                        colors = fieldColors
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            if (isUsernameAvailable && usernameError == null) {
                                onComplete(name.trim(), username.trim().lowercase(), if (isMale) "Male" else "Female", sect.trim())
                            }
                        },
                        enabled = isUsernameAvailable && usernameError == null && name.trim().isNotEmpty(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("profile_setup_complete_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldPrimary,
                            disabledContainerColor = GoldPrimary.copy(alpha = 0.3f)
                        )
                    ) {
                        Text("SAVE AND CONTINUE", color = if (isUsernameAvailable) EmeraldCard else EmeraldCard.copy(alpha = 0.5f), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = onSignOut,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFF1B4E38)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Text("SIGN OUT / CANCEL", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun BlockedScreen(message: String) {
    Box(
        modifier = Modifier.fillMaxSize().background(EmeraldBackground).padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(Icons.Default.Lock, contentDescription = "Locked", tint = GoldPrimary, modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Access Blocked",
                fontSize = 22.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                message,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsernameMandatorySetupOverlay(
    currentStats: UserStatsEntity,
    checkUsernameAvailability: (String, (Boolean) -> Unit) -> Unit,
    onSaveUsername: (String) -> Unit
) {
    var usernameInput by remember { mutableStateOf("") }
    var isCheckingUsername by remember { mutableStateOf(false) }
    var usernameError by remember { mutableStateOf<String?>(null) }
    var isUsernameAvailable by remember { mutableStateOf(false) }

    LaunchedEffect(usernameInput) {
        val trimmed = usernameInput.trim().lowercase()
        if (trimmed.isEmpty()) {
            usernameError = null
            isUsernameAvailable = false
            return@LaunchedEffect
        }
        val regex = Regex("^[a-zA-Z0-9_]{3,20}$")
        if (!regex.matches(trimmed)) {
            usernameError = "3-20 chars, letters, numbers & underscores only"
            isUsernameAvailable = false
            return@LaunchedEffect
        }

        isCheckingUsername = true
        usernameError = null
        checkUsernameAvailability(trimmed) { available ->
            isCheckingUsername = false
            isUsernameAvailable = available
            if (!available) {
                usernameError = "Username is already taken"
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .clickable(enabled = false) {}, // Block click-through
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = EmeraldCard),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.NewReleases,
                    contentDescription = "Upgrade Announcement",
                    tint = GoldPrimary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "UPGRADING TAQWAHUB",
                    color = GoldPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "To take your experience to the next level, we are introducing unique usernames! This ensures you are uniquely identified on the Global Leaderboard and enables seamless interactions.",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(20.dp))

                val fieldColors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedBorderColor = Color(0xFF1B4E38),
                    unfocusedBorderColor = Color(0xFF1B4E38),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )

                OutlinedTextField(
                    value = usernameInput,
                    onValueChange = { input ->
                        usernameInput = input.filter { it.isLetterOrDigit() || it == '_' }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("mandatory_username_field"),
                    placeholder = { Text("Enter username", color = TextGray.copy(alpha=0.5f), fontSize = 13.sp) },
                    leadingIcon = {
                        Text(
                            text = "@",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                usernameInput = suggestThemeUsername()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Casino,
                                contentDescription = "Suggest Username",
                                tint = GoldPrimary
                            )
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = fieldColors,
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isCheckingUsername) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            color = GoldPrimary,
                            strokeWidth = 1.5.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Checking availability...", color = GoldPrimary.copy(alpha = 0.8f), fontSize = 11.sp)
                    } else if (usernameError != null) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error",
                            tint = AlertRed,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(usernameError!!, color = AlertRed, fontSize = 11.sp)
                    } else if (isUsernameAvailable) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Available",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Username is available!", color = Color(0xFF10B981), fontSize = 11.sp)
                    } else {
                        Text("Letters, numbers, or underscores (3-20 chars)", color = TextGray.copy(alpha = 0.6f), fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (isUsernameAvailable && usernameError == null) {
                            onSaveUsername(usernameInput.trim().lowercase())
                        }
                    },
                    enabled = isUsernameAvailable && usernameError == null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("mandatory_username_save_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        disabledContainerColor = GoldPrimary.copy(alpha = 0.3f)
                    )
                ) {
                    Text(
                        text = "SAVE & CONTINUE",
                        color = if (isUsernameAvailable) EmeraldCard else EmeraldCard.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
