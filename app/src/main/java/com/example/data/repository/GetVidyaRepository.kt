package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.AdmissionStatus
import com.example.data.model.ExamCategory
import com.example.data.model.OfficialAdmissionNotice
import com.example.data.model.VerificationStatus
import com.example.data.model.AdmissionUpdate
import com.example.data.model.College
import com.example.data.model.CollegeAdvertisement
import com.example.data.model.AdApprovalStatus
import com.example.data.model.CounsellingBooking
import com.example.data.model.NotificationItem
import com.example.data.model.PredictorInput
import com.example.data.model.Segment
import com.example.data.model.TestAttempt
import com.example.data.model.TestQuestion
import com.example.data.model.TestSeriesItem
import com.example.data.model.UserProfile
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import android.util.Log
import com.example.R
import com.example.data.firebase.AuthManager
import com.example.data.firebase.OperationType
import com.example.data.firebase.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GetVidyaRepository private constructor(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("get_vidya_prefs", Context.MODE_PRIVATE)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val firestoreDb = try {
        FirebaseFirestore.getInstance(context.getString(R.string.firestore_database_id))
    } catch (e: Exception) {
        Log.e("GetVidyaRepository", "Failed to get named Firestore instance", e)
        null
    }

    private var profileListener: ListenerRegistration? = null
    private var savedCollegesListener: ListenerRegistration? = null
    private var testAttemptsListener: ListenerRegistration? = null
    private var noticesListener: ListenerRegistration? = null
    private var adsListener: ListenerRegistration? = null
    private var userAdsListener: ListenerRegistration? = null

    // Official Admission Notices Live StateFlow
    private val _officialNotices = MutableStateFlow(OfficialNoticesData.allNotices)
    val officialNotices: StateFlow<List<OfficialAdmissionNotice>> = _officialNotices.asStateFlow()

    private val _lastSyncTime = MutableStateFlow(System.currentTimeMillis())
    val lastSyncTime: StateFlow<Long> = _lastSyncTime.asStateFlow()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val profileAdapter = moshi.adapter(UserProfile::class.java)
    private val attemptsListAdapter = moshi.adapter<List<TestAttempt>>(
        Types.newParameterizedType(List::class.java, TestAttempt::class.java)
    )
    private val bookingsListAdapter = moshi.adapter<List<CounsellingBooking>>(
        Types.newParameterizedType(List::class.java, CounsellingBooking::class.java)
    )
    private val notificationsListAdapter = moshi.adapter<List<NotificationItem>>(
        Types.newParameterizedType(List::class.java, NotificationItem::class.java)
    )

    // User Profile
    private val _userProfile = MutableStateFlow(loadProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    // Selected Segment (Medical, Engineering, Management)
    private val _selectedSegment = MutableStateFlow(loadSelectedSegment())
    val selectedSegment: StateFlow<Segment> = _selectedSegment.asStateFlow()

    // Saved Colleges IDs
    private val _savedCollegeIds = MutableStateFlow(loadSavedCollegeIds())
    val savedCollegeIds: StateFlow<Set<String>> = _savedCollegeIds.asStateFlow()

    // Test Attempts History
    private val _testAttempts = MutableStateFlow(loadTestAttempts())
    val testAttempts: StateFlow<List<TestAttempt>> = _testAttempts.asStateFlow()

    // Counselling Bookings
    private val _counsellingBookings = MutableStateFlow(loadCounsellingBookings())
    val counsellingBookings: StateFlow<List<CounsellingBooking>> = _counsellingBookings.asStateFlow()

    // Notifications
    private val _notifications = MutableStateFlow(loadNotifications())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    init {
        scope.launch {
            AuthManager.authStateFlow().collect { user ->
                if (user != null) {
                    attachFirestoreListeners(user.uid)
                } else {
                    detachFirestoreListeners()
                }
            }
        }
    }

    private fun requireUserId(): String {
        return Firebase.auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    private fun attachFirestoreListeners(uid: String) {
        val db = firestoreDb ?: return
        detachFirestoreListeners()

        // 1. User Profile Listener
        profileListener = db.collection("users").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.GET, "users/$uid")
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val fullName = snapshot.getString("fullName") ?: _userProfile.value.name
                    val email = snapshot.getString("email") ?: _userProfile.value.email
                    val targetExam = snapshot.getString("targetExam") ?: _userProfile.value.examAppeared
                    val dreamCollege = snapshot.getString("dreamCollege") ?: _userProfile.value.domicile
                    val phone = snapshot.getString("phone") ?: _userProfile.value.phone
                    val segString = snapshot.getString("selectedSegment")
                    if (!segString.isNullOrBlank()) {
                        try {
                            _selectedSegment.value = Segment.valueOf(segString)
                        } catch (_: Exception) {}
                    }
                    val updated = _userProfile.value.copy(
                        name = fullName,
                        email = email,
                        phone = phone,
                        examAppeared = targetExam,
                        domicile = dreamCollege,
                        isOnboarded = true
                    )
                    _userProfile.value = updated
                    prefs.edit().putString("user_profile", profileAdapter.toJson(updated)).apply()
                }
            }

        // 2. Saved Colleges Listener
        savedCollegesListener = db.collection("users").document(uid).collection("savedColleges")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, "users/$uid/savedColleges")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val ids = snapshot.documents.map { it.id }.toSet()
                    _savedCollegeIds.value = ids
                    prefs.edit().putStringSet("saved_colleges", ids).apply()
                }
            }

        // 3. Test Attempts Listener
        testAttemptsListener = db.collection("users").document(uid).collection("testAttempts")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, "users/$uid/testAttempts")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val attempts = snapshot.documents.mapNotNull { doc ->
                        try {
                            TestAttempt(
                                id = doc.id,
                                testId = doc.getString("testId") ?: "",
                                testTitle = doc.getString("examTitle") ?: doc.getString("testTitle") ?: "Mock Test",
                                score = doc.getLong("score")?.toInt() ?: 0,
                                totalMarks = doc.getLong("totalQuestions")?.toInt()?.times(4) ?: 100,
                                correctAnswers = doc.getLong("correctAnswers")?.toInt() ?: 0,
                                totalQuestions = doc.getLong("totalQuestions")?.toInt() ?: 25,
                                accuracyPercentage = doc.getDouble("percentileEstimate")?.toInt() ?: 80,
                                timestamp = doc.getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis(),
                                dateString = doc.getString("attemptDate") ?: "Recent",
                                attemptedQuestionIds = (doc.get("attemptedQuestionIds") as? List<*>)?.mapNotNull { (it as? Number)?.toInt() } ?: emptyList()
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    _testAttempts.value = attempts
                    prefs.edit().putString("test_attempts", attemptsListAdapter.toJson(attempts)).apply()
                }
            }

        // 4. Official Notices Listener
        noticesListener = db.collection("officialNotices")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, "officialNotices")
                    return@addSnapshotListener
                }
                if (snapshot != null && !snapshot.isEmpty) {
                    val parsedNotices = snapshot.documents.mapNotNull { doc ->
                        try {
                            val catString = doc.getString("category") ?: "Medical"
                            val examCategory = when {
                                catString.contains("Eng", ignoreCase = true) -> ExamCategory.ENGINEERING
                                catString.contains("Man", ignoreCase = true) -> ExamCategory.MANAGEMENT
                                else -> ExamCategory.MEDICAL
                            }
                            val rawStatus = doc.getString("admissionStatus")
                            val admissionStatus = AdmissionStatus.fromString(rawStatus)
                            val cycle = doc.getString("admissionCycle") ?: "2026-27"
                            val isHist = doc.getBoolean("isHistorical")
                                ?: (admissionStatus == AdmissionStatus.COMPLETED || cycle == "2024" || cycle == "2025")
                            val deadlineEpoch = doc.getLong("deadlineEpochMillis")
                            val lastVerifiedTs = doc.getTimestamp("lastVerifiedAt")?.toDate()?.time
                                ?: System.currentTimeMillis()

                            OfficialAdmissionNotice(
                                id = doc.id,
                                examId = doc.getString("examCode") ?: "exam",
                                examCategory = examCategory,
                                title = doc.getString("title") ?: "",
                                summary = doc.getString("summary") ?: "",
                                admissionCycle = cycle,
                                admissionStatus = admissionStatus,
                                publicationDate = doc.getString("publishedDate") ?: "Official Circular",
                                applicationOpenDate = doc.getString("applicationOpenDate"),
                                applicationCloseDate = doc.getString("applicationCloseDate"),
                                examDate = doc.getString("examDate"),
                                counsellingDeadline = doc.getString("counsellingDeadline"),
                                officialUrl = doc.getString("sourceUrl") ?: "https://nta.ac.in",
                                lastVerifiedTime = doc.getString("lastVerifiedTime") ?: "Verified from Official Portal",
                                lastVerifiedTimestamp = lastVerifiedTs,
                                isHistorical = isHist,
                                deadlineEpochMillis = deadlineEpoch,
                                verificationStatus = VerificationStatus.OFFICIAL_VERIFIED
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    if (parsedNotices.isNotEmpty()) {
                        val fsIds = parsedNotices.map { it.id }.toSet()
                        _officialNotices.value = parsedNotices + OfficialNoticesData.allNotices.filter { !fsIds.contains(it.id) }
                        _lastSyncTime.value = System.currentTimeMillis()

                        val notifItems = _officialNotices.value.filter { !it.isHistorical }.map { notice ->
                            NotificationItem(
                                id = notice.id,
                                title = "[${notice.admissionStatus.displayName}] ${notice.title}",
                                description = notice.summary,
                                timeAgo = notice.publicationDate,
                                category = notice.examCategory.displayName,
                                isRead = false,
                                officialUrl = notice.officialUrl,
                                isOfficialVerified = true
                            )
                        }
                        _notifications.value = notifItems
                    }
                }
            }

        // 5. Approved College Ads Listener (Public Feed)
        adsListener = db.collection("collegeAdvertisements")
            .whereEqualTo("approvalStatus", "APPROVED")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, "collegeAdvertisements")
                    return@addSnapshotListener
                }
                if (snapshot != null && !snapshot.isEmpty) {
                    val approvedList = snapshot.documents.mapNotNull { doc ->
                        try {
                            val segString = doc.getString("segment") ?: Segment.ENGINEERING.name
                            val segment = try { Segment.valueOf(segString) } catch (_: Exception) { Segment.ENGINEERING }
                            CollegeAdvertisement(
                                id = doc.id,
                                collegeId = doc.getString("collegeId") ?: doc.id,
                                collegeName = doc.getString("collegeName") ?: "",
                                localImageRes = R.drawable.college_campus,
                                headline = doc.getString("headline") ?: "",
                                promotionalText = doc.getString("promotionalText") ?: "",
                                courses = (doc.get("courses") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                                location = doc.getString("location") ?: "India",
                                officialDestinationUrl = doc.getString("officialDestinationUrl") ?: "",
                                admissionStatus = doc.getString("admissionStatus") ?: "Admissions Open",
                                verifiedDeadline = doc.getString("verifiedDeadline") ?: "",
                                isSponsored = true,
                                approvalStatus = "APPROVED",
                                segment = segment,
                                advertiserAccountId = doc.getString("advertiserAccountId") ?: ""
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    if (approvedList.isNotEmpty()) {
                        val approvedIds = approvedList.map { it.id }.toSet()
                        val pendingMine = _collegeAds.value.filter { it.approvalStatus == "PENDING_REVIEW" && !approvedIds.contains(it.id) }
                        _collegeAds.value = approvedList + pendingMine
                    }
                }
            }

        // 6. User's Own Submitted Campaigns Listener
        userAdsListener = db.collection("collegeAdvertisements")
            .whereEqualTo("advertiserAccountId", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (snapshot != null && !snapshot.isEmpty) {
                    val myAds = snapshot.documents.mapNotNull { doc ->
                        try {
                            val segString = doc.getString("segment") ?: Segment.ENGINEERING.name
                            val segment = try { Segment.valueOf(segString) } catch (_: Exception) { Segment.ENGINEERING }
                            CollegeAdvertisement(
                                id = doc.id,
                                collegeId = doc.getString("collegeId") ?: doc.id,
                                collegeName = doc.getString("collegeName") ?: "",
                                localImageRes = R.drawable.college_campus,
                                headline = doc.getString("headline") ?: "",
                                promotionalText = doc.getString("promotionalText") ?: "",
                                courses = (doc.get("courses") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                                location = doc.getString("location") ?: "India",
                                officialDestinationUrl = doc.getString("officialDestinationUrl") ?: "",
                                admissionStatus = doc.getString("admissionStatus") ?: "Submitted for Review",
                                verifiedDeadline = doc.getString("verifiedDeadline") ?: "",
                                isSponsored = true,
                                approvalStatus = doc.getString("approvalStatus") ?: "PENDING_REVIEW",
                                segment = segment,
                                advertiserAccountId = uid
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    if (myAds.isNotEmpty()) {
                        val myIds = myAds.map { it.id }.toSet()
                        _collegeAds.value = myAds + _collegeAds.value.filter { !myIds.contains(it.id) }
                    }
                }
            }
    }

    private fun detachFirestoreListeners() {
        profileListener?.remove()
        profileListener = null
        savedCollegesListener?.remove()
        savedCollegesListener = null
        testAttemptsListener?.remove()
        testAttemptsListener = null
        noticesListener?.remove()
        noticesListener = null
        adsListener?.remove()
        adsListener = null
        userAdsListener?.remove()
        userAdsListener = null
    }

    // --- Persistence Methods ---

    private fun loadProfile(): UserProfile {
        val json = prefs.getString("user_profile", null)
        return if (json != null) {
            try {
                profileAdapter.fromJson(json) ?: defaultProfile()
            } catch (e: Exception) {
                defaultProfile()
            }
        } else {
            defaultProfile()
        }
    }

    private fun defaultProfile(): UserProfile {
        val user = Firebase.auth.currentUser
        return UserProfile(
            name = user?.displayName ?: "Student",
            email = user?.email ?: "",
            phone = user?.phoneNumber ?: "",
            examAppeared = "NEET",
            scoreRank = "",
            category = "General",
            domicile = "Delhi",
            expectedBudget = "₹ 5 - 15 Lakhs",
            isOnboarded = user != null
        )
    }

    fun saveProfile(profile: UserProfile) {
        _userProfile.value = profile
        prefs.edit().putString("user_profile", profileAdapter.toJson(profile)).apply()

        // Sync to Firestore if authenticated
        val uid = Firebase.auth.currentUser?.uid
        if (uid != null && firestoreDb != null) {
            val payload = hashMapOf<String, Any>(
                "fullName" to profile.name.ifBlank { "Student" },
                "email" to profile.email.ifBlank { Firebase.auth.currentUser?.email ?: "student@getvidya.in" },
                "targetExam" to profile.examAppeared.ifBlank { "NEET" },
                "phone" to profile.phone,
                "selectedSegment" to _selectedSegment.value.name,
                "dreamCollege" to profile.domicile.ifBlank { "Top National College" },
                "percentileTarget" to (profile.scoreRank.toIntOrNull()?.coerceIn(0, 100) ?: 95),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            firestoreDb.collection("users").document(uid)
                .set(payload, SetOptions.merge())
                .addOnFailureListener { error ->
                    handleFirestoreError(error, OperationType.UPDATE, "users/$uid")
                }
        }
    }

    fun logout() {
        detachFirestoreListeners()
        val guest = UserProfile(
            name = "",
            email = "",
            phone = "",
            examAppeared = "NEET",
            scoreRank = "",
            category = "General",
            domicile = "Delhi",
            expectedBudget = "₹ 5 - 15 Lakhs",
            isOnboarded = false
        )
        _userProfile.value = guest
        prefs.edit().putString("user_profile", profileAdapter.toJson(guest)).apply()
        _savedCollegeIds.value = emptySet()
        prefs.edit().remove("saved_colleges").apply()
        _testAttempts.value = emptyList()
        prefs.edit().remove("test_attempts").apply()
    }

    private fun loadSelectedSegment(): Segment {
        val name = prefs.getString("selected_segment", Segment.MEDICAL.name)
        return try {
            Segment.valueOf(name ?: Segment.MEDICAL.name)
        } catch (e: Exception) {
            Segment.MEDICAL
        }
    }

    fun setSegment(segment: Segment) {
        _selectedSegment.value = segment
        prefs.edit().putString("selected_segment", segment.name).apply()

        val uid = Firebase.auth.currentUser?.uid
        if (uid != null && firestoreDb != null) {
            val docRef = firestoreDb.collection("users").document(uid)
            docRef.set(mapOf("selectedSegment" to segment.name), SetOptions.merge())
                .addOnFailureListener { error ->
                    handleFirestoreError(error, OperationType.UPDATE, docRef.path)
                }
        }
    }

    private fun loadSavedCollegeIds(): Set<String> {
        return prefs.getStringSet("saved_colleges", emptySet()) ?: emptySet()
    }

    fun toggleSaveCollege(collegeId: String) {
        val current = _savedCollegeIds.value.toMutableSet()
        val isNowSaved = !current.contains(collegeId)
        if (isNowSaved) {
            current.add(collegeId)
        } else {
            current.remove(collegeId)
        }
        _savedCollegeIds.value = current
        prefs.edit().putStringSet("saved_colleges", current).apply()

        val uid = Firebase.auth.currentUser?.uid
        if (uid != null && firestoreDb != null) {
            val docRef = firestoreDb.collection("users").document(uid).collection("savedColleges").document(collegeId)
            if (isNowSaved) {
                val col = collegesList.find { it.id == collegeId }
                val payload = hashMapOf<String, Any>(
                    "collegeId" to collegeId,
                    "collegeName" to (col?.name ?: collegeId),
                    "location" to (col?.location ?: "India"),
                    "rating" to 4.8,
                    "cutoffText" to (col?.rankRange ?: "Top Cutoff"),
                    "fees" to (col?.feesPerYear ?: "Annual Fees"),
                    "savedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload)
                    .addOnFailureListener { error ->
                        handleFirestoreError(error, OperationType.CREATE, docRef.path)
                    }
            } else {
                docRef.delete()
                    .addOnFailureListener { error ->
                        handleFirestoreError(error, OperationType.DELETE, docRef.path)
                    }
            }
        }
    }

    private fun loadTestAttempts(): List<TestAttempt> {
        val json = prefs.getString("test_attempts", null)
        if (json != null) {
            try {
                val list = attemptsListAdapter.fromJson(json)
                if (list != null) return list
            } catch (e: Exception) {
                // fall through
            }
        }
        return emptyList()
    }

    fun recordTestAttempt(attempt: TestAttempt) {
        val list = listOf(attempt) + _testAttempts.value.filter { it.id != attempt.id }
        _testAttempts.value = list
        prefs.edit().putString("test_attempts", attemptsListAdapter.toJson(list)).apply()

        val uid = Firebase.auth.currentUser?.uid
        if (uid != null && firestoreDb != null) {
            val docRef = firestoreDb.collection("users").document(uid).collection("testAttempts").document(attempt.id)
            val payload = hashMapOf<String, Any>(
                "testId" to attempt.testId,
                "examCode" to attempt.testId.take(30),
                "examTitle" to attempt.testTitle,
                "score" to attempt.score,
                "totalQuestions" to attempt.totalQuestions.coerceAtLeast(1),
                "correctAnswers" to attempt.correctAnswers,
                "percentileEstimate" to attempt.accuracyPercentage.toDouble(),
                "attemptDate" to attempt.dateString,
                "attemptedQuestionIds" to attempt.attemptedQuestionIds,
                "createdAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload)
                .addOnFailureListener { error ->
                    handleFirestoreError(error, OperationType.CREATE, docRef.path)
                }
        }
    }

    private fun loadCounsellingBookings(): List<CounsellingBooking> {
        val json = prefs.getString("counselling_bookings", null)
        if (json != null) {
            try {
                val list = bookingsListAdapter.fromJson(json)
                if (list != null) return list
            } catch (e: Exception) {
                // fall through
            }
        }
        return emptyList()
    }

    fun addCounsellingBooking(booking: CounsellingBooking) {
        val list = listOf(booking) + _counsellingBookings.value
        _counsellingBookings.value = list
        prefs.edit().putString("counselling_bookings", bookingsListAdapter.toJson(list)).apply()
    }

    private fun loadNotifications(): List<NotificationItem> {
        val json = prefs.getString("notifications_list", null)
        if (json != null) {
            try {
                val list = notificationsListAdapter.fromJson(json)
                if (list != null && list.isNotEmpty()) return list
            } catch (e: Exception) {
                // fall through
            }
        }
        return OfficialNoticesData.allNotices.map { notice ->
            NotificationItem(
                id = notice.id,
                title = notice.title,
                description = notice.summary,
                timeAgo = notice.publicationDate,
                category = notice.examCategory.displayName,
                isRead = false,
                officialUrl = notice.officialUrl,
                isOfficialVerified = true
            )
        }
    }

    fun markNotificationAsRead(id: String) {
        val list = _notifications.value.map {
            if (it.id == id) it.copy(isRead = true) else it
        }
        _notifications.value = list
        prefs.edit().putString("notifications_list", notificationsListAdapter.toJson(list)).apply()
    }

    fun markAllNotificationsRead() {
        val list = _notifications.value.map { it.copy(isRead = true) }
        _notifications.value = list
        prefs.edit().putString("notifications_list", notificationsListAdapter.toJson(list)).apply()
    }

    // --- College Advertisements System (Management & Verified Feed) ---

    private val _collegeAds = MutableStateFlow(loadDefaultCollegeAds())
    val collegeAds: StateFlow<List<CollegeAdvertisement>> = _collegeAds.asStateFlow()

    fun getApprovedCollegeAds(segment: Segment? = null): List<CollegeAdvertisement> {
        val approved = _collegeAds.value.filter { it.isApprovedAndActive }
        return if (segment != null) {
            val matching = approved.filter { it.segment == segment }
            if (matching.isNotEmpty()) matching else approved
        } else {
            approved
        }
    }

    fun recordAdClick(adId: String) {
        _collegeAds.value = _collegeAds.value.map {
            if (it.id == adId) it.copy(clicksCount = it.clicksCount + 1) else it
        }
    }

    fun recordAdImpression(adId: String) {
        _collegeAds.value = _collegeAds.value.map {
            if (it.id == adId) it.copy(impressionsCount = it.impressionsCount + 1) else it
        }
    }

    fun submitAdvertisement(ad: CollegeAdvertisement) {
        val uid = Firebase.auth.currentUser?.uid ?: "local_advertiser"
        val pendingAd = ad.copy(
            approvalStatus = "PENDING_REVIEW",
            advertiserAccountId = uid,
            impressionsCount = 0,
            clicksCount = 0
        )
        val updated = listOf(pendingAd) + _collegeAds.value.filter { it.id != ad.id }
        _collegeAds.value = updated

        if (firestoreDb != null && Firebase.auth.currentUser != null) {
            val docRef = firestoreDb.collection("collegeAdvertisements").document(pendingAd.id)
            val payload = hashMapOf<String, Any?>(
                "collegeId" to pendingAd.collegeId,
                "collegeName" to pendingAd.collegeName,
                "headline" to pendingAd.headline,
                "promotionalText" to pendingAd.promotionalText,
                "courses" to pendingAd.courses,
                "location" to pendingAd.location,
                "officialDestinationUrl" to pendingAd.officialDestinationUrl,
                "admissionStatus" to pendingAd.admissionStatus,
                "verifiedDeadline" to pendingAd.verifiedDeadline,
                "isSponsored" to true,
                "approvalStatus" to "PENDING_REVIEW",
                "segment" to pendingAd.segment.name,
                "advertiserAccountId" to uid,
                "createdAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload)
                .addOnFailureListener { error ->
                    handleFirestoreError(error, OperationType.CREATE, docRef.path)
                }
        }
    }

    private fun loadDefaultCollegeAds(): List<CollegeAdvertisement> {
        return listOf(
            CollegeAdvertisement(
                id = "ad_bits_pilani_01",
                collegeId = "bits_pilani",
                collegeName = "BITS Pilani",
                localImageRes = R.drawable.college_campus,
                headline = "BITSAT 2025 Applications Open • Direct Merit Admissions",
                promotionalText = "Join India's top ranked private institute with campuses at Pilani, Goa, and Hyderabad. Flexible dual degree options, zero-attendance policy, and renowned Practice School industry internships.",
                courses = listOf("B.E. Computer Science", "B.E. Electronics", "M.Sc. Economics", "B.Pharm"),
                location = "Pilani, Rajasthan & Goa & Hyderabad",
                officialDestinationUrl = "https://www.bitsadmission.com/",
                admissionStatus = "BITSAT 2025 Registration Live",
                verifiedDeadline = "Apply by: April 18, 2025",
                isSponsored = true,
                approvalStatus = "APPROVED",
                segment = Segment.ENGINEERING,
                advertiserAccountId = "inst_bits_ad",
                campaignStartDate = "15 Jan 2025",
                campaignEndDate = "18 Apr 2025",
                impressionsCount = 3840,
                clicksCount = 492
            ),
            CollegeAdvertisement(
                id = "ad_vit_vellore_01",
                collegeId = "vit_vellore",
                collegeName = "Vellore Institute of Technology (VIT)",
                localImageRes = R.drawable.college_campus,
                headline = "VITEEE 2025: Engineering Admissions Across 4 Campuses",
                promotionalText = "Apply for B.Tech programmes across Vellore, Chennai, AP, and Bhopal. Over 7,000 campus placements in 2024, ABET accredited curricula, and global semester exchange partnerships.",
                courses = listOf("B.Tech CSE with AI/ML", "B.Tech ECE", "B.Tech Biotech", "B.Tech Mechanical"),
                location = "Vellore, Tamil Nadu",
                officialDestinationUrl = "https://viteee.vit.ac.in/",
                admissionStatus = "Slot Booking Active",
                verifiedDeadline = "Apply by: April 05, 2025",
                isSponsored = true,
                approvalStatus = "APPROVED",
                segment = Segment.ENGINEERING,
                advertiserAccountId = "inst_vit_ad",
                campaignStartDate = "01 Nov 2024",
                campaignEndDate = "05 Apr 2025",
                impressionsCount = 4210,
                clicksCount = 578
            ),
            CollegeAdvertisement(
                id = "ad_kmc_manipal_01",
                collegeId = "kmc_manipal",
                collegeName = "Kasturba Medical College (KMC)",
                localImageRes = R.drawable.aiims_campus,
                headline = "KMC Manipal: Excellence in Medical & Healthcare Education",
                promotionalText = "Ranked among India's top 10 medical colleges. Offering world-class tertiary hospital clinical training, high USMLE/PLAB success rates, and recognized MBBS qualifications globally.",
                courses = listOf("MBBS", "MD General Medicine", "MS Surgery", "B.Sc. Allied Health"),
                location = "Manipal & Mangalore, Karnataka",
                officialDestinationUrl = "https://manipal.edu/kmc-manipal.html",
                admissionStatus = "Admissions via NEET UG / MCC Counselling",
                verifiedDeadline = "MCC AIQ Round 1: June 2025",
                isSponsored = true,
                approvalStatus = "APPROVED",
                segment = Segment.MEDICAL,
                advertiserAccountId = "inst_kmc_ad",
                campaignStartDate = "01 Feb 2025",
                campaignEndDate = "30 Jun 2025",
                impressionsCount = 2950,
                clicksCount = 380
            ),
            CollegeAdvertisement(
                id = "ad_spjimr_01",
                collegeId = "spjimr_mumbai",
                collegeName = "SPJIMR Mumbai",
                localImageRes = R.drawable.college_campus,
                headline = "SPJIMR Post Graduate Diploma in Management (PGDM 2025)",
                promotionalText = "Recognized among top tier global business schools (Financial Times Global Ranking). Specializations in Marketing, Finance, Operations, and Information Management with non-classroom learning.",
                courses = listOf("PGDM (Two Year Full Time)", "Global Management Programme (GMP)", "PGP-BM"),
                location = "Andheri West, Mumbai, Maharashtra",
                officialDestinationUrl = "https://www.spjimr.org/",
                admissionStatus = "Shortlisting via CAT & GMAT",
                verifiedDeadline = "Interviews Scheduled: March 2025",
                isSponsored = true,
                approvalStatus = "APPROVED",
                segment = Segment.MANAGEMENT,
                advertiserAccountId = "inst_spjimr_ad",
                campaignStartDate = "01 Dec 2024",
                campaignEndDate = "31 Mar 2025",
                impressionsCount = 2100,
                clicksCount = 310
            ),
            CollegeAdvertisement(
                id = "ad_snu_01",
                collegeId = "snu_noida",
                collegeName = "Shiv Nadar University (SNU)",
                localImageRes = R.drawable.college_campus,
                headline = "SNU Institution of Eminence: Multi-Disciplinary B.Tech & MBA",
                promotionalText = "Generous merit scholarships up to 100% tuition waiver. World-class 286-acre residential campus, research-led undergraduate education, and top recruiters across technology & analytics.",
                courses = listOf("B.Tech Computer Science", "B.Tech AI & Data", "MBA Executive", "B.Sc. Research"),
                location = "Greater Noida, Delhi-NCR",
                officialDestinationUrl = "https://snu.edu.in/",
                admissionStatus = "SNUSAT 2025 Applications Open",
                verifiedDeadline = "Round 2 Deadline: March 31, 2025",
                isSponsored = true,
                approvalStatus = "APPROVED",
                segment = Segment.ENGINEERING,
                advertiserAccountId = "inst_snu_ad",
                campaignStartDate = "10 Jan 2025",
                campaignEndDate = "31 May 2025",
                impressionsCount = 1870,
                clicksCount = 245
            ),
            CollegeAdvertisement(
                id = "ad_amrita_pending_01",
                collegeId = "amrita_univ",
                collegeName = "Amrita Vishwa Vidyapeetham",
                localImageRes = R.drawable.college_campus,
                headline = "AEEE 2025: Engineering Admissions at Amrita",
                promotionalText = "Apply for B.Tech with AEEE or JEE Main score across Coimbatore, Amritapuri, Bengaluru, and Chennai campuses.",
                courses = listOf("B.Tech Computer Science", "B.Tech Robotics & AI"),
                location = "Coimbatore, Tamil Nadu",
                officialDestinationUrl = "https://www.amrita.edu/admissions/btech/",
                admissionStatus = "Submitted for Campaign Review",
                verifiedDeadline = "Campaign Review in Progress",
                isSponsored = true,
                approvalStatus = "PENDING_REVIEW",
                segment = Segment.ENGINEERING,
                advertiserAccountId = "inst_amrita_partner",
                campaignStartDate = "01 Apr 2025",
                campaignEndDate = "30 Jun 2025",
                impressionsCount = 0,
                clicksCount = 0
            )
        )
    }

    // --- Demo Datasets ---

    val admissionUpdates: List<AdmissionUpdate> = listOf(
        AdmissionUpdate(
            id = "up_1",
            segment = Segment.MEDICAL,
            title = "NEET UG 2025 Counselling Round 1 from 14 Jun",
            dateText = "2h ago",
            summary = "MCC announces tentative schedule for All India Quota 15% MBBS/BDS counselling round 1 registration.",
            fullDetails = "The Medical Counselling Committee (MCC) will begin online registration for NEET UG 2025 All India Quota (AIQ) round 1 from June 14, 2025. Candidates eligible under AIQ, Deemed, and Central Universities can register and submit choice locks.",
            isDemo = true
        ),
        AdmissionUpdate(
            id = "up_2",
            segment = Segment.ENGINEERING,
            title = "JEE Main 2025 Session 2 Form Released",
            dateText = "3h ago",
            summary = "NTA opens online application portal for Session 2 candidates. Last date for fee payment is March 12.",
            fullDetails = "National Testing Agency (NTA) has officially released the registration portal for JEE Main Session 2. Candidates who appeared in Session 1 can simply login with application numbers.",
            isDemo = true
        ),
        AdmissionUpdate(
            id = "up_3",
            segment = Segment.MANAGEMENT,
            title = "CAT 2025 Registration Starts from 1 Aug",
            dateText = "1d ago",
            summary = "IIM Calcutta to coordinate CAT 2025. Exam scheduled for last Sunday of November.",
            fullDetails = "Common Admission Test (CAT 2025) registration opens August 1, 2025 for admission to 21 IIMs and over 1,000 top business schools across India. Check eligibility and test centres.",
            isDemo = true
        ),
        AdmissionUpdate(
            id = "up_4",
            segment = Segment.MEDICAL,
            title = "NEET PG 2025 Exam Date Declared",
            dateText = "1d ago",
            summary = "NBEMS announces single-shift computer-based test date with updated test day procedures.",
            fullDetails = "National Board of Examinations in Medical Sciences (NBEMS) has released the official schedule for NEET PG 2025. Admit cards will be available two weeks before the exam.",
            isDemo = true
        )
    )

    val collegesList: List<College> = listOf(
        College(
            id = "aiims_delhi",
            name = "AIIMS Delhi",
            segment = Segment.MEDICAL,
            course = "MBBS",
            rankRange = "Rank Range: 1 – 5000",
            minRank = 1,
            maxRank = 5000,
            categoryMatch = "Dream",
            location = "New Delhi",
            rating = "4.8 ★",
            seats = 125,
            feesPerYear = "₹ 12,000 / year",
            overview = "All India Institute of Medical Sciences (AIIMS) New Delhi is India's premier autonomous medical research university and public hospital. Consistently ranked #1 in NIRF Medical Rankings with world-class faculty, state-of-the-art trauma centre, and unmatched clinical exposure.",
            cutoffsText = "Round 1 Closing Rank (General): 57 | Round 2: 61 | OBC Closing: 247 | EWS: 223 | SC: 965 | ST: 3087 (Year: 2024 MCC AIQ)",
            feesBreakdown = "Academic Tuition: ₹ 1,628/year | Hostel: ₹ 1,000/year | Caution Deposit: ₹ 100 | Total estimated annual fees: ~₹ 12,000 inclusive of subsidised mess charges.",
            placementsText = "100% internship placement with stipends exceeding ₹ 26,000/month. Highest percentage of graduates crack USMLE, PLAB, and NEET PG top specialities globally.",
            websiteUrl = "https://www.aiims.edu",
            isDemo = true
        ),
        College(
            id = "vmmc_delhi",
            name = "VMMC & Safdarjung Hospital",
            segment = Segment.MEDICAL,
            course = "MBBS",
            rankRange = "Rank Range: 100 – 1,800",
            minRank = 100,
            maxRank = 1800,
            categoryMatch = "Target",
            location = "New Delhi",
            rating = "4.7 ★",
            seats = 170,
            feesPerYear = "₹ 33,500 / year",
            overview = "Vardhman Mahavir Medical College is attached to the sprawling 2900-bed Safdarjung Hospital. Offers top tier medical education under Guru Gobind Singh Indraprastha University with enormous patient variety.",
            cutoffsText = "General AIQ Round 1: 107 | IP Quota: 1,420 | OBC AIQ: 350 | SC: 2,400",
            feesBreakdown = "Annual Tuition: ₹ 33,500 | Hostel: ₹ 12,000 | Examination Fees: ₹ 2,500",
            placementsText = "Internship stipend of ₹ 26,300/month with direct internal PG quota eligibility for GGSIPU medical seats.",
            websiteUrl = "http://www.vmmc-sjh.nic.in",
            isDemo = true
        ),
        College(
            id = "mamc_delhi",
            name = "Maulana Azad Medical College",
            segment = Segment.MEDICAL,
            course = "MBBS",
            rankRange = "Rank Range: 50 – 1,200",
            minRank = 50,
            maxRank = 1200,
            categoryMatch = "Dream",
            location = "New Delhi",
            rating = "4.9 ★",
            seats = 250,
            feesPerYear = "₹ 15,000 / year",
            overview = "Maulana Azad Medical College (MAMC) is linked to Lok Nayak and GB Pant hospitals. Renowned for academic excellence and prestigious alumni across global healthcare institutions.",
            cutoffsText = "General AIQ Round 1: 85 | Delhi State Quota: 1,180 | OBC: 310",
            feesBreakdown = "Annual Tuition: ₹ 15,000 | Subsidised living expenses on campus.",
            placementsText = "High match rate for MD/MS residency programs across premier Indian institutions.",
            websiteUrl = "https://mamc.delhi.gov.in",
            isDemo = true
        ),
        College(
            id = "kgmu_lucknow",
            name = "KGMU Lucknow",
            segment = Segment.MEDICAL,
            course = "MBBS",
            rankRange = "Rank Range: 800 – 4,500",
            minRank = 800,
            maxRank = 4500,
            categoryMatch = "Safe",
            location = "Lucknow, Uttar Pradesh",
            rating = "4.6 ★",
            seats = 250,
            feesPerYear = "₹ 54,000 / year",
            overview = "King George's Medical University is a century-old historic institution with over 4,500 hospital beds and distinguished specialty departments.",
            cutoffsText = "General AIQ: 1,098 | UP State Quota: 2,450 | OBC: 3,200",
            feesBreakdown = "Annual Fees: ₹ 54,000 including university welfare dues.",
            placementsText = "High clinical exposure; strong post-graduate track record in clinical sciences.",
            websiteUrl = "https://www.kgmu.org",
            isDemo = true
        ),
        College(
            id = "iit_bombay",
            name = "IIT Bombay",
            segment = Segment.ENGINEERING,
            course = "B.Tech Computer Science",
            rankRange = "Rank Range: 1 – 250",
            minRank = 1,
            maxRank = 250,
            categoryMatch = "Dream",
            location = "Mumbai, Maharashtra",
            rating = "4.9 ★",
            seats = 180,
            feesPerYear = "₹ 2.2 Lakhs / year",
            overview = "Indian Institute of Technology Bombay is recognized as an Institute of Eminence with world leading research labs, faculty, and vibrant student community.",
            cutoffsText = "JEE Advanced Closing Rank (Open): 68 | EWS: 12 | OBC-NCL: 45",
            feesBreakdown = "Tuition: ₹ 1,00,000/sem | Hostel & Mess: ₹ 35,000/sem (Fee waivers apply for low-income brackets).",
            placementsText = "Average CTC: ₹ 23.5 LPA | Highest International: ₹ 1.68 Crore | 100% placement rate for CSE.",
            websiteUrl = "https://www.iitb.ac.in",
            isDemo = true
        ),
        College(
            id = "iit_delhi",
            name = "IIT Delhi",
            segment = Segment.ENGINEERING,
            course = "B.Tech Electrical Engineering",
            rankRange = "Rank Range: 200 – 950",
            minRank = 200,
            maxRank = 950,
            categoryMatch = "Target",
            location = "New Delhi",
            rating = "4.8 ★",
            seats = 120,
            feesPerYear = "₹ 2.2 Lakhs / year",
            overview = "Located in Hauz Khas, IIT Delhi offers premier engineering education with extensive industry partnerships, incubation hubs, and cutting-edge robotics/AI labs.",
            cutoffsText = "JEE Advanced Open: 580 | OBC: 290 | SC: 140",
            feesBreakdown = "Tuition: ₹ 1,00,000/sem | Hostel: ₹ 30,000/sem",
            placementsText = "Average Package: ₹ 21.9 LPA with top global tech recruiters.",
            websiteUrl = "https://home.iitd.ac.in",
            isDemo = true
        ),
        College(
            id = "nit_trichy",
            name = "NIT Trichy",
            segment = Segment.ENGINEERING,
            course = "B.Tech Mechanical Engineering",
            rankRange = "Rank Range: 2,000 – 14,000",
            minRank = 2000,
            maxRank = 14000,
            categoryMatch = "Safe",
            location = "Tiruchirappalli, Tamil Nadu",
            rating = "4.7 ★",
            seats = 110,
            feesPerYear = "₹ 1.6 Lakhs / year",
            overview = "Ranked #1 among National Institutes of Technology in India by NIRF. Known for superb academic rigor and strong automotive & core engineering placements.",
            cutoffsText = "JEE Main Other State Open: 8,450 | Home State: 14,200",
            feesBreakdown = "Tuition: ₹ 62,500/sem | Hostel: ₹ 24,000/sem",
            placementsText = "Average CTC: ₹ 15.8 LPA | Core and Software companies recruitment.",
            websiteUrl = "https://www.nitt.edu",
            isDemo = true
        ),
        College(
            id = "iim_ahmedabad",
            name = "IIM Ahmedabad",
            segment = Segment.MANAGEMENT,
            course = "MBA (PGP)",
            rankRange = "CAT: 99.5 – 100 %ile",
            minRank = 99,
            maxRank = 100,
            categoryMatch = "Dream",
            location = "Ahmedabad, Gujarat",
            rating = "5.0 ★",
            seats = 395,
            feesPerYear = "₹ 12.5 Lakhs / year",
            overview = "India's premier management institution known worldwide for the case method pedagogy, Louis Kahn campus, and unparalleled executive leadership track record.",
            cutoffsText = "CAT Minimum Sectional: 85 %ile | Overall Call Cutoff: 99.65 %ile",
            feesBreakdown = "Total 2-Year Program Fee: ₹ 25,00,000 including hostel, study materials, and international exchange.",
            placementsText = "Average CTC: ₹ 34.36 LPA | Highest Domestic: ₹ 1.15 Crore | Top consulting and investment banking recruiters.",
            websiteUrl = "https://www.iima.ac.in",
            isDemo = true
        ),
        College(
            id = "fms_delhi",
            name = "FMS Delhi (Faculty of Management Studies)",
            segment = Segment.MANAGEMENT,
            course = "MBA Full Time",
            rankRange = "CAT: 98.8 – 99.8 %ile",
            minRank = 98,
            maxRank = 100,
            categoryMatch = "Target",
            location = "New Delhi",
            rating = "4.9 ★",
            seats = 287,
            feesPerYear = "₹ 1.0 Lakh / year",
            overview = "Affiliated with University of Delhi, FMS is widely celebrated for offering the highest ROI in management education in the world with minimal fees and massive placements.",
            cutoffsText = "Composite score based on CAT 99.3+ %ile with sectional weightages.",
            feesBreakdown = "Total 2-Year Tuition Fees: ~₹ 2,00,000 only.",
            placementsText = "Average Package: ₹ 34.1 LPA | 100% placements across consulting, product, and finance.",
            websiteUrl = "https://fms.edu",
            isDemo = true
        )
    )

    fun predictColleges(input: PredictorInput): List<College> {
        val userRank = input.rankOrScore.toIntOrNull() ?: 35000
        val segmentColleges = collegesList.filter { it.segment == input.segment }

        return segmentColleges.map { college ->
            val matchType = when {
                userRank <= college.minRank * 2 -> "Dream"
                userRank in college.minRank..college.maxRank -> "Target"
                else -> "Safe"
            }
            college.copy(categoryMatch = matchType)
        }
    }

    val testSeriesList: List<TestSeriesItem> = listOf(
        TestSeriesItem(
            id = "neet_mock_1",
            title = "NEET 2025 Full Syllabus High-Yield Test",
            segment = Segment.MEDICAL,
            examCategory = "Medical",
            syllabus = "Physics, Chemistry, Botany, Zoology • Complete Class 11 & 12",
            questionCount = 25,
            durationMinutes = 45,
            questions = QuestionBankData.medicalQuestions.take(25),
            isDemo = false
        ),
        TestSeriesItem(
            id = "jee_mock_1",
            title = "JEE Main 2025 Comprehensive Mock Test",
            segment = Segment.ENGINEERING,
            examCategory = "Engineering",
            syllabus = "Mathematics, Physics, Chemistry • Class 11 & 12 Combined",
            questionCount = 25,
            durationMinutes = 45,
            questions = QuestionBankData.engineeringQuestions.take(25),
            isDemo = false
        ),
        TestSeriesItem(
            id = "cat_mock_1",
            title = "CAT 2025 Sectional Test: Quant, VARC & DILR",
            segment = Segment.MANAGEMENT,
            examCategory = "Management",
            syllabus = "Quantitative Aptitude, Reading Comprehension, Verbal Ability, DILR",
            questionCount = 25,
            durationMinutes = 45,
            questions = QuestionBankData.managementQuestions.take(25),
            isDemo = false
        )
    )

    fun getActiveNoticesForSegment(segment: Segment): List<OfficialAdmissionNotice> {
        val targetCat = when (segment) {
            Segment.MEDICAL -> ExamCategory.MEDICAL
            Segment.ENGINEERING -> ExamCategory.ENGINEERING
            Segment.MANAGEMENT -> ExamCategory.MANAGEMENT
        }
        val pool = _officialNotices.value.filter { !it.isHistorical && it.examCategory == targetCat }
        return if (pool.isNotEmpty()) pool else OfficialNoticesData.getNoticesForSegment(segment)
    }

    fun getUpcomingDeadlinesForSegment(segment: Segment, now: Long = System.currentTimeMillis()): List<OfficialAdmissionNotice> {
        return getActiveNoticesForSegment(segment)
            .filter { it.hasFutureDeadline(now) }
            .sortedBy { it.deadlineEpochMillis ?: Long.MAX_VALUE }
    }

    fun getHistoricalNotices(segment: Segment? = null): List<OfficialAdmissionNotice> {
        val allHist = _officialNotices.value.filter { it.isHistorical }
        val pool = if (allHist.isNotEmpty()) allHist else OfficialNoticesData.historicalNotices
        return if (segment == null) {
            pool
        } else {
            val targetCat = when (segment) {
                Segment.MEDICAL -> ExamCategory.MEDICAL
                Segment.ENGINEERING -> ExamCategory.ENGINEERING
                Segment.MANAGEMENT -> ExamCategory.MANAGEMENT
            }
            pool.filter { it.examCategory == targetCat }
        }
    }

    companion object {
        @Volatile
        private var instance: GetVidyaRepository? = null

        fun getInstance(context: Context): GetVidyaRepository {
            return instance ?: synchronized(this) {
                instance ?: GetVidyaRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
