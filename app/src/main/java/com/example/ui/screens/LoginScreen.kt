package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.CircularProgressIndicator
import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.rememberCoroutineScope
import androidx.credentials.CredentialManager
import com.example.data.firebase.AuthManager
import com.example.data.repository.GetVidyaRepository
import com.example.R
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
 * Screen 2: Login / Sign Up Page
 */
@Composable
fun LoginScreen(
    onLoginSuccess: (isNewUser: Boolean) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onBackClick()
    }

    var isLoginSelected by remember { mutableStateOf(true) }
    var mobileNumber by remember { mutableStateOf("9876543210") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var otpStage by remember { mutableStateOf(false) }
    var enteredOtp by remember { mutableStateOf("1234") }
    var showTermsModal by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    var isGoogleLoading by remember { mutableStateOf(false) }
    var googleError by remember { mutableStateOf<String?>(null) }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("login_screen"),
        color = PureWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Nav
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.testTag("login_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = DarkNavy
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Logo
            Image(
                painter = painterResource(id = R.drawable.get_vidya_logo),
                contentDescription = "Get Vidya Logo",
                modifier = Modifier
                    .size(width = 72.dp, height = 60.dp)
                    .testTag("login_logo"),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Get Vidya",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = DarkNavy
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "Dream • Prepare • Get Admission",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = TextSecondaryNavy
            )

            Spacer(modifier = Modifier.height(26.dp))

            // Login / Sign Up Pill
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 380.dp)
                    .height(46.dp)
                    .clip(RoundedCornerShape(23.dp))
                    .background(SurfaceSubtle)
                    .border(1.dp, DividerColor, RoundedCornerShape(23.dp))
                    .padding(3.dp)
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isLoginSelected) RoyalBlue else Color.Transparent)
                            .clickable {
                                isLoginSelected = true
                                errorMessage = null
                                otpStage = false
                            }
                            .testTag("login_tab"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Login",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isLoginSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 14.sp
                            ),
                            color = if (isLoginSelected) PureWhite else TextSecondaryNavy
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (!isLoginSelected) RoyalBlue else Color.Transparent)
                            .clickable {
                                isLoginSelected = false
                                errorMessage = null
                                otpStage = false
                            }
                            .testTag("signup_tab"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Sign Up",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (!isLoginSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 14.sp
                            ),
                            color = if (!isLoginSelected) PureWhite else TextSecondaryNavy
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            // Form Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 380.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Mobile Number",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.5.sp
                    ),
                    color = TextPrimaryNavy,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                OutlinedTextField(
                    value = mobileNumber,
                    onValueChange = {
                        if (it.length <= 10 && it.all { c -> c.isDigit() }) {
                            mobileNumber = it
                            errorMessage = null
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("mobile_number_input"),
                    leadingIcon = {
                        Text(
                            text = "+91 ",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = DarkNavy,
                            modifier = Modifier.padding(start = 14.dp)
                        )
                    },
                    placeholder = { Text("98765 43210", color = TextMuted) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RoyalBlue,
                        unfocusedBorderColor = DividerColor,
                        focusedContainerColor = PureWhite,
                        unfocusedContainerColor = PureWhite
                    )
                )

                if (otpStage) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Enter OTP (Demonstration Code: 1234)",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.5.sp
                        ),
                        color = TextPrimaryNavy,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    OutlinedTextField(
                        value = enteredOtp,
                        onValueChange = {
                            if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                                enteredOtp = it
                                errorMessage = null
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("otp_input_field"),
                        placeholder = { Text("1234", color = TextMuted) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RoyalBlue,
                            unfocusedBorderColor = DividerColor
                        )
                    )
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        color = Color(0xFFEF4444),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                DemoDataBadge(
                    text = "Demo Mode: SMS gateway unconfigured. OTP validation runs in local demo mode.",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Action Button: Send OTP or Verify & Continue
                Button(
                    onClick = {
                        if (mobileNumber.length != 10) {
                            errorMessage = "Please enter a valid 10-digit Indian mobile number."
                        } else if (!otpStage) {
                            otpStage = true
                            errorMessage = null
                        } else {
                            if (enteredOtp == "1234" || enteredOtp.length == 4) {
                                // Successful verification
                                onLoginSuccess(!isLoginSelected)
                            } else {
                                errorMessage = "Invalid OTP. Use demo OTP 1234 to proceed."
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("send_otp_button"),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RoyalBlue,
                        contentColor = PureWhite
                    )
                ) {
                    Text(
                        text = if (otpStage) "Verify & Continue →" else "Send OTP",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Divider: "or continue with"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = DividerColor)
                    Text(
                        text = "  or continue with  ",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextMuted
                        )
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = DividerColor)
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Interactive Google Sign-In via Jetpack Credential Manager
                Button(
                    onClick = {
                        isGoogleLoading = true
                        googleError = null
                        val activity = context as? Activity
                        if (activity != null) {
                            AuthManager.onGoogleSignInClicked(
                                activity = activity,
                                credentialManager = credentialManager,
                                onAuthSuccess = { user ->
                                    isGoogleLoading = false
                                    val repo = GetVidyaRepository.getInstance(context)
                                    val profile = repo.userProfile.value
                                    repo.saveProfile(
                                        profile.copy(
                                            name = user.displayName?.ifBlank { profile.name } ?: profile.name,
                                            email = user.email?.ifBlank { profile.email } ?: profile.email,
                                            isOnboarded = true
                                        )
                                    )
                                    onLoginSuccess(!isLoginSelected)
                                },
                                onAuthError = { err ->
                                    isGoogleLoading = false
                                    googleError = err
                                },
                                scope = coroutineScope,
                                onAuthCancelled = {
                                    isGoogleLoading = false
                                }
                            )
                        } else {
                            isGoogleLoading = false
                            googleError = "Activity context required"
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("google_signin_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PureWhite,
                        contentColor = DarkNavy
                    ),
                    border = BorderStroke(1.2.dp, DividerColor),
                    enabled = !isGoogleLoading
                ) {
                    if (isGoogleLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp,
                            color = RoyalBlue
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "G",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFEA4335),
                                    fontSize = 18.sp
                                )
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Sign in with Google",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.5.sp,
                                    color = DarkNavy
                                )
                            )
                        }
                    }
                }

                if (googleError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = googleError ?: "",
                        color = Color(0xFFD32F2F),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Terms of Service Link
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "By continuing, you agree to our ",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    )
                    Text(
                        text = "Terms & Privacy Policy",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = RoyalBlue
                        ),
                        modifier = Modifier.clickable { showTermsModal = true }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        if (showTermsModal) {
            AlertDialog(
                onDismissRequest = { showTermsModal = false },
                title = { Text("Terms & Privacy Policy", fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("1. Data Privacy: Get Vidya stores student rank and preferences locally to provide personalized counselling predictions.")
                        Text("2. Algorithm Estimates: College predictions and cutoffs are derived from past year official MCC, JoSAA, and IIM datasets for informational use only.")
                        Text("3. Security: We do not share personal student phone numbers or credentials with unauthorized third parties.")
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showTermsModal = false }) {
                        Text("I Understand", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}
