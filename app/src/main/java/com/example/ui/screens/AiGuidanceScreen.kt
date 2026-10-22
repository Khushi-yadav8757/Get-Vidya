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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.data.repository.AiGuidanceService
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
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Screen 11: AI Guidance ("Ask Get Vidya AI")
 */
@Composable
fun AiGuidanceScreen(
    onOpenCounsellingClick: () -> Unit,
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    var lastUserPrompt by remember { mutableStateOf("") }

    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                id = "init_bot",
                text = "Hello! I am your Get Vidya AI Admission Assistant. Ask any question about entrance exam dates, counselling procedures, rank cutoffs, or college options.",
                isUser = false,
                isAiGenerated = true
            )
        )
    }

    val suggestedQuestions = listOf(
        "NEET cutoffs for AIIMS?",
        "JoSAA vs CSAB counselling rounds?",
        "CAT percentile required for IIMs?",
        "Documents required for choice filling?"
    )

    val isGeminiLive = AiGuidanceService.isGeminiConfigured()

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("ai_guidance_screen"),
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
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onBackClick != null) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier.testTag("ai_guidance_back")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = DarkNavy
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(WaveLightBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = RoyalBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "Ask Get Vidya AI",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = DarkNavy
                        )
                        Text(
                            text = if (isGeminiLive) "Online • Gemini Connected" else "Verified Guidance Mode",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp
                            ),
                            color = if (isGeminiLive) Color(0xFF10B981) else TextMuted
                        )
                    }
                }

                // Quick action to Counselling Guidance (Screen 12)
                TextButton(
                    onClick = onOpenCounsellingClick,
                    modifier = Modifier.testTag("open_counselling_btn")
                ) {
                    Text(
                        text = "Expert Help",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = RoyalBlue
                        )
                    )
                }
            }

            // Chat Messages List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages) { msg ->
                    ChatBubble(
                        message = msg,
                        onRetry = if (msg.isError && lastUserPrompt.isNotBlank()) {
                            {
                                isLoading = true
                                coroutineScope.launch {
                                    val reply = AiGuidanceService.getResponse(lastUserPrompt)
                                    messages.remove(msg)
                                    messages.add(reply)
                                    isLoading = false
                                }
                            }
                        } else null,
                        onViewReference = if (msg.isError) {
                            {
                                val reference = AiGuidanceService.getVerifiedKnowledgeReference(lastUserPrompt)
                                messages.add(
                                    ChatMessage(
                                        id = UUID.randomUUID().toString(),
                                        text = reference,
                                        isUser = false,
                                        isAiGenerated = false
                                    )
                                )
                            }
                        } else null
                    )
                }

                if (isLoading) {
                    item {
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = RoyalBlue
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Get Vidya AI is analyzing admission data...",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                    }
                }
            }

            // Suggested Questions Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(suggestedQuestions) { question ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceSubtle)
                            .clickable {
                                inputText = question
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = question,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                color = TextSecondaryNavy
                            )
                        )
                    }
                }
            }

            // Message Input Field
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Ask about colleges, cutoffs, or exams...", fontSize = 13.5.sp, color = TextMuted) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field"),
                    shape = RoundedCornerShape(24.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        val text = inputText.trim()
                        if (text.isNotEmpty() && !isLoading) {
                            lastUserPrompt = text
                            messages.add(ChatMessage(id = UUID.randomUUID().toString(), text = text, isUser = true))
                            inputText = ""
                            isLoading = true
                            coroutineScope.launch {
                                val reply = AiGuidanceService.getResponse(text)
                                messages.add(reply)
                                isLoading = false
                            }
                        }
                    }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RoyalBlue,
                        unfocusedBorderColor = DividerColor,
                        focusedContainerColor = SurfaceSubtle,
                        unfocusedContainerColor = SurfaceSubtle
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        val text = inputText.trim()
                        if (text.isNotEmpty() && !isLoading) {
                            lastUserPrompt = text
                            messages.add(ChatMessage(id = UUID.randomUUID().toString(), text = text, isUser = true))
                            inputText = ""
                            isLoading = true
                            coroutineScope.launch {
                                val reply = AiGuidanceService.getResponse(text)
                                messages.add(reply)
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(RoyalBlue)
                        .testTag("chat_send_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = PureWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(
    message: ChatMessage,
    onRetry: (() -> Unit)? = null,
    onViewReference: (() -> Unit)? = null
) {
    val isUser = message.isUser
    val isError = message.isError

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (isError) Color(0xFFFEE2E2) else WaveLightBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = if (isError) Color(0xFFDC2626) else RoyalBlue,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        val bubbleBg = when {
            isUser -> RoyalBlue
            isError -> Color(0xFFFEF2F2)
            else -> SurfaceSubtle
        }

        Column(
            modifier = Modifier
                .widthIn(max = 290.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .background(bubbleBg)
                .padding(14.dp)
        ) {
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.5.sp,
                    lineHeight = 19.sp
                ),
                color = when {
                    isUser -> PureWhite
                    isError -> Color(0xFF991B1B)
                    else -> DarkNavy
                }
            )

            if (!isUser && message.isAiGenerated) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "✦ AI-Generated Guidance • Verify with Official Portals",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = TextMuted
                )
            }

            if (isError) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (onRetry != null) {
                        TextButton(
                            onClick = onRetry,
                            modifier = Modifier.testTag("retry_ai_request_button")
                        ) {
                            Text(
                                text = "Retry ↺",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp
                                ),
                                color = Color(0xFFDC2626)
                            )
                        }
                    }

                    if (onViewReference != null) {
                        TextButton(
                            onClick = onViewReference,
                            modifier = Modifier.testTag("view_offline_reference_button")
                        ) {
                            Text(
                                text = "View Verified FAQs",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp
                                ),
                                color = RoyalBlue
                            )
                        }
                    }
                }
            }
        }
    }
}
