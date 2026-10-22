package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.CounsellingBooking
import com.example.data.model.Segment
import com.example.ui.components.DemoDataBadge
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.DividerColor
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.SurfaceSubtle
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimaryNavy
import com.example.ui.theme.TextSecondaryNavy
import java.util.UUID

/**
 * Screen 12: Counselling Guidance
 */
@Composable
fun CounsellingGuidanceScreen(
    currentSegment: Segment,
    studentName: String,
    studentPhone: String,
    onBookSession: (CounsellingBooking) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showBookingModal by remember { mutableStateOf(false) }
    var bookingConfirmed by remember { mutableStateOf(false) }

    // Booking form state
    var formName by remember { mutableStateOf(studentName.ifEmpty { "Aryan Sharma" }) }
    var formPhone by remember { mutableStateOf(studentPhone.ifEmpty { "+91 98765 43210" }) }
    var formDate by remember { mutableStateOf("Tomorrow (5:00 PM)") }
    var formQuery by remember { mutableStateOf("") }
    var formError by remember { mutableStateOf<String?>(null) }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("counselling_guidance_screen"),
        color = PureWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.testTag("counselling_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = DarkNavy
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Counselling Guidance",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = DarkNavy
                )
            }

            // Main Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                // Counsellor Banner Image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(20.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.counsellor_banner),
                        contentDescription = "Expert Counsellor",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Heading
                Text(
                    text = "Get Expert Guidance",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    ),
                    color = DarkNavy
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Personalized 1-on-1 admission counselling to help you navigate state quotas, choice locking, and college seat optimization.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.5.sp,
                        lineHeight = 19.sp
                    ),
                    color = TextSecondaryNavy
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Checklist covering counselling services
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceSubtle),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        ChecklistRow(title = "Centralized Counselling Process", desc = "MCC, JoSAA, CSAB, and State portal registration support")
                        ChecklistRow(title = "Smart Choice Filling Tips", desc = "Optimal preference order to maximize seat allocation probability")
                        ChecklistRow(title = "Important Documents Audit", desc = "Category, domicile, income certificate verification checklist")
                        ChecklistRow(title = "One-on-One Mentorship", desc = "30-minute private video call with experienced education counsellor")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Book a Session Button
                Button(
                    onClick = { showBookingModal = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("book_session_button"),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RoyalBlue,
                        contentColor = PureWhite
                    )
                ) {
                    Text(
                        text = "Book a Session →",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                DemoDataBadge(
                    text = "Demonstration booking mode • Records appointment locally in app profile",
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Booking Form Modal
        if (showBookingModal) {
            AlertDialog(
                onDismissRequest = { showBookingModal = false },
                title = {
                    Text(
                        text = "Book Counselling Session",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = DarkNavy
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Stream: ${currentSegment.displayName}", style = MaterialTheme.typography.bodySmall, color = RoyalBlue)

                        OutlinedTextField(
                            value = formName,
                            onValueChange = { formName = it },
                            label = { Text("Student Full Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = formPhone,
                            onValueChange = { formPhone = it },
                            label = { Text("WhatsApp Phone Number") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = formDate,
                            onValueChange = { formDate = it },
                            label = { Text("Preferred Time Slot") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = formQuery,
                            onValueChange = { formQuery = it },
                            label = { Text("Specific Question / Target College") },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("e.g. Help with UP state medical choice locking") }
                        )

                        if (formError != null) {
                            Text(text = formError!!, color = Color(0xFFEF4444), fontSize = 12.sp)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (formName.isBlank() || formPhone.isBlank()) {
                                formError = "Please enter student name and contact number."
                            } else {
                                formError = null
                                val booking = CounsellingBooking(
                                    id = UUID.randomUUID().toString(),
                                    studentName = formName,
                                    phone = formPhone,
                                    segment = currentSegment,
                                    preferredDate = formDate,
                                    slot = "30-Min Session",
                                    query = formQuery,
                                    isDemo = true
                                )
                                onBookSession(booking)
                                showBookingModal = false
                                bookingConfirmed = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                    ) {
                        Text("Confirm Booking")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showBookingModal = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Booking Confirmed Success Modal
        if (bookingConfirmed) {
            AlertDialog(
                onDismissRequest = { bookingConfirmed = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Session Booked!")
                    }
                },
                text = {
                    Text(
                        text = "Your counselling session for $formName is confirmed for $formDate. Our senior mentor will reach out on $formPhone before the call.\n\n(Saved to your active local profile session)"
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { bookingConfirmed = false },
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                    ) {
                        Text("Done")
                    }
                }
            )
        }
    }
}

@Composable
private fun ChecklistRow(title: String, desc: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Color(0xFF10B981),
            modifier = Modifier.size(20.dp).padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = DarkNavy
            )
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = TextSecondaryNavy
            )
        }
    }
}
