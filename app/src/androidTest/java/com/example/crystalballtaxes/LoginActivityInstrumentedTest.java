package com.example.crystalballtaxes;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.not;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class LoginActivityInstrumentedTest {

    private ActivityScenario<LoginActivity> activityScenario;

    @Before
    public void setup() {
        // Create an explicit intent to launch LoginActivity
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(),
                LoginActivity.class);

        // Clear any existing data
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // Launch the activity
        activityScenario = ActivityScenario.launch(intent);

        // Wait for activity to be stable
        activityScenario.onActivity(activity -> {
            // Optional: Add any initialization needed
        });
    }

    @Test
    public void testLoginScreenInitialState() {
        // Basic visibility checks
        onView(withId(R.id.emailEditTxt))
                .check(matches(isDisplayed()));
        onView(withId(R.id.passwordEditTxt))
                .check(matches(isDisplayed()));
        onView(withId(R.id.loginBtn))
                .check(matches(isDisplayed()))
                .check(matches(isEnabled()));
    }

    @Test
    public void testEmptyEmailValidation() {
        onView(withId(R.id.passwordEditTxt))
                .perform(replaceText("password123"), closeSoftKeyboard());

        onView(withId(R.id.loginBtn))
                .perform(click());

        onView(withId(R.id.emailEditTxt))
                .check(matches(hasErrorText("Email cannot be empty")));
    }

    @Test
    public void testEmptyPasswordValidation() {
        onView(withId(R.id.emailEditTxt))
                .perform(replaceText("test@example.com"), closeSoftKeyboard());

        onView(withId(R.id.loginBtn))
                .perform(click());

        onView(withId(R.id.passwordEditTxt))
                .check(matches(hasErrorText("Password cannot be empty")));
    }

    @Test
    public void testValidCredentialsSubmission() {
        // First verify views are displayed
        onView(withId(R.id.emailEditTxt))
                .check(matches(isDisplayed()))
                .perform(replaceText("test@example.com"), closeSoftKeyboard());

        onView(withId(R.id.passwordEditTxt))
                .check(matches(isDisplayed()))
                .perform(replaceText("password123"), closeSoftKeyboard());

        onView(withId(R.id.loginBtn))
                .check(matches(isDisplayed()))
                .check(matches(isEnabled()))
                .perform(click());

    }

    public interface ViewVisibilityIdlingResource {
        void waitForView(int viewId);
    }
}