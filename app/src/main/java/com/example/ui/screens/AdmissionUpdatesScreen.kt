package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AdmissionStatus
import com.example.data.model.ExamCategory
import com.example.data.model.OfficialAdmissionNotice
import com.example.data.model.Segment
import com.example.data.repository.OfficialNoticesData
import com.example.data.service.AdmissionStatusResolver
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.SurfaceSubtle
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondaryNavy

/**
 * Screen 6: Official Admission Notices & Live Status
 * Displays verified Firestore admission notices, separates active 2026-2027 cycles
 * from historical archive records, and provides live authority verification links.
 */
@Composable
fun AdmissionUpdatesScreen(
    officialNotices: List<OfficialAdmissionNotice> = emptyList(),
    lastSyncTime: Long = System.currentTimeMillis(),
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0: Active Cycles, 1: Historical Archive
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var selectedNoticeForDetail by remember { mutableStateOf<OfficialAdmissionNotice?>(null) }

    val filterOptions = listOf("All", "Medical", "Engineering", "Management")

    val allNoticesPool = remember(officialNotices) {
        if (officialNotices.isNotEmpty()) officialNotices else OfficialNoticesData.allNotices
    }

    val displayedList = remember(selectedTabIndex, selectedCategoryFilter, allNoticesPool) {
        val cyclePool = if (selectedTabIndex == 0) {
            allNoticesPool.filter { !it.isHistorical }
        } else {
            allNoticesPool.filter { it.isHistorical }
        }

        if (selectedCategoryFilter == "All") {
            cyclePool
        } else {
            cyclePool.filter {
                it.examCategory.displayName.equals(selectedCategoryFilter, ignoreCase = true)
            }
        }
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("admission_updates_screen"),
        color = PureWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Top App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.testTag("updates_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = DarkNavy
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Official Admission Notices",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp
                        ),
                        color = DarkNavy
                    )
                    Text(
                        text = "Last Live Sync: ${AdmissionStatusResolver.formatSyncTime(lastSyncTime)}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color(0xFF16A34A)
                    )
                }
            }

            // Tab Row: Active Cycles vs Historical Archive
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = PureWhite,
                contentColor = RoyalBlue,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = {
                        Text(
                            text = "Active Cycles (2026-27)",
                            fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Historical Archive",
                                fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Category Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filterOptions) { filter ->
                    val isSelected = filter == selectedCategoryFilter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) RoyalBlue else SurfaceSubtle)
                            .clickable { selectedCategoryFilter = filter }
                            .padding(horizontal = 16.dp, vertical = 7.dp)
                            .testTag("filter_chip_$filter"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = filter,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            ),
                            color = if (isSelected) PureWhite else TextSecondaryNavy
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Notice Cards List
            if (displayedList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (selectedTabIndex == 0)
                            "No active notices for this filter. All verified dates are up to date."
                        else
                            "No historical archive records for this category.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(displayedList) { notice ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { selectedNoticeForDetail = notice }
                                .testTag("notice_card_${notice.id}"),
                            colors = CardDefaults.cardColors(containerColor = SurfaceSubtle),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        val catIcon = when (notice.examCategory) {
                                            ExamCategory.MEDICAL -> Icons.Default.MedicalServices
                                            ExamCategory.ENGINEERING -> Icons.Default.Engineering
                                            ExamCategory.MANAGEMENT -> Icons.Default.BusinessCenter
                                            else -> Icons.Default.Verified
                                        }
                                        Icon(
                                            imageVector = catIcon,
                                            contentDescription = null,
                                            tint = RoyalBlue,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "${notice.examId.uppercase()} • ${notice.admissionCycle}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.5.sp
                                            ),
                                            color = DarkNavy
                                        )
                                    }

                                    AdmissionStatusBadge(status = notice.admissionStatus)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = notice.title,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        lineHeight = 20.sp
                                    ),
                                    color = DarkNavy
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = notice.summary,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp
                                    ),
                                    color = TextSecondaryNavy,
                                    maxLines = 3
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Schedule info
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(PureWhite)
                                        .padding(10.dp)
                                ) {
                                    if (notice.admissionStatus == AdmissionStatus.DATES_NOT_ANNOUNCED) {
                                        Text(
                                            text = "Schedule Status: Next Cycle — Dates Not Announced Yet",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = DarkNavy
                                        )
                                        Text(
                                            text = "Official authority has not yet opened registration for this cycle.",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = TextMuted
                                        )
                                    } else {
                                        if (notice.applicationOpenDate != null) {
                                            Text(
                                                text = "Application Opens: ${notice.applicationOpenDate}",
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                                                color = DarkNavy
                                            )
                                        }
                                        if (notice.applicationCloseDate != null) {
                                            Text(
                                                text = "Application Closes: ${notice.applicationCloseDate}",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = Color(0xFFDC2626)
                                            )
                                        }
                                        if (notice.examDate != null) {
                                            Text(
                                                text = "Examination Date: ${notice.examDate}",
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                                                color = DarkNavy
                                            )
                                        }
                                        if (notice.counsellingDeadline != null) {
                                            Text(
                                                text = "Counselling: ${notice.counsellingDeadline}",
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                                                color = RoyalBlue
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Action Row: View Official Notice & Verification info
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = notice.lastVerifiedTime,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp
                                        ),
                                        color = TextMuted
                                    )

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFEBF3FF))
                                            .clickable {
                                                try {
                                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(notice.officialUrl))
                                                    context.startActivity(intent)
                                                } catch (_: Exception) {}
                                            }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.OpenInBrowser,
                                            contentDescription = null,
                                            tint = RoyalBlue,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "View Official Notice",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = RoyalBlue
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }

        // Detail Dialog
        if (selectedNoticeForDetail != null) {
            val notice = selectedNoticeForDetail!!
            AlertDialog(
                onDismissRequest = { selectedNoticeForDetail = null },
                title = {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${notice.examId.uppercase()} • Cycle ${notice.admissionCycle}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = RoyalBlue,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            AdmissionStatusBadge(status = notice.admissionStatus)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = notice.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = DarkNavy
                        )
                    }
                },
                text = {
                    Column {
                        Text(
                            text = notice.summary,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 13.5.sp,
                                lineHeight = 19.sp
                            ),
                            color = TextSecondaryNavy
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Official Source URL:\n${notice.officialUrl}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                color = RoyalBlue
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = notice.lastVerifiedTime,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.5.sp,
                                color = TextMuted
                            )
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(notice.officialUrl))
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        }
                    ) {
                        Text("Open Official Portal ↗", color = RoyalBlue, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedNoticeForDetail = null }) {
                        Text("Close", color = TextMuted)
                    }
                }
            )
        }
    }
}
