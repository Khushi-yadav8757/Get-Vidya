package com.example.data.service

import com.example.data.model.AdmissionStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Reliable status calculation and resolution engine for admission cycles and examinations.
 * Adheres to official authority schedule phases and validates future deadlines against
 * the current system timestamp.
 */
object AdmissionStatusResolver {

    private val supportedDateFormats = listOf(
        SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = TimeZone.getTimeZone("Asia/Kolkata") },
        SimpleDateFormat("MMMM dd, yyyy", Locale.US).apply { timeZone = TimeZone.getTimeZone("Asia/Kolkata") },
        SimpleDateFormat("MMM dd, yyyy", Locale.US).apply { timeZone = TimeZone.getTimeZone("Asia/Kolkata") },
        SimpleDateFormat("dd MMM yyyy", Locale.US).apply { timeZone = TimeZone.getTimeZone("Asia/Kolkata") },
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }
    )

    fun parseEpochMillis(dateStr: String?): Long? {
        if (dateStr.isNullOrBlank()) return null
        val clean = dateStr.trim()
        for (format in supportedDateFormats) {
            try {
                val date = format.parse(clean)
                if (date != null) return date.time
            } catch (_: Exception) {}
        }
        return null
    }

    /**
     * Resolves the authoritative admission status given cycle timeline milestones.
     */
    fun resolveStatus(
        explicitStatus: AdmissionStatus? = null,
        openEpochMillis: Long? = null,
        closeEpochMillis: Long? = null,
        examEpochMillis: Long? = null,
        counsellingOpenEpochMillis: Long? = null,
        counsellingCloseEpochMillis: Long? = null,
        resultDeclaredEpochMillis: Long? = null,
        isAdmitCardReleased: Boolean = false,
        isCompleted: Boolean = false,
        now: Long = System.currentTimeMillis()
    ): AdmissionStatus {
        // If explicitly set to DATES_NOT_ANNOUNCED or NEEDS_VERIFICATION, respect that
        if (explicitStatus == AdmissionStatus.DATES_NOT_ANNOUNCED || explicitStatus == AdmissionStatus.NEEDS_VERIFICATION) {
            return explicitStatus
        }

        if (isCompleted) {
            return AdmissionStatus.COMPLETED
        }

        // 1. Counselling Open Phase
        if (counsellingOpenEpochMillis != null) {
            val closeTime = counsellingCloseEpochMillis ?: (counsellingOpenEpochMillis + 14L * 24 * 3600 * 1000)
            if (now in counsellingOpenEpochMillis..closeTime) {
                return AdmissionStatus.COUNSELLING_OPEN
            }
        }

        // 2. Result Declared Phase
        if (resultDeclaredEpochMillis != null && now >= resultDeclaredEpochMillis) {
            if (counsellingOpenEpochMillis == null || now < counsellingOpenEpochMillis) {
                return AdmissionStatus.RESULT_DECLARED
            }
        }

        // 3. Admit Card Released Phase
        if (isAdmitCardReleased || explicitStatus == AdmissionStatus.ADMIT_CARD_RELEASED) {
            if (examEpochMillis == null || now <= examEpochMillis) {
                return AdmissionStatus.ADMIT_CARD_RELEASED
            }
        }

        // 4. Registration Open Phase
        if (openEpochMillis != null && closeEpochMillis != null) {
            if (now in openEpochMillis..closeEpochMillis) {
                return AdmissionStatus.REGISTRATION_OPEN
            }
        }

        // 5. Registration Closed / Exam Scheduled Phase
        if (closeEpochMillis != null && now > closeEpochMillis) {
            if (examEpochMillis != null && now <= examEpochMillis) {
                return AdmissionStatus.EXAM_SCHEDULED
            }
            if (examEpochMillis != null && now > examEpochMillis && (resultDeclaredEpochMillis == null || now < resultDeclaredEpochMillis)) {
                return AdmissionStatus.COMPLETED
            }
            return AdmissionStatus.REGISTRATION_CLOSED
        }

        // 6. Upcoming Phase (Opening in future)
        if (openEpochMillis != null && now < openEpochMillis) {
            return AdmissionStatus.UPCOMING
        }

        // Default to explicitStatus if provided, or DATES_NOT_ANNOUNCED
        return explicitStatus ?: AdmissionStatus.DATES_NOT_ANNOUNCED
    }

    /**
     * Formats a epoch timestamp into a readable date string.
     */
    fun formatSyncTime(epochMillis: Long): String {
        val format = SimpleDateFormat("MMM dd, yyyy, hh:mm a", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("Asia/Kolkata")
        }
        return format.format(Date(epochMillis)) + " IST"
    }
}
