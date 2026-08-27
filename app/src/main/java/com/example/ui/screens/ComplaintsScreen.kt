package com.example.ui.screens

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.BugReport
import com.example.ui.theme.*
import com.example.viewmodel.TaqwaViewModel
import java.text.SimpleDateFormat
import java.util.*

data class ConversationItem(
    val userKey: String,
    val userEmail: String,
    val latestMessage: String,
    val latestTimestamp: Long,
    val pendingCount: Int,
    val reports: List<BugReport>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserComplaintsScreen(viewModel: TaqwaViewModel) {
    val context = LocalContext.current
    val userBugs = viewModel.userBugReportsList
    val listState = rememberLazyListState()

    // Screen state
    var typedMessage by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("Bug") } // Bug, Suggestion, Other
    var attachedScreenshotBase64 by remember { mutableStateOf("") }
    var attachedUri by remember { mutableStateOf<Uri?>(null) }
    var zoomImageUrl by remember { mutableStateOf<String?>(null) }
    var isSending by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.fetchUserBugReportsFromFirestore()
    }

    DisposableEffect(Unit) {
        viewModel.hasUnreadSupportReply = false
        viewModel.saveLastSeenComplaintsTime()
        onDispose {
            viewModel.hasUnreadSupportReply = false
            viewModel.saveLastSeenComplaintsTime()
        }
    }

    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            attachedUri = uri
            val base64 = compressUriToBase64(context, uri)
            if (base64 != null) {
                attachedScreenshotBase64 = base64
            } else {
                Toast.makeText(context, "Failed to load image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Scroll to bottom when new messages arrive
    LaunchedEffect(userBugs.size) {
        if (userBugs.isNotEmpty()) {
            listState.animateScrollToItem(userBugs.size - 1)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(EmeraldBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize().imePadding()) {
            // Header TopBar
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(GoldPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SupportAgent,
                                contentDescription = "Support",
                                tint = GoldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Help & Support",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Active Chat with Support Team",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 11.sp
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.currentView = "dashboard" }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = GoldPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.fetchUserBugReportsFromFirestore()
                        Toast.makeText(context, "Chat synced", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sync",
                            tint = GoldPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )

            Divider(color = GoldPrimary.copy(alpha = 0.15f), thickness = 1.dp)

            // Chat Messages Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (userBugs.isEmpty()) {
                    // Beautiful Empty Chat State
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0F2E23)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = "No Chat",
                                tint = GoldPrimary,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No messages yet",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Feel free to write a complaint, report a bug, or give suggestions. Our team is here to assist you!",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                } else {
                    // Chat Timeline (reversed sorting so oldest is top, newest is bottom)
                    val sortedBugs = remember(userBugs) { userBugs.sortedBy { it.timestamp } }
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        sortedBugs.forEach { report ->
                            item {
                                UserChatBubble(
                                    report = report,
                                    onImageClick = { zoomImageUrl = report.imageUrl }
                                )
                            }
                            if (report.adminReply.isNotEmpty()) {
                                item {
                                    AdminChatBubble(
                                        replyText = report.adminReply,
                                        replyTimestamp = report.adminReplyTimestamp,
                                        userTimestamp = report.timestamp,
                                        onImageClick = { zoomImageUrl = report.imageUrl }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Compose Area (WhatsApp style)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(EmeraldDark)
                    .border(width = 1.dp, color = GoldPrimary.copy(alpha = 0.15f), shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .navigationBarsPadding()
                    .padding(bottom = 8.dp)
            ) {
                // Category Selector Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Topic:",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    listOf("Bug", "Suggestion", "Other").forEach { type ->
                        val isSelected = selectedType == type
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) GoldPrimary else Color(0xFF1B4E38))
                                .clickable { selectedType = type }
                                .padding(horizontal = 12.dp, vertical = 4.dp),
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

                Divider(color = Color.White.copy(alpha = 0.08f), thickness = 1.dp)

                // Image Attachment Preview bar (above text input)
                if (attachedScreenshotBase64.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val bitmap = remember(attachedScreenshotBase64) { base64ToBitmap(attachedScreenshotBase64) }
                        if (bitmap != null) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, GoldPrimary, RoundedCornerShape(8.dp))
                            ) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Attachment preview",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                // Delete attachment button
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(Color.Red)
                                        .clickable {
                                            attachedScreenshotBase64 = ""
                                            attachedUri = null
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove attachment",
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Screenshot attached",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Chat message input row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Attachment button
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF133628))
                            .clickable { imageLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = "Attach picture",
                            tint = GoldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // TextField Input
                    OutlinedTextField(
                        value = typedMessage,
                        onValueChange = { typedMessage = it },
                        placeholder = { Text("Type a complaint...", color = Color.White.copy(alpha = 0.4f), fontSize = 14.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(max = 120.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = EmeraldCard,
                            unfocusedContainerColor = EmeraldCard,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = GoldPrimary.copy(alpha = 0.6f),
                            unfocusedBorderColor = GoldPrimary.copy(alpha = 0.15f)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Circular send button
                    val isSendEnabled = (typedMessage.trim().isNotEmpty() || attachedScreenshotBase64.isNotEmpty()) && !isSending
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (isSendEnabled) GoldPrimary else Color(0xFF1F3D32))
                            .clickable(enabled = isSendEnabled) {
                                isSending = true
                                val subjectText = "[$selectedType] support query"
                                val descText = typedMessage.trim().ifEmpty { "Sent an attachment screenshot." }
                                
                                viewModel.submitBugReport(
                                    subject = subjectText,
                                    description = descText,
                                    type = selectedType,
                                    imageUrl = attachedScreenshotBase64
                                ) { success, errorMsg ->
                                    isSending = false
                                    if (success) {
                                        Toast.makeText(context, "Complaint sent successfully!", Toast.LENGTH_SHORT).show()
                                        typedMessage = ""
                                        attachedScreenshotBase64 = ""
                                        attachedUri = null
                                    } else {
                                        Toast.makeText(context, "Failed: ${errorMsg ?: "Unknown error"}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = if (isSendEnabled) Color(0xFF021612) else Color.White.copy(alpha = 0.3f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Full Screen Image zoom overlay
        if (zoomImageUrl != null) {
            ImageZoomDialog(imageUrl = zoomImageUrl!!) { zoomImageUrl = null }
        }
    }
}

private fun formatChatTimestamp(rawTs: Long): String {
    if (rawTs <= 0L) return ""
    val ts = if (rawTs < 10_000_000_000L) rawTs * 1000L else rawTs
    val now = Calendar.getInstance()
    val msgTime = Calendar.getInstance().apply { timeInMillis = ts }

    val isSameYear = now.get(Calendar.YEAR) == msgTime.get(Calendar.YEAR)
    val isSameDay = isSameYear && now.get(Calendar.DAY_OF_YEAR) == msgTime.get(Calendar.DAY_OF_YEAR)
    val isYesterday = isSameYear && (now.get(Calendar.DAY_OF_YEAR) - msgTime.get(Calendar.DAY_OF_YEAR) == 1)

    val timeSdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return when {
        isSameDay -> timeSdf.format(msgTime.time)
        isYesterday -> "Yesterday, " + timeSdf.format(msgTime.time)
        isSameYear -> SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(msgTime.time)
        else -> SimpleDateFormat("dd/MM/yy, hh:mm a", Locale.getDefault()).format(msgTime.time)
    }
}

@Composable
fun UserChatBubble(report: BugReport, onImageClick: () -> Unit) {
    val rawTime = remember(report.timestamp) {
        if (report.timestamp > 0L) report.timestamp else System.currentTimeMillis()
    }
    val formattedTime = remember(rawTime) { formatChatTimestamp(rawTime) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.End
    ) {
        Box(
            modifier = Modifier
                .widthIn(min = 90.dp, max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 14.dp,
                        topEnd = 3.dp,
                        bottomStart = 14.dp,
                        bottomEnd = 14.dp
                    )
                )
                .background(Color(0xFF0F4E3B))
                .border(
                    width = 0.5.dp,
                    color = GoldPrimary.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(
                        topStart = 14.dp,
                        topEnd = 3.dp,
                        bottomStart = 14.dp,
                        bottomEnd = 14.dp
                    )
                )
                .padding(horizontal = 10.dp, vertical = 7.dp)
        ) {
            Column {
                // Category Badge inside bubble
                Text(
                    text = report.type.uppercase(Locale.getDefault()),
                    color = GoldPrimary,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(bottom = 2.dp)
                )

                // Optional screenshot attachment
                if (report.imageUrl.isNotEmpty()) {
                    val bitmap = remember(report.imageUrl) { base64ToBitmap(report.imageUrl) }
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Attached screenshot",
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 160.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onImageClick() }
                                .padding(bottom = 4.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                // Message description
                Text(
                    text = report.description,
                    color = Color.White,
                    fontSize = 13.5.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(3.dp))

                // Time & Status row (WhatsApp style bottom-right)
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = formattedTime,
                        color = Color.White.copy(alpha = 0.65f),
                        fontSize = 9.5.sp
                    )

                    // WhatsApp style Status Icons
                    when (report.status) {
                        "Pending" -> Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = "Pending",
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(11.dp)
                        )
                        "In Progress" -> Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Active",
                            tint = Color(0xFFFBBF24),
                            modifier = Modifier.size(11.dp)
                        )
                        "Resolved" -> Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = "Resolved",
                            tint = Color(0xFF34D399),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminChatBubble(
    replyText: String,
    replyTimestamp: Long,
    userTimestamp: Long,
    onImageClick: () -> Unit
) {
    val rawTime = remember(replyTimestamp, userTimestamp) {
        if (replyTimestamp > 0L) replyTimestamp
        else if (userTimestamp > 0L) userTimestamp + 60000L
        else System.currentTimeMillis()
    }
    val formattedTime = remember(rawTime) { formatChatTimestamp(rawTime) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(min = 90.dp, max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 3.dp,
                        topEnd = 14.dp,
                        bottomStart = 14.dp,
                        bottomEnd = 14.dp
                    )
                )
                .background(Color(0xFF133C30))
                .border(
                    width = 0.5.dp,
                    color = GoldPrimary.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(
                        topStart = 3.dp,
                        topEnd = 14.dp,
                        bottomStart = 14.dp,
                        bottomEnd = 14.dp
                    )
                )
                .padding(horizontal = 10.dp, vertical = 7.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.padding(bottom = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = "Admin Verified",
                        tint = GoldPrimary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "SUPPORT TEAM REPLY",
                        color = GoldPrimary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                Text(
                    text = replyText,
                    color = Color.White,
                    fontSize = 13.5.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(3.dp))

                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = formattedTime,
                        color = Color.White.copy(alpha = 0.65f),
                        fontSize = 9.5.sp
                    )
                    Icon(
                        imageVector = Icons.Default.DoneAll,
                        contentDescription = "Delivered",
                        tint = GoldPrimary.copy(alpha = 0.8f),
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ImageZoomDialog(imageUrl: String, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable { onDismiss() }
        ) {
            val bitmap = remember(imageUrl) { base64ToBitmap(imageUrl) }
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Zoomed screenshot",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentScale = ContentScale.Fit
                )
            } else {
                Text(
                    "Image not available",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // Close button at top right
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .statusBarsPadding()
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close zoom",
                    tint = Color.White
                )
            }
        }
    }
}

// ==========================================
// ADMIN SUPPORT COMPLAINTS SYSTEM SCREEN
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminComplaintsListScreen(viewModel: TaqwaViewModel) {
    val context = LocalContext.current
    val allReports = viewModel.bugReportsList

    // Automatically fetch bug reports on screen entry
    LaunchedEffect(Unit) {
        viewModel.fetchBugReportsFromFirestore()
    }

    // Grouping reports by user email or userId to form standard chats
    val groupedConversations = remember(allReports) {
        allReports.groupBy { it.userEmail.ifEmpty { it.userId } }
            .map { (userKey, reports) ->
                val sorted = reports.sortedBy { it.timestamp }
                val latestReport = sorted.last()
                val pendingCount = reports.count { it.status == "Pending" }
                ConversationItem(
                    userKey = userKey,
                    userEmail = reports.firstOrNull { it.userEmail.isNotEmpty() }?.userEmail ?: userKey,
                    latestMessage = latestReport.description,
                    latestTimestamp = latestReport.timestamp,
                    pendingCount = pendingCount,
                    reports = sorted
                )
            }.sortedByDescending { it.latestTimestamp }
    }

    var selectedUserKey by remember { mutableStateOf<String?>(null) }
    var showUserInfoDialog by remember { mutableStateOf<ConversationItem?>(null) }
    var zoomImageUrl by remember { mutableStateOf<String?>(null) }

    if (selectedUserKey == null) {
        // Conversation List Screen (WhatsApp Style Inbox)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(EmeraldBackground)
        ) {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(GoldPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.RateReview,
                                contentDescription = "Complaints Inbox",
                                tint = GoldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Support Tickets 👑",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "${groupedConversations.size} Conversations Total",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 11.sp
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.currentView = "dashboard" }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = GoldPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.fetchBugReportsFromFirestore()
                        Toast.makeText(context, "Inbox refreshed", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sync",
                            tint = GoldPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )

            Divider(color = GoldPrimary.copy(alpha = 0.15f), thickness = 1.dp)

            if (groupedConversations.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.MarkChatRead,
                        contentDescription = "No complaints",
                        tint = GoldPrimary.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "All clear!",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "No user complaints or bug reports found in system.",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(groupedConversations) { convo ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedUserKey = convo.userKey }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // User circular avatar with initial
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF133628)),
                                contentAlignment = Alignment.Center
                            ) {
                                val letter = convo.userEmail.firstOrNull()?.toString()?.uppercase() ?: "U"
                                Text(
                                    text = letter,
                                    color = GoldPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Conversation details
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = convo.userEmail,
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    
                                    val timeSdf = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
                                    val dateSdf = remember { SimpleDateFormat("dd MMM", Locale.getDefault()) }
                                    val timeStr = remember(convo.latestTimestamp) {
                                        val isToday = (System.currentTimeMillis() - convo.latestTimestamp) < 86400000
                                        if (isToday) timeSdf.format(Date(convo.latestTimestamp)) else dateSdf.format(Date(convo.latestTimestamp))
                                    }

                                    Text(
                                        text = timeStr,
                                        color = Color.White.copy(alpha = 0.5f),
                                        fontSize = 11.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = convo.latestMessage,
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )

                                    if (convo.pendingCount > 0) {
                                        Box(
                                            modifier = Modifier
                                                .size(18.dp)
                                                .clip(CircleShape)
                                                .background(GoldPrimary),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = convo.pendingCount.toString(),
                                                color = Color(0xFF021612),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Divider(
                            color = Color.White.copy(alpha = 0.05f),
                            thickness = 1.dp,
                            modifier = Modifier.padding(start = 80.dp)
                        )
                    }
                }
            }
        }
    } else {
        // Individual User Chat View Detail Screen
        val currentConvo = groupedConversations.find { it.userKey == selectedUserKey }
        if (currentConvo == null) {
            selectedUserKey = null
        } else {
            val detailListState = rememberLazyListState()
            var replyText by remember { mutableStateOf("") }
            var isSendingReply by remember { mutableStateOf(false) }

            LaunchedEffect(currentConvo.reports.size) {
                if (currentConvo.reports.isNotEmpty()) {
                    detailListState.animateScrollToItem(currentConvo.reports.size - 1)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(EmeraldBackground)
            ) {
                Column(modifier = Modifier.fillMaxSize().imePadding()) {
                    // Chat Header
                    TopAppBar(
                        title = {
                            Row(
                                modifier = Modifier.clickable { showUserInfoDialog = currentConvo },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF133628)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val letter = currentConvo.userEmail.firstOrNull()?.toString()?.uppercase() ?: "U"
                                    Text(
                                        text = letter,
                                        color = GoldPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = currentConvo.userEmail,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Click for Device & Meta info ℹ️",
                                        color = GoldPrimary,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = { selectedUserKey = null }) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = GoldPrimary
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = { showUserInfoDialog = currentConvo }) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "User Info",
                                    tint = GoldPrimary
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF091C16))
                    )

                    Divider(color = GoldPrimary.copy(alpha = 0.15f), thickness = 1.dp)

                    // Scrollable Chat area
                    LazyColumn(
                        state = detailListState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        currentConvo.reports.forEach { report ->
                            item {
                                UserChatBubble(
                                    report = report,
                                    onImageClick = { zoomImageUrl = report.imageUrl }
                                )
                            }

                            if (report.adminReply.isNotEmpty()) {
                                item {
                                    AdminChatBubble(
                                        replyText = report.adminReply,
                                        replyTimestamp = report.adminReplyTimestamp,
                                        userTimestamp = report.timestamp,
                                        onImageClick = { zoomImageUrl = report.imageUrl }
                                    )
                                }
                            }
                        }
                    }

                    // Status Quick Controls Bar
                    val latestActiveReport = currentConvo.reports.firstOrNull { it.status != "Resolved" } ?: currentConvo.reports.last()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF061510))
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ticket Status: ${latestActiveReport.status}",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (latestActiveReport.status != "In Progress") {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFFB45309))
                                        .clickable {
                                            viewModel.updateBugReportStatus(latestActiveReport.id, "In Progress")
                                            Toast.makeText(context, "Marked In Progress", Toast.LENGTH_SHORT).show()
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("IN PROGRESS", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            if (latestActiveReport.status != "Resolved") {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF047857))
                                        .clickable {
                                            viewModel.updateBugReportStatus(latestActiveReport.id, "Resolved")
                                            Toast.makeText(context, "Marked Resolved", Toast.LENGTH_SHORT).show()
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("RESOLVE", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF991B1B))
                                    .clickable {
                                        viewModel.deleteBugReport(latestActiveReport.id)
                                        Toast.makeText(context, "Deleted complaint", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("DELETE", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Admin Chat Input Box
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF091C16))
                            .navigationBarsPadding()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = replyText,
                            onValueChange = { replyText = it },
                            placeholder = { Text("Type an official support reply...", color = Color.White.copy(alpha = 0.4f), fontSize = 13.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(max = 120.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF133628),
                                unfocusedContainerColor = Color(0xFF133628),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = GoldPrimary.copy(alpha = 0.5f),
                                unfocusedBorderColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(20.dp),
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        val isReplyEnabled = replyText.trim().isNotEmpty() && !isSendingReply
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (isReplyEnabled) GoldPrimary else Color(0xFF1F3D32))
                                .clickable(enabled = isReplyEnabled) {
                                    isSendingReply = true
                                    viewModel.replyToBugReport(
                                        reportId = latestActiveReport.id,
                                        replyText = replyText.trim()
                                    ) { success, err ->
                                        isSendingReply = false
                                        if (success) {
                                            Toast.makeText(context, "Reply sent!", Toast.LENGTH_SHORT).show()
                                            replyText = ""
                                        } else {
                                            Toast.makeText(context, "Error: ${err ?: "Failed"}", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send",
                                tint = if (isReplyEnabled) Color(0xFF021612) else Color.White.copy(alpha = 0.3f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Details Info dialog
    if (showUserInfoDialog != null) {
        val details = showUserInfoDialog!!
        val latestRep = details.reports.last()
        Dialog(onDismissRequest = { showUserInfoDialog = null }) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF0F2E23))
                    .border(1.dp, GoldPrimary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "User Meta & Device Info ℹ️",
                        color = GoldPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Divider(color = Color.White.copy(alpha = 0.1f))

                    Row {
                        Text("Email: ", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(details.userEmail, color = Color.White, fontSize = 13.sp)
                    }

                    Row {
                        Text("User ID: ", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(latestRep.userId, color = Color.White, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }

                    Row {
                        Text("Device: ", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(latestRep.deviceModel.ifEmpty { "Unknown Model" }, color = Color.White, fontSize = 13.sp)
                    }

                    Row {
                        Text("App Version: ", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(latestRep.appVersion.ifEmpty { "1.0.0" }, color = Color.White, fontSize = 13.sp)
                    }

                    val dateSdf = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
                    Row {
                        Text("Last Active: ", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(dateSdf.format(Date(details.latestTimestamp)), color = Color.White, fontSize = 13.sp)
                    }

                    Row {
                        Text("Total Messages: ", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(details.reports.size.toString(), color = Color.White, fontSize = 13.sp)
                    }

                    Row {
                        Text("Pending: ", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(details.pendingCount.toString(), color = GoldPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { showUserInfoDialog = null },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("CLOSE", color = Color(0xFF021612), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Zoom Overlay
    if (zoomImageUrl != null) {
        ImageZoomDialog(imageUrl = zoomImageUrl!!) { zoomImageUrl = null }
    }
}
