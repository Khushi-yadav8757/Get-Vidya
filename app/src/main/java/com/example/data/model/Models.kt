package com.example.data.model

import com.squareup.moshi.JsonClass

enum class Segment(val displayName: String, val exams: String, val subtitle: String) {
    MEDICAL("Medical", "NEET UG | PG", "MBBS, MD/MS"),
    ENGINEERING("Engineering", "JEE Main | Advanced", "B.Tech"),
    MANAGEMENT("Management", "CAT | XAT | CMAT", "MBA")
}

enum class ExamCategory(val displayName: String) {
    MEDICAL("Medical"),
    ENGINEERING("Engineering"),
    MANAGEMENT("Management"),
    OTHER("Other Exams")
}

enum class VerificationStatus(val label: String, val isOfficial: Boolean) {
    OFFICIAL_VERIFIED("Official Source Verified", true),
    AWAITING_VERIFICATION("Awaiting Official Verification", false),
    AI_SUMMARIZED("AI-Assisted Official Summary", true),
    PRACTICE_CONTENT("Authentic Practice Paper", false)
}

@JsonClass(generateAdapter = true)
data class UserProfile(
    val name: String = "Aryan Sharma",
    val email: String = "aryan@gmail.com",
    val phone: String = "+91 98765 43210",
    val examAppeared: String = "NEET",
    val scoreRank: String = "720",
    val category: String = "General",
    val domicile: String = "Uttar Pradesh",
    val expectedBudget: String = "₹ 5 - 15 Lakhs",
    val isOnboarded: Boolean = true
)

@JsonClass(generateAdapter = true)
data class ExamInfo(
    val id: String,
    val name: String,
    val shortName: String,
    val category: ExamCategory,
    val description: String,
    val conductingBody: String,
    val officialWebsite: String,
    val totalTestSets: Int = 5,
    val isActive2025: Boolean = true,
    val hasOfficialPyq: Boolean = true
)

@JsonClass(generateAdapter = true)
data class ExamTestSet(
    val id: String,
    val examId: String,
    val examName: String,
    val setNumber: Int,
    val title: String,
    val year: Int,
    val sessionShift: String,
    val isPyq: Boolean,
    val questionCount: Int,
    val durationMinutes: Int,
    val subjects: String,
    val difficulty: String,
    val sourceDoc: String,
    val verificationStatus: VerificationStatus,
    val questions: List<TestQuestion>
) {
    fun toTestSeriesItem(): TestSeriesItem {
        return TestSeriesItem(
            id = id,
            title = title,
            segment = when {
                examId.contains("neet") || examId.contains("ini") -> Segment.MEDICAL
                examId.contains("cat") || examId.contains("xat") || examId.contains("snap") || examId.contains("cmat") -> Segment.MANAGEMENT
                else -> Segment.ENGINEERING
            },
            examCategory = examName,
            syllabus = "$subjects ($sessionShift)",
            questionCount = questionCount,
            durationMinutes = durationMinutes,
            questions = questions,
            isDemo = false
        )
    }
}

enum class AdmissionStatus(val displayName: String, val badgeColorHex: Long) {
    REGISTRATION_OPEN("Registration Open", 0xFF16A34A),
    UPCOMING("Upcoming", 0xFF2563EB),
    REGISTRATION_CLOSED("Registration Closed", 0xFFDC2626),
    EXAM_SCHEDULED("Exam Scheduled", 0xFF9333EA),
    ADMIT_CARD_RELEASED("Admit Card Released", 0xFFD97706),
    RESULT_DECLARED("Result Declared", 0xFF059669),
    COUNSELLING_OPEN("Counselling Open", 0xFF0284C7),
    COMPLETED("Completed", 0xFF6B7280),
    DATES_NOT_ANNOUNCED("Next Cycle — Dates Not Announced Yet", 0xFF4B5563),
    NEEDS_VERIFICATION("Needs Verification", 0xFFE11D48);

    companion object {
        fun fromString(value: String?): AdmissionStatus {
            if (value.isNullOrBlank()) return DATES_NOT_ANNOUNCED
            return try {
                valueOf(value.trim().uppercase())
            } catch (_: Exception) {
                entries.find {
                    it.name.equals(value.trim(), ignoreCase = true) ||
                    it.displayName.equals(value.trim(), ignoreCase = true)
                } ?: NEEDS_VERIFICATION
            }
        }
    }
}

@JsonClass(generateAdapter = true)
data class OfficialAdmissionNotice(
    val id: String,
    val examId: String,
    val examCategory: ExamCategory,
    val title: String,
    val summary: String,
    val admissionCycle: String = "2026-27",
    val admissionStatus: AdmissionStatus = AdmissionStatus.UPCOMING,
    val publicationDate: String,
    val applicationOpenDate: String? = null,
    val applicationCloseDate: String? = null,
    val examDate: String? = null,
    val counsellingDeadline: String? = null,
    val officialUrl: String,
    val lastVerifiedTime: String,
    val lastVerifiedTimestamp: Long = System.currentTimeMillis(),
    val isHistorical: Boolean = false,
    val deadlineEpochMillis: Long? = null,
    val verificationStatus: VerificationStatus = VerificationStatus.OFFICIAL_VERIFIED,
    val isRead: Boolean = false
) {
    fun hasFutureDeadline(currentTimestamp: Long = System.currentTimeMillis()): Boolean {
        if (isHistorical) return false
        if (admissionStatus == AdmissionStatus.DATES_NOT_ANNOUNCED ||
            admissionStatus == AdmissionStatus.REGISTRATION_CLOSED ||
            admissionStatus == AdmissionStatus.COMPLETED) {
            return false
        }
        return deadlineEpochMillis?.let { it > currentTimestamp } ?: false
    }
}

@JsonClass(generateAdapter = true)
data class AdmissionUpdate(
    val id: String,
    val segment: Segment,
    val title: String,
    val dateText: String,
    val summary: String,
    val fullDetails: String,
    val isDemo: Boolean = true
)

@JsonClass(generateAdapter = true)
data class College(
    val id: String,
    val name: String,
    val segment: Segment,
    val course: String,
    val rankRange: String,
    val minRank: Int,
    val maxRank: Int,
    val categoryMatch: String = "Dream",
    val location: String,
    val rating: String = "4.8 ★",
    val seats: Int,
    val feesPerYear: String,
    val overview: String,
    val cutoffsText: String,
    val feesBreakdown: String,
    val placementsText: String,
    val websiteUrl: String,
    val isDemo: Boolean = true
)

enum class QuestionType(val label: String) {
    VERIFIED_PYQ("Verified Official PYQ"),
    PRACTICE("Practice Question")
}

@JsonClass(generateAdapter = true)
data class TestQuestion(
    val id: Int,
    val examId: String = "",
    val examCategory: String = "",
    val subject: String,
    val topic: String = "General",
    val questionText: String,
    val options: List<String>,
    val correctOptionIndex: Int,
    val explanation: String = "",
    val sourceDoc: String = "",
    val year: Int? = null,
    val questionType: QuestionType = QuestionType.VERIFIED_PYQ,
    val verificationStatus: VerificationStatus = VerificationStatus.OFFICIAL_VERIFIED,
    val sourceUrl: String = "",
    val isVerified: Boolean = true
)

@JsonClass(generateAdapter = true)
data class TestSeriesItem(
    val id: String,
    val title: String,
    val segment: Segment,
    val examCategory: String, // Medical, Engineering, Management
    val syllabus: String,
    val questionCount: Int,
    val durationMinutes: Int,
    val questions: List<TestQuestion>,
    val isDemo: Boolean = false
)

@JsonClass(generateAdapter = true)
data class TestAttempt(
    val id: String = "",
    val testId: String = "",
    val testTitle: String = "",
    val score: Int = 0,
    val totalMarks: Int = 0,
    val correctAnswers: Int = 0,
    val totalQuestions: Int = 0,
    val accuracyPercentage: Int = 0,
    val timestamp: Long = 0L,
    val dateString: String = "",
    val attemptedQuestionIds: List<Int> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ChatMessage(
    val id: String,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val isError: Boolean = false,
    val isAiGenerated: Boolean = false
)

@JsonClass(generateAdapter = true)
data class CounsellingBooking(
    val id: String,
    val studentName: String,
    val phone: String,
    val segment: Segment,
    val preferredDate: String,
    val slot: String,
    val query: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isDemo: Boolean = true
)

@JsonClass(generateAdapter = true)
data class NotificationItem(
    val id: String,
    val title: String,
    val description: String,
    val timeAgo: String,
    val category: String,
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val officialUrl: String = "",
    val isOfficialVerified: Boolean = true
)

data class PredictorInput(
    val segment: Segment = Segment.MEDICAL,
    val rankOrScore: String = "35000",
    val category: String = "General",
    val quota: String = "All India",
    val domicile: String = "Uttar Pradesh",
    val budget: String = "₹ 5 - 15 Lakhs"
)
