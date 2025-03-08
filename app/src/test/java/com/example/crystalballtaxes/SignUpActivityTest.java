package com.example.crystalballtaxes;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import android.os.Looper;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {30})
public class SignUpActivityTest {

    private SignUpActivity signUpActivity;
    private DatabaseHelper mockDb;
    private FirebaseAuth mockAuth;
    private FirebaseUser mockUser;
    private TaskCompletionSource<AuthResult> taskCompletionSource;
    private ShadowLooper shadowLooper;

    @Before
    public void setUp() {
        // Initialize Firebase with test configuration
        if (FirebaseApp.getApps(RuntimeEnvironment.getApplication()).isEmpty()) {
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setProjectId("test-project")
                    .setApplicationId("1:123456789012:android:1234567890123456")
                    .setApiKey("test-api-key")
                    .build();

            FirebaseApp.initializeApp(RuntimeEnvironment.getApplication(), options);
        }

        // Mock dependencies
        mockDb = mock(DatabaseHelper.class);
        mockAuth = mock(FirebaseAuth.class);
        mockUser = mock(FirebaseUser.class);
        taskCompletionSource = new TaskCompletionSource<>();

        // Setup Firebase mock behavior
        when(mockAuth.createUserWithEmailAndPassword(anyString(), anyString()))
                .thenReturn(taskCompletionSource.getTask());
        when(mockAuth.getCurrentUser()).thenReturn(mockUser);

        // Setup database mock behavior
        when(mockDb.isDatabaseInitialized()).thenReturn(true);
        when(mockDb.addUser(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(1L);
        when(mockDb.getUserIdFromEmail(anyString())).thenReturn(1L);
        when(mockDb.initializeTaxRecord(anyLong())).thenReturn(1L);

        // Initialize activity
        signUpActivity = Robolectric.buildActivity(SignUpActivity.class)
                .create()
                .get();

        // Set mocked dependencies
        signUpActivity.setAuth(mockAuth);
        signUpActivity.setDatabase(mockDb);

        // Get the shadow looper for handling async operations
        shadowLooper = Shadows.shadowOf(Looper.getMainLooper());
    }

    @Test
    public void testDatabaseInitializationCheck() {
        when(mockDb.isDatabaseInitialized()).thenReturn(false);
        signUpActivity.testCreateUser("test@email.com", "password", "Test User", "1234567890");
        shadowLooper.idle();
        verify(mockAuth, never()).createUserWithEmailAndPassword(anyString(), anyString());
    }

    @Test
    public void testSuccessfulUserCreation() {
        // Arrange
        taskCompletionSource.setResult(mock(AuthResult.class));

        // Act
        signUpActivity.testCreateUser("test@email.com", "password", "Test User", "1234567890");
        shadowLooper.idle();  // Process all pending callbacks

        // Assert
        verify(mockDb).addUser("Test User", "test@email.com", "password", "1234567890");
        verify(mockDb).initializeTaxRecord(1L);
    }

    @Test
    public void testEmptyFieldValidation() {
        assertFalse(signUpActivity.testCreateUser("", "password", "name", "phone"));
        assertFalse(signUpActivity.testCreateUser("email", "", "name", "phone"));
        assertFalse(signUpActivity.testCreateUser("email", "password", "", "phone"));
        assertFalse(signUpActivity.testCreateUser("email", "password", "name", ""));
        shadowLooper.idle();
    }

    @Test
    public void testFailedDatabaseInsertion() {
        // Arrange
        taskCompletionSource.setResult(mock(AuthResult.class));
        when(mockDb.addUser(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(-1L);

        // Act
        signUpActivity.testCreateUser("test@email.com", "password", "Test User", "1234567890");
        shadowLooper.idle();  // Process all pending callbacks

        // Assert
        verify(mockUser).delete();
    }

    @Test
    public void testFailedFirebaseAuth() {
        // Arrange
        Exception testException = new Exception("Auth failed");
        taskCompletionSource.setException(testException);

        // Act
        signUpActivity.testCreateUser("test@email.com", "password", "Test User", "1234567890");
        shadowLooper.idle();  // Process all pending callbacks

        // Assert
        verify(mockDb, never()).addUser(anyString(), anyString(), anyString(), anyString());
    }
}