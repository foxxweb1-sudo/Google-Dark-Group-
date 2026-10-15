const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = "gen-lang-client-0088715159";
const DATABASE_ID = "ai-studio-android-cybenode-f4584900-89a7-4167-9d38-dfd675d0a521";
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

test("Users collection access", async (t) => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const unauthDb = testEnv.unauthenticatedContext().firestore();

  const aliceData = {
    userId: ALICE_UID,
    username: "alice123",
    name: "Alice",
    createdAt: new Date()
  };

  await t.test("Alice can create her own profile", async () => {
    await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).set(aliceData));
  });

  await t.test("Bob cannot create Alice's profile", async () => {
    await assertFails(bobDb.collection("users").doc(ALICE_UID).set(aliceData));
  });

  await t.test("Unauthenticated user cannot create profile", async () => {
    await assertFails(unauthDb.collection("users").doc(ALICE_UID).set(aliceData));
  });

  await t.test("Bob can read Alice's profile", async () => {
    await testEnv.withSecurityRulesDisabled(async (context) => {
      await context.firestore().collection("users").doc(ALICE_UID).set(aliceData);
    });
    await assertSucceeds(bobDb.collection("users").doc(ALICE_UID).get());
  });
});

test("Rooms collection access", async (t) => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  const roomData = {
    id: "room_abc",
    name: "Alice Room",
    hostId: ALICE_UID,
    isLocked: false,
    createdAt: new Date()
  };

  await t.test("Alice can create a room", async () => {
    await assertSucceeds(aliceDb.collection("rooms").doc("room_abc").set(roomData));
  });

  await t.test("Bob cannot delete Alice's room", async () => {
    await testEnv.withSecurityRulesDisabled(async (context) => {
      await context.firestore().collection("rooms").doc("room_abc").set(roomData);
    });
    await assertFails(bobDb.collection("rooms").doc("room_abc").delete());
  });
});
