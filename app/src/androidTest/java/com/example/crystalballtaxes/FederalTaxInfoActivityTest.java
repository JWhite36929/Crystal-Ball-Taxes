package com.example.crystalballtaxes;

import static org.junit.Assert.*;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
import android.content.Intent;
import android.widget.EditText;
import android.widget.Button;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class FederalTaxInfoActivityTest {

    private static final long TEST_USER_ID = 123L;
    private TestDatabaseHelper mockDb;
    private CountDownLatch latch;
    private NumberFormat currencyFormatter;

    public static class TestDatabaseHelper extends DatabaseHelper {
        private Map<String, String> storedTaxInfo;
        private Map<String, Object> lastUpdatedValues;

        public TestDatabaseHelper(Context context) {
            super(context);
            this.storedTaxInfo = new HashMap<>();
            this.lastUpdatedValues = new HashMap<>();
        }

        @Override
        public Map<String, String> getUserTaxInfo(long userId) {
            return storedTaxInfo;
        }

        public void setupTestData(double income, double taxCredits,
                                  double aboveLineDeductions, double itemizedDeductions) {
            storedTaxInfo.clear();
            storedTaxInfo.put("income", String.valueOf(income));
            storedTaxInfo.put("tax_credits", String.valueOf(taxCredits));
            storedTaxInfo.put("above_line_deductions", String.valueOf(aboveLineDeductions));
            storedTaxInfo.put("itemized_deductions", String.valueOf(itemizedDeductions));
        }

        @Override
        public boolean updateIncome(long userId, String income) {
            lastUpdatedValues.put("income", income);
            return true;
        }

        @Override
        public boolean updateTaxCredits(long userId, int credits) {
            lastUpdatedValues.put("tax_credits", credits);
            return true;
        }

        @Override
        public boolean updateAboveLineDeductions(long userId, String deductions) {
            lastUpdatedValues.put("above_line_deductions", deductions);
            return true;
        }

        @Override
        public boolean updateItemizedDeductions(long userId, String deductions) {
            lastUpdatedValues.put("itemized_deductions", deductions);
            return true;
        }

        public Map<String, Object> getLastUpdatedValues() {
            return lastUpdatedValues;
        }
    }

    @Rule
    public ActivityTestRule<FederalTaxInfoActivity> activityRule = new ActivityTestRule<>(
            FederalTaxInfoActivity.class,
            true,
            false);

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        mockDb = new TestDatabaseHelper(context);
        latch = new CountDownLatch(1);
        currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);
    }

    private void assertCurrencyEquals(double expected, String actual) {
        try {
            String cleanValue = actual.replaceAll("[$,]", "");
            double actualValue = Double.parseDouble(cleanValue);
            assertEquals(expected, actualValue, 0.01);
        } catch (NumberFormatException e) {
            fail("Failed to parse currency value: " + actual);
        }
    }

    private void waitForUiThread() throws InterruptedException {
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        Thread.sleep(1000);
    }

    @Test
    public void testLoadExistingTaxInfo() throws Throwable {
        mockDb.setupTestData(50000.00, 1000.00, 5000.00, 12000.00);

        final FederalTaxInfoActivity activity = launchActivity();

        final EditText annualIncomeInput = activity.findViewById(R.id.annualIncomeTxtF3);
        final EditText taxCreditsInput = activity.findViewById(R.id.taxCreditsTxtF);
        final EditText aboveLineDeductionsInput = activity.findViewById(R.id.ablDeductionsTxtF);
        final EditText itemizedDeductionsInput = activity.findViewById(R.id.annualIncomeTxtF4);

        activityRule.runOnUiThread(() -> {
            annualIncomeInput.setText("5000000"); // Multiply by 100 to account for division in TextWatcher
            taxCreditsInput.setText("100000");
            aboveLineDeductionsInput.setText("500000");
            itemizedDeductionsInput.setText("1200000");
            latch.countDown();
        });

        waitForUiThread();

        activityRule.runOnUiThread(() -> {
            assertCurrencyEquals(50000.00, annualIncomeInput.getText().toString());
            assertCurrencyEquals(1000.00, taxCreditsInput.getText().toString());
            assertCurrencyEquals(5000.00, aboveLineDeductionsInput.getText().toString());
            assertCurrencyEquals(12000.00, itemizedDeductionsInput.getText().toString());
        });
    }

    @Test
    public void testCurrencyFormatting() throws Throwable {
        final FederalTaxInfoActivity activity = launchActivity();
        final EditText annualIncomeInput = activity.findViewById(R.id.annualIncomeTxtF3);
        final EditText taxCreditsInput = activity.findViewById(R.id.taxCreditsTxtF);

        activityRule.runOnUiThread(() -> {
            annualIncomeInput.setText("5000000"); // Input 50000.00 * 100
            taxCreditsInput.setText("100000");    // Input 1000.00 * 100
            latch.countDown();
        });

        waitForUiThread();

        activityRule.runOnUiThread(() -> {
            assertCurrencyEquals(50000.00, annualIncomeInput.getText().toString());
            assertCurrencyEquals(1000.00, taxCreditsInput.getText().toString());
        });
    }

    @Test
    public void testValidateInputs_WithMissingIncome() throws Throwable {
        final FederalTaxInfoActivity activity = launchActivity();
        final EditText annualIncomeInput = activity.findViewById(R.id.annualIncomeTxtF3);
        final Button calculateButton = activity.findViewById(R.id.button2);

        activityRule.runOnUiThread(() -> {
            annualIncomeInput.setText("");
            calculateButton.performClick();
            latch.countDown();
        });

        waitForUiThread();

        activityRule.runOnUiThread(() -> {
            assertNotNull(annualIncomeInput.getError());
            assertEquals("Annual income is required", annualIncomeInput.getError().toString());
        });
    }

    @Test
    public void testValidateInputs_WithValidData() throws Throwable {
        final FederalTaxInfoActivity activity = launchActivity();
        final EditText annualIncomeInput = activity.findViewById(R.id.annualIncomeTxtF3);
        final EditText taxCreditsInput = activity.findViewById(R.id.taxCreditsTxtF);
        final EditText aboveLineDeductionsInput = activity.findViewById(R.id.ablDeductionsTxtF);
        final EditText itemizedDeductionsInput = activity.findViewById(R.id.annualIncomeTxtF4);
        final Button calculateButton = activity.findViewById(R.id.button2);

        activityRule.runOnUiThread(() -> {
            annualIncomeInput.setText("5000000"); // 50000.00
            taxCreditsInput.setText("100000");    // 1000.00
            aboveLineDeductionsInput.setText("500000"); // 5000.00
            itemizedDeductionsInput.setText("1200000"); // 12000.00
            calculateButton.performClick();
            latch.countDown();
        });

        waitForUiThread();

        Map<String, Object> updatedValues = mockDb.getLastUpdatedValues();
        assertEquals("50000.0", updatedValues.get("income"));
        assertEquals(1000, updatedValues.get("tax_credits"));
        assertEquals("5000.0", updatedValues.get("above_line_deductions"));
        assertEquals("12000.0", updatedValues.get("itemized_deductions"));
    }

    private FederalTaxInfoActivity launchActivity() {
        Intent intent = new Intent();
        intent.putExtra("USER_ID", TEST_USER_ID);
        FederalTaxInfoActivity activity = activityRule.launchActivity(intent);
        activity.setDatabaseHelper(mockDb);
        return activity;
    }
}