package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.room.UserStatsEntity
import com.example.ui.theme.*
import com.example.viewmodel.TaqwaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    viewModel: TaqwaViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val stats by viewModel.stats.collectAsState(initial = UserStatsEntity())
    val userName = stats.name.ifBlank { "Servant of Allah" }
    val globalLeaderboard = viewModel.globalLeaderboard
    val leaderboardCountdown = viewModel.leaderboardResetTimeRemaining

    val currentEmail = remember(viewModel.currentUser?.email, stats.email) {
        viewModel.currentUser?.email?.trim()?.lowercase() ?: stats.email.trim().lowercase()
    }
    val currentUsername = remember(stats.username) { stats.username.trim().lowercase() }
    val currentName = remember(stats.name) { stats.name.trim().lowercase() }

    val isRowMe = remember(currentEmail, currentUsername, currentName) {
        { row: UserStatsEntity ->
            val rowEmail = row.email.trim().lowercase()
            val rowUsername = row.username.trim().lowercase()
            val rowName = row.name.trim().lowercase()
            
            if (currentEmail.isNotBlank() && rowEmail.isNotBlank()) {
                rowEmail == currentEmail
            } else if (currentUsername.isNotBlank() && rowUsername.isNotBlank()) {
                rowUsername == currentUsername
            } else {
                rowName == currentName && rowName != "servant of allah"
            }
        }
    }

    // Find user's rank
    val userRank = remember(globalLeaderboard, isRowMe) {
        globalLeaderboard.indexOfFirst { isRowMe(it) } + 1
    }
    val displayRank = if (userRank > 0) "#$userRank" else "Unranked"

    // Profile Dialog State
    var profileDialogUser by remember { mutableStateOf<UserStatsEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EmeraldBackground)
    ) {
        TopAppBar(
            title = { Text("Global Leaderboard", color = Color.White, fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = EmeraldDark)
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Standing card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = GoldPrimary),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("YOUR CURRENT STANDING", color = OnGoldText, fontSize = 9.sp, fontWeight = FontWeight.Black)
                                Text("Rank $displayRank globally", color = OnGoldText, fontSize = 20.sp, fontWeight = FontWeight.Black)
                            }
                            Box(
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("${stats.weeklyXp} Weekly XP", color = OnGoldText, fontSize = 13.sp, fontWeight = FontWeight.Black)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        HorizontalDivider(
                            modifier = Modifier.fillMaxWidth(),
                            thickness = 1.dp,
                            color = OnGoldText.copy(alpha = 0.25f)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Time remaining",
                                tint = OnGoldText,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "RESETS IN: ",
                                color = OnGoldText.copy(alpha = 0.8f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = leaderboardCountdown.ifEmpty { "Calculating..." },
                                color = OnGoldText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

            // Description of reset
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = TextGray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Ranks reset weekly. Tap any user below to view their earned achievements and shining trophies.",
                        color = TextGray,
                        fontSize = 11.sp,
                        lineHeight = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Leaderboard Entries
            if (globalLeaderboard.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Leaderboard, contentDescription = null, tint = TextGray, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No users ranked yet this week.", color = TextGray)
                        }
                    }
                }
            } else {
                val top99 = globalLeaderboard.take(99)
                val isYouInTop99 = top99.any { isRowMe(it) }

                itemsIndexed(top99) { index, row ->
                    val isYou = isRowMe(row)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isYou) GoldPrimary.copy(alpha = 0.08f) else EmeraldCard)
                            .border(
                                1.dp,
                                if (isYou) GoldPrimary.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.02f),
                                RoundedCornerShape(16.dp)
                            )
                            .clickable {
                                profileDialogUser = row
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Rank Number Badge
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (index) {
                                            0 -> Color(0xFFFBBF24) // Gold
                                            1 -> Color(0xFF9CA3AF) // Silver
                                            2 -> Color(0xFFD97706) // Bronze
                                            else -> Color.White.copy(alpha = 0.05f)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (index + 1).toString(),
                                    color = if (index < 3) OnGoldText else Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Compact round avatar for leaderboard list
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, GoldPrimary.copy(alpha = 0.25f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                val bitmap = remember(row.profilePictureBase64) {
                                    if (row.profilePictureBase64.isNotEmpty()) {
                                        decodeBase64ToBitmap(row.profilePictureBase64)
                                    } else null
                                }
                                if (bitmap != null) {
                                    Image(
                                        bitmap = bitmap,
                                        contentDescription = "Leaderboard profile photo",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    WhatsAppPlaceholderAvatar(modifier = Modifier.fillMaxSize())
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
<<<<<<< HEAD
                                    Text(
                                        text = if (isYou) "👑 $userName" else row.name,
=======
                                    val rowDisplayName = when {
                                        row.name.isNotBlank() -> row.name
                                        row.username.isNotBlank() -> "@${row.username.removePrefix("@")}"
                                        else -> "TaqwaUser_${(row.id.hashCode() and 0x00FFFFFF).toString(16)}"
                                    }
                                    Text(
                                        text = rowDisplayName,
>>>>>>> 6e834ed (Update Taqwahub)
                                        color = if (isYou) GoldPrimary else Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = if (isYou) FontWeight.Black else FontWeight.Bold
                                    )
<<<<<<< HEAD
                                    if (isYou) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .background(GoldPrimary.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "YOU",
                                                color = GoldPrimary,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    }
=======
>>>>>>> 6e834ed (Update Taqwahub)
                                    if (viewModel.isUserVerified(row)) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.Verified,
                                            contentDescription = "Verified Servant",
                                            tint = GoldPrimary,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                }
                                
                                // Show tiny indicator if they have podium finishes
                                if (row.firstPlaceCount > 0 || row.secondPlaceCount > 0 || row.thirdPlaceCount > 0) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.padding(top = 2.dp)
                                    ) {
                                        if (row.firstPlaceCount > 0) {
                                            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(10.dp))
                                            Text("${row.firstPlaceCount}", color = Color(0xFFFBBF24), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                        if (row.secondPlaceCount > 0) {
                                            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(10.dp))
                                            Text("${row.secondPlaceCount}", color = Color(0xFF9CA3AF), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                        if (row.thirdPlaceCount > 0) {
                                            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(10.dp))
                                            Text("${row.thirdPlaceCount}", color = Color(0xFFD97706), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${row.weeklyXp} XP",
                                color = if (isYou) GoldPrimary else Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Weekly",
                                color = TextGray,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (!isYouInTop99) {
                    val fullIndex = globalLeaderboard.indexOfFirst { isRowMe(it) }
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(GoldPrimary.copy(alpha = 0.08f))
                                .border(
                                    2.dp,
                                    GoldPrimary.copy(alpha = 0.6f),
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    profileDialogUser = stats
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Rank Number Badge
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(GoldPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (fullIndex != -1) (fullIndex + 1).toString() else "Unranked",
                                        color = OnGoldText,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                // Compact round avatar
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .border(1.dp, GoldPrimary.copy(alpha = 0.4f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val bitmap = remember(stats.profilePictureBase64) {
                                        if (stats.profilePictureBase64.isNotEmpty()) {
                                            decodeBase64ToBitmap(stats.profilePictureBase64)
                                        } else null
                                    }
                                    if (bitmap != null) {
                                        Image(
                                            bitmap = bitmap,
                                            contentDescription = "My profile photo",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        WhatsAppPlaceholderAvatar(modifier = Modifier.fillMaxSize())
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
<<<<<<< HEAD
                                        Text(
                                            text = "👑 $userName",
=======
                                        val myDisplayName = when {
                                            userName.isNotBlank() -> userName
                                            stats.username.isNotBlank() -> "@${stats.username.removePrefix("@")}"
                                            else -> "TaqwaUser_${(stats.id.hashCode() and 0x00FFFFFF).toString(16)}"
                                        }
                                        Text(
                                            text = myDisplayName,
>>>>>>> 6e834ed (Update Taqwahub)
                                            color = GoldPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Black
                                        )
<<<<<<< HEAD
                                        Spacer(modifier = Modifier.width(6.dp))
                                        // "ME" Tag
                                        Box(
                                            modifier = Modifier
                                                .background(GoldPrimary.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "YOU",
                                                color = GoldPrimary,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
=======
>>>>>>> 6e834ed (Update Taqwahub)
                                    }
                                    Text(
                                        text = "Your Position",
                                        color = TextGray,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            // XP Display
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${stats.weeklyXp} XP",
                                    color = GoldPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "Weekly",
                                    color = TextGray,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Interactive Profile Dialog
    profileDialogUser?.let { user ->
        UserProfileDialog(
            user = user,
            isVerified = viewModel.isUserVerified(user),
            onDismiss = { profileDialogUser = null }
        )
    }

    if (viewModel.showWeeklyResetDialog) {
        WeeklyResetRewardDialog(
            rank = viewModel.weeklyResetRank,
            xpEarned = viewModel.weeklyResetXp,
            trophyPlace = viewModel.weeklyResetTrophy,
            bonusXp = viewModel.weeklyResetBonusXp,
            onDismiss = {
                viewModel.dismissWeeklyAwardDialog()
            }
        )
    }
}

@Composable
fun WeeklyResetRewardDialog(
    rank: Int,
    xpEarned: Int,
    trophyPlace: Int, // 1 = Gold, 2 = Silver, 3 = Bronze, 0 = none
    bonusXp: Int,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = EmeraldDark),
            border = BorderStroke(2.dp, GoldPrimary)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Celebration Icon or Trophy
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(GoldPrimary.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (trophyPlace in 1..3) Icons.Default.EmojiEvents else Icons.Default.Stars,
                        contentDescription = "Trophy",
                        tint = when (trophyPlace) {
                            1 -> Color(0xFFFBBF24) // Gold
                            2 -> Color(0xFF9CA3AF) // Silver
                            3 -> Color(0xFFD97706) // Bronze
                            else -> GoldPrimary
                        },
                        modifier = Modifier.size(48.dp)
                    )
                }

                // Title
                Text(
                    text = if (trophyPlace in 1..3) "Congratulations! 🎉" else "Week Completed!",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )

                // Message description
                Text(
                    text = if (trophyPlace in 1..3) {
                        val trophyName = when (trophyPlace) {
                            1 -> "Gold Champion Trophy"
                            2 -> "Silver Contender Trophy"
                            else -> "Bronze Elite Trophy"
                        }
                        "Masha'Allah! You finished in Rank #$rank globally last week! You earned $xpEarned XP, and you have been awarded the prestigious $trophyName with an extra +$bonusXp bonus XP! Keep shining!"
                    } else {
                        "Congratulations on completing your spiritual journey last week with $xpEarned XP! You placed Rank #$rank. Keep striving and dedicating yourself daily to claim a top-3 trophy next week!"
                    },
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Beautiful Confirm Button
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        contentColor = OnGoldText
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (trophyPlace in 1..3) "Alhamdulillah" else "Insha'Allah, I will try!",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
fun UserProfileDialog(
    user: UserStatsEntity,
    isVerified: Boolean,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = EmeraldDark),
            border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Spiritual Standings Profile",
                        color = GoldPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = GoldPrimary)
                    }
                }

                // User Bio Details Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = EmeraldCard),
                    border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, GoldPrimary.copy(alpha = 0.3f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                val bitmap = remember(user.profilePictureBase64) {
                                    if (user.profilePictureBase64.isNotEmpty()) {
                                        decodeBase64ToBitmap(user.profilePictureBase64)
                                    } else null
                                }
                                if (bitmap != null) {
                                    Image(
                                        bitmap = bitmap,
                                        contentDescription = "Profile Picture",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    WhatsAppPlaceholderAvatar(modifier = Modifier.fillMaxSize())
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
<<<<<<< HEAD
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(user.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
=======
                                val profileDisplayName = when {
                                    user.name.isNotBlank() -> user.name
                                    user.username.isNotBlank() -> "@${user.username.removePrefix("@")}"
                                    else -> "TaqwaUser_${(user.id.hashCode() and 0x00FFFFFF).toString(16)}"
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(profileDisplayName, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
>>>>>>> 6e834ed (Update Taqwahub)
                                    if (isVerified) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.Verified,
                                            contentDescription = "Verified Servant",
                                            tint = GoldPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
<<<<<<< HEAD
                                Text("${user.sectOrCast} • ${user.gender}", color = TextGray, fontSize = 11.sp)
=======

                                val handle = if (user.username.isNotBlank()) "@${user.username.removePrefix("@")}" else ""
                                val caste = if (user.sectOrCast.isNotBlank() && !user.sectOrCast.equals("none", ignoreCase = true)) user.sectOrCast else ""
                                val gender = if (user.gender.isNotBlank() && !user.gender.equals("none", ignoreCase = true)) user.gender else ""
                                val metadataParts = listOf(handle, caste, gender).filter { it.isNotBlank() }
                                val subtitleText = metadataParts.joinToString(" • ")

                                if (subtitleText.isNotBlank()) {
                                    Text(subtitleText, color = TextGray, fontSize = 11.sp)
                                }
>>>>>>> 6e834ed (Update Taqwahub)
                            }
                        }

                        HorizontalDivider(color = GoldPrimary.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                Text("TOTAL XP", color = TextGray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                Text("${user.totalXp}", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Black)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                Text("WEEKLY XP", color = TextGray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                Text("${user.weeklyXp}", color = GoldPrimary, fontSize = 15.sp, fontWeight = FontWeight.Black)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                Text("STREAK", color = TextGray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = Color(0xFFFACC15), modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("${user.currentStreak}d", color = Color(0xFFFACC15), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Shining Podium Badges Section
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Podium Honor Trophies",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ProfileTrophyBadge(
                            placeName = "Champion",
                            count = user.firstPlaceCount,
                            color = Color(0xFFFBBF24),
                            tintColor = Color(0xFFFBBF24),
                            placeNum = 1,
                            modifier = Modifier.weight(1f)
                        )
                        ProfileTrophyBadge(
                            placeName = "Contender",
                            count = user.secondPlaceCount,
                            color = Color(0xFF9CA3AF),
                            tintColor = Color(0xFF9CA3AF),
                            placeNum = 2,
                            modifier = Modifier.weight(1f)
                        )
                        ProfileTrophyBadge(
                            placeName = "Elite",
                            count = user.thirdPlaceCount,
                            color = Color(0xFFD97706),
                            tintColor = Color(0xFFD97706),
                            placeNum = 3,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Spiritual Honors Section
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Spiritual Badges & Honors",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val completedCount = remember(user.completedSurahs) {
                            user.completedSurahs.split(",").filter { it.isNotEmpty() }.size
                        }
                        val devotionBadges = remember(user, completedCount) {
                            listOf(
                                DevotionBadge(
                                    name = "Devoted Disciple",
                                    description = "Complete your first daily task or prayer action to begin your journey.",
                                    tier = BadgeTier.BRONZE,
                                    icon = Icons.Default.Star,
                                    isEarned = user.totalTasksCompleted >= 1,
                                    currentProgress = user.totalTasksCompleted,
                                    targetProgress = 1,
                                    progressString = "${kotlin.math.min(user.totalTasksCompleted, 1)} / 1 action"
                                ),
                                DevotionBadge(
                                    name = "Ignited Constancy",
                                    description = "Maintain an active spiritual streak of 3 days or more in the tracker.",
                                    tier = BadgeTier.BRONZE,
                                    icon = Icons.Default.LocalFireDepartment,
                                    isEarned = user.currentStreak >= 3,
                                    currentProgress = user.currentStreak,
                                    targetProgress = 3,
                                    progressString = "${kotlin.math.min(user.currentStreak, 3)} / 3 days streak"
                                ),
                                DevotionBadge(
                                    name = "Identity of Faith",
                                    description = "Claim your customizable servant name in settings to establish your unique identity.",
                                    tier = BadgeTier.BRONZE,
                                    icon = Icons.Default.Fingerprint,
                                    isEarned = user.name != "Servant of Allah",
                                    currentProgress = if (user.name != "Servant of Allah") 1 else 0,
                                    targetProgress = 1,
                                    progressString = if (user.name != "Servant of Allah") "1 / 1 claimed" else "0 / 1 claimed"
                                ),
                                DevotionBadge(
                                    name = "Steadfast Servant",
                                    description = "Log over 10 total actions in your spiritual history.",
                                    tier = BadgeTier.SILVER,
                                    icon = Icons.Default.DoneAll,
                                    isEarned = user.totalTasksCompleted >= 10,
                                    currentProgress = user.totalTasksCompleted,
                                    targetProgress = 10,
                                    progressString = "${kotlin.math.min(user.totalTasksCompleted, 10)} / 10 actions"
                                ),
                                DevotionBadge(
                                    name = "Dhikr Master",
                                    description = "Pledge and count 100+ total Tasbeeh counters.",
                                    tier = BadgeTier.SILVER,
                                    icon = Icons.Default.VolunteerActivism,
                                    isEarned = user.tasbeehCount >= 100,
                                    currentProgress = user.tasbeehCount,
                                    targetProgress = 100,
                                    progressString = "${kotlin.math.min(user.tasbeehCount, 100)} / 100 dhikr"
                                ),
                                DevotionBadge(
                                    name = "Quran Scholar",
                                    description = "Complete the reading or recitation of 5 or more Surahs.",
                                    tier = BadgeTier.SILVER,
                                    icon = Icons.Default.MenuBook,
                                    isEarned = completedCount >= 5,
                                    currentProgress = completedCount,
                                    targetProgress = 5,
                                    progressString = "${kotlin.math.min(completedCount, 5)} / 5 Surahs"
                                ),
                                DevotionBadge(
                                    name = "Streak Warrior",
                                    description = "Unlock the ultimate constancy by achieving a streak of 15 days or more.",
                                    tier = BadgeTier.GOLD,
                                    icon = Icons.Default.WorkspacePremium,
                                    isEarned = user.longestStreak >= 15,
                                    currentProgress = user.longestStreak,
                                    targetProgress = 15,
                                    progressString = "${kotlin.math.min(user.longestStreak, 15)} / 15 days streak"
                                ),
                                DevotionBadge(
                                    name = "Hifz Al-Quran",
                                    description = "Recite all 114 Surahs completely in the application for the ultimate spiritual honor.",
                                    tier = BadgeTier.GOLD,
                                    icon = Icons.Default.AutoAwesome,
                                    isEarned = completedCount >= 114,
                                    currentProgress = completedCount,
                                    targetProgress = 114,
                                    progressString = "${kotlin.math.min(completedCount, 114)} / 114 Surahs"
                                )
                            )
                        }

                        val chunks = devotionBadges.chunked(4)
                        chunks.forEach { rowBadges ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                rowBadges.forEach { badge ->
                                    BadgeIconItem(
                                        badge = badge,
                                        modifier = Modifier.weight(1f),
                                        onClick = { }
                                    )
                                }
                                repeat(4 - rowBadges.size) {
                                    Spacer(modifier = Modifier.weight(1f))
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
fun ShiningTrophyBadge(
    placeName: String,
    count: Int,
    color: Color,
    tintColor: Color,
    placeNum: Int,
    modifier: Modifier = Modifier
) {
    val isEarned = count > 0

    // Pulsing/Shining animation logic for active badges
    val infiniteTransition = rememberInfiniteTransition()
    val shineAlpha by if (isEarned) {
        infiniteTransition.animateFloat(
            initialValue = 0.5f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    } else {
        remember { mutableStateOf(1f) }
    }

    val glowScale by if (isEarned) {
        infiniteTransition.animateFloat(
            initialValue = 0.95f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    } else {
        remember { mutableStateOf(1f) }
    }

    Card(
        modifier = modifier
            .graphicsLayer {
                scaleX = glowScale
                scaleY = glowScale
            }
            .border(
                1.dp,
                if (isEarned) color.copy(alpha = shineAlpha) else Color.White.copy(alpha = 0.05f),
                RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isEarned) EmeraldCard else EmeraldCard.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (isEarned) color.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.03f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (placeNum) {
                        1 -> Icons.Default.EmojiEvents
                        2 -> Icons.Default.MilitaryTech
                        else -> Icons.Default.WorkspacePremium
                    },
                    contentDescription = null,
                    tint = if (isEarned) tintColor else TextGray.copy(alpha = 0.3f),
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = placeName,
                color = if (isEarned) Color.White else TextGray.copy(alpha = 0.5f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = if (isEarned) "${count}x Earned" else "Locked",
                color = if (isEarned) GoldPrimary else TextGray.copy(alpha = 0.4f),
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

fun getAchievementsForStats(stats: UserStatsEntity): List<Triple<String, String, Boolean>> {
    val completedCount = stats.completedSurahs.split(",")
        .filter { it.isNotEmpty() }
        .size

    return listOf(
        Triple("Devoted Disciple", "First daily task or prayer action complete.", stats.totalTasksCompleted >= 1),
        Triple("Ignited Constancy", "Spiritual streak of 3+ days maintained.", stats.currentStreak >= 3),
        Triple("Quran Scholar", "Completed recitation of 5+ Surahs.", completedCount >= 5),
        Triple("Steadfast Servant", "Over 10 total actions logged in history.", stats.totalTasksCompleted >= 10),
        Triple("Dhikr Master", "Pledged 100+ total tasbeeh counters.", stats.tasbeehCount >= 100),
        Triple("Streak Warrior", "Spiritual streak of 15+ days achieved.", stats.longestStreak >= 15),
        Triple("Identity of Faith (Rare)", "Claimed customizable name in settings.", stats.name != "Servant of Allah"),
        Triple("Hifz Al-Quran (Ultimate Honor)", "Recited all 114 Surahs completely in app.", completedCount >= 114)
    )
}
