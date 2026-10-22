package com.example.data.service

import com.example.data.model.ExamTestSet
import com.example.data.model.Segment
import com.example.data.model.TestAttempt
import com.example.data.model.TestQuestion
import com.example.data.model.TestSeriesItem
import com.example.data.repository.QuestionBankData
import java.util.UUID

sealed interface QuestionSelectionResult {
    data class Success(
        val testItem: TestSeriesItem,
        val attemptId: String,
        val selectedQuestionIds: List<Int>,
        val isRepeatedPool: Boolean,
        val infoMessage: String? = null
    ) : QuestionSelectionResult

    data class InsufficientQuestions(
        val message: String,
        val availableCount: Int
    ) : QuestionSelectionResult
}

object QuestionSelectionService {

    const val MINIMUM_QUESTIONS_REQUIRED = 25

    /**
     * Safely shuffles the options of a question and re-computes the correctOptionIndex
     * so the correct answer is NEVER altered when options are re-ordered.
     */
    fun shuffleOptionsPreservingCorrect(question: TestQuestion): TestQuestion {
        if (question.options.isEmpty() || question.correctOptionIndex !in question.options.indices) {
            return question
        }
        val correctOptionText = question.options[question.correctOptionIndex]
        val shuffledOptions = question.options.shuffled()
        val newCorrectIndex = shuffledOptions.indexOf(correctOptionText)
        return question.copy(
            options = shuffledOptions,
            correctOptionIndex = if (newCorrectIndex != -1) newCorrectIndex else question.correctOptionIndex
        )
    }

    /**
     * Selects at least 25 distinct questions for a new attempt.
     * Prefers questions unseen in previous attempts for the same exam/set.
     */
    fun prepareAttemptQuestions(
        examId: String,
        setId: String,
        candidateQuestions: List<TestQuestion>,
        userAttempts: List<TestAttempt>,
        targetCount: Int = MINIMUM_QUESTIONS_REQUIRED
    ): QuestionSelectionResult {
        // Remove duplicates by ID in candidate pool
        val distinctPool = candidateQuestions.distinctBy { it.id }

        if (distinctPool.size < MINIMUM_QUESTIONS_REQUIRED) {
            return QuestionSelectionResult.InsufficientQuestions(
                message = "This test set currently has ${distinctPool.size} verified questions available. At least $MINIMUM_QUESTIONS_REQUIRED questions are required to start a test attempt.",
                availableCount = distinctPool.size
            )
        }

        // Retrieve question IDs previously seen in attempts for this test/exam
        val seenQuestionIds = userAttempts
            .filter { it.testId == setId || it.testId.startsWith(examId) }
            .flatMap { it.attemptedQuestionIds }
            .toSet()

        val unseenPool = distinctPool.filter { it.id !in seenQuestionIds }
        val seenPool = distinctPool.filter { it.id in seenQuestionIds }

        val selectedQuestions = mutableListOf<TestQuestion>()
        var isRepeatedPool = false
        var infoMessage: String? = null

        if (unseenPool.size >= targetCount) {
            // Sufficient brand new unseen questions available
            selectedQuestions.addAll(unseenPool.shuffled().take(targetCount))
        } else {
            // Use all unseen questions first
            selectedQuestions.addAll(unseenPool.shuffled())
            val remainingNeeded = targetCount - selectedQuestions.size
            // Fill remainder with seen questions in randomized combination
            selectedQuestions.addAll(seenPool.shuffled().take(remainingNeeded))
            isRepeatedPool = true
            infoMessage = if (unseenPool.isEmpty()) {
                "You have attempted all available questions in this set. Questions have been remixed in a new combination."
            } else {
                "Included ${unseenPool.size} new questions and $remainingNeeded questions from your review pool."
            }
        }

        // Shuffle question sequence and option order safely
        val finalQuestions = selectedQuestions
            .shuffled()
            .map { shuffleOptionsPreservingCorrect(it) }

        val attemptId = "att_${UUID.randomUUID().toString().take(10)}"
        val selectedIds = finalQuestions.map { it.id }

        val segment = when {
            examId.contains("neet") || examId.contains("ini") -> Segment.MEDICAL
            examId.contains("cat") || examId.contains("xat") || examId.contains("snap") || examId.contains("cmat") -> Segment.MANAGEMENT
            else -> Segment.ENGINEERING
        }

        val testItem = TestSeriesItem(
            id = setId,
            title = "$examId Test Attempt ($targetCount Questions)",
            segment = segment,
            examCategory = segment.displayName,
            syllabus = "Verified Syllabus Coverage",
            questionCount = finalQuestions.size,
            durationMinutes = if (finalQuestions.size >= 50) 90 else 45,
            questions = finalQuestions,
            isDemo = false
        )

        return QuestionSelectionResult.Success(
            testItem = testItem,
            attemptId = attemptId,
            selectedQuestionIds = selectedIds,
            isRepeatedPool = isRepeatedPool,
            infoMessage = infoMessage
        )
    }

    /**
     * Resolves questions for an ExamTestSet from its own pool or the master category pool.
     */
    fun getFullCandidatePool(testSet: ExamTestSet): List<TestQuestion> {
        val pool = mutableListOf<TestQuestion>()
        pool.addAll(testSet.questions)

        // Enrich from master category bank if set questions are fewer than 30
        val categoryQuestions = when {
            testSet.examId.contains("neet") || testSet.examId.contains("ini") -> QuestionBankData.medicalQuestions
            testSet.examId.contains("cat") || testSet.examId.contains("xat") || testSet.examId.contains("snap") || testSet.examId.contains("cmat") -> QuestionBankData.managementQuestions
            else -> QuestionBankData.engineeringQuestions
        }

        categoryQuestions.forEach { q ->
            if (pool.none { it.id == q.id }) {
                pool.add(q)
            }
        }

        return pool
    }
}
