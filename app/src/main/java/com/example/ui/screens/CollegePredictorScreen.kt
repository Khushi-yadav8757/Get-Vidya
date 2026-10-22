package com.example.ui.screens

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.College
import com.example.data.model.PredictorInput
import com.example.data.model.Segment
import com.example.ui.components.DemoDataBadge
import com.example.ui.components.SegmentSelectorPills
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
 * Screen 7: College Predictor
 */
@Composable
fun CollegePredictorScreen(
    currentSegment: Segment,
    savedCollegeIds: Set<String>,
    onToggleSave: (String) -> Unit,
    onCollegeClick: (College) -> Unit,
    onPredict: (PredictorInput) -> List<College>,
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var segment by remember { mutableStateOf(currentSegment) }
    var rankOrScore by remember { mutableStateOf("35000") }
    var category by remember { mutableStateOf("General") }
    var quota by remember { mutableStateOf("All India") }
    var domicile by remember { mutableStateOf("Uttar Pradesh") }

    var categoryDropdownOpen by remember { mutableStateOf(false) }
    var quotaDropdownOpen by remember { mutableStateOf(false) }

    val categoryList = listOf("General", "OBC", "SC", "ST", "EWS")
    val quotaList = listOf("All India", "State Domicile", "Deemed/Central", "Management")

    var predictedColleges by remember {
        mutableStateOf(
            onPredict(
                PredictorInput(
                    segment = segment,
                    rankOrScore = rankOrScore,
                    category = category,
                    quota = quota,
                    domicile = domicile
                )
            )
        )
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("college_predictor_screen"),
        color = PureWhite
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onBackClick != null) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier.testTag("predictor_back_button")
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
                        text = "College Predictor",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = DarkNavy
                    )
                }

                // Segment Selector Pills
                SegmentSelectorPills(
                    selectedSegment = segment,
                    onSegmentSelected = { newSeg ->
                        segment = newSeg
                        rankOrScore = if (newSeg == Segment.MANAGEMENT) "99" else "35000"
                        predictedColleges = onPredict(
                            PredictorInput(
                                segment = newSeg,
                                rankOrScore = rankOrScore,
                                category = category,
                                quota = quota,
                                domicile = domicile
                            )
                        )
                    }
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Input fields
                val labelRank = when (segment) {
                    Segment.MEDICAL -> "NEET Rank"
                    Segment.ENGINEERING -> "JEE Main / Adv Rank"
                    Segment.MANAGEMENT -> "CAT Percentile"
                }

                Text(
                    text = labelRank,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = TextPrimaryNavy
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = rankOrScore,
                    onValueChange = { rankOrScore = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("predictor_rank_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RoyalBlue,
                        unfocusedBorderColor = DividerColor
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Category Field
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = TextPrimaryNavy
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { categoryDropdownOpen = true }
                            .testTag("predictor_category_input"),
                        trailingIcon = {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = TextMuted)
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RoyalBlue,
                            unfocusedBorderColor = DividerColor
                        )
                    )
                    DropdownMenu(
                        expanded = categoryDropdownOpen,
                        onDismissRequest = { categoryDropdownOpen = false }
                    ) {
                        categoryList.forEach { opt ->
                            DropdownMenuItem(
                                text = { Text(opt) },
                                onClick = {
                                    category = opt
                                    categoryDropdownOpen = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quota Field
                Text(
                    text = "Quota",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = TextPrimaryNavy
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = quota,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { quotaDropdownOpen = true }
                            .testTag("predictor_quota_input"),
                        trailingIcon = {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = TextMuted)
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RoyalBlue,
                            unfocusedBorderColor = DividerColor
                        )
                    )
                    DropdownMenu(
                        expanded = quotaDropdownOpen,
                        onDismissRequest = { quotaDropdownOpen = false }
                    ) {
                        quotaList.forEach { opt ->
                            DropdownMenuItem(
                                text = { Text(opt) },
                                onClick = {
                                    quota = opt
                                    quotaDropdownOpen = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // "Predict My Colleges →" Button
                Button(
                    onClick = {
                        predictedColleges = onPredict(
                            PredictorInput(
                                segment = segment,
                                rankOrScore = rankOrScore,
                                category = category,
                                quota = quota,
                                domicile = domicile
                            )
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("predict_colleges_button"),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RoyalBlue,
                        contentColor = PureWhite
                    )
                ) {
                    Text(
                        text = "Predict My Colleges →",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Your Top Matches",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        ),
                        color = DarkNavy
                    )
                    DemoDataBadge(text = "Sample Prediction")
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Predictions are algorithmic estimates based on historical cutoffs and not official admission guarantees.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.5.sp
                    ),
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(14.dp))
            }

            // Results List
            items(predictedColleges) { college ->
                val isSaved = savedCollegeIds.contains(college.id)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { onCollegeClick(college) }
                        .testTag("college_card_${college.id}"),
                    colors = CardDefaults.cardColors(containerColor = SurfaceSubtle),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(WaveLightBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = RoyalBlue,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = college.name,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    ),
                                    color = DarkNavy
                                )

                                val matchBadgeColor = when (college.categoryMatch) {
                                    "Dream" -> Color(0xFFD1FAE5) to Color(0xFF065F46)
                                    "Target" -> Color(0xFFFEF3C7) to Color(0xFF92400E)
                                    else -> Color(0xFFDBEAFE) to Color(0xFF1E40AF)
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(matchBadgeColor.first)
                                        .padding(horizontal = 7.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = college.categoryMatch,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = matchBadgeColor.second
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = "${college.course} • ${college.location}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = TextSecondaryNavy
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = college.rankRange,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.5.sp
                                ),
                                color = TextMuted
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        IconButton(
                            onClick = { onToggleSave(college.id) },
                            modifier = Modifier.testTag("bookmark_${college.id}")
                        ) {
                            Icon(
                                imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = if (isSaved) "Unsave" else "Save",
                                tint = if (isSaved) RoyalBlue else TextMuted
                            )
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
