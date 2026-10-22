package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.HeaderWave
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.TextSecondaryNavy
import kotlinx.coroutines.launch

/**
 * Page 1: Splash / Welcome Screen for Get Vidya.
 * Follows the design specification:
 * - White background with deep royal blue curved header occupying ~35% of the screen.
 * - Smooth layered wave shapes (white & light blue) at the bottom of the blue header.
 * - Get Vidya logo (mortarboard cap with orange flame swoosh).
 * - "Get Vidya" app name in large bold dark navy typography.
 * - "Your Admission Partner" tagline.
 * - "For Medical, Engineering & Management" subtitle.
 * - College campus illustration with academic building, clock tower, green lawns.
 * - Rounded royal blue "Get Started →" button.
 * - "Learn More" text button.
 */
@Composable
fun WelcomeScreen(
    onGetStartedClick: () -> Unit,
    onLearnMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Entrance animations
    val animAlpha = remember { Animatable(0f) }
    val animTranslateY = remember { Animatable(36f) }
    val logoScale = remember { Animatable(0.82f) }

    LaunchedEffect(Unit) {
        launch {
            animAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
            )
        }
        launch {
            animTranslateY.animateTo(
                targetValue = 0f,
                animationSpec = spring(dampingRatio = 0.82f, stiffness = 320f)
            )
        }
        launch {
            logoScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(dampingRatio = 0.72f, stiffness = 380f)
            )
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(PureWhite)
            .testTag("welcome_screen")
    ) {
        val screenHeight = maxHeight
        val headerHeight = (screenHeight * 0.35f).coerceIn(240.dp, 320.dp)

        // 1. Deep Royal Blue Curved Header occupying approx. top 35%
        HeaderWave(
            headerHeight = headerHeight,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // 2. Main Scrollable Content Container
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Spacing to position the logo gracefully below the blue header wave
            val topSpacerHeight = (headerHeight * 0.58f).coerceAtLeast(145.dp)
            Spacer(modifier = Modifier.height(topSpacerHeight))

            // Center Content with animated entrance
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp)
                    .padding(horizontal = 24.dp)
                    .graphicsLayer {
                        translationY = animTranslateY.value
                    }
                    .alpha(animAlpha.value),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Get Vidya Logo: Graduation cap + orange swoosh flame
                Box(
                    modifier = Modifier
                        .scale(logoScale.value)
                        .padding(bottom = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.get_vidya_logo),
                        contentDescription = stringResource(id = R.string.app_name),
                        modifier = Modifier
                            .size(width = 84.dp, height = 70.dp)
                            .testTag("get_vidya_logo"),
                        contentScale = ContentScale.Fit
                    )
                }

                // App Name: "Get Vidya" in large, bold, dark navy-blue typography
                Text(
                    text = "Get Vidya",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 35.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = DarkNavy,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("app_name_text")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Tagline: "Your Admission Partner"
                Text(
                    text = "Your Admission Partner",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 16.5.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = DarkNavy,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("tagline_text")
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Subtitle: "For Medical, Engineering & Management"
                Text(
                    text = "For Medical, Engineering &\nManagement",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 19.sp
                    ),
                    color = TextSecondaryNavy,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("subtitle_text")
                )

                Spacer(modifier = Modifier.height(18.dp))

                // College Campus Illustration: Academic building, clock tower, green lawns
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.college_campus),
                        contentDescription = "College Campus",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 165.dp)
                            .testTag("college_campus_illustration"),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Primary Action Button: Rounded Royal-Blue "Get Started →"
                Button(
                    onClick = onGetStartedClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("get_started_button"),
                    shape = RoundedCornerShape(27.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RoyalBlue,
                        contentColor = PureWhite
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 3.dp,
                        pressedElevation = 6.dp
                    )
                ) {
                    Text(
                        text = "Get Started →",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.2.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Secondary Action: "Learn More" text button
                TextButton(
                    onClick = onLearnMoreClick,
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("learn_more_button")
                ) {
                    Text(
                        text = "Learn More",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = RoyalBlue
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
