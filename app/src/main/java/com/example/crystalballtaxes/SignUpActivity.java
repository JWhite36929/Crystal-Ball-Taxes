package com.example.crystalballtaxes;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {
    private EditText nameTxtF, emailTxtF, passwordTxtF, phoneTxtF;
    private ProgressBar signUpProgressBar;
    FirebaseAuth mAuth;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        db = new DatabaseHelper(this);

        nameTxtF = findViewById(R.id.newAccUserName);
        emailTxtF = findViewById(R.id.newAccEmail);
        passwordTxtF = findViewById(R.id.newAccPasswrd);
        phoneTxtF = findViewById(R.id.newAccPhoneNum);
        signUpProgressBar = findViewById(R.id.signUpProgressBar);
        Button createAccBtn = findViewById(R.id.createNewAccBtn);

        signUpProgressBar.setVisibility(View.INVISIBLE);

        createAccBtn.setOnClickListener(view -> {
            signUpProgressBar.setVisibility(View.VISIBLE);
            createUser();
        });
    }

    void setAuth(FirebaseAuth auth) {
        this.mAuth = auth;
    }

    void setDatabase(DatabaseHelper database) {
        this.db = database;
    }

    private void createUser() {
        String email = emailTxtF.getText().toString().trim();
        String password = passwordTxtF.getText().toString().trim();
        String name = nameTxtF.getText().toString().trim();
        String phone = phoneTxtF.getText().toString().trim();

        testCreateUser(email, password, name, phone);
    }

    boolean testCreateUser(String email, String password, String name, String phone) {
        // Input validation
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) ||
                TextUtils.isEmpty(name) || TextUtils.isEmpty(phone)) {
            if (signUpProgressBar != null) {
                signUpProgressBar.setVisibility(View.INVISIBLE);
            }
            return false;
        }

        // Database initialization check
        if (!db.isDatabaseInitialized()) {
            Log.e("SignUpActivity", "Database not properly initialized!");
            if (this != null) {  // Check for testing environment
                Toast.makeText(this, "Database initialization error", Toast.LENGTH_LONG).show();
            }
            if (signUpProgressBar != null) {
                signUpProgressBar.setVisibility(View.INVISIBLE);
            }
            return false;
        }

        // Firebase authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("SignUpActivity", "Firebase auth successful, adding to SQLite...");
                        long userId = db.addUser(name, email, password, phone);

                        if (userId != -1) {
                            long verifyId = db.getUserIdFromEmail(email);
                            if (verifyId != -1) {
                                long taxRecordId = db.initializeTaxRecord(userId);
                                Log.d("SignUpActivity", "Initialized tax record: " + taxRecordId);

                                if (this != null) {  // Check for testing environment
                                    Toast.makeText(this, "User registered successfully", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(this, LoginActivity.class);
                                    intent.putExtra("userID", userId);
                                    startActivity(intent);
                                    finish();
                                }
                            } else {
                                handleRegistrationError("Database verification failed");
                            }
                        } else {
                            handleRegistrationError("Failed to create local user record");
                        }
                    } else {
                        handleRegistrationError(Objects.requireNonNull(task.getException()).getMessage());
                    }
                    if (signUpProgressBar != null) {
                        signUpProgressBar.setVisibility(View.INVISIBLE);
                    }
                });
        return true;
    }

    private void handleRegistrationError(String error) {
        Log.e("SignUpActivity", "Registration error: " + error);
        if (this != null) {  // Check for testing environment
            Toast.makeText(this, "Registration Error: " + error, Toast.LENGTH_SHORT).show();
        }
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.delete();
        }
    }

    public void goBack(View v) {
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
    }
}