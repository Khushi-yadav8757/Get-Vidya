const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated: cannot read or write user profile", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).set({
    fullName: "Alice Student",
    email: "alice@example.com",
    targetExam: "NEET UG",
  }));
});

test("Authenticated user: can read and write own profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).set({
    fullName: "Alice Student",
    email: "alice@example.com",
    targetExam: "NEET UG",
    dreamCollege: "AIIMS New Delhi",
    percentileTarget: 99,
  }));
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());
});

test("Authenticated user: cannot read another user's profile", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(BOB_UID).set({
      fullName: "Bob Student",
      email: "bob@example.com",
      targetExam: "JEE Main",
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).get());
});

test("Authenticated user: can manage own saved colleges", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("savedColleges").doc("iit-bombay").set({
    collegeId: "iit-bombay",
    collegeName: "IIT Bombay",
    location: "Mumbai, Maharashtra",
    rating: 4.9,
    cutoffText: "JEE Adv Rank < 65",
    fees: "₹2.2 Lakhs / yr",
  }));
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("savedColleges").doc("iit-bombay").get());

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("savedColleges").doc("iit-bombay").get());
});

test("Authenticated user: can record own test attempt", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("testAttempts").doc("attempt-1").set({
    testId: "neet-mock-1",
    examCode: "neet-ug",
    examTitle: "NEET 2025 Full Syllabus Mock Test",
    score: 640,
    totalQuestions: 180,
    correctAnswers: 160,
    percentileEstimate: 98.7,
    attemptDate: "2025-05-10",
  }));

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("testAttempts").doc("attempt-1").get());
});

test("Public: anyone can read official notices, but cannot write", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("officialNotices").doc("notice-1").set({
      title: "NEET UG 2025 Registration Open",
      examCode: "neet-ug",
      publishedDate: "2025-02-15",
      summary: "National Testing Agency announces registration dates.",
      sourceUrl: "https://exams.nta.ac.in/NEET/",
      category: "Medical",
      isUrgent: true,
    });
  });

  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertSucceeds(unauthDb.collection("officialNotices").doc("notice-1").get());

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("officialNotices").doc("notice-2").set({
    title: "Fake notice",
    examCode: "neet-ug",
    summary: "Hacked notice",
  }));
});

test("College Advertisements: only approved ads visible to public; authenticated user can submit pending ad", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("collegeAdvertisements").doc("ad-approved").set({
      collegeName: "BITS Pilani",
      headline: "BITSAT 2025 Live",
      officialDestinationUrl: "https://bitsadmission.com",
      approvalStatus: "APPROVED",
      advertiserAccountId: "bits_inst",
    });
    await context.firestore().collection("collegeAdvertisements").doc("ad-pending").set({
      collegeName: "Secret College",
      headline: "Pending Review Ad",
      officialDestinationUrl: "https://secret.edu",
      approvalStatus: "PENDING_REVIEW",
      advertiserAccountId: ALICE_UID,
    });
  });

  const unauthDb = testEnv.unauthenticatedContext().firestore();
  // Anyone can read approved ad
  await assertSucceeds(unauthDb.collection("collegeAdvertisements").doc("ad-approved").get());
  // Public cannot read unapproved ad
  await assertFails(unauthDb.collection("collegeAdvertisements").doc("ad-pending").get());

  // Alice (the advertiser) can read her own pending ad
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("collegeAdvertisements").doc("ad-pending").get());

  // Alice can submit a new ad with PENDING_REVIEW
  await assertSucceeds(aliceDb.collection("collegeAdvertisements").doc("ad-new").set({
    collegeName: "New College",
    headline: "Admissions 2025",
    officialDestinationUrl: "https://newcollege.edu",
    approvalStatus: "PENDING_REVIEW",
    advertiserAccountId: ALICE_UID,
  }));

  // Alice cannot self-approve her own ad
  await assertFails(aliceDb.collection("collegeAdvertisements").doc("ad-hacked").set({
    collegeName: "Hacked College",
    headline: "Admissions 2025",
    officialDestinationUrl: "https://hacked.edu",
    approvalStatus: "APPROVED",
    advertiserAccountId: ALICE_UID,
  }));
});

