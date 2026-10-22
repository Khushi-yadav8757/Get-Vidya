package com.example

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class FirestoreRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun unauthenticatedUserCannotWriteProfile() = runBlocking {
    auth.signOut()
    try {
      firestore.collection("users").document("unauth_user").set(
        mapOf(
          "fullName" to "Hacker",
          "email" to "hacker@test.com",
          "targetExam" to "NEET"
        )
      ).await()
      fail("Expected FirebaseFirestoreException PERMISSION_DENIED")
    } catch (e: FirebaseFirestoreException) {
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, e.code)
    }
  }

  @Test
  fun authenticatedUserCanManageOwnProfile() = runBlocking {
    val aliceUid = signInTestUser("alice@test.com")
    val userDoc = firestore.collection("users").document(aliceUid)

    userDoc.set(
      mapOf(
        "fullName" to "Alice Sharma",
        "email" to "alice@test.com",
        "targetExam" to "NEET UG",
        "dreamCollege" to "AIIMS Delhi",
        "percentileTarget" to 99
      )
    ).await()

    val snapshot = userDoc.get().await()
    assertTrue(snapshot.exists())
    assertEquals("Alice Sharma", snapshot.getString("fullName"))
    assertEquals("NEET UG", snapshot.getString("targetExam"))
  }

  @Test
  fun crossUserCannotReadAnotherUsersProfile() = runBlocking {
    val aliceUid = signInTestUser("alice@test.com")
    firestore.collection("users").document(aliceUid).set(
      mapOf(
        "fullName" to "Alice Sharma",
        "email" to "alice@test.com",
        "targetExam" to "NEET UG"
      )
    ).await()

    // Sign in as Bob
    val bobUid = signInTestUser("bob@test.com")
    try {
      firestore.collection("users").document(aliceUid).get().await()
      fail("Expected PERMISSION_DENIED when Bob attempts to read Alice's profile")
    } catch (e: FirebaseFirestoreException) {
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, e.code)
    }
  }

  @Test
  fun authenticatedUserCanSaveCollegesWithIsolation() = runBlocking {
    val aliceUid = signInTestUser("alice@test.com")
    val collegeDoc = firestore.collection("users").document(aliceUid)
      .collection("savedColleges").document("aiims-delhi")

    collegeDoc.set(
      mapOf(
        "collegeId" to "aiims-delhi",
        "collegeName" to "AIIMS New Delhi",
        "location" to "New Delhi",
        "rating" to 4.9,
        "cutoffText" to "Rank 1 - 50",
        "fees" to "₹ 1,628 / yr"
      )
    ).await()

    val snapshot = collegeDoc.get().await()
    assertTrue(snapshot.exists())
    assertEquals("AIIMS New Delhi", snapshot.getString("collegeName"))

    // Bob cannot read Alice's saved college
    signInTestUser("bob@test.com")
    try {
      firestore.collection("users").document(aliceUid)
        .collection("savedColleges").document("aiims-delhi").get().await()
      fail("Expected PERMISSION_DENIED when Bob reads Alice's saved college")
    } catch (e: FirebaseFirestoreException) {
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, e.code)
    }
  }

  @Test
  fun authenticatedUserCanSaveTestAttempts() = runBlocking {
    val aliceUid = signInTestUser("alice@test.com")
    val attemptDoc = firestore.collection("users").document(aliceUid)
      .collection("testAttempts").document("attempt-001")

    attemptDoc.set(
      mapOf(
        "testId" to "neet_mock_1",
        "examCode" to "neet-ug",
        "examTitle" to "NEET 2025 Full Mock",
        "score" to 650,
        "totalQuestions" to 180,
        "correctAnswers" to 165,
        "percentileEstimate" to 99.2,
        "attemptDate" to "2025-05-15"
      )
    ).await()

    val snapshot = attemptDoc.get().await()
    assertTrue(snapshot.exists())
    assertEquals(650L, snapshot.getLong("score"))
  }
}
