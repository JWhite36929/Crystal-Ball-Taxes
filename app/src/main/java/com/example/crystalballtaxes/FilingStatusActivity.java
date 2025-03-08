package com.example.crystalballtaxes;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.appcompat.app.AppCompatActivity;

public class FilingStatusActivity extends AppCompatActivity {
    private RadioButton singleRBtn;
    private RadioButton marriedJointBtn;
    private RadioButton marriedFiledSepRBtn;
    private RadioButton headOfHouseRBtn;
    private RadioGroup radioGroup;
    private TextView errorText;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filing_status);

        db = new DatabaseHelper(this);

        radioGroup = findViewById(R.id.radioGroup);
        Button nextBtn = findViewById(R.id.nextBtn);
        errorText = findViewById(R.id.errorText);

        singleRBtn = findViewById(R.id.singleRBtn);
        marriedJointBtn = findViewById(R.id.marriedJointBtn);
        marriedFiledSepRBtn = findViewById(R.id.marriedFiledSepRBtn);
        headOfHouseRBtn = findViewById(R.id.headOfHouseRBtn);

        //hide error on start
        errorText.setVisibility(View.GONE);

        nextBtn.setOnClickListener(v -> {
            if (radioGroup.getCheckedRadioButtonId() == -1) {
                // show error if no selection
                errorText.setVisibility(View.VISIBLE);
                radioGroup.requestFocus();
                return;
            }

            String filingStatus = "";

            // get the selected radio button
            if (singleRBtn.isChecked()) {
                filingStatus = "Single";
                Log.d("FilingStatusActivity", "single");
            } else if (marriedJointBtn.isChecked()) {
                filingStatus = "Married Filing Jointly";
                Log.d("FilingStatusActivity", "married");
            } else if (marriedFiledSepRBtn.isChecked()) {
                filingStatus = "Married Filing Separately";
                Log.d("FilingStatusActivity", "separated");
            } else if (headOfHouseRBtn.isChecked()) {
                filingStatus = "Head of Household";
                Log.d("FilingStatusActivity", "HOH");
            }

            // first try to get userId from intent
            long userId = getIntent().getLongExtra("USER_ID", -1);

            // if not found in intent, try to get it from Firebase Auth
            if (userId == -1) {
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null && currentUser.getEmail() != null) {
                    userId = db.getUserIdFromEmail(currentUser.getEmail());
                }
            }

            if (userId != -1) {
                if (!db.taxRecordExists(userId)) {
                    long taxRecordId = db.initializeTaxRecord(userId);
                    if (taxRecordId == -1) {
                        Toast.makeText(FilingStatusActivity.this,
                                "Error initializing tax record", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                // update filing status
                boolean success = db.updateFilingStatus(userId, filingStatus);

                if (success) {
                    Toast.makeText(FilingStatusActivity.this,
                            "Filing status saved", Toast.LENGTH_SHORT).show();

                    // navigate to next screen with userId
                    Intent intent = new Intent(FilingStatusActivity.this,
                            DependentsActivity.class);
                    intent.putExtra("USER_ID", userId); // Pass the SQLite user ID
                    startActivity(intent);
                    finish();
                } else {
                    // error saving to database
                    Toast.makeText(FilingStatusActivity.this,
                            "Error saving filing status", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e(TAG, "Could not determine user ID");
                Toast.makeText(FilingStatusActivity.this,
                        "Error: User not found", Toast.LENGTH_SHORT).show();
            }
        });

    }
    public void setDatabaseHelper(DatabaseHelper dbHelper) {
        this.db = dbHelper;
    }

}