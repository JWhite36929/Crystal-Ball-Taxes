package com.example.crystalballtaxes;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

public class FederalTaxInfoActivity extends AppCompatActivity {
    private static final String TAG = "FederalTaxInfoActivity";

    private DatabaseHelper db;
    private EditText annualIncomeInput;
    private EditText taxCreditsInput;
    private EditText aboveLineDeductionsInput;
    private EditText itemizedDeductionsInput;
    private Button calculateButton;

    private long userId = -1;
    private NumberFormat currencyFormatter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_federal_tax_info);

        // initialize database helper
        db = new DatabaseHelper(this);

        // get user ID from intent
        userId = getIntent().getLongExtra("USER_ID", -1);
        if (userId == -1) {
            Log.e(TAG, "No user ID provided");
            Toast.makeText(this, "Error: User not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // initialize UI elements
        initializeViews();

        // set up currency formatter
        currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);

        // load existing tax info if available
        loadExistingTaxInfo();

        // set up input formatting
        setupCurrencyFormatting();

        // set up calculate button
        calculateButton.setOnClickListener(v -> {
            if (validateInputs()) {
                saveTaxInfo();
                goToTaxResults();
            }
        });
    }

    private void initializeViews() {
        annualIncomeInput = findViewById(R.id.annualIncomeTxtF3);
        taxCreditsInput = findViewById(R.id.taxCreditsTxtF);
        aboveLineDeductionsInput = findViewById(R.id.ablDeductionsTxtF);
        itemizedDeductionsInput = findViewById(R.id.annualIncomeTxtF4);
        calculateButton = findViewById(R.id.button2);
    }

    private void loadExistingTaxInfo() {
        Map<String, String> taxInfo = db.getUserTaxInfo(userId);

        try {
            double income = taxInfo.get("income") != null ? Double.parseDouble(taxInfo.get("income")) : 0;
            if (income != 0) {
                annualIncomeInput.setText(currencyFormatter.format(income));

                if (taxInfo.get("tax_credits") != null) {
                    double credits = Double.parseDouble(taxInfo.get("tax_credits"));
                    taxCreditsInput.setText(currencyFormatter.format(credits));
                }

                if (taxInfo.get("above_line_deductions") != null) {
                    double aboveLine = Double.parseDouble(taxInfo.get("above_line_deductions"));
                    aboveLineDeductionsInput.setText(currencyFormatter.format(aboveLine));
                }

                if (taxInfo.get("itemized_deductions") != null) {
                    double itemized = Double.parseDouble(taxInfo.get("itemized_deductions"));
                    itemizedDeductionsInput.setText(currencyFormatter.format(itemized));
                }
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "Error parsing existing tax info: " + e.getMessage());
        }
    }

    //allows for currency formatting when the user is inputting their income
    private void setupCurrencyFormatting() {
        TextWatcher currencyWatcher = new TextWatcher() {
            private boolean isUpdating = false;
            private String previousCleanString = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdating) return;

                String str = s.toString();
                String cleanString = str.replaceAll("[$,.]", "");

                // don't format if user hasn't started entering numbers
                if (str.equals("Annual Income") || str.equals("Total Tax Credits") ||
                        str.equals("Above the line Deductions") || str.equals("Itemized Deductions") ||
                        cleanString.isEmpty()) {
                    return;
                }

                // only format if the clean string is different
                if (cleanString.equals(previousCleanString)) {
                    return;
                }

                isUpdating = true;
                previousCleanString = cleanString;

                try {
                    double parsed = Double.parseDouble(cleanString);
                    parsed = parsed / 100.0;
                    String formatted = currencyFormatter.format(parsed);
                    s.replace(0, s.length(), formatted);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Error formatting currency: " + e.getMessage());
                }

                isUpdating = false;
            }
        };

        annualIncomeInput.addTextChangedListener(currencyWatcher);
        taxCreditsInput.addTextChangedListener(currencyWatcher);
        aboveLineDeductionsInput.addTextChangedListener(currencyWatcher);
        itemizedDeductionsInput.addTextChangedListener(currencyWatcher);
    }

    private boolean validateInputs() {
        if (TextUtils.isEmpty(annualIncomeInput.getText())) {
            annualIncomeInput.setError("Annual income is required");
            annualIncomeInput.requestFocus();
            return false;
        }

        try {
            parseAmount(annualIncomeInput.getText().toString());
            if (!TextUtils.isEmpty(taxCreditsInput.getText())) {
                parseAmount(taxCreditsInput.getText().toString());
            }
            if (!TextUtils.isEmpty(aboveLineDeductionsInput.getText())) {
                parseAmount(aboveLineDeductionsInput.getText().toString());
            }
            if (!TextUtils.isEmpty(itemizedDeductionsInput.getText())) {
                parseAmount(itemizedDeductionsInput.getText().toString());
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid currency amounts", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private double parseAmount(String amount) {
        String cleanAmount = amount.replaceAll("[$,]", "");
        return Double.parseDouble(cleanAmount);
    }

    private void saveTaxInfo() {
        try {
            double income = parseAmount(annualIncomeInput.getText().toString());
            double taxCredits = TextUtils.isEmpty(taxCreditsInput.getText()) ? 0 :
                    parseAmount(taxCreditsInput.getText().toString());
            double aboveLineDeductions = TextUtils.isEmpty(aboveLineDeductionsInput.getText()) ? 0 :
                    parseAmount(aboveLineDeductionsInput.getText().toString());
            double itemizedDeductions = TextUtils.isEmpty(itemizedDeductionsInput.getText()) ? 0 :
                    parseAmount(itemizedDeductionsInput.getText().toString());

            // update database with new values
            boolean success = true;
            success &= db.updateIncome(userId, String.valueOf(income));
            success &= db.updateTaxCredits(userId, (int)taxCredits);
            success &= db.updateAboveLineDeductions(userId, String.valueOf(aboveLineDeductions));
            success &= db.updateItemizedDeductions(userId, String.valueOf(itemizedDeductions));

            if (success) {
                Toast.makeText(this, "Tax information saved successfully", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Tax info saved successfully for user " + userId);
            } else {
                Toast.makeText(this, "Error saving some tax information", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e(TAG, "Error saving tax info: " + e.getMessage());
            Toast.makeText(this, "Error saving tax information", Toast.LENGTH_SHORT).show();
        }
    }
    public void setDatabaseHelper(DatabaseHelper dbHelper) {
        this.db = dbHelper;
    }

    private void goToTaxResults() {
        Intent intent = new Intent(this, TaxResultsActivity.class);
        intent.putExtra("USER_ID", userId);
        startActivity(intent);
        finish();
    }
}