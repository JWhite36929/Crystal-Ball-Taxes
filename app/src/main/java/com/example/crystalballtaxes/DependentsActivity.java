package com.example.crystalballtaxes;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DependentsActivity extends AppCompatActivity {
    private static final String TAG = "DependentsActivity";
    private DatabaseHelper db;
    private EditText firstNameInput, lastNameInput, ssnInput, dobInput;
    private Spinner relationshipSpinner;
    private int userId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dependents);

        // initialize database helper
        db = new DatabaseHelper(this);

        // get user ID from intent
        userId = (int) getIntent().getLongExtra("USER_ID", -1);
        if (userId == -1) {
            Log.e(TAG, "No user ID provided");
            Toast.makeText(this, "Error: User not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize UI elements
        firstNameInput = findViewById(R.id.dependFirstNameTxtF);
        lastNameInput = findViewById(R.id.dependLastNameTxtF);
        ssnInput = findViewById(R.id.dependSSNTxtF3);
        dobInput = findViewById(R.id.dependDOBTxtF);
        relationshipSpinner = findViewById(R.id.spinner2);
        Button addDependentBtn = findViewById(R.id.addDependBtn);
        Button noDependentsBtn = findViewById(R.id.button);

        // set up the spinner with relationship options
        String[] relationships = new String[]{
                "Relationship to Taxpayer",
                "Father",
                "Mother",
                "Brother",
                "Sister",
                "Son",
                "Daughter"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, relationships);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        relationshipSpinner.setAdapter(adapter);

        // set up button click listeners
        addDependentBtn.setOnClickListener(v -> addDependent());

        noDependentsBtn.setOnClickListener(v -> proceedToNextScreen());
    }

    private void addDependent() {
        // get values from form
        String firstName = firstNameInput.getText().toString().trim();
        String lastName = lastNameInput.getText().toString().trim();
        String ssn = ssnInput.getText().toString().trim();
        String dob = dobInput.getText().toString().trim();
        String relationship = relationshipSpinner.getSelectedItem().toString();

        // validate input
        if (!validateInput(firstName, lastName, ssn, dob, relationship)) {
            return;
        }

        // add dependent to database
        int result = db.addDependent(firstName, lastName, ssn, dob, relationship, userId);

        if (result != -1) {
            Toast.makeText(this, "Dependent added successfully", Toast.LENGTH_SHORT).show();
            clearFields();
        } else {
            Toast.makeText(this, "Error adding dependent", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateInput(String firstName, String lastName, String ssn,
                                  String dob, String relationship) {
        if (TextUtils.isEmpty(firstName)) {
            firstNameInput.setError("First name is required");
            firstNameInput.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(lastName)) {
            lastNameInput.setError("Last name is required");
            lastNameInput.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(ssn)) {
            ssnInput.setError("SSN is required");
            ssnInput.requestFocus();
            return false;
        }

        if (!isValidSSN(ssn)) {
            ssnInput.setError("Invalid SSN format (XXX-XX-XXXX)");
            ssnInput.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(dob)) {
            dobInput.setError("Date of birth is required");
            dobInput.requestFocus();
            return false;
        }

        if (!isValidDate(dob)) {
            dobInput.setError("Invalid date format (MM/DD/YYYY)");
            dobInput.requestFocus();
            return false;
        }

        if (relationship.equals("Relationship to Taxpayer")) {
            Toast.makeText(this, "Please select a relationship", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private boolean isValidSSN(String ssn) {
        // SSN validation regex (XXX-XX-XXXX)
        //will require user to manually input hyphens
        return ssn.matches("\\d{3}-\\d{2}-\\d{4}");
    }

    private boolean isValidDate(String date) {
        // date validation regex (MM/DD/YYYY)
        //will require user to manually input slashes
        return date.matches("(0[1-9]|1[0-2])/(0[1-9]|[12]\\d|3[01])/\\d{4}");
    }

    private void clearFields() {
        firstNameInput.setText("");
        lastNameInput.setText("");
        ssnInput.setText("");
        dobInput.setText("");
        relationshipSpinner.setSelection(0);
    }

    private void proceedToNextScreen() {
        Intent intent = new Intent(DependentsActivity.this, FederalTaxInfoActivity.class);
        // Get the user ID that was passed to this activity
        long userId = getIntent().getLongExtra("USER_ID", -1);

        if (userId == -1) {
            // If not found in intent, try to get it from Firebase Auth
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null && currentUser.getEmail() != null) {
                userId = db.getUserIdFromEmail(currentUser.getEmail());
            }
        }

        if (userId != -1) {
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
            finish();
        } else {
            Log.e(TAG, "Could not determine user ID");
            Toast.makeText(this, "Error: User not found", Toast.LENGTH_SHORT).show();
        }
    }
    public void setDatabaseHelper(DatabaseHelper dbHelper) {
        this.db = dbHelper;
    }
}