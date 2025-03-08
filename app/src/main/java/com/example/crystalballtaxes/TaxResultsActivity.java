package com.example.crystalballtaxes;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

public class TaxResultsActivity extends AppCompatActivity {
    private static final String TAG = "TaxResultsActivity";
    private DatabaseHelper db;
    private TextView agiTextView, taxableIncomeTextView, grossTaxLiabilityTextView, taxesDueTextView;
    private long userId;
    private NumberFormat currencyFormatter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tax_results);

        initializeViews();
        currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);

        // Only initialize db if it hasn't been injected (for testing)
        if (db == null) {
            db = new DatabaseHelper(this);
        }

        // get user ID from intent
        userId = getIntent().getLongExtra("USER_ID", -1);
        if (userId == -1) {
            Log.e(TAG, "No user ID provided");
            Toast.makeText(this, "Error: User not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupButtons();

    }

    private void initializeViews() {
        agiTextView = findViewById(R.id.AGItxtF);
        taxableIncomeTextView = findViewById(R.id.taxableIncTxt);
        grossTaxLiabilityTextView = findViewById(R.id.grossTaxLibTxt);
        taxesDueTextView = findViewById(R.id.taxesDueTxt);
    }

    private void setupButtons() {
        Button saveButton = findViewById(R.id.saveTaxInfoBtn);
        Button homeButton = findViewById(R.id.backToHomeBtn);

        saveButton.setOnClickListener(v -> Toast.makeText(this, "Tax results saved", Toast.LENGTH_SHORT).show());

        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }

    @SuppressLint("SetTextI18n")
    private void calculateAndDisplayTaxes() {
        Map<String, String> taxInfo = db.getUserTaxInfo(userId);
        if (taxInfo.isEmpty()) {
            Toast.makeText(this, "Error: No tax information found", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // parse values
            double income = Double.parseDouble(taxInfo.get("income"));
            double aboveLineDeductions = Double.parseDouble(taxInfo.get("above_line_deductions"));
            double itemizedDeductions = Double.parseDouble(taxInfo.get("itemized_deductions"));
            double taxCredits = Double.parseDouble(taxInfo.get("tax_credits"));
            String filingStatus = taxInfo.get("filing_status");

            // calculate AGI
            double agi = income - aboveLineDeductions;

            // determine standard deduction based on filing status
            assert filingStatus != null;
            double standardDeduction = getStandardDeduction(filingStatus);

            // use greater of itemized or standard deduction
            double finalDeduction = Math.max(itemizedDeductions, standardDeduction);

            // calculate taxable income
            double taxableIncome = Math.max(0, agi - finalDeduction);

            // calculate gross tax liability
            double grossTaxLiability = calculateTaxBrackets(taxableIncome, filingStatus);
            // calculate final tax due
            double taxDue = Math.max(0, grossTaxLiability - taxCredits);

            // update UI
            agiTextView.setText("Adjusted Gross Income: " + currencyFormatter.format(agi));
            taxableIncomeTextView.setText("Taxable Income: " + currencyFormatter.format(taxableIncome));
            grossTaxLiabilityTextView.setText("Gross Tax Liability: " + currencyFormatter.format(grossTaxLiability));
            taxesDueTextView.setText("Taxes Due: " + currencyFormatter.format(taxDue));

        } catch (NumberFormatException e) {
            Log.e(TAG, "Error parsing tax values: " + e.getMessage());
            Toast.makeText(this, "Error calculating taxes", Toast.LENGTH_SHORT).show();
        }
    }

    private double getStandardDeduction(String filingStatus) {
        // 2023 standard deduction amounts
        //single and married filed separately are the same amount which is why there is no break for single
        switch (filingStatus) {
            case "Married Filing Jointly":
                return 27700.0;
            case "Head of Household":
                return 20800.0;
            case "Single":
            case "Married Filing Separately":
                return 13850.0;
            default:
                return 13850.0;
        }
    }

    public void setDatabaseHelper(DatabaseHelper dbHelper) {
        this.db = dbHelper;
    }

    private double calculateTaxBrackets(double taxableIncome, String filingStatus) {
        double tax = 0;

        switch (filingStatus) {
            case "Married Filing Jointly":
                // 10% bracket
                if (taxableIncome > 0) {
                    tax += Math.min(22000, taxableIncome) * 0.10;
                }
                // 12% bracket
                if (taxableIncome > 22000) {
                    tax += Math.min(89450 - 22000, Math.max(0, taxableIncome - 22000)) * 0.12;
                }
                // 22% bracket
                if (taxableIncome > 89450) {
                    tax += Math.min(190750 - 89450, Math.max(0, taxableIncome - 89450)) * 0.22;
                }
                // 24% bracket
                if (taxableIncome > 190750) {
                    tax += Math.min(364200 - 190750, Math.max(0, taxableIncome - 190750)) * 0.24;
                }
                // 32% bracket
                if (taxableIncome > 364200) {
                    tax += Math.min(462500 - 364200, Math.max(0, taxableIncome - 364200)) * 0.32;
                }
                // 35% bracket
                if (taxableIncome > 462500) {
                    tax += Math.min(693750 - 462500, Math.max(0, taxableIncome - 462500)) * 0.35;
                }
                // 37% bracket
                if (taxableIncome > 693750) {
                    tax += (taxableIncome - 693750) * 0.37;
                }
                break;

            case "Single":
            case "Married Filing Separately":
                // 10% bracket
                if (taxableIncome > 0) {
                    tax += Math.min(11000, taxableIncome) * 0.10;
                }
                // 12% bracket
                if (taxableIncome > 11000) {
                    tax += Math.min(44725 - 11000, Math.max(0, taxableIncome - 11000)) * 0.12;
                }
                // 22% bracket
                if (taxableIncome > 44725) {
                    tax += Math.min(95375 - 44725, Math.max(0, taxableIncome - 44725)) * 0.22;
                }
                // 24% bracket
                if (taxableIncome > 95375) {
                    tax += Math.min(182100 - 95375, Math.max(0, taxableIncome - 95375)) * 0.24;
                }
                // 32% bracket
                if (taxableIncome > 182100) {
                    tax += Math.min(231250 - 182100, Math.max(0, taxableIncome - 182100)) * 0.32;
                }
                // 35% bracket
                if (taxableIncome > 231250) {
                    tax += Math.min(578125 - 231250, Math.max(0, taxableIncome - 231250)) * 0.35;
                }
                // 37% bracket
                if (taxableIncome > 578125) {
                    tax += (taxableIncome - 578125) * 0.37;
                }
                break;

            case "Head of Household":
                // 10% bracket
                if (taxableIncome > 0) {
                    tax += Math.min(15700, taxableIncome) * 0.10;
                }
                // 12% bracket
                if (taxableIncome > 15700) {
                    tax += Math.min(59850 - 15700, Math.max(0, taxableIncome - 15700)) * 0.12;
                }
                // 22% bracket
                if (taxableIncome > 59850) {
                    tax += Math.min(95350 - 59850, Math.max(0, taxableIncome - 59850)) * 0.22;
                }
                // 24% bracket
                if (taxableIncome > 95350) {
                    tax += Math.min(182100 - 95350, Math.max(0, taxableIncome - 95350)) * 0.24;
                }
                // 32% bracket
                if (taxableIncome > 182100) {
                    tax += Math.min(231250 - 182100, Math.max(0, taxableIncome - 182100)) * 0.32;
                }
                // 35% bracket
                if (taxableIncome > 231250) {
                    tax += Math.min(578100 - 231250, Math.max(0, taxableIncome - 231250)) * 0.35;
                }
                // 37% bracket
                if (taxableIncome > 578100) {
                    tax += (taxableIncome - 578100) * 0.37;
                }
                break;
        }

        return tax;
    }

    public void recalculateTaxes() {
        calculateAndDisplayTaxes();
    }
}