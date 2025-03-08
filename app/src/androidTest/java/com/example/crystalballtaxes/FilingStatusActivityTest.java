package com.example.crystalballtaxes;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Intent;
import android.os.SystemClock;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(AndroidJUnit4.class)
public class FilingStatusActivityTest {

    @Mock
    private DatabaseHelper mockDb;

    private static final long TEST_USER_ID = 1L;
    private Intent testIntent;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        testIntent = new Intent(ApplicationProvider.getApplicationContext(), FilingStatusActivity.class);
        testIntent.putExtra("USER_ID", TEST_USER_ID);

        // Configure mock behavior
        when(mockDb.taxRecordExists(TEST_USER_ID)).thenReturn(false);
        when(mockDb.initializeTaxRecord(TEST_USER_ID)).thenReturn(1L);
        when(mockDb.updateFilingStatus(anyLong(), eq("Single"))).thenReturn(true);
        when(mockDb.updateFilingStatus(anyLong(), eq("Married Filing Jointly"))).thenReturn(true);
        when(mockDb.updateFilingStatus(anyLong(), eq("Married Filing Separately"))).thenReturn(true);
        when(mockDb.updateFilingStatus(anyLong(), eq("Head of Household"))).thenReturn(true);
    }

    @Test
    public void testErrorTextInitiallyHidden() {
        try (ActivityScenario<FilingStatusActivity> scenario = ActivityScenario.launch(testIntent)) {
            scenario.onActivity(activity -> {
                activity.setDatabaseHelper(mockDb);
            });

            onView(withId(R.id.errorText)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        }
    }

    @Test
    public void testErrorShownWhenNoSelection() {
        try (ActivityScenario<FilingStatusActivity> scenario = ActivityScenario.launch(testIntent)) {
            scenario.onActivity(activity -> {
                activity.setDatabaseHelper(mockDb);
            });

            // Click next without selecting anything
            onView(withId(R.id.nextBtn)).perform(click());

            // Verify error is shown
            onView(withId(R.id.errorText)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void testSingleFilingStatusSelection() {
        try (ActivityScenario<FilingStatusActivity> scenario = ActivityScenario.launch(testIntent)) {
            scenario.onActivity(activity -> {
                activity.setDatabaseHelper(mockDb);
            });

            // Select Single filing status
            onView(withId(R.id.singleRBtn)).perform(click());

            // Click next
            onView(withId(R.id.nextBtn)).perform(click());

            // Verify database was updated correctly
            verify(mockDb).updateFilingStatus(TEST_USER_ID, "Single");

            // Verify activity is finishing (moving to next screen)
            scenario.onActivity(activity -> {
                assertTrue(activity.isFinishing());
            });
        }
    }

    @Test
    public void testMarriedJointFilingStatusSelection() {
        try (ActivityScenario<FilingStatusActivity> scenario = ActivityScenario.launch(testIntent)) {
            scenario.onActivity(activity -> {
                activity.setDatabaseHelper(mockDb);
            });

            // Select Married Filing Jointly status
            onView(withId(R.id.marriedJointBtn)).perform(click());

            // Click next
            onView(withId(R.id.nextBtn)).perform(click());

            // Verify database was updated correctly
            verify(mockDb).updateFilingStatus(TEST_USER_ID, "Married Filing Jointly");

            // Verify activity is finishing
            scenario.onActivity(activity -> {
                assertTrue(activity.isFinishing());
            });
        }
    }

    @Test
    public void testTaxRecordInitialization() {
        try (ActivityScenario<FilingStatusActivity> scenario = ActivityScenario.launch(testIntent)) {
            scenario.onActivity(activity -> {
                activity.setDatabaseHelper(mockDb);
            });

            // Select any status
            onView(withId(R.id.singleRBtn)).perform(click());

            // Click next
            onView(withId(R.id.nextBtn)).perform(click());

            // Verify tax record was initialized
            verify(mockDb).taxRecordExists(TEST_USER_ID);
            verify(mockDb).initializeTaxRecord(TEST_USER_ID);
        }
    }

    @Test
    public void testDatabaseError() {
        // Configure mock to return false for update
        when(mockDb.updateFilingStatus(TEST_USER_ID, "Single")).thenReturn(false);

        try (ActivityScenario<FilingStatusActivity> scenario = ActivityScenario.launch(testIntent)) {
            scenario.onActivity(activity -> {
                activity.setDatabaseHelper(mockDb);
            });

            // Select Single and click next
            onView(withId(R.id.singleRBtn)).perform(click());
            onView(withId(R.id.nextBtn)).perform(click());

            // Instead of checking for Toast, verify that activity is not finishing
            scenario.onActivity(activity -> {
                assertFalse(activity.isFinishing());
            });

            // Verify the database interaction
            verify(mockDb).updateFilingStatus(TEST_USER_ID, "Single");
        }
    }
}