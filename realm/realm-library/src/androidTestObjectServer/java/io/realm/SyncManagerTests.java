/*
 * Copyright 2016 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.realm;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.util.Collection;
import java.util.Set;

import io.realm.rule.TestRealmConfigurationFactory;

import static io.realm.util.SyncTestUtils.createTestUser;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class SyncManagerTests {

    private Context context;
    private UserStore userStore;

    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getContext();
        userStore = new UserStore() {
            @Override
            public User put(String key, User user) {
                return null;
            }

            @Override
            public User get(String key) {
                return null;
            }

            @Override
            public User remove(String key) {
                return null;
            }

            @Override
            public Collection<User> allUsers() {
                return null;
            }

            @Override
            public void clear() {
            }
        };
    }

    @After
    public void tearDown() {
    }

    @Test
    public void init() {
        // Realm.init() calls SyncManager.init() wihich will start a thread for the sync client
        boolean found = false;
        Set<Thread> threads = Thread.getAllStackTraces().keySet();
        for (Thread thread : threads) {
            if (thread.getName().equals("RealmSyncClient")) {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }

    @Test
    public void set_userStore() {
        SyncManager.setUserStore(userStore);
        assertTrue(userStore.equals(SyncManager.getUserStore()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void set_userStore_null() {
        SyncManager.setUserStore(null);
    }

    @Test
    public void authListener() {
        User user = createTestUser();
        final int[] counter = {0, 0};

        AuthenticationListener authenticationListener = new AuthenticationListener() {
            @Override
            public void loggedIn(User user) {
                counter[0]++;
            }

            @Override
            public void loggedOut(User user) {
                counter[1]++;
            }
        };

        SyncManager.addAuthenticationListener(authenticationListener);
        SyncManager.notifyUserLoggedIn(user);
        SyncManager.notifyUserLoggedOut(user);
        assertEquals(1, counter[0]);
        assertEquals(1, counter[1]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void authListener_null() {
        SyncManager.addAuthenticationListener(null);
    }

    @Test
    public void authListener_remove() {
        User user = createTestUser();
        final int[] counter = {0, 0};

        AuthenticationListener authenticationListener = new AuthenticationListener() {
            @Override
            public void loggedIn(User user) {
                counter[0]++;
            }

            @Override
            public void loggedOut(User user) {
                counter[1]++;
            }
        };

        SyncManager.addAuthenticationListener(authenticationListener);

        SyncManager.removeAuthenticationListener(authenticationListener);

        SyncManager.notifyUserLoggedIn(user);
        SyncManager.notifyUserLoggedOut(user);

        // no listener to update counters
        assertEquals(0, counter[0]);
        assertEquals(0, counter[1]);
    }

    @Test
    public void session() {
        User user = createTestUser();
        String url = "realm://objectserver.realm.io/default";
        SyncConfiguration config = new SyncConfiguration.Builder(user, url)
                .build();

        Session session = SyncManager.getSession(config);
        assertEquals(user, session.getUser()); // see also SessionTests
    }
}
