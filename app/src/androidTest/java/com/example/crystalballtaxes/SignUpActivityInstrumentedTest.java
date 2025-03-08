package com.example.crystalballtaxes;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.espresso.intent.Intents;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class SignUpActivityInstrumentedTest {

    @Rule
    public ActivityScenarioRule<SignUpActivity> activityRule =
            new ActivityScenarioRule<>(SignUpActivity.class);

    @Before
    public void setUp() {
        // Initialize Intents before each test
        Intents.init();
    }

    @After
    public void tearDown() {
        // Release Intents after each test
        Intents.release();
    }

    @Test
    public void testUIElementsDisplayed() {
        // Check if all UI elements are displayed
        onView(withId(R.id.newAccUserName)).check(matches(isDisplayed()));
        onView(withId(R.id.newAccEmail)).check(matches(isDisplayed()));
        onView(withId(R.id.newAccPasswrd)).check(matches(isDisplayed()));
        onView(withId(R.id.newAccPhoneNum)).check(matches(isDisplayed()));
        onView(withId(R.id.createNewAccBtn)).check(matches(isDisplayed()));
    }

    @Test
    public void testUserInputFlow() {
        // Enter user details
        onView(withId(R.id.newAccUserName))
                .perform(typeText("Test User"), closeSoftKeyboard());
        onView(withId(R.id.newAccEmail))
                .perform(typeText("test@example.com"), closeSoftKeyboard());
        onView(withId(R.id.newAccPasswrd))
                .perform(typeText("password123"), closeSoftKeyboard());
        onView(withId(R.id.newAccPhoneNum))
                .perform(typeText("1234567890"), closeSoftKeyboard());

        // Click create account button
        onView(withId(R.id.createNewAccBtn)).perform(click());
    }

    @Test
    public void testNavigationBack() {
        // Find the back arrow image view by its ID and click it
        // Replace R.id.back_arrow with your actual back button ID
        onView(withId(R.id.backToLoginBtn)).perform(click());

        // Verify that we navigated to LoginActivity
        intended(hasComponent(LoginActivity.class.getName()));
    }
}