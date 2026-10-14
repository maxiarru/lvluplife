const assert = require('node:assert/strict');
const { readFileSync } = require('node:fs');
const { resolve } = require('node:path');
const { assertFails, assertSucceeds, initializeTestEnvironment } = require('@firebase/rules-unit-testing');
const { doc, getDoc, setDoc, writeBatch } = require('firebase/firestore');

const rules = readFileSync(resolve('firestore.rules'), 'utf8');
const initialAttributes = Object.fromEntries(
  ['STR', 'DEX', 'CON', 'INT', 'WIT', 'MEN'].map((attribute) => [attribute, { level: 1, xp: 0 }]),
);
const initialAccount = (username) => ({
  username,
  usernameKey: username.toLowerCase(),
  character: { generalLevel: 1, class: 'Novato', attributes: initialAttributes },
});

(async () => {
  const environment = await initializeTestEnvironment({
    projectId: 'demo-lvl-up-life-rules',
    firestore: { rules },
  });
  try {
    const alice = environment.authenticatedContext('alice', { firebase: { sign_in_provider: 'google.com' } }).firestore();
    const bob = environment.authenticatedContext('bob', { firebase: { sign_in_provider: 'google.com' } }).firestore();
    const anonymous = environment.unauthenticatedContext().firestore();
    const account = (db, uid) => doc(db, 'accounts', uid);
    const name = (db, key) => doc(db, 'usernames', key);

    const createAlice = writeBatch(alice);
    createAlice.set(name(alice, 'martin'), { uid: 'alice' });
    createAlice.set(account(alice, 'alice'), initialAccount('Martin'));
    await assertSucceeds(createAlice.commit());
    assert.equal((await assertSucceeds(getDoc(account(alice, 'alice')))).data().username, 'Martin');
    await assertFails(getDoc(account(bob, 'alice')));
    await assertFails(getDoc(account(anonymous, 'alice')));
    await assertFails(getDoc(name(bob, 'martin')));

    const duplicateName = writeBatch(bob);
    duplicateName.set(name(bob, 'martin'), { uid: 'bob' });
    duplicateName.set(account(bob, 'bob'), initialAccount('martin'));
    await assertFails(duplicateName.commit());
    await assertFails(setDoc(account(bob, 'bob'), initialAccount('Bob')));
    await assertFails(setDoc(account(alice, 'alice'), { ...initialAccount('Martin'), character: { generalLevel: 50 } }));
    await assertFails(setDoc(name(bob, 'bob'), { uid: 'bob' }));

    const createBob = writeBatch(bob);
    createBob.set(name(bob, 'bob'), { uid: 'bob' });
    createBob.set(account(bob, 'bob'), initialAccount('Bob'));
    await assertSucceeds(createBob.commit());

    const existingRecord = doc(alice, 'users/alice/records/record-1');
    await assertSucceeds(setDoc(existingRecord, {
      date: '2026-10-13', createdAt: 1791900000000, kind: 'meal', minutes: 0,
      primary: 'CON', secondary: null, catalogId: 'MEAL',
    }));
    await assertFails(getDoc(doc(bob, 'users/alice/records/record-1')));
    console.log('Reglas de cuentas, nombres y registros: OK');
  } finally {
    await environment.cleanup();
  }
})().catch((error) => { console.error(error); process.exitCode = 1; });
