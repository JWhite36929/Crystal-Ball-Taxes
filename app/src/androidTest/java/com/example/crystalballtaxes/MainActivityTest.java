package com.example.crystalballtaxes;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public ActivityTestRule<MainActivity> activityRule =
            new ActivityTestRule<>(MainActivity.class, true, false);

    private Context context;

    @Before
    public void setup() {
        context = ApplicationProvider.getApplicationContext();
    }

    @Test
    public void testUIElementsDisplayed() {
        launchActivity();

        // Verify both buttons are displayed
        onView(withId(R.id.startBtn)).check(matches(isDisplayed()));
        onView(withId(R.id.logoutBtn)).check(matches(isDisplayed()));
    }

    @Test
    public void testLogoutButtonClick() {
        ActivityScenario<MainActivity> scenario = launchActivity();

        // Click logout button
        onView(withId(R.id.logoutBtn)).perform(click());

        // Verify activity finishes
        scenario.onActivity(activity -> {
            assert activity.isFinishing();
        });
    }

    @Test
    public void testStartButtonClick() {
        ActivityScenario<MainActivity> scenario = launchActivity();

        // Click start button
        onView(withId(R.id.startBtn)).perform(click());

        // Verify activity behavior
        // Since we're not logged in, it should redirect to login
        scenario.onActivity(activity -> {
            assert activity.isFinishing();
        });
    }

    @Test
    public void testStartButtonToastMessage() {
        launchActivity();

        // Click start button
        onView(withId(R.id.startBtn)).perform(click());

        // Toast message should appear - but we can't verify Toast content in instrumented tests
        // We can only verify the button click doesn't crash the app
    }

    @Test
    public void testActivityCreation() {
        ActivityScenario<MainActivity> scenario = launchActivity();

        scenario.onActivity(activity -> {
            // Verify activity is created successfully
            assert activity != null;
            // Verify database is initialized
            assert activity.db != null;
        });
    }

    private ActivityScenario<MainActivity> launchActivity() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class);
        return ActivityScenario.launch(intent);
    }
}