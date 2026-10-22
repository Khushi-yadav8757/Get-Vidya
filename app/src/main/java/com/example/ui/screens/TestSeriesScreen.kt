package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExamCategory
import com.example.data.model.ExamInfo
import com.example.data.model.Segment
import com.example.data.model.TestAttempt
import com.example.data.model.TestSeriesItem
import com.example.data.repository.ExamCatalogueData
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
 * Screen 8: Test Series Screen.
 * Updated to:
 * - Use category filter labels: All, Medical, Engineering, Management
 * - Properly filter both Exam Series and Practice Tests
 * - Responsive full-width cards without horizontal clipping
 * - Enforce minimum 25 questions per test set
 */
@Composable
fun TestSeriesScreen(
    testSeriesList: List<TestSeriesItem>,
    testAttempts: List<TestAttempt>,
    onStartTest: (TestSeriesItem) -> Unit,
    onSeeAllAttemptsClick: () -> Unit,
    onExamClick: (ExamInfo) -> Unit = {},
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Medical", "Engineering", "Management")

    val filteredTests = remember(selectedCategory, testSeriesList) {
        when (selectedCategory) {
            "Medical" -> testSeriesList.filter {
                it.segment == Segment.MEDICAL ||
                    it.examCategory.equals("Medical", ignoreCase = true) ||
                    it.examCategory.equals("NEET", ignoreCase = true)
            }
            "Engineering" -> testSeriesList.filter {
                it.segment == Segment.ENGINEERING ||
                    it.examCategory.equals("Engineering", ignoreCase = true) ||
                    it.examCategory.equals("JEE", ignoreCase = true)
            }
            "Management" -> testSeriesList.filter {
                it.segment == Segment.MANAGEMENT ||
                    it.examCategory.equals("Management", ignoreCase = true) ||
                    it.examCategory.equals("CAT", ignoreCase = true)
            }
            else -> testSeriesList
        }
    }

    val filteredExams = remember(selectedCategory) {
        when (selectedCategory) {
            "Medical" -> ExamCatalogueData.allExams.filter { it.category == ExamCategory.MEDICAL }
            "Engineering" -> ExamCatalogueData.allExams.filter { it.category == ExamCategory.ENGINEERING }
            "Management" -> ExamCatalogueData.allExams.filter { it.category == ExamCategory.MANAGEMENT }
            else -> ExamCatalogueData.allExams
        }
    }

    val latestAttempt = testAttempts.firstOrNull()

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("test_series_screen"),
        color = PureWhite
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Top App Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PureWhite)
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("test_series_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = DarkNavy,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "Test Series",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 21.sp
                        ),
                        color = DarkNavy
                    )
                }

                HorizontalDivider(color = DividerColor.copy(alpha = 0.6f), thickness = 1.dp)
            }

            // 2. Horizontally Aligned Category Filters: [ All | Medical | Engineering | Management ]
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(categories) { category ->
                    val isSelected = category == selectedCategory

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) RoyalBlue else SurfaceSubtle)
                            .then(
                                if (!isSelected) {
                                    Modifier.border(
                                        BorderStroke(1.dp, DividerColor),
                                        RoundedCornerShape(20.dp)
                                    )
                                } else {
                                    Modifier
                                }
                            )
                            .clickable { selectedCategory = category }
                            .padding(horizontal = 18.dp, vertical = 8.dp)
                            .testTag("test_category_chip_$category"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.5.sp
                            ),
                            color = if (isSelected) PureWhite else TextSecondaryNavy
                        )
                    }
                }
            }

            // 3. Main Scrollable List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Exam Series Catalogue Section Header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedCategory == "All") "Supported Entrance Exams" else "$selectedCategory Entrance Exams",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.5.sp
                            ),
                            color = DarkNavy
                        )

                        Text(
                            text = "${filteredExams.size} Exams Available",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            ),
                            color = RoyalBlue
                        )
                    }
                }

                // Dedicated Exam Cards (Full-Width, Responsive, No Horizontal Clipping)
                items(filteredExams, key = { it.id }) { exam ->
                    ExamCatalogueCard(
                        exam = exam,
                        onViewSetsClick = { onExamClick(exam) }
                    )
                }

                // Quick Practice Tests Header
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedCategory == "All") "Quick Practice Tests" else "$selectedCategory Practice Tests",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = DarkNavy
                        )

                        Text(
                            text = "${filteredTests.size} ${if (filteredTests.size == 1) "Test" else "Tests"}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.5.sp
                            ),
                            color = TextMuted
                        )
                    }
                }

                if (filteredTests.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceSubtle),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, DividerColor)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Assignment,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No practice tests for $selectedCategory yet",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.5.sp
                                    ),
                                    color = DarkNavy
                                )
                                Text(
                                    text = "Select an exam above to practice with its 5 dedicated test sets.",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                    color = TextMuted
                                )
                            }
                        }
                    }
                } else {
                    items(filteredTests, key = { it.id }) { testItem ->
                        TestCard(
                            testItem = testItem,
                            onStartTest = { onStartTest(testItem) }
                        )
                    }
                }

                // Recent Attempt Performance Summary
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Attempt Analytics",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = DarkNavy
                        )

                        if (testAttempts.isNotEmpty()) {
                            Text(
                                text = "View History →",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp
                                ),
                                color = RoyalBlue,
                                modifier = Modifier
                                    .clickable { onSeeAllAttemptsClick() }
                                    .testTag("see_all_attempts_button")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (latestAttempt != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("latest_attempt_card"),
                            colors = CardDefaults.cardColors(containerColor = PureWhite),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, DividerColor),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = latestAttempt.testTitle,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.5.sp
                                        ),
                                        color = DarkNavy,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = latestAttempt.dateString,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                                        color = TextMuted
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "${latestAttempt.score}/${latestAttempt.totalMarks}",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            ),
                                            color = RoyalBlue
                                        )
                                        Text(text = "Score", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "${latestAttempt.correctAnswers}/${latestAttempt.totalQuestions}",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            ),
                                            color = Color(0xFF16A34A)
                                        )
                                        Text(text = "Correct", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "${latestAttempt.accuracyPercentage}%",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            ),
                                            color = DarkNavy
                                        )
                                        Text(text = "Accuracy", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    }
                                }
                            }
                        }
                    } else {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SurfaceSubtle),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, DividerColor)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Assignment,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "No Tests Completed Yet",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    ),
                                    color = DarkNavy
                                )
                                Text(
                                    text = "Start any test above to record your score, speed, and accuracy.",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                    color = TextMuted
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))
                }
            }
        }
    }
}

/**
 * Individual test card item.
 */
@Composable
private fun TestCard(
    testItem: TestSeriesItem,
    onStartTest: () -> Unit
) {
    val (badgeBg, badgeTextColor) = when (testItem.examCategory.uppercase()) {
        "MEDICAL", "NEET" -> Color(0xFFEEF4FF) to RoyalBlue
        "ENGINEERING", "JEE" -> Color(0xFFF3E8FF) to Color(0xFF7E3AF2)
        "MANAGEMENT", "CAT" -> Color(0xFFECFDF5) to Color(0xFF059669)
        else -> WaveLightBlue to RoyalBlue
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("test_card_${testItem.id}"),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, DividerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Category Badge & Duration Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeBg)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = testItem.examCategory,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp
                        ),
                        color = badgeTextColor
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = "Duration",
                        tint = TextMuted,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "${testItem.durationMinutes} Minutes",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.5.sp
                        ),
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Test Title
            Text(
                text = testItem.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.5.sp,
                    lineHeight = 21.sp
                ),
                color = DarkNavy,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Question Count and Syllabus
            Text(
                text = "${testItem.questions.size} Questions • ${testItem.syllabus}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 12.5.sp,
                    lineHeight = 17.5.sp
                ),
                color = TextSecondaryNavy,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(14.dp))

            val canStart = testItem.questions.size >= 25

            Button(
                onClick = onStartTest,
                enabled = canStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("start_test_button_${testItem.id}"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RoyalBlue,
                    contentColor = PureWhite,
                    disabledContainerColor = Color(0xFFE2E8F0),
                    disabledContentColor = TextMuted
                )
            ) {
                Text(
                    text = if (canStart) "Start Test →" else "Min 25 Questions Required",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp
                    )
                )
            }
        }
    }
}

/**
 * Full-width responsive card for an entrance exam series.
 * Displays exam name, institution/conducting body, category, 5-sets badge, and View Sets button.
 */
@Composable
private fun ExamCatalogueCard(
    exam: ExamInfo,
    onViewSetsClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("exam_catalogue_card_${exam.id}"),
        colors = CardDefaults.cardColors(containerColor = SurfaceSubtle),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, DividerColor)
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
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(WaveLightBlue)
                        .padding(horizontal = 9.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = exam.category.displayName,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = RoyalBlue
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFECFDF5))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "5 Sets • 25 Qs Each",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = Color(0xFF059669)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = exam.name,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                color = DarkNavy
            )

            Text(
                text = exam.conductingBody,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = RoyalBlue
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = exam.description,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    lineHeight = 16.5.sp
                ),
                color = TextSecondaryNavy,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onViewSetsClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .testTag("view_exam_sets_${exam.id}"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RoyalBlue,
                    contentColor = PureWhite
                )
            ) {
                Text(
                    text = "View 5 Test Sets →",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                )
            }
        }
    }
}
