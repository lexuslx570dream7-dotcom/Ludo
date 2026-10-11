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

beforeEach(async () => {
  await testEnv.clearFirestore();
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

test("Unauthenticated read/write to users rejected", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).set({
    userId: ALICE_UID,
    username: "Alice",
    coins: 1000,
    gems: 50,
    level: 1,
    createdAt: new Date(),
    updatedAt: new Date()
  }));
});

test("Authenticated user can write own profile but not another user's profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  
  // Alice writes Alice
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).set({
    userId: ALICE_UID,
    username: "Alice",
    coins: 1000,
    gems: 50,
    level: 1,
    createdAt: new Date(),
    updatedAt: new Date()
  }));

  // Alice attempts to write Bob
  await assertFails(aliceDb.collection("users").doc(BOB_UID).set({
    userId: BOB_UID,
    username: "BobHacked",
    coins: 999999,
    gems: 99999,
    level: 99,
    createdAt: new Date(),
    updatedAt: new Date()
  }));
});

test("Alice can create game room where she is creator", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();

  await assertSucceeds(aliceDb.collection("game_rooms").doc("room_1").set({
    id: "room_1",
    mode: "TWO_PLAYER",
    status: "WAITING",
    creatorId: ALICE_UID,
    playerUids: [ALICE_UID],
    createdAt: new Date(),
    updatedAt: new Date()
  }));

  // But Alice cannot forge creatorId as Bob
  await assertFails(aliceDb.collection("game_rooms").doc("room_2").set({
    id: "room_2",
    mode: "TWO_PLAYER",
    status: "WAITING",
    creatorId: BOB_UID,
    playerUids: [BOB_UID],
    createdAt: new Date(),
    updatedAt: new Date()
  }));
});
