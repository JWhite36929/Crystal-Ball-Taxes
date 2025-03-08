package com.example.crystalballtaxes;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(AndroidJUnit4.class)
public class DependentsActivityTest {

    @Mock
    private DatabaseHelper mockDb;

    private static final long TEST_USER_ID = 1L;
    private Intent testIntent;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        testIntent = new Intent(ApplicationProvider.getApplicationContext(), DependentsActivity.class);
        testIntent.putExtra("USER_ID", TEST_USER_ID);

        // Configure mock behavior
        when(mockDb.addDependent(
                anyString(), anyString(), anyString(), anyString(), anyString(), anyInt()))
                .thenReturn(1); // Return success
    }

    @Test
    public void testValidDependentAddition() {
        try (ActivityScenario<DependentsActivity> scenario = ActivityScenario.launch(testIntent)) {
            scenario.onActivity(activity -> {
                activity.setDatabaseHelper(mockDb);
            });

            // Input valid dependent information
            onView(withId(R.id.dependFirstNameTxtF))
                    .perform(typeText("John"), closeSoftKeyboard());
            onView(withId(R.id.dependLastNameTxtF))
                    .perform(typeText("Doe"), closeSoftKeyboard());
            onView(withId(R.id.dependSSNTxtF3))
                    .perform(typeText("123-45-6789"), closeSoftKeyboard());
            onView(withId(R.id.dependDOBTxtF))
                    .perform(typeText("01/01/2000"), closeSoftKeyboard());

            // Select relationship from spinner
            onView(withId(R.id.spinner2)).perform(click());
            onView(withText("Son"))
                    .inRoot(isPlatformPopup())
                    .perform(click());

            // Click add dependent
            onView(withId(R.id.addDependBtn)).perform(click());

            // Verify database call
            verify(mockDb).addDependent(
                    "John", "Doe", "123-45-6789", "01/01/2000", "Son", (int)TEST_USER_ID
            );
        }
    }

    @Test
    public void testEmptyFieldValidation() {
        try (ActivityScenario<DependentsActivity> scenario = ActivityScenario.launch(testIntent)) {
            scenario.onActivity(activity -> {
                activity.setDatabaseHelper(mockDb);
            });

            // Click add without filling any fields
            onView(withId(R.id.addDependBtn)).perform(click());

            // Verify error messages
            onView(withId(R.id.dependFirstNameTxtF))
                    .check(matches(hasErrorText("First name is required")));
        }
    }

    @Test
    public void testInvalidSSNFormat() {
        try (ActivityScenario<DependentsActivity> scenario = ActivityScenario.launch(testIntent)) {
            scenario.onActivity(activity -> {
                activity.setDatabaseHelper(mockDb);
            });

            // Input invalid SSN
            onView(withId(R.id.dependFirstNameTxtF))
                    .perform(typeText("John"), closeSoftKeyboard());
            onView(withId(R.id.dependLastNameTxtF))
                    .perform(typeText("Doe"), closeSoftKeyboard());
            onView(withId(R.id.dependSSNTxtF3))
                    .perform(typeText("12345"), closeSoftKeyboard());
            onView(withId(R.id.dependDOBTxtF))
                    .perform(typeText("01/01/2000"), closeSoftKeyboard());

            // Click add
            onView(withId(R.id.addDependBtn)).perform(click());

            // Verify error message
            onView(withId(R.id.dependSSNTxtF3))
                    .check(matches(hasErrorText("Invalid SSN format (XXX-XX-XXXX)")));
        }
    }

    @Test
    public void testInvalidDateFormat() {
        try (ActivityScenario<DependentsActivity> scenario = ActivityScenario.launch(testIntent)) {
            scenario.onActivity(activity -> {
                activity.setDatabaseHelper(mockDb);
            });

            // Input invalid date
            onView(withId(R.id.dependFirstNameTxtF))
                    .perform(typeText("John"), closeSoftKeyboard());
            onView(withId(R.id.dependLastNameTxtF))
                    .perform(typeText("Doe"), closeSoftKeyboard());
            onView(withId(R.id.dependSSNTxtF3))
                    .perform(typeText("123-45-6789"), closeSoftKeyboard());
            onView(withId(R.id.dependDOBTxtF))
                    .perform(typeText("2000/01/01"), closeSoftKeyboard());

            // Click add
            onView(withId(R.id.addDependBtn)).perform(click());

            // Verify error message
            onView(withId(R.id.dependDOBTxtF))
                    .check(matches(hasErrorText("Invalid date format (MM/DD/YYYY)")));
        }
    }

    @Test
    public void testNoDependentsNavigation() {
        try (ActivityScenario<DependentsActivity> scenario = ActivityScenario.launch(testIntent)) {
            scenario.onActivity(activity -> {
                activity.setDatabaseHelper(mockDb);
            });

            // Click no dependents button
            onView(withId(R.id.button)).perform(click());

            // Verify activity is finishing
            scenario.onActivity(activity -> {
                assertTrue(activity.isFinishing());
            });
        }
    }

    @Test
    public void testFieldClearingAfterAdd() {
        try (ActivityScenario<DependentsActivity> scenario = ActivityScenario.launch(testIntent)) {
            scenario.onActivity(activity -> {
                activity.setDatabaseHelper(mockDb);
            });

            // Input valid dependent information
            onView(withId(R.id.dependFirstNameTxtF))
                    .perform(typeText("John"), closeSoftKeyboard());
            onView(withId(R.id.dependLastNameTxtF))
                    .perform(typeText("Doe"), closeSoftKeyboard());
            onView(withId(R.id.dependSSNTxtF3))
                    .perform(typeText("123-45-6789"), closeSoftKeyboard());
            onView(withId(R.id.dependDOBTxtF))
                    .perform(typeText("01/01/2000"), closeSoftKeyboard());

            // Select relationship
            onView(withId(R.id.spinner2)).perform(click());
            onView(withText("Son"))
                    .inRoot(isPlatformPopup())
                    .perform(click());

            // Click add
            onView(withId(R.id.addDependBtn)).perform(click());

            // Verify fields are cleared
            onView(withId(R.id.dependFirstNameTxtF))
                    .check(matches(withText("")));
            onView(withId(R.id.dependLastNameTxtF))
                    .check(matches(withText("")));
            onView(withId(R.id.dependSSNTxtF3))
                    .check(matches(withText("")));
            onView(withId(R.id.dependDOBTxtF))
                    .check(matches(withText("")));
        }
    }
}