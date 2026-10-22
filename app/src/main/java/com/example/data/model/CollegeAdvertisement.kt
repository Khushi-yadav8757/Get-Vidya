package com.example.data.model

import com.squareup.moshi.JsonClass

enum class AdApprovalStatus(val label: String) {
    APPROVED("Approved & Live"),
    PENDING_REVIEW("Pending Verification"),
    REJECTED("Rejected")
}

@JsonClass(generateAdapter = true)
data class CollegeAdvertisement(
    val id: String,
    val collegeId: String,
    val collegeName: String,
    val logoUrl: String = "",
    val imageUrl: String = "",
    val localImageRes: Int? = null,
    val headline: String,
    val promotionalText: String,
    val courses: List<String>,
    val location: String,
    val officialDestinationUrl: String,
    val admissionStatus: String? = "Applications Open 2025",
    val verifiedDeadline: String? = null,
    val isSponsored: Boolean = true,
    val approvalStatus: String = "APPROVED", // "APPROVED", "PENDING_REVIEW", "REJECTED"
    val segment: Segment = Segment.ENGINEERING,
    val advertiserAccountId: String = "partner_inst_01",
    val campaignStartDate: String = "01 Jan 2025",
    val campaignEndDate: String = "30 Jun 2025",
    val impressionsCount: Int = 1240,
    val clicksCount: Int = 188
) {
    val isApprovedAndActive: Boolean
        get() = approvalStatus == "APPROVED"
}
