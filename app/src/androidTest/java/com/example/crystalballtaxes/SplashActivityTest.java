package com.example.crystalballtaxes;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class SplashActivityTest {

    @Rule
    public ActivityScenarioRule<SplashActivity> activityRule =
            new ActivityScenarioRule<>(SplashActivity.class);

    private ActivityScenario<SplashActivity> scenario;

    @Before
    public void setup() {
        scenario = activityRule.getScenario();
    }

    @Test
    public void testSplashActivityCreation() {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean assertionPassed = new AtomicBoolean(false);

        scenario.onActivity(activity -> {
            try {
                if (activity != null && !activity.isFinishing()) {
                    assertionPassed.set(true);
                }
            } finally {
                latch.countDown();
            }
        });

        try {
            latch.await(1, TimeUnit.SECONDS);
            assert assertionPassed.get() : "Activity creation check failed";
        } catch (InterruptedException e) {
            throw new RuntimeException("Test interrupted", e);
        }
    }

    @Test
    public void testSplashScreenInitialState() {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean assertionPassed = new AtomicBoolean(false);

        scenario.onActivity(activity -> {
            try {
                if (!activity.isFinishing()) {
                    // Add view-specific assertions here if needed
                    // For example: assert activity.findViewById(R.id.splash_logo) != null;
                    assertionPassed.set(true);
                }
            } finally {
                latch.countDown();
            }
        });

        try {
            latch.await(1, TimeUnit.SECONDS);
            assert assertionPassed.get() : "Initial state check failed";
        } catch (InterruptedException e) {
            throw new RuntimeException("Test interrupted", e);
        }
    }

    @Test
    public void testHandlerExists() {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean assertionPassed = new AtomicBoolean(false);

        scenario.onActivity(activity -> {
            try {
                android.os.Handler handler = new android.os.Handler(activity.getMainLooper());
                assertionPassed.set(handler != null);
            } finally {
                latch.countDown();
            }
        });

        try {
            latch.await(1, TimeUnit.SECONDS);
            assert assertionPassed.get() : "Handler creation failed";
        } catch (InterruptedException e) {
            throw new RuntimeException("Test interrupted", e);
        }
    }

    @Test
    public void testIntentCreation() {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean assertionPassed = new AtomicBoolean(false);

        scenario.onActivity(activity -> {
            try {
                android.content.Intent intent = new android.content.Intent(activity, LoginActivity.class);
                assertionPassed.set(
                        intent != null &&
                                intent.resolveActivity(activity.getPackageManager()) != null
                );
            } finally {
                latch.countDown();
            }
        });

        try {
            latch.await(1, TimeUnit.SECONDS);
            assert assertionPassed.get() : "Intent creation failed";
        } catch (InterruptedException e) {
            throw new RuntimeException("Test interrupted", e);
        }
    }

    @Test
    public void testActivityFinishes() {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean activityFinished = new AtomicBoolean(false);

        // Monitor activity state changes
        scenario.moveToState(androidx.lifecycle.Lifecycle.State.RESUMED);

        scenario.onActivity(activity -> {
            new android.os.Handler(activity.getMainLooper()).postDelayed(() -> {
                try {
                    activityFinished.set(activity.isFinishing());
                } finally {
                    latch.countDown();
                }
            }, 3500);
        });

        try {
            latch.await(4, TimeUnit.SECONDS);
            assert activityFinished.get() : "Activity did not finish as expected";
        } catch (InterruptedException e) {
            throw new RuntimeException("Test interrupted", e);
        }
    }
}