package com.example

import android.os.Bundle
import android.widget.Toast
import com.example.data.firebase.configureAppCheck
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.data.model.College
import com.example.data.model.ExamInfo
import com.example.data.model.Segment
import com.example.data.model.TestSeriesItem
import com.example.data.model.UserProfile
import com.example.data.repository.GetVidyaRepository
import com.example.data.service.QuestionSelectionResult
import com.example.data.service.QuestionSelectionService
import com.example.ui.components.BottomNavTab
import com.example.ui.components.GetVidyaBottomBar
import com.example.ui.screens.AdmissionUpdatesScreen
import com.example.ui.screens.AdvertiserDashboardScreen
import com.example.ui.screens.AiGuidanceScreen
import com.example.ui.screens.CollegeDetailsScreen
import com.example.ui.screens.CollegePredictorScreen
import com.example.ui.screens.CounsellingGuidanceScreen
import com.example.ui.screens.ExamTestSetsScreen
import com.example.ui.screens.FeedScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LearnMoreScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SegmentsScreen
import com.example.ui.screens.TestPlayerScreen
import com.example.ui.screens.TestSeriesScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.GetVidyaTheme
import com.example.ui.theme.PureWhite

sealed interface AppDestination {
    data object Welcome : AppDestination
    data object Login : AppDestination
    data object LearnMore : AppDestination
    data object SegmentsSelection : AppDestination
    data class Main(val tab: BottomNavTab = BottomNavTab.HOME) : AppDestination
    data object AdmissionUpdates : AppDestination
    data class CollegeDetail(val college: College) : AppDestination
    data class ExamTestSets(val exam: ExamInfo) : AppDestination
    data class TestPlayer(val testItem: TestSeriesItem) : AppDestination
    data object CounsellingGuidance : AppDestination
    data object Notifications : AppDestination
    data object AdvertiserDashboard : AppDestination
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureAppCheck(this, intent)
        enableEdgeToEdge()
        setContent {
            GetVidyaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = PureWhite
                ) {
                    GetVidyaMasterApp()
                }
            }
        }
    }
}

@Composable
fun GetVidyaMasterApp() {
    val context = LocalContext.current
    val repository = remember { GetVidyaRepository.getInstance(context) }

    val userProfile by repository.userProfile.collectAsState()
    val selectedSegment by repository.selectedSegment.collectAsState()
    val savedCollegeIds by repository.savedCollegeIds.collectAsState()
    val testAttempts by repository.testAttempts.collectAsState()
    val notifications by repository.notifications.collectAsState()
    val collegeAds by repository.collegeAds.collectAsState()
    val officialNotices by repository.officialNotices.collectAsState()
    val lastSyncTime by repository.lastSyncTime.collectAsState()

    val unreadNotifCount = remember(notifications) {
        notifications.count { !it.isRead }
    }

    val savedCollegesList = remember(savedCollegeIds, repository.collegesList) {
        repository.collegesList.filter { savedCollegeIds.contains(it.id) }
    }

    // Navigation backstack
    val backStack = remember {
        mutableStateListOf<AppDestination>(
            if (userProfile.isOnboarded) {
                AppDestination.Main(BottomNavTab.HOME)
            } else {
                AppDestination.Welcome
            }
        )
    }

    val currentDestination = backStack.lastOrNull() ?: AppDestination.Welcome

    fun navigateTo(dest: AppDestination) {
        backStack.add(dest)
    }

    fun popBack(): Boolean {
        return if (backStack.size > 1) {
            backStack.removeAt(backStack.size - 1)
            true
        } else {
            false
        }
    }

    // Hardware Back Button Handler
    BackHandler(enabled = backStack.size > 1) {
        val current = backStack.lastOrNull()
        if (current is AppDestination.Main && current.tab != BottomNavTab.HOME) {
            // If in Main on another tab, return to Home tab
            backStack[backStack.size - 1] = AppDestination.Main(BottomNavTab.HOME)
        } else {
            popBack()
        }
    }

    AnimatedContent(
        targetState = currentDestination,
        transitionSpec = {
            (slideInHorizontally { it / 3 } + fadeIn()).togetherWith(slideOutHorizontally { -it / 3 } + fadeOut())
        },
        label = "AppNavigation"
    ) { dest ->
        when (dest) {
            is AppDestination.Welcome -> {
                WelcomeScreen(
                    onGetStartedClick = { navigateTo(AppDestination.Login) },
                    onLearnMoreClick = { navigateTo(AppDestination.LearnMore) }
                )
            }

            is AppDestination.Login -> {
                LoginScreen(
                    onLoginSuccess = { _ ->
                        navigateTo(AppDestination.SegmentsSelection)
                    },
                    onBackClick = { popBack() }
                )
            }

            is AppDestination.LearnMore -> {
                LearnMoreScreen(
                    onBackClick = { popBack() },
                    onGetStartedClick = {
                        popBack()
                        navigateTo(AppDestination.Login)
                    }
                )
            }

            is AppDestination.SegmentsSelection -> {
                SegmentsScreen(
                    currentSegment = selectedSegment,
                    onSegmentSelected = { seg ->
                        repository.setSegment(seg)
                        // Navigates directly to Home Dashboard
                        backStack.clear()
                        backStack.add(AppDestination.Main(BottomNavTab.HOME))
                    },
                    onBackClick = { popBack() }
                )
            }

            is AppDestination.Main -> {
                Scaffold(
                    bottomBar = {
                        GetVidyaBottomBar(
                            selectedTab = dest.tab,
                            onTabSelected = { newTab ->
                                backStack[backStack.size - 1] = AppDestination.Main(newTab)
                            }
                        )
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (dest.tab) {
                            BottomNavTab.HOME -> {
                                HomeScreen(
                                    userProfile = userProfile,
                                    selectedSegment = selectedSegment,
                                    onSegmentChange = { seg -> repository.setSegment(seg) },
                                    unreadNotificationCount = unreadNotifCount,
                                    onNotificationsClick = { navigateTo(AppDestination.Notifications) },
                                    onViewUpdateDetailsClick = { _ -> navigateTo(AppDestination.AdmissionUpdates) },
                                    onSeeAllDeadlinesClick = { navigateTo(AppDestination.AdmissionUpdates) },
                                    admissionUpdates = repository.admissionUpdates,
                                    collegeAds = collegeAds,
                                    collegesList = repository.collegesList,
                                    officialNotices = officialNotices,
                                    lastSyncTime = lastSyncTime,
                                    onNavigateToFeed = {
                                        backStack[backStack.size - 1] = AppDestination.Main(BottomNavTab.FEED)
                                    },
                                    onNavigateToCollegeDetail = { col -> navigateTo(AppDestination.CollegeDetail(col)) },
                                    onAdClick = { id -> repository.recordAdClick(id) }
                                )
                            }

                            BottomNavTab.FEED -> {
                                FeedScreen(
                                    approvedAds = repository.getApprovedCollegeAds(),
                                    collegesList = repository.collegesList,
                                    onExploreCollege = { col -> navigateTo(AppDestination.CollegeDetail(col)) },
                                    onAdClick = { id -> repository.recordAdClick(id) },
                                    onAdImpression = { id -> repository.recordAdImpression(id) },
                                    onOpenPartnerPortal = { navigateTo(AppDestination.AdvertiserDashboard) },
                                    onBackClick = {
                                        backStack[backStack.size - 1] = AppDestination.Main(BottomNavTab.HOME)
                                    }
                                )
                            }

                            BottomNavTab.TESTS -> {
                                TestSeriesScreen(
                                    testSeriesList = repository.testSeriesList,
                                    testAttempts = testAttempts,
                                    onStartTest = { testItem ->
                                        val candidatePool = testItem.questions
                                        val result = QuestionSelectionService.prepareAttemptQuestions(
                                            examId = testItem.id,
                                            setId = testItem.id,
                                            candidateQuestions = candidatePool,
                                            userAttempts = testAttempts,
                                            targetCount = 25
                                        )
                                        when (result) {
                                            is QuestionSelectionResult.Success -> {
                                                if (result.infoMessage != null) {
                                                    Toast.makeText(context, result.infoMessage, Toast.LENGTH_SHORT).show()
                                                }
                                                navigateTo(
                                                    AppDestination.TestPlayer(
                                                        result.testItem.copy(
                                                            title = testItem.title,
                                                            examCategory = testItem.examCategory,
                                                            syllabus = testItem.syllabus,
                                                            durationMinutes = testItem.durationMinutes
                                                        )
                                                    )
                                                )
                                            }
                                            is QuestionSelectionResult.InsufficientQuestions -> {
                                                Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    },
                                    onExamClick = { exam -> navigateTo(AppDestination.ExamTestSets(exam)) },
                                    onSeeAllAttemptsClick = {
                                        backStack[backStack.size - 1] = AppDestination.Main(BottomNavTab.PROFILE)
                                    },
                                    onBackClick = {
                                        backStack[backStack.size - 1] = AppDestination.Main(BottomNavTab.HOME)
                                    }
                                )
                            }

                            BottomNavTab.PREDICTOR -> {
                                CollegePredictorScreen(
                                    currentSegment = selectedSegment,
                                    savedCollegeIds = savedCollegeIds,
                                    onToggleSave = { id -> repository.toggleSaveCollege(id) },
                                    onCollegeClick = { col -> navigateTo(AppDestination.CollegeDetail(col)) },
                                    onPredict = { input -> repository.predictColleges(input) }
                                )
                            }

                            BottomNavTab.GUIDANCE -> {
                                AiGuidanceScreen(
                                    onOpenCounsellingClick = { navigateTo(AppDestination.CounsellingGuidance) }
                                )
                            }

                            BottomNavTab.PROFILE -> {
                                ProfileScreen(
                                    userProfile = userProfile,
                                    savedColleges = savedCollegesList,
                                    testAttempts = testAttempts,
                                    onSaveProfile = { updated -> repository.saveProfile(updated) },
                                    onNavigateToNotifications = { navigateTo(AppDestination.Notifications) },
                                    onNavigateToCollegeDetail = { col -> navigateTo(AppDestination.CollegeDetail(col)) },
                                    onOpenAdvertiserDashboard = { navigateTo(AppDestination.AdvertiserDashboard) },
                                    onLogout = {
                                        repository.logout()
                                        backStack.clear()
                                        backStack.add(AppDestination.Welcome)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            is AppDestination.AdmissionUpdates -> {
                AdmissionUpdatesScreen(
                    officialNotices = officialNotices,
                    lastSyncTime = lastSyncTime,
                    onBackClick = { popBack() }
                )
            }

            is AppDestination.CollegeDetail -> {
                CollegeDetailsScreen(
                    college = dest.college,
                    isSaved = savedCollegeIds.contains(dest.college.id),
                    onToggleSave = { repository.toggleSaveCollege(dest.college.id) },
                    onBackClick = { popBack() }
                )
            }

            is AppDestination.ExamTestSets -> {
                ExamTestSetsScreen(
                    exam = dest.exam,
                    onStartTestSet = { testSet ->
                        val candidatePool = QuestionSelectionService.getFullCandidatePool(testSet)
                        val result = QuestionSelectionService.prepareAttemptQuestions(
                            examId = testSet.examId,
                            setId = testSet.id,
                            candidateQuestions = candidatePool,
                            userAttempts = testAttempts,
                            targetCount = 25
                        )
                        when (result) {
                            is QuestionSelectionResult.Success -> {
                                if (result.infoMessage != null) {
                                    Toast.makeText(context, result.infoMessage, Toast.LENGTH_SHORT).show()
                                }
                                navigateTo(
                                    AppDestination.TestPlayer(
                                        result.testItem.copy(
                                            id = testSet.id,
                                            title = testSet.title,
                                            examCategory = dest.exam.name,
                                            syllabus = "${testSet.subjects} (${testSet.sessionShift})",
                                            durationMinutes = testSet.durationMinutes
                                        )
                                    )
                                )
                            }
                            is QuestionSelectionResult.InsufficientQuestions -> {
                                Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    onBackClick = { popBack() }
                )
            }

            is AppDestination.TestPlayer -> {
                TestPlayerScreen(
                    testItem = dest.testItem,
                    onTestSubmitted = { attempt -> repository.recordTestAttempt(attempt) },
                    onExit = { popBack() }
                )
            }

            is AppDestination.CounsellingGuidance -> {
                CounsellingGuidanceScreen(
                    currentSegment = selectedSegment,
                    studentName = userProfile.name,
                    studentPhone = userProfile.phone,
                    onBookSession = { booking -> repository.addCounsellingBooking(booking) },
                    onBackClick = { popBack() }
                )
            }

            is AppDestination.Notifications -> {
                NotificationsScreen(
                    notifications = notifications,
                    onMarkAsRead = { id -> repository.markNotificationAsRead(id) },
                    onMarkAllAsRead = { repository.markAllNotificationsRead() },
                    onBackClick = { popBack() }
                )
            }

            is AppDestination.AdvertiserDashboard -> {
                AdvertiserDashboardScreen(
                    campaigns = collegeAds,
                    onSubmitCampaign = { newAd -> repository.submitAdvertisement(newAd) },
                    onBackClick = { popBack() }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainAppPreview() {
    GetVidyaTheme {
        GetVidyaMasterApp()
    }
}
