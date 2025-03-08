package com.example.crystalballtaxes;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import android.content.Intent;
import android.widget.Button;
import android.widget.TextView;
import android.util.Log;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import static org.robolectric.Shadows.shadowOf;

import java.util.HashMap;
import java.util.Map;

@RunWith(RobolectricTestRunner.class)
public class TaxResultsActivityTest {
    private static final String TAG = "TaxResultsActivityTest";

    @Mock
    private DatabaseHelper mockDb;

    private ActivityController<TaxResultsActivity> controller;
    private TaxResultsActivity activity;
    private Map<String, String> testTaxInfo;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Create test data
        testTaxInfo = new HashMap<>();
        testTaxInfo.put("income", "50000");
        testTaxInfo.put("above_line_deductions", "2000");
        testTaxInfo.put("itemized_deductions", "10000");
        testTaxInfo.put("tax_credits", "1000");
        testTaxInfo.put("filing_status", "Single");

        // Set up mock behavior
        when(mockDb.getUserTaxInfo(anyLong())).thenReturn(testTaxInfo);

        // Create intent with test user ID
        Intent intent = new Intent(RuntimeEnvironment.getApplication(), TaxResultsActivity.class);
        intent.putExtra("USER_ID", 1L);

        // Create controller but don't create activity yet
        controller = Robolectric.buildActivity(TaxResultsActivity.class, intent);
        activity = controller.get();

        // Set mock before onCreate is called
        activity.setDatabaseHelper(mockDb);
    }

    @Test
    public void testTaxCalculationForSingleFiler() {
        // Verify mock is properly set up
        assertNotNull("Mock database should not be null", mockDb);

        // Log for debugging
        Log.d(TAG, "Starting test with mock: " + mockDb);

        // Start activity lifecycle
        controller.create().start().resume();

        // Process any pending messages
        shadowOf(RuntimeEnvironment.getApplication().getMainLooper()).idle();

        // Force a calculation - this will be the only time getUserTaxInfo is called
        activity.recalculateTaxes();

        // Process any pending messages again
        shadowOf(RuntimeEnvironment.getApplication().getMainLooper()).idle();

        // Verify mock was called exactly once
        verify(mockDb, times(1)).getUserTaxInfo(anyLong());

        // Get views
        TextView agiView = activity.findViewById(R.id.AGItxtF);
        TextView taxableIncomeView = activity.findViewById(R.id.taxableIncTxt);
        TextView grossTaxView = activity.findViewById(R.id.grossTaxLibTxt);
        TextView taxesDueView = activity.findViewById(R.id.taxesDueTxt);

        // Log actual values for debugging
        Log.d(TAG, "AGI View Text: " + agiView.getText());
        Log.d(TAG, "Taxable Income View Text: " + taxableIncomeView.getText());
        Log.d(TAG, "Gross Tax View Text: " + grossTaxView.getText());
        Log.d(TAG, "Taxes Due View Text: " + taxesDueView.getText());

        // Assert view content with correct tax calculation
        assertEquals("Adjusted Gross Income: $48,000.00",
                agiView.getText().toString());
        assertEquals("Taxable Income: $34,150.00",
                taxableIncomeView.getText().toString());
        assertEquals("Gross Tax Liability: $3,878.00",
                grossTaxView.getText().toString());
        assertEquals("Taxes Due: $2,878.00",  // Updated: $3,878 - $1,000 tax credits
                taxesDueView.getText().toString());
    }

    @Test
    public void testHomeButtonNavigation() {
        // Start activity
        controller.create();

        Button homeButton = activity.findViewById(R.id.backToHomeBtn);
        homeButton.performClick();

        assertTrue(activity.isFinishing());
        Intent expectedIntent = new Intent(activity, MainActivity.class);
        expectedIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        Intent actualIntent = shadowOf(activity).getNextStartedActivity();
        assertEquals(expectedIntent.getComponent(), actualIntent.getComponent());
        assertEquals(Intent.FLAG_ACTIVITY_CLEAR_TOP, actualIntent.getFlags());
    }

    @After
    public void tearDown() {
        if (controller != null) {
            controller.destroy();
        }
    }
}