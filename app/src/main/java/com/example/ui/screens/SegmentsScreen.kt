package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Segment
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.SurfaceSubtle
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimaryNavy
import com.example.ui.theme.TextSecondaryNavy

/**
 * Screen 5: Choose Your Segment (Medical, Engineering, Management)
 * Selecting a segment saves choice and navigates DIRECTLY to the Home Dashboard.
 */
@Composable
fun SegmentsScreen(
    currentSegment: Segment,
    onSegmentSelected: (Segment) -> Unit,
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("segments_screen"),
        color = PureWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onBackClick != null) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("segments_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = DarkNavy
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = "Segments",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = DarkNavy
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Subtitle
            Text(
                text = "Choose Your Segment",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = DarkNavy
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Select your target admission stream to personalize college predictions, mock test series, and counselling deadlines.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.5.sp,
                    lineHeight = 19.sp
                ),
                color = TextSecondaryNavy
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Card 1: Medical
            SegmentOptionCard(
                title = "Medical",
                exams = "NEET UG | PG",
                subtitle = "MBBS, MD/MS, BDS, AYUSH",
                icon = Icons.Default.MedicalServices,
                accentColor = RoyalBlue,
                isSelected = currentSegment == Segment.MEDICAL,
                onClick = { onSegmentSelected(Segment.MEDICAL) },
                testTag = "segment_card_medical"
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Card 2: Engineering
            SegmentOptionCard(
                title = "Engineering",
                exams = "JEE Main | Advanced",
                subtitle = "B.Tech, B.E., Dual Degree",
                icon = Icons.Default.Engineering,
                accentColor = Color(0xFF7E3AF2),
                isSelected = currentSegment == Segment.ENGINEERING,
                onClick = { onSegmentSelected(Segment.ENGINEERING) },
                testTag = "segment_card_engineering"
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Card 3: Management
            SegmentOptionCard(
                title = "Management",
                exams = "CAT | XAT | CMAT",
                subtitle = "MBA, PGDM, Executive MBA",
                icon = Icons.Default.BusinessCenter,
                accentColor = Color(0xFF0E9F6E),
                isSelected = currentSegment == Segment.MANAGEMENT,
                onClick = { onSegmentSelected(Segment.MANAGEMENT) },
                testTag = "segment_card_management"
            )

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun SegmentOptionCard(
    title: String,
    exams: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .testTag(testTag),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFEEF4FF) else SurfaceSubtle
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 1.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = DarkNavy
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = exams,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    ),
                    color = TextPrimaryNavy
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp
                    ),
                    color = TextMuted
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Select",
                tint = TextMuted,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
