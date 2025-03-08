package com.example.crystalballtaxes;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

@RunWith(AndroidJUnit4.class)
public class TaxResultsActivityInstrumentedTest {

    @Mock
    private DatabaseHelper mockDb;
    private Map<String, String> testTaxInfo;
    private Intent testIntent;

    @Before
    public void setUp() {
        // Initialize mocks
        MockitoAnnotations.openMocks(this);

        // Create test data
        testTaxInfo = new HashMap<>();
        testTaxInfo.put("income", "50000");
        testTaxInfo.put("above_line_deductions", "2000");
        testTaxInfo.put("itemized_deductions", "10000");
        testTaxInfo.put("tax_credits", "1000");
        testTaxInfo.put("filing_status", "Single");

        // Set up mock behavior
        when(mockDb.getUserTaxInfo(1L)).thenReturn(testTaxInfo);

        // Create intent with test user ID
        testIntent = new Intent(ApplicationProvider.getApplicationContext(), TaxResultsActivity.class);
        testIntent.putExtra("USER_ID", 1L);
    }

    @Test
    public void testTaxCalculationDisplay() {
        // Launch activity with intent
        try (ActivityScenario<TaxResultsActivity> scenario = ActivityScenario.launch(testIntent)) {
            // Inject mock database
            scenario.onActivity(activity -> {
                activity.setDatabaseHelper(mockDb);
                activity.recalculateTaxes();
            });

            // Verify displayed values using Espresso
            onView(withId(R.id.AGItxtF))
                    .check(matches(withText("Adjusted Gross Income: $48,000.00")));

            onView(withId(R.id.taxableIncTxt))
                    .check(matches(withText("Taxable Income: $34,150.00")));

            onView(withId(R.id.grossTaxLibTxt))
                    .check(matches(withText("Gross Tax Liability: $3,878.00")));

            onView(withId(R.id.taxesDueTxt))
                    .check(matches(withText("Taxes Due: $2,878.00")));
        }
    }

    @Test
    public void testActivityInCorrectContext() {
        String packageName = InstrumentationRegistry.getInstrumentation()
                .getTargetContext()
                .getPackageName();
        assertEquals("com.example.crystalballtaxes", packageName);
    }
}