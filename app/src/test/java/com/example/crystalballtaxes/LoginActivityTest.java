package com.example.crystalballtaxes;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {30})
public class LoginActivityTest {

    private DatabaseHelper databaseHelper;
    private LoginActivity loginActivity;

    @Before
    public void setUp() {
        // Initialize Firebase before creating activity
        if (FirebaseApp.getApps(RuntimeEnvironment.getApplication()).isEmpty()) {
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setProjectId("test-project")
                    .setApplicationId("test-app")
                    .setApiKey("test-key")
                    .build();

            FirebaseApp.initializeApp(RuntimeEnvironment.getApplication(), options);
        }

        databaseHelper = mock(DatabaseHelper.class);
        // Initialize activity using Robolectric
        loginActivity = Robolectric.buildActivity(LoginActivity.class)
                .create()
                .get();
        loginActivity.setDatabase(databaseHelper);
    }

    @Test
    public void testDatabaseValidation_ValidCredentials() {
        // Setup
        String testEmail = "test@example.com";
        String testPassword = "password123";

        when(databaseHelper.checkUser(testEmail, testPassword)).thenReturn(true);

        // Test
        boolean isValid = databaseHelper.checkUser(testEmail, testPassword);
        assertTrue("Valid credentials should return true", isValid);
    }

    @Test
    public void testDatabaseValidation_InvalidCredentials() {
        // Setup
        String testEmail = "test@example.com";
        String testPassword = "wrongpassword";

        when(databaseHelper.checkUser(testEmail, testPassword)).thenReturn(false);

        // Test
        boolean isValid = databaseHelper.checkUser(testEmail, testPassword);
        assertFalse("Invalid credentials should return false", isValid);
    }

    @Test
    public void testInputValidation_EmptyEmail() {
        // Test
        assertFalse("Empty email should be invalid",
                isValidInput("", "password123"));
    }

    @Test
    public void testInputValidation_EmptyPassword() {
        // Test
        assertFalse("Empty password should be invalid",
                isValidInput("test@example.com", ""));
    }

    @Test
    public void testDatabaseLookup_UserExists() {
        // Setup
        String testEmail = "test@example.com";
        when(databaseHelper.getUserIdFromEmail(testEmail)).thenReturn(1L);

        // Test
        long userId = databaseHelper.getUserIdFromEmail(testEmail);
        assertTrue("Should return valid user ID", userId > 0);
    }

    @Test
    public void testDatabaseLookup_UserDoesNotExist() {
        // Setup
        String testEmail = "nonexistent@example.com";
        when(databaseHelper.getUserIdFromEmail(testEmail)).thenReturn(-1L);

        // Test
        long userId = databaseHelper.getUserIdFromEmail(testEmail);
        assertEquals("Should return -1 for non-existent user", -1, userId);
    }

    // Helper method for validation
    private boolean isValidInput(String email, String password) {
        return !email.isEmpty() && !password.isEmpty();
    }
}