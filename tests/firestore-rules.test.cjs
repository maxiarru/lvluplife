const assert = require('node:assert/strict');
const { readFileSync } = require('node:fs');
const { resolve } = require('node:path');
const { assertFails, assertSucceeds, initializeTestEnvironment } = require('@firebase/rules-unit-testing');
const { doc, getDoc, setDoc, writeBatch, Timestamp, serverTimestamp } = require('firebase/firestore');

const rules = readFileSync(resolve('firestore.rules'), 'utf8');
const initialAttributes = Object.fromEntries(
  ['STR', 'DEX', 'CON', 'INT', 'WIT', 'MEN'].map((attribute) => [attribute, { level: 1, xp: 0 }]),
);
const initialAccount = (username) => ({
  username,
  usernameKey: username.toLowerCase(),
  character: { generalLevel: 1, class: 'Novato', attributes: initialAttributes },
  currentCycleId: 'initial',
  cycleStartedAt: serverTimestamp(),
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
    await assertFails(setDoc(account(bob, 'alice'), {currentCycleId: 'aaaaaaaaaaaaaaaaaaaa'}, {merge: true}));
    await assertFails(setDoc(account(alice, 'alice'), {character: {generalLevel: 50}}, {merge: true}));
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
    const customRef = doc(alice, 'accounts/alice/customActivities/abcdefghijklmnopqrst');
    await assertSucceeds(setDoc(customRef, {
      name: 'Correr', category: 'Deporte', kind: 'duration', primary: 'DEX', secondary: null,
      createdAt: serverTimestamp(),
    }));
    await assertFails(getDoc(doc(bob, customRef.path)));
    await assertFails(setDoc(doc(bob, 'accounts/alice/customActivities/other'), { name: 'Ajena' }));
    await assertFails(setDoc(doc(alice, 'accounts/alice/customActivities/badcustomactivityid'), {
      name: 'Inválida', category: 'Libre', kind: 'duration', primary: 'CON', secondary: null,
      createdAt: serverTimestamp(),
    }));
    const clockRef = doc(alice, 'accounts/alice/clock/server');
    await assertSucceeds(setDoc(clockRef, {now: serverTimestamp()}));
    await assertFails(getDoc(doc(bob, clockRef.path)));
    await assertFails(setDoc(clockRef, {now: Timestamp.now()}));

    const today = new Date(Date.now() - 3 * 3600000).toISOString().slice(0, 10);
    const day = Timestamp.fromDate(new Date(`${today}T00:00:00Z`));
    const dayRef = doc(alice, `accounts/alice/days/initial_${day.toMillis()}`);
    const id = 'abcdefghijklmnopqrst';
    const entry = {
      name: 'Entrenamiento de fuerza', category: 'Actividad física', kind: 'duration',
      catalogId: 'STRENGTH', customId: null, primary: 'STR', secondary: null,
      minutes: 60, createdAt: serverTimestamp(),
    };
    const dayDoc = {cycleId: 'initial', day, entries: {[id]: entry}, totalMinutes: 60, changedId: id, movePeer: null};
    await assertSucceeds(setDoc(dayRef, dayDoc));
    await assertSucceeds(getDoc(dayRef));
    const saved = (await getDoc(dayRef)).data().entries[id];
    await assertSucceeds(setDoc(dayRef, {...dayDoc,
      entries: {[id]: {...saved, minutes: 120}}, totalMinutes: 120,
    }));
    const yesterday = new Date(day.toMillis() - 86400000);
    const yesterdayStamp = Timestamp.fromDate(yesterday);
    const yesterdayRef = doc(alice, `accounts/alice/days/initial_${yesterdayStamp.toMillis()}`);
    const move = writeBatch(alice);
    move.set(dayRef, {...dayDoc, entries: {}, totalMinutes: 0, movePeer: yesterdayRef.id});
    move.set(yesterdayRef, {cycleId: 'initial', day: yesterdayStamp, entries: {[id]: {...saved, minutes: 120}},
      totalMinutes: 120, changedId: id, movePeer: dayRef.id});
    await assertSucceeds(move.commit());
    await assertSucceeds(setDoc(yesterdayRef, {cycleId: 'initial', day: yesterdayStamp, entries: {},
      totalMinutes: 0, changedId: id, movePeer: null}));
    const fullId = 'bbbbbbbbbbbbbbbbbbbb';
    const oneId = 'cccccccccccccccccccc';
    const fullEntry = {...entry, minutes: 1440, createdAt: serverTimestamp()};
    await assertSucceeds(setDoc(dayRef, {...dayDoc, entries: {[fullId]: fullEntry},
      totalMinutes: 1440, changedId: fullId}));
    const fullSaved = (await getDoc(dayRef)).data().entries[fullId];
    await assertFails(setDoc(dayRef, {...dayDoc, entries: {
      [fullId]: fullSaved, [oneId]: {...entry, minutes: 1, createdAt: serverTimestamp()},
    }, totalMinutes: 1441, changedId: oneId}));
    await assertFails(setDoc(dayRef, {...dayDoc, entries: {[fullId]: {...fullSaved, minutes: 1439}},
      totalMinutes: 1440, changedId: fullId}));
    await assertSucceeds(setDoc(dayRef, {...dayDoc, entries: {}, totalMinutes: 0,
      changedId: fullId, movePeer: null}));
    const firstHourId = 'gggggggggggggggggggg';
    const remainingDayId = 'hhhhhhhhhhhhhhhhhhhh';
    await assertSucceeds(setDoc(dayRef, {...dayDoc, entries: {
      [firstHourId]: {...entry, minutes: 60, createdAt: serverTimestamp()},
    }, totalMinutes: 60, changedId: firstHourId, movePeer: null}));
    const firstHourSaved = (await getDoc(dayRef)).data().entries[firstHourId];
    await assertSucceeds(setDoc(dayRef, {...dayDoc, entries: {
      [firstHourId]: firstHourSaved,
      [remainingDayId]: {...entry, minutes: 1380, createdAt: serverTimestamp()},
    }, totalMinutes: 1440, changedId: remainingDayId, movePeer: null}));
    await assertSucceeds(setDoc(dayRef, {...dayDoc, entries: {
      [firstHourId]: firstHourSaved,
    }, totalMinutes: 60, changedId: remainingDayId, movePeer: null}));
    await assertSucceeds(setDoc(dayRef, {...dayDoc, entries: {}, totalMinutes: 0,
      changedId: firstHourId, movePeer: null}));
    await assertSucceeds(setDoc(dayRef, {...dayDoc, entries: {
      [firstHourId]: {...entry, minutes: 60, createdAt: serverTimestamp()},
    }, totalMinutes: 60, changedId: firstHourId, movePeer: null}));
    const savedMixedHour = (await getDoc(dayRef)).data().entries[firstHourId];
    await assertSucceeds(setDoc(dayRef, {...dayDoc, entries: {
      [firstHourId]: savedMixedHour,
      [remainingDayId]: {
        name: 'Correr', category: 'Deporte', kind: 'duration', catalogId: null,
        customId: 'abcdefghijklmnopqrst', primary: 'DEX', secondary: null,
        minutes: 1380, createdAt: serverTimestamp(),
      },
    }, totalMinutes: 1440, changedId: remainingDayId, movePeer: null}));
    await assertSucceeds(setDoc(dayRef, {...dayDoc, entries: {
      [firstHourId]: savedMixedHour,
    }, totalMinutes: 60, changedId: remainingDayId, movePeer: null}));
    await assertSucceeds(setDoc(dayRef, {...dayDoc, entries: {}, totalMinutes: 0,
      changedId: firstHourId, movePeer: null}));
    const afterDeleteId = 'eeeeeeeeeeeeeeeeeeee';
    await assertSucceeds(setDoc(dayRef, {...dayDoc, entries: {
      [afterDeleteId]: {...entry, minutes: 30, createdAt: serverTimestamp()},
    }, totalMinutes: 30, changedId: afterDeleteId, movePeer: null}));
    const otherDayId = 'ffffffffffffffffffff';
    await assertSucceeds(setDoc(yesterdayRef, {cycleId: 'initial', day: yesterdayStamp,
      entries: {[otherDayId]: {...entry, minutes: 30, createdAt: serverTimestamp()}},
      totalMinutes: 30, changedId: otherDayId, movePeer: null}));
    const staleId = 'dddddddddddddddddddd';
    const staleEntry = {...saved, createdAt: Timestamp.fromMillis(Date.now() - 301000)};
    await environment.withSecurityRulesDisabled(async (context) => {
      await setDoc(doc(context.firestore(), yesterdayRef.path), {cycleId: 'initial', day: yesterdayStamp,
        entries: {[staleId]: staleEntry}, totalMinutes: 60, changedId: staleId, movePeer: null});
    });
    await assertFails(setDoc(yesterdayRef, {cycleId: 'initial', day: yesterdayStamp,
      entries: {}, totalMinutes: 0, changedId: staleId, movePeer: null}));
    await assertFails(getDoc(doc(bob, dayRef.path)));
    await assertFails(getDoc(doc(anonymous, dayRef.path)));
    await assertFails(setDoc(dayRef, {...dayDoc, totalMinutes: 1441}));
    await assertFails(setDoc(doc(bob, dayRef.path), {...dayDoc, totalMinutes: 1}));
    const nextCycle = 'zzzzzzzzzzzzzzzzzzzz';
    await assertFails(setDoc(account(alice, 'alice'), {currentCycleId: nextCycle}, {merge: true}));
    await assertSucceeds(setDoc(account(alice, 'alice'), {
      currentCycleId: nextCycle, cycleStartedAt: serverTimestamp(),
      character: {generalLevel: 1, class: 'Novato', attributes: initialAttributes},
    }, {merge: true}));
    assert.equal((await getDoc(account(alice, 'alice'))).data().username, 'Martin');
    await assertSucceeds(getDoc(customRef));
    await assertSucceeds(getDoc(dayRef));
    await assertFails(setDoc(dayRef, {...dayDoc, entries: {}, totalMinutes: 0,
      changedId: afterDeleteId, movePeer: null}));
    const nextRef = doc(alice, `accounts/alice/days/${nextCycle}_${day.toMillis()}`);
    await assertSucceeds(setDoc(nextRef, {cycleId: nextCycle, day,
      entries: {[oneId]: {...entry, minutes: 30, createdAt: serverTimestamp()}},
      totalMinutes: 30, changedId: oneId, movePeer: null}));
    await assertFails(getDoc(doc(bob, nextRef.path)));
    console.log('Reglas de cuentas, nombres, actividades e historial: OK');
  } finally {
    await environment.cleanup();
  }
})().catch((error) => { console.error(error); process.exitCode = 1; });
