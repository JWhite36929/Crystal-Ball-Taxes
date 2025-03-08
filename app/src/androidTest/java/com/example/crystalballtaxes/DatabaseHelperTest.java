package com.example.crystalballtaxes;

import static org.junit.Assert.*;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;

@RunWith(AndroidJUnit4.class)
public class DatabaseHelperTest {
    private DatabaseHelper db;
    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        db = new DatabaseHelper(context);
    }

    @After
    public void tearDown() {
        db.close();
    }

    @Test
    public void testAddUser() {
        // Test adding a normal user
        long userId = db.addUser("John Doe", "john@example.com", "password123", "1234567890");
        assertTrue("User ID should be positive", userId > 0);

        // Test duplicate email
        long duplicateId = db.addUser("John Doe2", "john@example.com", "password456", "0987654321");
        assertEquals("Duplicate email should return -1", -1, duplicateId);

        // Test case-insensitive email duplicate
        long caseInsensitiveId = db.addUser("John Doe3", "JOHN@example.com", "password789", "1122334455");
        assertEquals("Case-insensitive duplicate email should return -1", -1, caseInsensitiveId);
    }

    @Test
    public void testGetUserIdFromEmail() {
        // Add test user
        long addedId = db.addUser("Jane Doe", "jane@example.com", "password123", "1234567890");

        // Test normal retrieval
        long retrievedId = db.getUserIdFromEmail("jane@example.com");
        assertEquals("Retrieved ID should match added ID", addedId, retrievedId);

        // Test case-insensitive retrieval
        long caseInsensitiveId = db.getUserIdFromEmail("JANE@EXAMPLE.COM");
        assertEquals("Case-insensitive retrieval should work", addedId, caseInsensitiveId);

        // Test non-existent email
        long nonExistentId = db.getUserIdFromEmail("nonexistent@example.com");
        assertEquals("Non-existent email should return -1", -1, nonExistentId);
    }

    @Test
    public void testCheckUser() {
        // Add test user
        db.addUser("Test User", "test@example.com", "password123", "1234567890");

        // Test correct credentials
        assertTrue("Valid credentials should return true",
                db.checkUser("test@example.com", "password123"));

        // Test case-insensitive email
        assertTrue("Case-insensitive email should work",
                db.checkUser("TEST@EXAMPLE.COM", "password123"));

        // Test wrong password
        assertFalse("Wrong password should return false",
                db.checkUser("test@example.com", "wrongpassword"));

        // Test non-existent user
        assertFalse("Non-existent user should return false",
                db.checkUser("nonexistent@example.com", "password123"));
    }

    @Test
    public void testTaxRecordOperations() {
        // Add test user
        long userId = db.addUser("Tax Test", "tax@example.com", "password123", "1234567890");

        // Test initializing tax record
        long taxRecordId = db.initializeTaxRecord(userId);
        assertTrue("Tax record ID should be positive", taxRecordId > 0);

        // Test duplicate initialization
        long duplicateTaxRecord = db.initializeTaxRecord(userId);
        assertEquals("Duplicate tax record should return -1", -1, duplicateTaxRecord);

        // Test updating filing status
        assertTrue("Filing status update should succeed",
                db.updateFilingStatus(userId, "Single"));

        // Test updating income
        assertTrue("Income update should succeed",
                db.updateIncome(userId, "50000"));

        // Test updating deductions
        assertTrue("Above line deductions update should succeed",
                db.updateAboveLineDeductions(userId, "5000"));
        assertTrue("Itemized deductions update should succeed",
                db.updateItemizedDeductions(userId, "12000"));

        // Test updating tax credits
        assertTrue("Tax credits update should succeed",
                db.updateTaxCredits(userId, 1000));

        // Test retrieving tax info
        Map<String, String> taxInfo = db.getUserTaxInfo(userId);
        assertNotNull("Tax info should not be null", taxInfo);
        assertEquals("Income should match", "50000", taxInfo.get("income"));
        assertEquals("Filing status should match", "Single", taxInfo.get("filing_status"));
        assertEquals("Above line deductions should match", "5000", taxInfo.get("above_line_deductions"));
        assertEquals("Itemized deductions should match", "12000", taxInfo.get("itemized_deductions"));
        assertEquals("Tax credits should match", "1000", taxInfo.get("tax_credits"));
    }

    @Test
    public void testDependentOperations() {
        // Add test user
        long userId = db.addUser("Dependent Test", "dependent@example.com", "password123", "1234567890");
        assertTrue("User should exist", db.userExists((int)userId));

        // Test adding first dependent
        int firstDependentId = db.addDependent("John", "Doe", "123-45-6789",
                "2000-01-01", "Child", (int)userId);
        assertTrue("First dependent ID should be valid", firstDependentId >= 0);

        // Test adding second dependent
        int secondDependentId = db.addDependent("Jane", "Doe", "987-65-4321",
                "2002-01-01", "Child", (int)userId);
        assertTrue("Second dependent ID should be greater than first",
                secondDependentId > firstDependentId);

        // Test getting next dependent ID
        int nextId = db.getNextDependentId((int)userId);
        assertEquals("Next ID should be one more than last added",
                secondDependentId + 1, nextId);
    }

    @Test
    public void testInvalidOperations() {
        // Test operations with invalid user ID
        long invalidUserId = 999999; // Use a large number unlikely to exist

        // Test tax operations
        assertFalse("Tax record update should fail for invalid user",
                db.updateFilingStatus(invalidUserId, "Single"));
        assertFalse("Income update should fail for invalid user",
                db.updateIncome(invalidUserId, "50000"));

        Map<String, String> taxInfo = db.getUserTaxInfo(invalidUserId);
        assertTrue("Tax info should be empty for invalid user", taxInfo.isEmpty());

        // Test dependent operations
        int dependentId = db.addDependent("Invalid", "User", "000-00-0000",
                "2000-01-01", "Child", (int)invalidUserId);
        assertEquals("Adding dependent should fail for invalid user", -1, dependentId);

        // Test getting next dependent ID for invalid user
        int nextDependentId = db.getNextDependentId((int)invalidUserId);
        assertEquals("Getting next dependent ID should fail for invalid user", -1, nextDependentId);

        // Test user existence
        assertFalse("User should not exist", db.userExists((int)invalidUserId));
    }

    @Test
    public void testDatabaseInitialization() {
        assertTrue("Database should be initialized", db.isDatabaseInitialized());
    }
}