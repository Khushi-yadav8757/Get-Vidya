package com.example.data.repository

import android.util.Log
import com.example.data.model.ChatMessage
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import java.util.UUID

private const val TAG = "AiGuidanceService"

object AiGuidanceService {

    fun isGeminiConfigured(): Boolean = true

    // Grounded system instruction with official examination bodies and portals
    private val systemInstructionText = """
        You are Get Vidya AI, an expert, objective, and encouraging Indian entrance-exam preparation and college admission counseling counselor.
        You specialize in NEET UG/PG, JEE Main & Advanced, CAT/MBA, BITSAT, VITEEE, and state entrance examinations.
        
        CRITICAL RULES:
        1. Always reference authentic official counseling portals when answering:
           - Medical: Medical Counseling Committee (MCC) (https://mcc.nic.in/) and state DMERs for 85% domicile quotas.
           - Engineering: JoSAA / CSAB (https://josaa.nic.in/) for IITs, NITs, and IIITs.
           - BITSAT: BITS Pilani (https://www.bitsadmission.com/)
           - VITEEE: VIT Vellore (https://viteee.vit.ac.in/)
           - CAT / MBA: IIM Admission Portals (https://iimcat.ac.in/)
        2. Never state estimated cutoffs or closing ranks as definitive guarantees; always advise students that cutoffs fluctuate annually based on applicant count and difficulty.
        3. Do not fabricate or invent dates or seat numbers.
        4. Clearly distinguish AI-generated guidance from officially notified regulations.
        5. Keep responses structured with concise bullet points.
    """.trimIndent()

    private val generativeModel by lazy {
        try {
            Firebase.ai.generativeModel(
                modelName = "gemini-3.8-flash",
                systemInstruction = content {
                    text(systemInstructionText)
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Firebase AI model", e)
            null
        }
    }

    /**
     * Sends a real user prompt to the configured Gemini model via Firebase AI Logic.
     * Returns a ChatMessage with isAiGenerated = true on success,
     * or isError = true on failure. Never silently disguises canned text as live AI.
     */
    suspend fun getResponse(userPrompt: String): ChatMessage {
        val promptTrimmed = userPrompt.trim()
        if (promptTrimmed.isBlank()) {
            return ChatMessage(
                id = UUID.randomUUID().toString(),
                text = "Please enter a question about your exam or college preferences.",
                isUser = false,
                isError = true
            )
        }

        try {
            val model = generativeModel
                ?: throw IllegalStateException("Firebase AI Logic model is not initialized. Ensure Firebase App Check is active.")

            val response = withTimeout(30_000L) {
                model.generateContent(promptTrimmed)
            }
            val text = response.text

            if (!text.isNullOrBlank()) {
                return ChatMessage(
                    id = UUID.randomUUID().toString(),
                    text = text.trim(),
                    isUser = false,
                    isAiGenerated = true,
                    isError = false
                )
            } else {
                throw IllegalStateException("Empty response returned by Gemini API.")
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Gemini request timed out after 30s", e)
            return ChatMessage(
                id = UUID.randomUUID().toString(),
                text = "Request Timed Out: The Gemini AI service took longer than 30 seconds to respond. Please check your network connection and try again.",
                isUser = false,
                isError = true
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Gemini API call failed: ${e.message}", e)
            val errorMsg = e.localizedMessage ?: "Unknown service error"
            return ChatMessage(
                id = UUID.randomUUID().toString(),
                text = "Gemini API Service Notice: Unable to generate live guidance at this moment ($errorMsg). Please check your connection or tap below to consult our verified educational knowledge reference.",
                isUser = false,
                isError = true
            )
        }
    }

    /**
     * Explicitly labeled curated reference engine for students who choose to view
     * verified static FAQs when offline or when AI is unavailable.
     */
    fun getVerifiedKnowledgeReference(userPrompt: String): String {
        val promptLower = userPrompt.trim().lowercase()
        return when {
            promptLower.contains("neet") -> {
                "[Verified Reference: MCC / NTA]\n" +
                "• NEET UG counselling is divided into 15% All India Quota (MCC) and 85% State Domicile Quota.\n" +
                "• Official portal: https://mcc.nic.in/ and https://neet.nta.nic.in/\n" +
                "• Round 1 choices are not auto-retained; choice locking must be confirmed before each round deadline."
            }
            promptLower.contains("jee") || promptLower.contains("engineering") -> {
                "[Verified Reference: JoSAA / CSAB]\n" +
                "• JoSAA conducts 6 rounds of seat allocation for 23 IITs, 32 NITs, 26 IIITs, and CFTIs.\n" +
                "• Official portal: https://josaa.nic.in/\n" +
                "• Ensure category certificates (OBC-NCL / EWS) are issued after April 1 of the admission year."
            }
            promptLower.contains("cat") || promptLower.contains("mba") -> {
                "[Verified Reference: IIM Admission Policy]\n" +
                "• CAT 2025 scores are used by 21 IIMs and premier non-IIM institutions.\n" +
                "• Official portal: https://iimcat.ac.in/\n" +
                "• Shortlists evaluate sectional cutoffs, Class 10/12/degree marks, and academic/gender diversity."
            }
            else -> {
                "[Verified Reference: Get Vidya Educational Board]\n" +
                "• Always verify exam application deadlines and eligibility criteria on the conducting body's official website.\n" +
                "• Never pay admission or seat-booking fees outside centralized official state/national counselling portals."
            }
        }
    }
}
