package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
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
import com.example.R
import com.example.data.model.CollegeAdvertisement
import com.example.data.model.Segment
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.DividerColor
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.SurfaceSubtle
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondaryNavy
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvertiserDashboardScreen(
    campaigns: List<CollegeAdvertisement>,
    onSubmitCampaign: (CollegeAdvertisement) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBackClick)
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Campaigns & Analytics", "Submit New Campaign")

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("advertiser_dashboard_screen"),
        color = PureWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.testTag("advertiser_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = DarkNavy
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "College Partner Portal",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = DarkNavy
                    )
                    Text(
                        text = "Institution Ad Management & Review",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp
                        ),
                        color = TextSecondaryNavy
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(RoyalBlue.copy(alpha = 0.1f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "Partner Desk",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = RoyalBlue
                    )
                }
            }

            HorizontalDivider(color = DividerColor)

            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = PureWhite,
                contentColor = RoyalBlue,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = RoyalBlue
                    )
                }
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.5.sp
                                ),
                                color = if (selectedTab == index) RoyalBlue else TextMuted
                            )
                        }
                    )
                }
            }

            if (selectedTab == 0) {
                CampaignsOverviewTab(campaigns = campaigns)
            } else {
                SubmitCampaignTab(
                    onSubmit = { newAd ->
                        onSubmitCampaign(newAd)
                        Toast.makeText(
                            context,
                            "Campaign submitted for Compliance Review. Status: PENDING_REVIEW",
                            Toast.LENGTH_LONG
                        ).show()
                        selectedTab = 0
                    }
                )
            }
        }
    }
}

@Composable
private fun CampaignsOverviewTab(campaigns: List<CollegeAdvertisement>) {
    val context = LocalContext.current
    val totalImpressions = remember(campaigns) { campaigns.sumOf { it.impressionsCount } }
    val totalClicks = remember(campaigns) { campaigns.sumOf { it.clicksCount } }
    val approvedCount = remember(campaigns) { campaigns.count { it.isApprovedAndActive } }
    val pendingCount = remember(campaigns) { campaigns.count { !it.isApprovedAndActive } }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))

            // Security & Policy Guard Notice
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = RoyalBlue,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Institutional Governance Notice",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp
                            ),
                            color = DarkNavy
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Get Vidya strictly restricts advertisement publishing to verified educational institutions. Direct self-publishing and unverified promotions are blocked. Every campaign undergoes human integrity review against official university accreditations before appearing in the student feed.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                lineHeight = 17.sp
                            ),
                            color = TextSecondaryNavy
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Telemetry Metric Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "Total Impressions",
                    value = String.format("%,d", totalImpressions),
                    icon = Icons.Default.TrendingUp,
                    modifier = Modifier.weight(1f),
                    color = RoyalBlue
                )
                MetricCard(
                    title = "Verified Clicks",
                    value = String.format("%,d", totalClicks),
                    icon = Icons.Default.Campaign,
                    modifier = Modifier.weight(1f),
                    color = Color(0xFF0D9488)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "Live in Feed",
                    value = "$approvedCount Approved",
                    icon = Icons.Default.CheckCircle,
                    modifier = Modifier.weight(1f),
                    color = Color(0xFF16A34A)
                )
                MetricCard(
                    title = "Under Review",
                    value = "$pendingCount Pending",
                    icon = Icons.Default.HourglassTop,
                    modifier = Modifier.weight(1f),
                    color = Color(0xFFD97706)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Partner Campaigns (${campaigns.size})",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                color = DarkNavy
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        items(campaigns) { ad ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceSubtle)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = ad.collegeName,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.5.sp
                            ),
                            color = DarkNavy,
                            modifier = Modifier.weight(1f)
                        )

                        val (statusText, statusBg, statusColor) = if (ad.isApprovedAndActive) {
                            Triple("LIVE IN FEED", Color(0xFFDCFCE7), Color(0xFF15803D))
                        } else {
                            Triple("PENDING REVIEW", Color(0xFFFEF3C7), Color(0xFFB45309))
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(statusBg)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = statusColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = ad.headline,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        ),
                        color = RoyalBlue
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = ad.promotionalText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        ),
                        color = TextSecondaryNavy,
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "👀 ${ad.impressionsCount} views • 🔗 ${ad.clicksCount} clicks",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = TextMuted
                        )

                        Text(
                            text = "Official Link ↗",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            ),
                            color = RoyalBlue,
                            modifier = Modifier.clickable {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(ad.officialDestinationUrl))
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            }
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    color: Color = RoyalBlue
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceSubtle)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = TextSecondaryNavy
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                color = DarkNavy
            )
        }
    }
}

@Composable
private fun SubmitCampaignTab(
    onSubmit: (CollegeAdvertisement) -> Unit
) {
    var collegeName by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var coursesText by remember { mutableStateOf("") }
    var headline by remember { mutableStateOf("") }
    var promotionalText by remember { mutableStateOf("") }
    var destinationUrl by remember { mutableStateOf("https://") }
    var deadline by remember { mutableStateOf("") }
    var selectedSegment by remember { mutableStateOf(Segment.ENGINEERING) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Submit Institutional Ad Campaign",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                ),
                color = DarkNavy
            )

            Text(
                text = "Authorized college representatives may submit campaigns for verification.",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = TextSecondaryNavy
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = collegeName,
                onValueChange = { collegeName = it },
                label = { Text("College / University Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ad_input_college_name"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Campus Location (e.g. Pune, Maharashtra)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ad_input_location"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = coursesText,
                onValueChange = { coursesText = it },
                label = { Text("Key Courses (comma separated: B.Tech CSE, MBA, MBBS)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ad_input_courses"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = headline,
                onValueChange = { headline = it },
                label = { Text("Campaign Headline") },
                placeholder = { Text("e.g. Admissions 2025 Open • Merit Scholarships") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ad_input_headline"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = promotionalText,
                onValueChange = { promotionalText = it },
                label = { Text("Promotional Description") },
                placeholder = { Text("Highlight accreditations, placements, scholarships...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .testTag("ad_input_promo_text"),
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = destinationUrl,
                onValueChange = { destinationUrl = it },
                label = { Text("Official Destination URL") },
                placeholder = { Text("https://admissions.college.edu") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ad_input_url"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = deadline,
                onValueChange = { deadline = it },
                label = { Text("Verified Application Deadline / Status") },
                placeholder = { Text("e.g. Apply before 30 April 2025") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ad_input_deadline"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Target Academic Segment",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = DarkNavy
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Segment.values().forEach { seg ->
                    val isSelected = seg == selectedSegment
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) RoyalBlue else SurfaceSubtle)
                            .clickable { selectedSegment = seg }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = seg.displayName,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            ),
                            color = if (isSelected) PureWhite else DarkNavy
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (collegeName.isNotBlank() && headline.isNotBlank()) {
                        val coursesList = coursesText
                            .split(",")
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }
                            .ifEmpty { listOf("Undergraduate", "Postgraduate") }

                        val newAd = CollegeAdvertisement(
                            id = "ad_custom_${UUID.randomUUID().toString().take(8)}",
                            collegeId = "custom_${collegeName.lowercase().replace(" ", "_")}",
                            collegeName = collegeName.trim(),
                            localImageRes = R.drawable.college_campus,
                            headline = headline.trim(),
                            promotionalText = promotionalText.trim().ifEmpty { "Official campus admission campaign." },
                            courses = coursesList,
                            location = location.trim().ifEmpty { "India" },
                            officialDestinationUrl = if (destinationUrl.startsWith("http")) destinationUrl.trim() else "https://$destinationUrl",
                            admissionStatus = "Admissions Live",
                            verifiedDeadline = deadline.trim().ifEmpty { "Active for 2025 Cycle" },
                            isSponsored = true,
                            approvalStatus = "PENDING_REVIEW",
                            segment = selectedSegment,
                            advertiserAccountId = "inst_${UUID.randomUUID().toString().take(6)}",
                            campaignStartDate = "Today",
                            campaignEndDate = "30 Jun 2025",
                            impressionsCount = 0,
                            clicksCount = 0
                        )
                        onSubmit(newAd)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("submit_ad_button"),
                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                shape = RoundedCornerShape(14.dp),
                enabled = collegeName.isNotBlank() && headline.isNotBlank()
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Submit for Compliance Review",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
