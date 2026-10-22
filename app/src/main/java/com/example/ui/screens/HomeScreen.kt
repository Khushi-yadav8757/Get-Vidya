package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AdmissionStatus
import com.example.data.model.ExamCategory
import com.example.data.model.OfficialAdmissionNotice
import com.example.data.model.AdmissionUpdate
import com.example.data.model.College
import com.example.data.model.CollegeAdvertisement
import com.example.data.model.Segment
import com.example.data.model.UserProfile
import com.example.data.repository.OfficialNoticesData
import com.example.data.service.AdmissionStatusResolver
import com.example.ui.components.DemoDataBadge
import com.example.ui.components.SegmentSelectorPills
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.SurfaceSubtle
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondaryNavy

/**
 * Screen 4: Home Dashboard
 * Connected to verified Firestore official notices pipeline.
 * Features real-time admission status calculation, official portal verification,
 * and upcoming future deadline enforcement.
 */
@Composable
fun AdmissionStatusBadge(
    status: AdmissionStatus,
    modifier: Modifier = Modifier
) {
    val containerColor = Color(status.badgeColorHex).copy(alpha = 0.14f)
    val contentColor = Color(status.badgeColorHex)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(containerColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
            .testTag("status_badge_${status.name.lowercase()}")
    ) {
        Text(
            text = status.displayName,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            ),
            color = contentColor
        )
    }
}

@Composable
fun HomeScreen(
    userProfile: UserProfile,
    selectedSegment: Segment,
    onSegmentChange: (Segment) -> Unit,
    unreadNotificationCount: Int,
    onNotificationsClick: () -> Unit,
    onViewUpdateDetailsClick: (AdmissionUpdate) -> Unit,
    onSeeAllDeadlinesClick: () -> Unit,
    admissionUpdates: List<AdmissionUpdate>,
    collegeAds: List<CollegeAdvertisement> = emptyList(),
    collegesList: List<College> = emptyList(),
    officialNotices: List<OfficialAdmissionNotice> = emptyList(),
    lastSyncTime: Long = System.currentTimeMillis(),
    onNavigateToFeed: () -> Unit = {},
    onNavigateToCollegeDetail: (College) -> Unit = {},
    onAdClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen"),
        color = PureWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Header Row: Greeting & Notification Bell
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hello, ${userProfile.name.ifEmpty { "Aryan" }} 👋",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 23.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = DarkNavy
                    )
                    Text(
                        text = "Let's plan your future!",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp
                        ),
                        color = TextSecondaryNavy
                    )
                }

                IconButton(
                    onClick = onNotificationsClick,
                    modifier = Modifier.testTag("notification_bell_button")
                ) {
                    BadgedBox(
                        badge = {
                            if (unreadNotificationCount > 0) {
                                Badge(
                                    containerColor = Color(0xFFEF4444),
                                    contentColor = PureWhite
                                ) {
                                    Text(text = unreadNotificationCount.toString())
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Notifications",
                            tint = DarkNavy,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Segment Selector Pills: [ Medical | Engineering | Management ]
            SegmentSelectorPills(
                selectedSegment = selectedSegment,
                onSegmentSelected = onSegmentChange
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 1. LATEST UPDATES (Verified official notices pipeline with live status badges)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Latest Updates",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        ),
                        color = DarkNavy
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFEFF6FF))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${selectedSegment.displayName} • 2026-27",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = RoyalBlue
                        )
                    }
                }

                Text(
                    text = "View All →",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = RoyalBlue,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.5.sp
                    ),
                    modifier = Modifier
                        .clickable { onNotificationsClick() }
                        .testTag("view_all_updates_button")
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Live synchronization status row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF16A34A))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Live Sync: ${AdmissionStatusResolver.formatSyncTime(lastSyncTime)}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            val latestSegmentNotices = remember(selectedSegment, officialNotices) {
                val pool = if (officialNotices.isNotEmpty()) {
                    officialNotices.filter { notice ->
                        !notice.isHistorical && when (selectedSegment) {
                            Segment.MEDICAL -> notice.examCategory == ExamCategory.MEDICAL
                            Segment.ENGINEERING -> notice.examCategory == ExamCategory.ENGINEERING
                            Segment.MANAGEMENT -> notice.examCategory == ExamCategory.MANAGEMENT
                        }
                    }
                } else {
                    OfficialNoticesData.getNoticesForSegment(selectedSegment)
                }
                // Deduplicate by examId so Management exams like CMAT, CAT, XAT, SNAP, NMAT, MAT are unique
                val seenExams = mutableSetOf<String>()
                val distinctNotices = mutableListOf<OfficialAdmissionNotice>()
                for (notice in pool) {
                    if (seenExams.add(notice.examId.lowercase())) {
                        distinctNotices.add(notice)
                    }
                }
                if (distinctNotices.isNotEmpty()) distinctNotices else pool
            }

            if (latestSegmentNotices.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceSubtle),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "No active updates for ${selectedSegment.displayName}. Verified sources are continuously checked.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            } else {
                latestSegmentNotices.take(5).forEach { notice ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(notice.officialUrl))
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    onNotificationsClick()
                                }
                            }
                            .testTag("latest_update_item_${notice.id}"),
                        colors = CardDefaults.cardColors(containerColor = SurfaceSubtle),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "${notice.examId.uppercase()} • Cycle ${notice.admissionCycle}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        ),
                                        color = DarkNavy
                                    )
                                }
                                AdmissionStatusBadge(status = notice.admissionStatus)
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = notice.title,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp,
                                    lineHeight = 18.sp
                                ),
                                color = DarkNavy,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = notice.summary,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.5.sp,
                                    lineHeight = 16.sp
                                ),
                                color = TextSecondaryNavy,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Dates & Official Source row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    val deadlineLabel = when {
                                        notice.admissionStatus == AdmissionStatus.DATES_NOT_ANNOUNCED -> "Schedule: To be announced by authority"
                                        notice.applicationCloseDate != null -> "Deadline: ${notice.applicationCloseDate}"
                                        notice.examDate != null -> "Exam Date: ${notice.examDate}"
                                        else -> "Published: ${notice.publicationDate}"
                                    }
                                    Text(
                                        text = deadlineLabel,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = if (notice.admissionStatus == AdmissionStatus.DATES_NOT_ANNOUNCED) TextMuted else Color(0xFFDC2626)
                                    )
                                    Text(
                                        text = notice.lastVerifiedTime,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.5.sp
                                        ),
                                        color = TextMuted
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFEBF3FF))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Official Notice ↗",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 11.sp,
                                            color = RoyalBlue,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // 2. UPCOMING DEADLINES SECTION (Filtered strictly to valid future closing dates)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Upcoming Deadlines",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    ),
                    color = DarkNavy
                )

                Text(
                    text = "See All →",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = RoyalBlue,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.5.sp
                    ),
                    modifier = Modifier
                        .clickable { onSeeAllDeadlinesClick() }
                        .testTag("see_all_deadlines_button")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            val upcomingDeadlines = remember(selectedSegment, officialNotices) {
                val pool = if (officialNotices.isNotEmpty()) {
                    officialNotices.filter { notice ->
                        !notice.isHistorical && when (selectedSegment) {
                            Segment.MEDICAL -> notice.examCategory == ExamCategory.MEDICAL
                            Segment.ENGINEERING -> notice.examCategory == ExamCategory.ENGINEERING
                            Segment.MANAGEMENT -> notice.examCategory == ExamCategory.MANAGEMENT
                        }
                    }
                } else {
                    OfficialNoticesData.getNoticesForSegment(selectedSegment)
                }
                // Filter strictly to valid future deadlines (eliminates expired 2024/2025 dates & unannounced CMAT dates)
                pool.filter { it.hasFutureDeadline() }
                    .sortedBy { it.deadlineEpochMillis ?: Long.MAX_VALUE }
            }

            if (upcomingDeadlines.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceSubtle),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "No immediate application closing deadlines for ${selectedSegment.displayName}. Verified portals are checked daily.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            } else {
                upcomingDeadlines.take(3).forEach { notice ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(notice.officialUrl))
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    onSeeAllDeadlinesClick()
                                }
                            }
                            .testTag("deadline_card_${notice.id}"),
                        colors = CardDefaults.cardColors(containerColor = SurfaceSubtle),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFFFE8E8)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = notice.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    ),
                                    color = DarkNavy,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Portal: ${notice.officialUrl.removePrefix("https://").removeSuffix("/")}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 11.sp,
                                        color = RoyalBlue,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = notice.applicationCloseDate ?: notice.examDate ?: notice.publicationDate,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp
                                    ),
                                    color = Color(0xFFDC2626)
                                )
                                Text(
                                    text = "Apply ↗",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        color = RoyalBlue,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // 3. COLLEGE FEED (Discovery Preview Section)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "College Feed",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        ),
                        color = DarkNavy
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFFEF3C7))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Featured",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFFB45309)
                        )
                    }
                }

                Text(
                    text = "View All in Feed →",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = RoyalBlue,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.5.sp
                    ),
                    modifier = Modifier
                        .clickable { onNavigateToFeed() }
                        .testTag("home_view_all_feed_button")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            val previewAds = remember(collegeAds, selectedSegment) {
                val segmentAds = collegeAds.filter { it.isApprovedAndActive && it.segment == selectedSegment }
                if (segmentAds.isNotEmpty()) segmentAds.take(2) else collegeAds.filter { it.isApprovedAndActive }.take(2)
            }

            previewAds.forEach { ad ->
                CollegeFeedCard(
                    ad = ad,
                    onExplore = {
                        val matching = collegesList.firstOrNull { col ->
                            col.id == ad.collegeId || col.name.contains(ad.collegeName.take(6), ignoreCase = true)
                        } ?: College(
                            id = ad.collegeId,
                            name = ad.collegeName,
                            segment = ad.segment,
                            course = ad.courses.firstOrNull() ?: "Undergraduate",
                            rankRange = "Merit Based",
                            minRank = 1,
                            maxRank = 10000,
                            categoryMatch = "Target",
                            location = ad.location,
                            rating = "4.8 ★",
                            seats = 120,
                            feesPerYear = "Scholarships Available",
                            overview = ad.promotionalText,
                            cutoffsText = "Official admission portal: ${ad.officialDestinationUrl}",
                            feesBreakdown = "Check website prospectus.",
                            placementsText = "Top campus placements.",
                            websiteUrl = ad.officialDestinationUrl,
                            isDemo = false
                        )
                        onNavigateToCollegeDetail(matching)
                    },
                    onApply = {
                        onAdClick(ad.id)
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(ad.officialDestinationUrl))
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(14.dp))

            DemoDataBadge(
                text = "Official Data Source • Verified against NTA, MCC, BITS, VIT & IIM Portals",
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
