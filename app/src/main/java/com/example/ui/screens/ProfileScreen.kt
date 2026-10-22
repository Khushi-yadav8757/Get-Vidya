package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.rememberCoroutineScope
import androidx.credentials.CredentialManager
import com.example.data.firebase.AuthManager
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.College
import com.example.data.model.TestAttempt
import com.example.data.model.UserProfile
import com.example.ui.components.DemoDataBadge
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.DividerColor
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.SurfaceSubtle
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimaryNavy
import com.example.ui.theme.TextSecondaryNavy
import com.example.ui.theme.WaveLightBlue

/**
 * Screen 14: Profile and Settings
 */
@Composable
fun ProfileScreen(
    userProfile: UserProfile,
    savedColleges: List<College>,
    testAttempts: List<TestAttempt>,
    onSaveProfile: (UserProfile) -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToCollegeDetail: (College) -> Unit,
    onOpenAdvertiserDashboard: () -> Unit = {},
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showEditProfileModal by remember { mutableStateOf(false) }
    var showSavedItemsModal by remember { mutableStateOf(false) }
    var showTestHistoryModal by remember { mutableStateOf(false) }
    var showSettingsModal by remember { mutableStateOf(false) }
    var showHelpModal by remember { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    // Settings toggles
    var notificationsEnabled by remember { mutableStateOf(true) }
    var emailAlertsEnabled by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("profile_screen"),
        color = PureWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Student Profile Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceSubtle),
                shape = RoundedCornerShape(22.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .border(2.dp, RoyalBlue, CircleShape)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.student_avatar),
                            contentDescription = "Profile Photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = userProfile.name.ifEmpty { "Aryan Sharma" },
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = DarkNavy
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = userProfile.email.ifEmpty { "aryan@gmail.com" },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.5.sp
                        ),
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(WaveLightBlue)
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${userProfile.examAppeared} • Rank ${userProfile.scoreRank.ifEmpty { "720" }} • ${userProfile.domicile}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = RoyalBlue
                        )
                    }

                    val currentUser = Firebase.auth.currentUser
                    val isCloudSynced = currentUser != null

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isCloudSynced) Color(0xFF16A34A) else Color(0xFFEAB308))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isCloudSynced) "Firestore Synced (${currentUser?.email ?: "Google Account"})" else "Local Session (Sign In with Google to Sync)",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = if (isCloudSynced) Color(0xFF16A34A) else TextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Menu Items List
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceSubtle),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    ProfileMenuItem(
                        icon = Icons.Default.Person,
                        title = "My Profile",
                        onClick = { showEditProfileModal = true },
                        testTag = "menu_my_profile"
                    )

                    HorizontalDivider(color = DividerColor.copy(alpha = 0.6f), modifier = Modifier.padding(horizontal = 16.dp))

                    ProfileMenuItem(
                        icon = Icons.Default.Bookmark,
                        title = "Saved Items",
                        badge = "${savedColleges.size}",
                        onClick = { showSavedItemsModal = true },
                        testTag = "menu_saved_items"
                    )

                    HorizontalDivider(color = DividerColor.copy(alpha = 0.6f), modifier = Modifier.padding(horizontal = 16.dp))

                    ProfileMenuItem(
                        icon = Icons.Default.Assignment,
                        title = "Test History",
                        badge = "${testAttempts.size}",
                        onClick = { showTestHistoryModal = true },
                        testTag = "menu_test_history"
                    )

                    HorizontalDivider(color = DividerColor.copy(alpha = 0.6f), modifier = Modifier.padding(horizontal = 16.dp))

                    ProfileMenuItem(
                        icon = Icons.Default.Notifications,
                        title = "Notifications",
                        onClick = onNavigateToNotifications,
                        testTag = "menu_notifications"
                    )

                    HorizontalDivider(color = DividerColor.copy(alpha = 0.6f), modifier = Modifier.padding(horizontal = 16.dp))

                    ProfileMenuItem(
                        icon = Icons.AutoMirrored.Filled.HelpOutline,
                        title = "Help & Support",
                        onClick = { showHelpModal = true },
                        testTag = "menu_help"
                    )

                    HorizontalDivider(color = DividerColor.copy(alpha = 0.6f), modifier = Modifier.padding(horizontal = 16.dp))

                    ProfileMenuItem(
                        icon = Icons.Default.Campaign,
                        title = "College Partner Portal",
                        badge = "Desk",
                        onClick = onOpenAdvertiserDashboard,
                        testTag = "menu_advertiser_portal"
                    )

                    HorizontalDivider(color = DividerColor.copy(alpha = 0.6f), modifier = Modifier.padding(horizontal = 16.dp))

                    ProfileMenuItem(
                        icon = Icons.Default.Settings,
                        title = "Settings",
                        onClick = { showSettingsModal = true },
                        testTag = "menu_settings"
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Logout Action
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { showLogoutConfirm = true }
                    .testTag("menu_logout"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F2)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Logout",
                        tint = Color(0xFFE11D48),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = "Logout",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = Color(0xFFE11D48)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }

        // 1. Edit Profile Modal
        if (showEditProfileModal) {
            var editName by remember { mutableStateOf(userProfile.name) }
            var editEmail by remember { mutableStateOf(userProfile.email) }
            var editPhone by remember { mutableStateOf(userProfile.phone) }
            var editRank by remember { mutableStateOf(userProfile.scoreRank) }

            AlertDialog(
                onDismissRequest = { showEditProfileModal = false },
                title = { Text("Edit Student Profile", fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text("Full Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editEmail,
                            onValueChange = { editEmail = it },
                            label = { Text("Email Address") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editPhone,
                            onValueChange = { editPhone = it },
                            label = { Text("Phone Number") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editRank,
                            onValueChange = { editRank = it },
                            label = { Text("Exam Score / Rank") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val updated = userProfile.copy(
                                name = editName,
                                email = editEmail,
                                phone = editPhone,
                                scoreRank = editRank
                            )
                            onSaveProfile(updated)
                            showEditProfileModal = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                    ) {
                        Text("Save Changes")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditProfileModal = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // 2. Saved Items Modal
        if (showSavedItemsModal) {
            AlertDialog(
                onDismissRequest = { showSavedItemsModal = false },
                title = { Text("Saved Colleges (${savedColleges.size})", fontWeight = FontWeight.Bold) },
                text = {
                    if (savedColleges.isEmpty()) {
                        Text("No colleges saved yet. Use the bookmark icon on college cards to save.")
                    } else {
                        LazyColumn(modifier = Modifier.height(280.dp)) {
                            items(savedColleges) { col ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable {
                                            showSavedItemsModal = false
                                            onNavigateToCollegeDetail(col)
                                        },
                                    colors = CardDefaults.cardColors(containerColor = SurfaceSubtle)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(col.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DarkNavy)
                                            Text("${col.course} • ${col.location}", fontSize = 12.sp, color = TextSecondaryNavy)
                                        }
                                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = RoyalBlue, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSavedItemsModal = false }) {
                        Text("Close", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        // 3. Test History Modal
        if (showTestHistoryModal) {
            AlertDialog(
                onDismissRequest = { showTestHistoryModal = false },
                title = { Text("Test History (${testAttempts.size})", fontWeight = FontWeight.Bold) },
                text = {
                    if (testAttempts.isEmpty()) {
                        Text("No test attempts recorded yet.")
                    } else {
                        LazyColumn(modifier = Modifier.height(280.dp)) {
                            items(testAttempts) { att ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = SurfaceSubtle)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(att.testTitle, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = DarkNavy, modifier = Modifier.weight(1f))
                                            Text("${att.score}/${att.totalMarks}", fontWeight = FontWeight.Bold, color = RoyalBlue)
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("Accuracy: ${att.accuracyPercentage}% • ${att.dateString}", fontSize = 12.sp, color = TextMuted)
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showTestHistoryModal = false }) {
                        Text("Close", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        // 4. Settings Modal
        if (showSettingsModal) {
            AlertDialog(
                onDismissRequest = { showSettingsModal = false },
                title = { Text("App Settings", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Admission Notifications", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("Real-time round & cutoff alerts", fontSize = 11.5.sp, color = TextMuted)
                            }
                            Switch(
                                checked = notificationsEnabled,
                                onCheckedChange = { notificationsEnabled = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = RoyalBlue)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Email Updates", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("Weekly counselling digest", fontSize = 11.5.sp, color = TextMuted)
                            }
                            Switch(
                                checked = emailAlertsEnabled,
                                onCheckedChange = { emailAlertsEnabled = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = RoyalBlue)
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSettingsModal = false }) {
                        Text("Save & Close", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        // 5. Help & Support Modal
        if (showHelpModal) {
            AlertDialog(
                onDismissRequest = { showHelpModal = false },
                title = { Text("Get Vidya Support", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("We are dedicated to guiding students through college admissions across India.")
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("📧 Email: support@getvidya.in", fontWeight = FontWeight.SemiBold)
                        Text("📞 Helpline: 1800-120-VIDYA (Toll Free)", fontWeight = FontWeight.SemiBold)
                        Text("🕒 Hours: 9:00 AM - 7:00 PM IST (Mon - Sat)")
                        Spacer(modifier = Modifier.height(6.dp))
                        DemoDataBadge(text = "Dedicated student helpline available during counselling season")
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showHelpModal = false }) {
                        Text("Close", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        // 6. Logout Confirmation Dialog
        if (showLogoutConfirm) {
            AlertDialog(
                onDismissRequest = { showLogoutConfirm = false },
                title = { Text("Logout?") },
                text = { Text("Are you sure you want to log out of Get Vidya? Your student profile, saved colleges, and test attempts are backed up to Firebase Firestore.") },
                confirmButton = {
                    Button(
                        onClick = {
                            showLogoutConfirm = false
                            AuthManager.signOut(
                                context = context,
                                credentialManager = credentialManager,
                                onSignOutComplete = onLogout,
                                scope = coroutineScope
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48))
                    ) {
                        Text("Logout")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutConfirm = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    badge: String? = null,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(WaveLightBlue),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = RoyalBlue,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.5.sp
            ),
            color = DarkNavy,
            modifier = Modifier.weight(1f)
        )

        if (badge != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(WaveLightBlue)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = badge,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = RoyalBlue
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(20.dp)
        )
    }
}
