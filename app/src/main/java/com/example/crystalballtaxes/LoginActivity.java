package com.example.crystalballtaxes;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button signUpButton;
    private TextView forgotPasswordText;
    private ProgressBar loginProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize views
        emailEditText = findViewById(R.id.emailEditTxt);
        passwordEditText = findViewById(R.id.passwordEditTxt);
        loginButton = findViewById(R.id.loginBtn);
        signUpButton = findViewById(R.id.signUpBtn);
        forgotPasswordText = findViewById(R.id.forgotPassTxt);
        loginProgressBar = findViewById(R.id.loginProgressBar);

        // Set click listeners
        loginButton.setOnClickListener(v -> validateAndLogin());
        signUpButton.setOnClickListener(v -> handleSignUp());
        forgotPasswordText.setOnClickListener(v -> handleForgotPassword());
    }

    private void validateAndLogin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty()) {
            emailEditText.setError("Email cannot be empty");
            return;
        }

        if (password.isEmpty()) {
            passwordEditText.setError("Password cannot be empty");
            return;
        }

        // Show progress bar
        loginProgressBar.setVisibility(View.VISIBLE);
        loginButton.setEnabled(false);

        // Perform login
        // Your login logic here
    }

    private void handleForgotPassword() {
        String email = emailEditText.getText().toString().trim();
        if (email.isEmpty()) {
            emailEditText.setError("Enter email to reset password");
            return;
        }
        // Handle forgot password logic
    }

    private void handleSignUp() {
        // Handle sign up navigation
    }
}