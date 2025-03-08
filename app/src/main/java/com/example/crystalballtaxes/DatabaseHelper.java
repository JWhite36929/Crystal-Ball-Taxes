package com.example.crystalballtaxes;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.database.Cursor;


import java.util.HashMap;
import java.util.Map;

/*
*
* User table will have the following columns:
* user_id(primary key), user_name, user_email, user_password(may use a hash), user_phone
*
* Tax table will have the following columns:
* tax_info_id(primary key), user_id (foreign key), filing_status, income
*
*dependent table will have the following columns:
* user_id (foreign key), dependent_id(primary key), dependent_first_name, dependent_last_name, dependent_ssn, dependent_dob, dependent_relation
*
* */
public class DatabaseHelper extends SQLiteOpenHelper{

    //setting up for the databaseHelper constructor
    private static final String TAG = "DatabaseHelper";
    static final String DATABASE_NAME = "crystalBallTaxes.db";
    static final int DATABASE_VERSION = 7;

    //initialize table names
    private static final String USER_TABLE = "USERS";
    private static final String TAX_INFO_TABLE = "TAX_INFO";
    private static final String DEPENDENT_INFO_TABLE = "DEPENDENTS";

    //initialize id column since both tables use it
    private static final String KEY_ID = "ID";

    //initialize user table column names
    private static final String USER_NAME = "NAME";
    private static final String USER_EMAIL = "EMAIL";
    private static final String USER_PASSWORD = "PASSWORD";
    private static final String USER_PHONE = "PHONE";

    //initialize tax table column names
    private static final String TAX_INFO_ID = "TAX_INFO_ID";
    private static final String USER_ID = "USER_ID";
    private static final String FILING_STATUS = "FILING_STATUS";
    private static final String INCOME = "INCOME";
    private static final String TAX_CREDITS = "TAX_CREDITS";
    private static final String ABOVE_LINE_DEDUCTIONS = "ABOVE_LINE_DEDUCTIONS";
    private static final String ITEMIZED_DEDUCTIONS = "ITEMIZED_DEDUCTIONS";


    //initialize dependent table column names
    private static final String DEPENDENT_ID = "DEPENDENT_ID";
    private static final String DEPENDENT_FNAME = "DEPENDENT_FIRST_NAME";
    private static final String DEPENDENT_LNAME = "DEPENDENT_LAST_NAME";
    private static final String DEPENDENT_SSN = "DEPENDENT_SSN";
    private static final String DEPENDENT_DOB = "DEPENDENT_DOB";
    private static final String DEPENDENT_RELATION = "DEPENDENT_RELATION";

    //string query for creating the user table
    private static final String CREATE_USER_TABLE = "CREATE TABLE " + USER_TABLE + "(" +
            KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            USER_NAME + " TEXT, " +
            USER_EMAIL + " TEXT, " +
            USER_PASSWORD + " TEXT, " +
            USER_PHONE + " TEXT);";

    //string query for creating the tax table
    //uses foreign key to link the two tables together
    private static final String CREATE_TABLE_TAX_INFO = "CREATE TABLE " + TAX_INFO_TABLE + "(" +
            TAX_INFO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            USER_ID + " INTEGER," +
            INCOME + " TEXT NOT NULL," +
            FILING_STATUS + " TEXT NOT NULL," +
            TAX_CREDITS + " TEXT," +
            ABOVE_LINE_DEDUCTIONS + " TEXT," +
            ITEMIZED_DEDUCTIONS + " TEXT," +
            "FOREIGN KEY (" + USER_ID + ") REFERENCES " + USER_TABLE + "(" + KEY_ID + ")" +
            ")";

    //create dependent table
    private static final String CREATE_TABLE_DEPENDENTS = "CREATE TABLE " + DEPENDENT_INFO_TABLE + "("
            + USER_ID + " INTEGER,"
            + DEPENDENT_ID + " INTEGER NOT NULL,"
            + DEPENDENT_FNAME + " TEXT,"
            + DEPENDENT_LNAME + " TEXT,"
            + DEPENDENT_SSN + " TEXT,"
            + DEPENDENT_DOB + " TEXT,"
            + DEPENDENT_RELATION + " TEXT,"
            + "PRIMARY KEY (" + DEPENDENT_ID + "," + USER_ID + ")," //composite primary key to ensure unique dependent id
            + "FOREIGN KEY (" + USER_ID + ") REFERENCES " + USER_TABLE + "(" + KEY_ID + ")" +
            ")";

    //constructor
    public DatabaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //create tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USER_TABLE);
        db.execSQL(CREATE_TABLE_TAX_INFO);
        db.execSQL(CREATE_TABLE_DEPENDENTS);
    }
    public boolean userExists(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(USER_TABLE,
                new String[]{KEY_ID},
                KEY_ID + "=?",
                new String[]{String.valueOf(userId)},
                null, null, null);

        boolean exists = cursor != null && cursor.getCount() > 0;
        if (cursor != null) {
            cursor.close();
        }
        return exists;
    }


    //onupgrade function to wipe tables when version updates
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + USER_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + TAX_INFO_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + DEPENDENT_INFO_TABLE);
        onCreate(db);
    }

    //add user to the database
    //modify addUser to prevent duplicate emails regardless of case
    public long addUser(String name, String email, String password, String phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        long id;

        db.beginTransaction();
        try {
            // check for existing user with case insensitive email comparison
            String query = "SELECT " + KEY_ID +
                    " FROM " + USER_TABLE +
                    " WHERE LOWER(" + USER_EMAIL + ") = LOWER(?)";

            Cursor cursor = db.rawQuery(query, new String[]{email});

            if (cursor != null && cursor.getCount() > 0) {
                Log.e(TAG, "User with email " + email + " already exists (case-insensitive match)");
                cursor.close();
                return -1;
            }
            if (cursor != null) {
                cursor.close();
            }

            ContentValues values = new ContentValues();
            values.put(USER_NAME, name);
            values.put(USER_EMAIL, email); // store original email case
            values.put(USER_PASSWORD, password);
            values.put(USER_PHONE, phone);

            id = db.insert(USER_TABLE, null, values);

            if (id != -1) {
                // verify the insertion
                Cursor verificationCursor = db.query(USER_TABLE,
                        null,
                        KEY_ID + "=?",
                        new String[]{String.valueOf(id)},
                        null, null, null);

                if (verificationCursor != null && verificationCursor.moveToFirst()) {
                    @SuppressLint("Range")
                    String verifyEmail = verificationCursor.getString(verificationCursor.getColumnIndex(USER_EMAIL));
                    Log.d(TAG, "Successfully added user: " + id + " with email: " + verifyEmail);
                    verificationCursor.close();
                    db.setTransactionSuccessful();
                }
            } else {
                Log.e(TAG, "Failed to add user to database");
            }

        } catch (Exception e) {
            Log.e(TAG, "Error adding user: " + e.getMessage());
            e.printStackTrace();
            return -1;
        } finally {
            db.endTransaction();
        }

        return id;
    }

    //was previously assigning -1 to userID
    //now is case insensitive when geting user id from email
    //along with better logging
    @SuppressLint("Range")
    public long getUserIdFromEmail(String userEmail) {
        SQLiteDatabase database = this.getReadableDatabase();
        long userId = -1;

        Log.d(TAG, "Attempting to get user ID for email: " + userEmail);

        try {
            // use LOWER() function in SQLite to make case-insensitive comparison
            String query = "SELECT " + KEY_ID + ", " + USER_EMAIL +
                    " FROM " + USER_TABLE +
                    " WHERE LOWER(" + USER_EMAIL + ") = LOWER(?)";

            Cursor cursor = database.rawQuery(query, new String[]{userEmail});

            if (cursor != null && cursor.moveToFirst()) {
                userId = cursor.getLong(cursor.getColumnIndex(KEY_ID));
                String foundEmail = cursor.getString(cursor.getColumnIndex(USER_EMAIL));
                Log.d(TAG, "Found user ID: " + userId + " for email: " + foundEmail);
                cursor.close();
            } else {
                Log.e(TAG, "No user found for email: " + userEmail);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error querying user: " + e.getMessage());
            e.printStackTrace();
        }

        return userId;
    }

    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        boolean isValid = false;

        try {
            // use LOWER() function again
            String query = "SELECT " + USER_EMAIL + ", " + USER_PASSWORD +
                    " FROM " + USER_TABLE +
                    " WHERE LOWER(" + USER_EMAIL + ") = LOWER(?)";

            Cursor cursor = db.rawQuery(query, new String[]{email});

            if (cursor != null && cursor.moveToFirst()) {
                @SuppressLint("Range")
                String storedPassword = cursor.getString(cursor.getColumnIndex(USER_PASSWORD));


                // password comparison remains case-sensitive
                if (storedPassword.equals(password)) {
                    isValid = true;
                    Log.d(TAG, "User authenticated successfully for email: " + email);
                } else {
                    Log.d(TAG, "Password mismatch for email: " + email);
                }
                cursor.close();
            } else {
                Log.d(TAG, "No user found with email: " + email);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking user: " + e.getMessage());
            e.printStackTrace();
        }

        return isValid;
    }

    //helper method to check if the database is properly initialized
    public boolean isDatabaseInitialized() {
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name=?",
                    new String[]{USER_TABLE});
            boolean exists = false;
            if (cursor != null) {
                exists = cursor.getCount() > 0;
                cursor.close();
            }
            return exists;
        } catch (Exception e) {
            Log.e(TAG, "Error checking database initialization: " + e.getMessage());
            return false;
        }
    }

    //tax table operations

    //initalize the record but forgot to check if it existed initally last time
    public long initializeTaxRecord(long userId) {
        //the check
        if (taxRecordExists(userId)) {
            Log.d(TAG, "Tax record already exists for user: " + userId);
            return -1;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(USER_ID, userId);
        values.put(INCOME, "0");
        values.put(FILING_STATUS, "");
        values.put(TAX_CREDITS, "0");
        values.put(ABOVE_LINE_DEDUCTIONS, "0");
        values.put(ITEMIZED_DEDUCTIONS, "0");

        long id = db.insert(TAX_INFO_TABLE, null, values);
        Log.d(TAG, "Initialized tax record: " + id + " for user: " + userId);
        return id;
    }


    //rewrote to work off tax info id instead of user id when inserting
    public boolean taxRecordExists(long userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TAX_INFO_TABLE,
                new String[]{TAX_INFO_ID},
                USER_ID + "=?",
                new String[]{String.valueOf(userId)},
                null, null, null);

        boolean exists = cursor != null && cursor.getCount() > 0;
        if (cursor != null) {
            cursor.close();
        }

        Log.d(TAG, "Tax record exists for user " + userId + ": " + exists);
        return exists;
    }

    //rewrote to handle errors better

    @SuppressLint("Range")
    public boolean updateFilingStatus(long userId, String filingStatus) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(FILING_STATUS, filingStatus);

        // check if tax record exits
        Cursor cursor = db.query(TAX_INFO_TABLE,
                new String[]{TAX_INFO_ID},
                USER_ID + "=?",
                new String[]{String.valueOf(userId)},
                null, null, null);

        boolean success = false;

        if (cursor != null && cursor.moveToFirst()) {
            // get the tax_info_id for this user
            long taxInfoId = cursor.getLong(cursor.getColumnIndex(TAX_INFO_ID));

            // update using tax_info_id instead of user_id
            int rowsAffected = db.update(TAX_INFO_TABLE,
                    values,
                    TAX_INFO_ID + "=?",
                    new String[]{String.valueOf(taxInfoId)});

            success = rowsAffected > 0;

            Log.d(TAG, "Attempted to update filing status for user " + userId +
                    " (Tax Info ID: " + taxInfoId + "): " + filingStatus +
                    " - Success: " + success);

            //check to see if it was updated
            Cursor verificationCursor = db.query(TAX_INFO_TABLE,
                    new String[]{FILING_STATUS},
                    TAX_INFO_ID + "=?",
                    new String[]{String.valueOf(taxInfoId)},
                    null, null, null);

            if (verificationCursor != null && verificationCursor.moveToFirst()) {
                String updatedStatus = verificationCursor.getString(verificationCursor.getColumnIndex(FILING_STATUS));
                Log.d(TAG, "Verified filing status after update: " + updatedStatus);
                verificationCursor.close();
            }

            cursor.close();
        } else {
            Log.e(TAG, "No tax record found for user ID: " + userId);
        }

        return success;
    }

    public boolean updateIncome(long userId, String income) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(INCOME, income);

        int rowsAffected = db.update(TAX_INFO_TABLE,
                values,
                USER_ID + "=?",
                new String[]{String.valueOf(userId)});

        Log.d(TAG, "Updated income for user " + userId + ": " + income);
        return rowsAffected > 0;
    }

    public boolean updateAboveLineDeductions(long userId, String deductions) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ABOVE_LINE_DEDUCTIONS, deductions);

        int rowsAffected = db.update(TAX_INFO_TABLE,
                values,
                USER_ID + "=?",
                new String[]{String.valueOf(userId)});

        Log.d(TAG, "Updated above-line deductions for user " + userId + ": " + deductions);
        return rowsAffected > 0;
    }

    public boolean updateItemizedDeductions(long userId, String deductions) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ITEMIZED_DEDUCTIONS, deductions);

        int rowsAffected = db.update(TAX_INFO_TABLE,
                values,
                USER_ID + "=?",
                new String[]{String.valueOf(userId)});

        Log.d(TAG, "Updated itemized deductions for user " + userId + ": " + deductions);
        return rowsAffected > 0;
    }

    public boolean updateTaxCredits(long userId, int credits) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TAX_CREDITS, String.valueOf(credits));

        int rowsAffected = db.update(TAX_INFO_TABLE,
                values,
                USER_ID + "=?",
                new String[]{String.valueOf(userId)});

        Log.d(TAG, "Updated tax credits for user " + userId + ": " + credits);
        return rowsAffected > 0;
    }


    //standardizing get methods to return a data structure instead of logging to tag
    @SuppressLint("Range")
    public Map<String, String> getUserTaxInfo(long userId) {
        Map<String, String> taxInfo = new HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(
                    TAX_INFO_TABLE,
                    new String[]{INCOME, ABOVE_LINE_DEDUCTIONS, ITEMIZED_DEDUCTIONS, TAX_CREDITS, FILING_STATUS},
                    USER_ID + " = ?",
                    new String[]{String.valueOf(userId)},
                    null, null, null
            );

            if (cursor != null && cursor.moveToFirst()) {
                taxInfo.put("income", cursor.getString(cursor.getColumnIndex(INCOME)));
                taxInfo.put("above_line_deductions", cursor.getString(cursor.getColumnIndex(ABOVE_LINE_DEDUCTIONS)));
                taxInfo.put("itemized_deductions", cursor.getString(cursor.getColumnIndex(ITEMIZED_DEDUCTIONS)));
                taxInfo.put("tax_credits", cursor.getString(cursor.getColumnIndex(TAX_CREDITS)));
                taxInfo.put("filing_status", cursor.getString(cursor.getColumnIndex(FILING_STATUS)));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting tax info: " + e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return taxInfo;
    }

    @Override
    public synchronized void close() {
        SQLiteDatabase db = getWritableDatabase();
        if (db != null) {
            db.close();
        }
        super.close();
    }

    /* This will allow the primary key of dependents to find the next available int for the dependents using the user id */
    public int getNextDependentId(int userId) {
        SQLiteDatabase db = getReadableDatabase();

        if (!userExists(userId)) {
            Log.e(TAG, "Cannot get next dependent ID: User ID " + userId + " does not exist");
            return -1;
        }

        Cursor cursor = db.rawQuery("SELECT MAX(dependent_id) FROM dependents WHERE user_id = ?", new String[]{String.valueOf(userId)});
        int nextId = 0;
        if (cursor.moveToFirst() && !cursor.isNull(0)) {
            nextId = cursor.getInt(0) + 1;
        }
        cursor.close();
        return nextId;
    }

    public int addDependent(String firstName, String lastName, String ssn, String dob, String relation, int userId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();

        try {
            if (!userExists(userId)) {
                Log.e(TAG, "Cannot add dependent: User ID " + userId + " does not exist");
                return -1;
            }
            // get the next available dependent id for this user
            int dependentId = getNextDependentId(userId);

            // populate the values for insertion
            values.put(USER_ID, userId);
            values.put(DEPENDENT_ID, dependentId);
            values.put(DEPENDENT_FNAME, firstName);
            values.put(DEPENDENT_LNAME, lastName);
            values.put(DEPENDENT_SSN, ssn);
            values.put(DEPENDENT_DOB, dob);
            values.put(DEPENDENT_RELATION, relation);

            // insert the dependent record
            long result = db.insert(DEPENDENT_INFO_TABLE, null, values);

            if (result == -1) {
                Log.e(TAG, "Failed to add dependent for user: " + userId);
                return -1;
            }

            Log.d(TAG, "Successfully added dependent " + dependentId + " for user " + userId);
            return dependentId;

        } catch (Exception e) {
            Log.e(TAG, "Error adding dependent: " + e.getMessage());
            return -1;
        }
    }


}
