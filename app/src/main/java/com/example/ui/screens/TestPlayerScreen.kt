package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TestAttempt
import com.example.data.model.TestSeriesItem
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
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Screen 9: Test Player
 */
@Composable
fun TestPlayerScreen(
    testItem: TestSeriesItem,
    onTestSubmitted: (TestAttempt) -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentIndex by remember { mutableIntStateOf(0) }
    val userAnswers = remember { mutableStateMapOf<Int, Int>() } // questionIndex -> selectedOptionIndex
    val markedForReview = remember { mutableStateMapOf<Int, Boolean>() }

    var secondsRemaining by remember { mutableIntStateOf(testItem.durationMinutes * 60) }
    var isSubmitted by remember { mutableStateOf(false) }
    var showSubmitConfirmation by remember { mutableStateOf(false) }
    var calculatedAttempt by remember { mutableStateOf<TestAttempt?>(null) }

    // Intercept hardware back button to confirm exit
    var showExitDialog by remember { mutableStateOf(false) }
    BackHandler {
        if (isSubmitted) {
            onExit()
        } else {
            showExitDialog = true
        }
    }

    // Countdown Timer
    LaunchedEffect(isSubmitted) {
        while (!isSubmitted && secondsRemaining > 0) {
            delay(1000L)
            secondsRemaining--
        }
        if (secondsRemaining <= 0 && !isSubmitted) {
            // Auto submit
            isSubmitted = true
        }
    }

    val questions = testItem.questions

    fun computeAndFinish() {
        var correctCount = 0
        questions.forEachIndexed { idx, q ->
            val ans = userAnswers[idx]
            if (ans != null && ans == q.correctOptionIndex) {
                correctCount++
            }
        }
        val totalQuestions = questions.size
        val score = correctCount * 4 // NEET marking: +4
        val totalMarks = totalQuestions * 4
        val accuracy = if (userAnswers.isNotEmpty()) (correctCount * 100) / userAnswers.size else 0

        val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        val attempt = TestAttempt(
            id = UUID.randomUUID().toString(),
            testId = testItem.id,
            testTitle = testItem.title,
            score = score,
            totalMarks = totalMarks,
            correctAnswers = correctCount,
            totalQuestions = totalQuestions,
            accuracyPercentage = accuracy,
            timestamp = System.currentTimeMillis(),
            dateString = dateFormat.format(Date()),
            attemptedQuestionIds = questions.map { it.id }
        )
        calculatedAttempt = attempt
        isSubmitted = true
        onTestSubmitted(attempt)
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("test_player_screen"),
        color = PureWhite
    ) {
        if (!isSubmitted) {
            val currentQ = questions.getOrNull(currentIndex) ?: questions[0]
            val selectedOption = userAnswers[currentIndex]
            val isMarked = markedForReview[currentIndex] == true

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                // Top Row: Back button, Test Title, Timer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { showExitDialog = true },
                        modifier = Modifier.testTag("test_player_exit_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Exit Test",
                            tint = DarkNavy
                        )
                    }

                    Text(
                        text = testItem.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = DarkNavy,
                        maxLines = 1
                    )

                    // Timer Circular badge
                    val hours = secondsRemaining / 3600
                    val minutes = (secondsRemaining % 3600) / 60
                    val seconds = secondsRemaining % 60
                    val timeStr = String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(WaveLightBlue)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = timeStr,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            ),
                            color = RoyalBlue
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Stats Row: Answered, Not Answered, Marked
                val answeredCount = userAnswers.size
                val notAnsweredCount = questions.size - answeredCount
                val markedCount = markedForReview.values.count { it }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StatPill(color = Color(0xFF10B981), label = "Answered", count = answeredCount)
                    StatPill(color = Color(0xFFEF4444), label = "Not Answered", count = notAnsweredCount)
                    StatPill(color = Color(0xFF8B5CF6), label = "Marked", count = markedCount)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Question Palette Indicator Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    questions.forEachIndexed { idx, _ ->
                        val isCurr = idx == currentIndex
                        val isAns = userAnswers.containsKey(idx)
                        val isRev = markedForReview[idx] == true

                        val chipBg = when {
                            isCurr -> RoyalBlue
                            isRev -> Color(0xFF8B5CF6)
                            isAns -> Color(0xFF10B981)
                            else -> SurfaceSubtle
                        }

                        val textColor = if (isCurr || isRev || isAns) PureWhite else TextSecondaryNavy

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(chipBg)
                                .clickable { currentIndex = idx },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Q${idx + 1}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                color = textColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Scrollable Question Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Question Number and Subject
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Q.${currentIndex + 1} of ${questions.size}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = DarkNavy
                        )
                        DemoDataBadge(text = "${currentQ.subject} • Sample Question")
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Question Text
                    Text(
                        text = currentQ.questionText,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp,
                            lineHeight = 23.sp
                        ),
                        color = DarkNavy
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Multiple Choice Options (A, B, C, D)
                    val optionLetters = listOf("A", "B", "C", "D")
                    currentQ.options.forEachIndexed { optIndex, optionText ->
                        val isSelected = selectedOption == optIndex
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) RoyalBlue else DividerColor,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable {
                                    userAnswers[currentIndex] = optIndex
                                }
                                .testTag("option_${currentIndex}_$optIndex"),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFFEEF4FF) else PureWhite
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) RoyalBlue else SurfaceSubtle),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = optionLetters.getOrElse(optIndex) { "${optIndex + 1}" },
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = if (isSelected) PureWhite else DarkNavy
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Text(
                                    text = optionText,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) DarkNavy else TextSecondaryNavy
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mark for Review Button
                    OutlinedButton(
                        onClick = {
                            markedForReview[currentIndex] = !(markedForReview[currentIndex] ?: false)
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = if (isMarked) Icons.Default.Flag else Icons.Outlined.Flag,
                            contentDescription = null,
                            tint = if (isMarked) Color(0xFF8B5CF6) else TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isMarked) "Marked" else "Review",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isMarked) Color(0xFF8B5CF6) else TextSecondaryNavy
                        )
                    }

                    // Clear Answer Button
                    TextButton(
                        onClick = { userAnswers.remove(currentIndex) },
                        enabled = userAnswers.containsKey(currentIndex)
                    ) {
                        Text("Clear", color = TextMuted)
                    }

                    // Next or Submit Button
                    if (currentIndex < questions.size - 1) {
                        Button(
                            onClick = { currentIndex++ },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                        ) {
                            Text("Next →", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = { showSubmitConfirmation = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                        ) {
                            Text("Submit Test", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            // Results & Review Screen
            val attempt = calculatedAttempt!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Test Completed! 🎉",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = DarkNavy
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = testItem.title, style = MaterialTheme.typography.bodyMedium, color = TextSecondaryNavy)

                Spacer(modifier = Modifier.height(20.dp))

                // Score Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceSubtle),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${attempt.score} / ${attempt.totalMarks}",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 32.sp
                            ),
                            color = RoyalBlue
                        )
                        Text("Total Marks Scored", style = MaterialTheme.typography.bodySmall, color = TextMuted)

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${attempt.correctAnswers}/${attempt.totalQuestions}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF10B981)
                                )
                                Text("Correct", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${attempt.accuracyPercentage}%",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = DarkNavy
                                )
                                Text("Accuracy", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Answer Key & Explanations",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = DarkNavy,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(12.dp))

                questions.forEachIndexed { idx, q ->
                    val userAns = userAnswers[idx]
                    val isCorrect = userAns == q.correctOptionIndex

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceSubtle),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Q${idx + 1}: ${q.subject}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = DarkNavy
                                )
                                if (isCorrect) {
                                    Text("✓ Correct (+4)", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                } else if (userAns != null) {
                                    Text("✗ Incorrect (-1)", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                } else {
                                    Text("Unattempted (0)", color = TextMuted, fontSize = 12.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = q.questionText, style = MaterialTheme.typography.bodyMedium, color = DarkNavy)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Correct Answer: ${q.options[q.correctOptionIndex]}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = Color(0xFF10B981)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Explanation: ${q.explanation}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondaryNavy
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onExit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                ) {
                    Text("Return to Test Series", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Confirmation Modal before Submit
        if (showSubmitConfirmation) {
            AlertDialog(
                onDismissRequest = { showSubmitConfirmation = false },
                title = { Text("Submit Test?") },
                text = {
                    Text("You have answered ${userAnswers.size} of ${questions.size} questions. Are you sure you want to finalize your submission?")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showSubmitConfirmation = false
                            computeAndFinish()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                    ) {
                        Text("Yes, Submit")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSubmitConfirmation = false }) {
                        Text("Resume Test")
                    }
                }
            )
        }

        // Exit confirmation dialog
        if (showExitDialog) {
            AlertDialog(
                onDismissRequest = { showExitDialog = false },
                title = { Text("Quit Test?") },
                text = { Text("Your ongoing answers will not be saved if you leave now.") },
                confirmButton = {
                    Button(
                        onClick = {
                            showExitDialog = false
                            onExit()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                    ) {
                        Text("Quit")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showExitDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun StatPill(color: Color, label: String, count: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = "$label: $count",
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium
            ),
            color = TextSecondaryNavy
        )
    }
}
