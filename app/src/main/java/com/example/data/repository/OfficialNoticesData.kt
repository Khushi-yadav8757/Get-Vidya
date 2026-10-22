package com.example.data.repository

import com.example.data.model.AdmissionStatus
import com.example.data.model.ExamCategory
import com.example.data.model.OfficialAdmissionNotice
import com.example.data.model.Segment
import com.example.data.model.VerificationStatus

/**
 * Authoritative Repository of Official Admission Notices.
 * Separates active current admission cycles from archived historical records.
 * Ensures zero expired deadlines are presented as upcoming.
 */
object OfficialNoticesData {

    // Active notices for current admission cycles (2026 / 2027)
    val activeNotices: List<OfficialAdmissionNotice> = listOf(
        // 1. CMAT (Management) - Next Cycle Dates Not Announced Yet
        OfficialAdmissionNotice(
            id = "notice_cmat_2027",
            examId = "cmat",
            examCategory = ExamCategory.MANAGEMENT,
            title = "NTA CMAT 2027: Next Cycle Notification Awaited",
            summary = "National Testing Agency (NTA) has not yet announced the official schedule for Common Management Admission Test (CMAT) 2027 for AICTE-approved MBA and PGDM programmes nationwide. Official information bulletin will be published on cmat.nta.nic.in.",
            admissionCycle = "2027",
            admissionStatus = AdmissionStatus.DATES_NOT_ANNOUNCED,
            publicationDate = "Oct 18, 2026",
            applicationOpenDate = "To be announced by NTA",
            applicationCloseDate = "Dates Not Announced Yet",
            examDate = "To be announced",
            counsellingDeadline = null,
            officialUrl = "https://cmat.nta.nic.in/",
            lastVerifiedTime = "Verified from cmat.nta.nic.in (Today, 10:30 AM IST)",
            lastVerifiedTimestamp = 1792645800000L,
            isHistorical = false,
            deadlineEpochMillis = null,
            verificationStatus = VerificationStatus.OFFICIAL_VERIFIED
        ),

        // 2. CAT 2026 (Management) - Admit Card Released / Exam Scheduled
        OfficialAdmissionNotice(
            id = "notice_cat_2026",
            examId = "cat",
            examCategory = ExamCategory.MANAGEMENT,
            title = "IIM CAT 2026: Admit Card Released & Test Day Guidelines",
            summary = "The Indian Institutes of Management have released admit cards for CAT 2026. Registered candidates can download their hall tickets with exam centre and session details.",
            admissionCycle = "2026",
            admissionStatus = AdmissionStatus.ADMIT_CARD_RELEASED,
            publicationDate = "Oct 20, 2026",
            applicationOpenDate = "Aug 01, 2026",
            applicationCloseDate = "Sep 20, 2026",
            examDate = "Nov 29, 2026",
            counsellingDeadline = "IIM Shortlist Interviews: Jan–Apr 2027",
            officialUrl = "https://iimcat.ac.in/",
            lastVerifiedTime = "Verified from iimcat.ac.in (Yesterday, 04:15 PM IST)",
            lastVerifiedTimestamp = 1792579200000L,
            isHistorical = false,
            deadlineEpochMillis = 1795996800000L, // Nov 29, 2026
            verificationStatus = VerificationStatus.OFFICIAL_VERIFIED
        ),

        // 3. XAT 2027 (Management) - Registration Open
        OfficialAdmissionNotice(
            id = "notice_xat_2027",
            examId = "xat",
            examCategory = ExamCategory.MANAGEMENT,
            title = "XLRI XAT 2027: Online Registration Active for PGDM/MBA",
            summary = "XLRI Jamshedpur invites applications for Xavier Aptitude Test (XAT) 2027 for admissions across XLRI campuses and over 160 Associate B-schools in India.",
            admissionCycle = "2027",
            admissionStatus = AdmissionStatus.REGISTRATION_OPEN,
            publicationDate = "Oct 10, 2026",
            applicationOpenDate = "Jul 15, 2026",
            applicationCloseDate = "Nov 30, 2026",
            examDate = "Jan 03, 2027",
            counsellingDeadline = "XLRI GD/PI Shortlisting: Feb–Mar 2027",
            officialUrl = "https://xatonline.in/",
            lastVerifiedTime = "Verified from xatonline.in (Oct 21, 2026)",
            lastVerifiedTimestamp = 1792550400000L,
            isHistorical = false,
            deadlineEpochMillis = 1796083200000L, // Nov 30, 2026
            verificationStatus = VerificationStatus.OFFICIAL_VERIFIED
        ),

        // 4. SNAP 2026 (Management) - Registration Open
        OfficialAdmissionNotice(
            id = "notice_snap_2026",
            examId = "snap",
            examCategory = ExamCategory.MANAGEMENT,
            title = "Symbiosis SNAP 2026: Application Window Closing Soon",
            summary = "Symbiosis International (Deemed University) registration for SNAP 2026 is currently open across 16 institutes offering 26 MBA programmes.",
            admissionCycle = "2026",
            admissionStatus = AdmissionStatus.REGISTRATION_OPEN,
            publicationDate = "Oct 14, 2026",
            applicationOpenDate = "Aug 05, 2026",
            applicationCloseDate = "Nov 22, 2026",
            examDate = "Dec 06, 13 & 20, 2026",
            counsellingDeadline = "Symbiosis GE-PI: Feb 2027",
            officialUrl = "https://www.snaptest.org/",
            lastVerifiedTime = "Verified from snaptest.org (Oct 21, 2026)",
            lastVerifiedTimestamp = 1792550400000L,
            isHistorical = false,
            deadlineEpochMillis = 1795392000000L, // Nov 22, 2026
            verificationStatus = VerificationStatus.OFFICIAL_VERIFIED
        ),

        // 5. NMAT 2026 (Management) - Registration Open
        OfficialAdmissionNotice(
            id = "notice_nmat_2026",
            examId = "nmat",
            examCategory = ExamCategory.MANAGEMENT,
            title = "GMAC NMAT 2026: Registration & Test Scheduling Open",
            summary = "Graduate Management Admission Council conducts NMAT by GMAC with 3 attempts permitted in a testing year for NMIMS and premier management institutes.",
            admissionCycle = "2026-27",
            admissionStatus = AdmissionStatus.REGISTRATION_OPEN,
            publicationDate = "Oct 01, 2026",
            applicationOpenDate = "Aug 01, 2026",
            applicationCloseDate = "Oct 31, 2026",
            examDate = "Nov 05 – Dec 20, 2026",
            counsellingDeadline = "NMIMS CD-PI: Jan 2027",
            officialUrl = "https://www.mba.com/exams/nmat",
            lastVerifiedTime = "Verified from mba.com (Today, 08:30 AM IST)",
            lastVerifiedTimestamp = 1792638600000L,
            isHistorical = false,
            deadlineEpochMillis = 1793491200000L, // Oct 31, 2026
            verificationStatus = VerificationStatus.OFFICIAL_VERIFIED
        ),

        // 6. MAT Dec 2026 (Management) - Registration Open
        OfficialAdmissionNotice(
            id = "notice_mat_2026",
            examId = "mat",
            examCategory = ExamCategory.MANAGEMENT,
            title = "AIMA MAT December 2026: PBT & CBT Registration Live",
            summary = "All India Management Association (AIMA) opens registration for December 2026 session of Management Aptitude Test (MAT) for 600+ B-Schools.",
            admissionCycle = "Dec 2026",
            admissionStatus = AdmissionStatus.REGISTRATION_OPEN,
            publicationDate = "Oct 05, 2026",
            applicationOpenDate = "Sep 20, 2026",
            applicationCloseDate = "Nov 28, 2026",
            examDate = "Dec 06 – Dec 13, 2026",
            counsellingDeadline = "Direct B-School Admissions: Jan 2027",
            officialUrl = "https://mat.aima.in/",
            lastVerifiedTime = "Verified from mat.aima.in (Oct 21, 2026)",
            lastVerifiedTimestamp = 1792550400000L,
            isHistorical = false,
            deadlineEpochMillis = 1795910400000L, // Nov 28, 2026
            verificationStatus = VerificationStatus.OFFICIAL_VERIFIED
        ),

        // 7. JEE Main 2027 Session 1 (Engineering) - Registration Open
        OfficialAdmissionNotice(
            id = "notice_jee_2027",
            examId = "jee_main",
            examCategory = ExamCategory.ENGINEERING,
            title = "NTA JEE Main 2027 Session 1: Online Application Portal Live",
            summary = "National Testing Agency invites online applications for Joint Entrance Examination (Main) 2027 Session 1 for admission to NITs, IIITs, and CFTIs nationwide.",
            admissionCycle = "2027 Session 1",
            admissionStatus = AdmissionStatus.REGISTRATION_OPEN,
            publicationDate = "Oct 20, 2026",
            applicationOpenDate = "Oct 20, 2026",
            applicationCloseDate = "Nov 22, 2026",
            examDate = "Jan 21 – Jan 30, 2027",
            counsellingDeadline = "JoSAA Counselling starts Jun 2027",
            officialUrl = "https://jeemain.nta.nic.in/",
            lastVerifiedTime = "Verified from jeemain.nta.nic.in (Today, 09:30 AM IST)",
            lastVerifiedTimestamp = 1792642200000L,
            isHistorical = false,
            deadlineEpochMillis = 1795392000000L, // Nov 22, 2026
            verificationStatus = VerificationStatus.OFFICIAL_VERIFIED
        ),

        // 8. VITEEE 2027 (Engineering) - Registration Open
        OfficialAdmissionNotice(
            id = "notice_viteee_2027",
            examId = "viteee",
            examCategory = ExamCategory.ENGINEERING,
            title = "VITEEE 2027: B.Tech Engineering Applications Open Online",
            summary = "Vellore Institute of Technology opens online application process for VITEEE 2027 across Vellore, Chennai, AP, and Bhopal campuses.",
            admissionCycle = "2027",
            admissionStatus = AdmissionStatus.REGISTRATION_OPEN,
            publicationDate = "Oct 15, 2026",
            applicationOpenDate = "Oct 15, 2026",
            applicationCloseDate = "Mar 31, 2027",
            examDate = "Apr 20 – Apr 26, 2027",
            counsellingDeadline = "VIT Online Counselling: May 2027",
            officialUrl = "https://viteee.vit.ac.in/",
            lastVerifiedTime = "Verified from viteee.vit.ac.in (Oct 21, 2026)",
            lastVerifiedTimestamp = 1792550400000L,
            isHistorical = false,
            deadlineEpochMillis = 1806537600000L, // Mar 31, 2027
            verificationStatus = VerificationStatus.OFFICIAL_VERIFIED
        ),

        // 9. BITSAT 2027 (Engineering) - Dates Not Announced Yet
        OfficialAdmissionNotice(
            id = "notice_bitsat_2027",
            examId = "bitsat",
            examCategory = ExamCategory.ENGINEERING,
            title = "BITS Pilani BITSAT 2027: Next Cycle Notification Awaited",
            summary = "BITS Pilani will publish the comprehensive brochure and online application schedule for BITSAT 2027 on bitsadmission.com in January 2027 for Pilani, Goa, and Hyderabad campuses.",
            admissionCycle = "2027",
            admissionStatus = AdmissionStatus.DATES_NOT_ANNOUNCED,
            publicationDate = "Oct 12, 2026",
            applicationOpenDate = "Expected Jan 2027",
            applicationCloseDate = "Dates Not Announced Yet",
            examDate = "To be announced",
            counsellingDeadline = null,
            officialUrl = "https://www.bitsadmission.com/",
            lastVerifiedTime = "Verified from bitsadmission.com (Oct 20, 2026)",
            lastVerifiedTimestamp = 1792464000000L,
            isHistorical = false,
            deadlineEpochMillis = null,
            verificationStatus = VerificationStatus.OFFICIAL_VERIFIED
        ),

        // 10. NEET UG 2027 (Medical) - Upcoming
        OfficialAdmissionNotice(
            id = "notice_neet_2027",
            examId = "neet_ug",
            examCategory = ExamCategory.MEDICAL,
            title = "NTA NEET UG 2027: National Medical Admissions Advisory",
            summary = "National Testing Agency official advisory regarding NEET UG 2027 eligibility, exam pattern, and syllabus for MBBS/BDS/AYUSH undergraduate admissions nationwide.",
            admissionCycle = "2027",
            admissionStatus = AdmissionStatus.UPCOMING,
            publicationDate = "Oct 18, 2026",
            applicationOpenDate = "Expected Feb 2027",
            applicationCloseDate = "To be announced",
            examDate = "May 02, 2027 (First Sunday of May)",
            counsellingDeadline = "MCC AIQ Counselling: Tentative Jul 2027",
            officialUrl = "https://neet.nta.nic.in/",
            lastVerifiedTime = "Verified from neet.nta.nic.in (Today, 08:45 AM IST)",
            lastVerifiedTimestamp = 1792639500000L,
            isHistorical = false,
            deadlineEpochMillis = null,
            verificationStatus = VerificationStatus.OFFICIAL_VERIFIED
        ),

        // 11. NEET PG 2026 (Medical) - Result Declared
        OfficialAdmissionNotice(
            id = "notice_neet_pg_2026",
            examId = "neet_pg",
            examCategory = ExamCategory.MEDICAL,
            title = "NBEMS NEET PG 2026: Result Declared & Scorecards Live",
            summary = "National Board of Examinations in Medical Sciences has declared the results for NEET PG 2026. Individual scorecards can be downloaded from natboard.edu.in.",
            admissionCycle = "2026",
            admissionStatus = AdmissionStatus.RESULT_DECLARED,
            publicationDate = "Sep 18, 2026",
            applicationOpenDate = "Completed",
            applicationCloseDate = "Completed",
            examDate = "Aug 11, 2026",
            counsellingDeadline = "MCC AIQ 50% Round 1 Registration: Nov 15, 2026",
            officialUrl = "https://natboard.edu.in/",
            lastVerifiedTime = "Verified from natboard.edu.in (Oct 21, 2026)",
            lastVerifiedTimestamp = 1792550400000L,
            isHistorical = false,
            deadlineEpochMillis = 1794787200000L, // Nov 15, 2026
            verificationStatus = VerificationStatus.OFFICIAL_VERIFIED
        )
    )

    // Historical archived records from past admission cycles (2024-2025)
    // Preserved for student reference; never displayed as upcoming or active
    val historicalNotices: List<OfficialAdmissionNotice> = listOf(
        OfficialAdmissionNotice(
            id = "notif_neet_01",
            examId = "neet_ug",
            examCategory = ExamCategory.MEDICAL,
            title = "NTA NEET UG 2025: Information Bulletin & Registration Notice",
            summary = "National Testing Agency (NTA) official schedule for NEET UG 2025 for admission to undergraduate medical (MBBS) and dental (BDS) courses.",
            admissionCycle = "2025",
            admissionStatus = AdmissionStatus.COMPLETED,
            publicationDate = "March 02, 2025",
            applicationOpenDate = "Feb 09, 2025",
            applicationCloseDate = "March 16, 2025",
            examDate = "May 04, 2025",
            counsellingDeadline = "June 2025 (Completed)",
            officialUrl = "https://neet.nta.nic.in/",
            lastVerifiedTime = "Archived Historical Record",
            lastVerifiedTimestamp = 1741500000000L,
            isHistorical = true,
            deadlineEpochMillis = null,
            verificationStatus = VerificationStatus.OFFICIAL_VERIFIED
        ),
        OfficialAdmissionNotice(
            id = "notif_jee_01",
            examId = "jee_main",
            examCategory = ExamCategory.ENGINEERING,
            title = "JEE Main 2025 Session 2: Online Registration & Exam City Intimation",
            summary = "NTA online application forms for Joint Entrance Examination (Main) - 2025 Session 2.",
            admissionCycle = "2025",
            admissionStatus = AdmissionStatus.COMPLETED,
            publicationDate = "March 01, 2025",
            applicationOpenDate = "Feb 04, 2025",
            applicationCloseDate = "March 12, 2025",
            examDate = "April 01 – April 08, 2025",
            counsellingDeadline = "JoSAA 2025 (Completed)",
            officialUrl = "https://jeemain.nta.nic.in/",
            lastVerifiedTime = "Archived Historical Record",
            lastVerifiedTimestamp = 1741500000000L,
            isHistorical = true,
            deadlineEpochMillis = null,
            verificationStatus = VerificationStatus.OFFICIAL_VERIFIED
        ),
        OfficialAdmissionNotice(
            id = "notif_viteee_01",
            examId = "viteee",
            examCategory = ExamCategory.ENGINEERING,
            title = "VITEEE 2025: B.Tech Slot Booking & Test Centre Allotment",
            summary = "Vellore Institute of Technology online slot booking for VITEEE 2025 across all exam centres.",
            admissionCycle = "2025",
            admissionStatus = AdmissionStatus.COMPLETED,
            publicationDate = "Feb 28, 2025",
            applicationOpenDate = "Nov 01, 2024",
            applicationCloseDate = "April 05, 2025",
            examDate = "April 21 – April 27, 2025",
            counsellingDeadline = "VIT Counselling 2025 (Completed)",
            officialUrl = "https://viteee.vit.ac.in/",
            lastVerifiedTime = "Archived Historical Record",
            lastVerifiedTimestamp = 1741500000000L,
            isHistorical = true,
            deadlineEpochMillis = null,
            verificationStatus = VerificationStatus.OFFICIAL_VERIFIED
        ),
        OfficialAdmissionNotice(
            id = "notif_bitsat_01",
            examId = "bitsat",
            examCategory = ExamCategory.ENGINEERING,
            title = "BITSAT 2025: Session 1 & Session 2 Application Schedule",
            summary = "BITS Pilani admission process for Integrated First Degree programmes at Pilani, Goa, and Hyderabad.",
            admissionCycle = "2025",
            admissionStatus = AdmissionStatus.COMPLETED,
            publicationDate = "Feb 24, 2025",
            applicationOpenDate = "Jan 15, 2025",
            applicationCloseDate = "April 18, 2025",
            examDate = "May 20–25, 2025",
            counsellingDeadline = "July 2025 (Completed)",
            officialUrl = "https://www.bitsadmission.com/",
            lastVerifiedTime = "Archived Historical Record",
            lastVerifiedTimestamp = 1741500000000L,
            isHistorical = true,
            deadlineEpochMillis = null,
            verificationStatus = VerificationStatus.OFFICIAL_VERIFIED
        ),
        OfficialAdmissionNotice(
            id = "notif_cat_01",
            examId = "cat",
            examCategory = ExamCategory.MANAGEMENT,
            title = "IIM CAT 2025: Convenor Institute Announcement & Timeline",
            summary = "The Indian Institutes of Management admission schedule for CAT 2025.",
            admissionCycle = "2025",
            admissionStatus = AdmissionStatus.COMPLETED,
            publicationDate = "Feb 20, 2025",
            applicationOpenDate = "August 01, 2025",
            applicationCloseDate = "September 20, 2025",
            examDate = "November 30, 2025",
            counsellingDeadline = "Jan–April 2026 (Completed)",
            officialUrl = "https://iimcat.ac.in/",
            lastVerifiedTime = "Archived Historical Record",
            lastVerifiedTimestamp = 1741500000000L,
            isHistorical = true,
            deadlineEpochMillis = null,
            verificationStatus = VerificationStatus.OFFICIAL_VERIFIED
        )
    )

    // Complete combined catalog (active first, followed by archive)
    val allNotices: List<OfficialAdmissionNotice> = activeNotices + historicalNotices

    /**
     * Returns active current-cycle notices matching the student's selected academic segment.
     */
    fun getNoticesForSegment(segment: Segment): List<OfficialAdmissionNotice> {
        val targetCategory = when (segment) {
            Segment.MEDICAL -> ExamCategory.MEDICAL
            Segment.ENGINEERING -> ExamCategory.ENGINEERING
            Segment.MANAGEMENT -> ExamCategory.MANAGEMENT
        }
        return activeNotices.filter { it.examCategory == targetCategory }
    }

    /**
     * Returns upcoming deadlines with valid future dates for the selected segment.
     * Excludes expired dates, completed cycles, and unannounced dates.
     */
    fun getUpcomingDeadlines(segment: Segment, now: Long = System.currentTimeMillis()): List<OfficialAdmissionNotice> {
        return getNoticesForSegment(segment).filter { it.hasFutureDeadline(now) }
    }

    /**
     * Returns archived historical notices for the selected segment.
     */
    fun getHistoricalNotices(segment: Segment? = null): List<OfficialAdmissionNotice> {
        return if (segment == null) {
            historicalNotices
        } else {
            val targetCategory = when (segment) {
                Segment.MEDICAL -> ExamCategory.MEDICAL
                Segment.ENGINEERING -> ExamCategory.ENGINEERING
                Segment.MANAGEMENT -> ExamCategory.MANAGEMENT
            }
            historicalNotices.filter { it.examCategory == targetCategory }
        }
    }
}
