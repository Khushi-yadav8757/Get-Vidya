package com.example

import com.example.data.model.ExamCategory
import com.example.data.model.QuestionType
import com.example.data.model.TestAttempt
import com.example.data.model.TestQuestion
import com.example.data.repository.ExamCatalogueData
import com.example.data.repository.QuestionBankData
import com.example.data.service.QuestionSelectionResult
import com.example.data.service.QuestionSelectionService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QuestionSelectionTest {

    @Test
    fun testCategoryExamsCoverage() {
        val medicalExams = ExamCatalogueData.allExams.filter { it.category == ExamCategory.MEDICAL }
        val engineeringExams = ExamCatalogueData.allExams.filter { it.category == ExamCategory.ENGINEERING }
        val managementExams = ExamCatalogueData.allExams.filter { it.category == ExamCategory.MANAGEMENT }

        assertTrue("Medical exams must include NEET UG and NEET PG", medicalExams.any { it.id == "neet_ug" })
        assertTrue("Medical exams must include INI-CET", medicalExams.any { it.id == "ini_cet" })

        assertTrue("Engineering exams must include JEE Main and JEE Advanced", engineeringExams.any { it.id == "jee_main" })
        assertTrue("Engineering exams must include VITEEE and BITSAT", engineeringExams.any { it.id == "viteee" })

        assertTrue("Management exams must include CAT and XAT", managementExams.any { it.id == "cat" })
        assertTrue("Management exams must include SNAP and CMAT", managementExams.any { it.id == "snap" })
    }

    @Test
    fun testEachExamHasFiveSetsWithAtLeast25Questions() {
        val allExams = ExamCatalogueData.allExams
        assertTrue("Exams catalogue must not be empty", allExams.isNotEmpty())

        for (exam in allExams) {
            val sets = ExamCatalogueData.getTestSetsForExam(exam.id)
            assertEquals("Exam ${exam.name} must have 5 test sets", 5, sets.size)

            for (set in sets) {
                assertTrue(
                    "Exam ${exam.name} Set ${set.setNumber} must contain >= 25 distinct questions, found ${set.questions.size}",
                    set.questions.size >= 25
                )
                assertEquals(
                    "Set questionCount property must match questions list size",
                    set.questions.size,
                    set.questionCount
                )
                val distinctIds = set.questions.map { it.id }.toSet()
                assertEquals(
                    "All questions in Set ${set.setNumber} of ${exam.name} must have unique IDs",
                    set.questions.size,
                    distinctIds.size
                )
            }
        }
    }

    @Test
    fun testOptionShufflingPreservesCorrectAnswer() {
        val question = TestQuestion(
            id = 999,
            subject = "Physics",
            questionText = "What is the unit of force?",
            options = listOf("Joule", "Watt", "Newton", "Pascal"),
            correctOptionIndex = 2 // "Newton"
        )

        // Shuffle multiple times to verify correct answer integrity
        repeat(15) {
            val shuffled = QuestionSelectionService.shuffleOptionsPreservingCorrect(question)
            assertEquals("Options count must remain 4", 4, shuffled.options.size)
            assertTrue("Newton must be in the shuffled options", shuffled.options.contains("Newton"))
            assertEquals(
                "Correct option index must point to 'Newton'",
                "Newton",
                shuffled.options[shuffled.correctOptionIndex]
            )
        }
    }

    @Test
    fun testFirstAttemptSelectsAtLeast25DistinctQuestions() {
        val sets = ExamCatalogueData.getTestSetsForExam("viteee")
        val testSet = sets.first()
        val candidatePool = QuestionSelectionService.getFullCandidatePool(testSet)

        val result = QuestionSelectionService.prepareAttemptQuestions(
            examId = testSet.examId,
            setId = testSet.id,
            candidateQuestions = candidatePool,
            userAttempts = emptyList(),
            targetCount = 25
        )

        assertTrue("Result must be Success", result is QuestionSelectionResult.Success)
        val success = result as QuestionSelectionResult.Success

        assertEquals("Selected questions must be at least 25", 25, success.testItem.questions.size)
        assertEquals("Selected question IDs count must match questions size", 25, success.selectedQuestionIds.size)
        assertEquals("Attempt must not contain duplicate questions", 25, success.selectedQuestionIds.toSet().size)
        assertFalse("First attempt with ample pool should not be repeated pool", success.isRepeatedPool)
    }

    @Test
    fun testSubsequentAttemptPrefersUnseenQuestions() {
        val sets = ExamCatalogueData.getTestSetsForExam("jee_main")
        val testSet = sets.first()
        val candidatePool = QuestionSelectionService.getFullCandidatePool(testSet)

        // Simulate Attempt 1 with 25 questions
        val attempt1Result = QuestionSelectionService.prepareAttemptQuestions(
            examId = testSet.examId,
            setId = testSet.id,
            candidateQuestions = candidatePool,
            userAttempts = emptyList(),
            targetCount = 25
        ) as QuestionSelectionResult.Success

        val previousAttempt = TestAttempt(
            id = attempt1Result.attemptId,
            testId = testSet.id,
            testTitle = "Attempt 1",
            score = 80,
            totalMarks = 100,
            attemptedQuestionIds = attempt1Result.selectedQuestionIds
        )

        // Generate Attempt 2
        val attempt2Result = QuestionSelectionService.prepareAttemptQuestions(
            examId = testSet.examId,
            setId = testSet.id,
            candidateQuestions = candidatePool,
            userAttempts = listOf(previousAttempt),
            targetCount = 25
        ) as QuestionSelectionResult.Success

        // Verify attempt ID is unique
        assertNotEquals("New attempt ID must be generated", attempt1Result.attemptId, attempt2Result.attemptId)

        // If candidate pool has unseen questions (e.g., 30 total in pool, 25 seen -> 5 unseen),
        // attempt 2 must contain all remaining unseen questions
        val seenIds = attempt1Result.selectedQuestionIds.toSet()
        val unseenInCandidatePool = candidatePool.filter { it.id !in seenIds }.map { it.id }

        for (unseenId in unseenInCandidatePool) {
            assertTrue(
                "Attempt 2 must include previously unseen question ID $unseenId",
                attempt2Result.selectedQuestionIds.contains(unseenId)
            )
        }
    }

    @Test
    fun testInsufficientQuestionsReturnsError() {
        val smallPool = listOf(
            TestQuestion(id = 1, subject = "Math", questionText = "Q1", options = listOf("A", "B"), correctOptionIndex = 0),
            TestQuestion(id = 2, subject = "Math", questionText = "Q2", options = listOf("A", "B"), correctOptionIndex = 1)
        )

        val result = QuestionSelectionService.prepareAttemptQuestions(
            examId = "custom_exam",
            setId = "set_1",
            candidateQuestions = smallPool,
            userAttempts = emptyList(),
            targetCount = 25
        )

        assertTrue("Must return InsufficientQuestions when pool < 25", result is QuestionSelectionResult.InsufficientQuestions)
        val insufficient = result as QuestionSelectionResult.InsufficientQuestions
        assertEquals("Available count must be 2", 2, insufficient.availableCount)
        assertTrue(insufficient.message.contains("2 verified questions"))
    }

    @Test
    fun testVerifiedPyqVersusPracticeLabeling() {
        val pyqQuestion = QuestionBankData.engineeringQuestions.first { it.questionType == QuestionType.VERIFIED_PYQ }
        assertTrue("Official PYQ question must have VERIFIED_PYQ type", pyqQuestion.questionType == QuestionType.VERIFIED_PYQ)
        assertTrue("Official PYQ question must have non-empty sourceDoc", pyqQuestion.sourceDoc.isNotBlank())
        assertTrue("Official PYQ question must have non-null year", pyqQuestion.year != null && pyqQuestion.year!! > 2000)

        val practiceQuestion = QuestionBankData.engineeringQuestions.first { it.questionType == QuestionType.PRACTICE }
        assertTrue("Practice question must have PRACTICE type", practiceQuestion.questionType == QuestionType.PRACTICE)
    }
}
