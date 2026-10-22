package com.example

import com.example.data.model.CollegeAdvertisement
import com.example.data.model.Segment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CollegeAdvertisementTest {

    @Test
    fun testApprovedAdvertisementStatus() {
        val approvedAd = CollegeAdvertisement(
            id = "test_ad_1",
            collegeId = "bits_pilani",
            collegeName = "BITS Pilani",
            headline = "Admissions 2025",
            promotionalText = "Join premier institute",
            courses = listOf("B.E. CSE"),
            location = "Pilani",
            officialDestinationUrl = "https://www.bitsadmission.com",
            approvalStatus = "APPROVED",
            segment = Segment.ENGINEERING
        )

        assertTrue("Approved ad must have isApprovedAndActive == true", approvedAd.isApprovedAndActive)
    }

    @Test
    fun testPendingAdvertisementStatusNotActive() {
        val pendingAd = CollegeAdvertisement(
            id = "test_ad_2",
            collegeId = "amrita",
            collegeName = "Amrita University",
            headline = "AEEE 2025",
            promotionalText = "Engineering admissions",
            courses = listOf("B.Tech"),
            location = "Coimbatore",
            officialDestinationUrl = "https://www.amrita.edu",
            approvalStatus = "PENDING_REVIEW",
            segment = Segment.ENGINEERING
        )

        assertFalse("Pending ad must NOT be active in student feed", pendingAd.isApprovedAndActive)
    }

    @Test
    fun testTelemetryTracking() {
        val ad = CollegeAdvertisement(
            id = "test_ad_3",
            collegeId = "vit",
            collegeName = "VIT Vellore",
            headline = "VITEEE 2025",
            promotionalText = "Slots open",
            courses = listOf("B.Tech"),
            location = "Vellore",
            officialDestinationUrl = "https://viteee.vit.ac.in",
            impressionsCount = 100,
            clicksCount = 10
        )

        val updatedAd = ad.copy(
            impressionsCount = ad.impressionsCount + 1,
            clicksCount = ad.clicksCount + 1
        )

        assertEquals(101, updatedAd.impressionsCount)
        assertEquals(11, updatedAd.clicksCount)
    }
}
