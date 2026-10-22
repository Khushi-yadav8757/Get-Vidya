package com.example

import com.example.data.model.ExamCategory
import com.example.data.model.Segment
import com.example.data.repository.ExamCatalogueData
import com.example.data.repository.OfficialNoticesData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun examCatalogue_containsAllRequiredCategories() {
        val categories = ExamCatalogueData.allExams.map { it.category }.toSet()
        assertTrue(categories.contains(ExamCategory.ENGINEERING))
        assertTrue(categories.contains(ExamCategory.MEDICAL))
        assertTrue(categories.contains(ExamCategory.MANAGEMENT))
        assertTrue(categories.contains(ExamCategory.OTHER))
    }

    @Test
    fun examCatalogue_everyExamHasFiveTestSets() {
        val sampleExams = listOf("viteee", "jee_main", "neet_ug", "cat")
        for (examId in sampleExams) {
            val sets = ExamCatalogueData.getTestSetsForExam(examId)
            assertEquals("Exam $examId should provide 5 test sets", 5, sets.size)
            assertTrue("Set 1 must be PYQ", sets[0].isPyq)
            assertFalse("Set 5 should be practice simulation", sets[4].isPyq)
            assertNotNull("Set toTestSeriesItem conversion must work", sets[0].toTestSeriesItem())
        }
    }

    @Test
    fun officialNotices_haveVerifiedOfficialUrls() {
        val notices = OfficialNoticesData.allNotices
        assertTrue("Notices list should not be empty", notices.isNotEmpty())
        for (notice in notices) {
            assertTrue("Notice URL must be https", notice.officialUrl.startsWith("https://"))
            assertNotNull("Notice publication date must be populated", notice.publicationDate)
        }
        val medicalNotices = OfficialNoticesData.getNoticesForSegment(Segment.MEDICAL)
        assertTrue(medicalNotices.isNotEmpty())
    }
}
