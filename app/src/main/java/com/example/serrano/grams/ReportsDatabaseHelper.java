package com.example.serrano.grams;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by MholzSerrano on 11/12/2016.
 */
public class ReportsDatabaseHelper extends SQLiteOpenHelper{
    private static ReportsDatabaseHelper sInstance;

    // Database Info
    private static final String DATABASE_NAME = "postsDatabase";
    private static final int DATABASE_VERSION = 1;
    private static final String TAG = "aven";

    // Table Names
    private static final String TABLE_REPORTS = "reports";
    private static final String TABLE_USERS = "users";

    // Report Table Columns
    private static final String KEY_REPORT_ID = "id";
    private static final String KEY_REPORT_USER_ID = "user_id";
    private static final String KEY_REPORT_CONTENT = "content";
    private static final String KEY_REPORT_IMAGE_URL = "image";
    private static final String KEY_REPORT_CHECKPOINT = "checkpoint_id";
    private static final String KEY_REPORT_TIMESTAMP = "timestamp";

    // User Table Columns
    private static final String KEY_USER_ID = "id";
    private static final String KEY_USER_REAL_ID = "user_id";
    private static final String KEY_USER_FIRSTNAME = "firstname";
    private static final String KEY_USER_LASTNAME = "lastname";
    private static final String KEY_USER_MIDDLENAME = "middlename";
    private static final String KEY_USER_BIRTHDATE = "birthdate";
    private static final String KEY_USER_ADDRESS = "address";
    private static final String KEY_USER_CONTACT = "contact";
    private static final String KEY_USER_PROFILE_URL = "image";

    private ReportsDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized ReportsDatabaseHelper getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new ReportsDatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    // Called when the database connection is being configured.
    // Configure database settings for things like foreign key support, write-ahead logging, etc.
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    // Called when the database is created for the FIRST time.
    // If a database already exists on disk with the same DATABASE_NAME, this method will NOT be called.
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_REPORTS_TABLE = "CREATE TABLE " + TABLE_REPORTS +
                "(" +
                KEY_REPORT_ID + " INTEGER PRIMARY KEY," + // Define a primary key
                //KEY_REPORT_USER_ID_FK + " INTEGER REFERENCES " + TABLE_USERS + "," + // Define a foreign key
                KEY_REPORT_USER_ID + " INTEGER," + // Define a foreign key
                KEY_REPORT_CONTENT + " TEXT," +
                KEY_REPORT_IMAGE_URL + " TEXT," +
                KEY_REPORT_CHECKPOINT + " TEXT" +
                KEY_REPORT_TIMESTAMP + " TEXT" +
                ")";

        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS +
                "(" +
                KEY_USER_ID             + " INTEGER PRIMARY KEY," +
                KEY_USER_REAL_ID        + " INTEGER," +
                KEY_USER_FIRSTNAME      + " TEXT," +
                KEY_USER_MIDDLENAME     + " TEXT," +
                KEY_USER_LASTNAME       + " TEXT," +
                KEY_USER_ADDRESS        + " TEXT," +
                KEY_USER_BIRTHDATE      + " TEXT," +
                KEY_USER_CONTACT        + " TEXT," +
                KEY_USER_PROFILE_URL    + " TEXT" +
                ")";

        db.execSQL(CREATE_REPORTS_TABLE);
        db.execSQL(CREATE_USERS_TABLE);
    }

    // Called when the database needs to be upgraded.
    // This method will only be called if a database already exists on disk with the same DATABASE_NAME,
    // but the DATABASE_VERSION is different than the version of the database that exists on disk.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            // Simplest implementation is to drop all old tables and recreate them
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_REPORTS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
            onCreate(db);
        }
    }


    //CRUD
    public void addReport(Report report) {
        // Create and/or open the database for writing
        SQLiteDatabase db = getWritableDatabase();

        // It's a good idea to wrap our insert in a transaction. This helps with performance and ensures
        // consistency of the database.
        db.beginTransaction();
        try {
            // The user might already exist in the database (i.e. the same user created multiple posts).
            // String userId = getUserId(report.user);
            if(report.image.equals("null")){
                report.image = "default.jpg";
            }
            ContentValues values = new ContentValues();
            values.put(KEY_REPORT_USER_ID, report.user);
            values.put(KEY_REPORT_CONTENT, report.content);
            values.put(KEY_REPORT_CHECKPOINT, report.checkpoint_id);
            values.put(KEY_REPORT_IMAGE_URL, report.image);

            // Notice how we haven't specified the primary key. SQLite auto increments the primary key column.
            db.insertOrThrow(TABLE_REPORTS, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to add post to database");
        } finally {
            db.endTransaction();
        }
    }

    public String getUserId(String user_id) {
        // The database connection is cached so it's not expensive to call getWriteableDatabase() multiple times.
        SQLiteDatabase db = getWritableDatabase();
        long userId = -1;
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_USER_REAL_ID, user_id);

            // First try to update the user in case the user already exists in the database
            // This assumes userNames are unique
            int rows = db.update(TABLE_USERS, values, KEY_USER_REAL_ID + "= ?", new String[]{user_id});

            // Check if update succeeded
            if (rows == 1) {
                // Get the primary key of the user we just updated
                String usersSelectQuery = String.format("SELECT %s FROM %s WHERE %s = ?",
                        KEY_USER_ID, TABLE_USERS, KEY_USER_REAL_ID);
                Cursor cursor = db.rawQuery(usersSelectQuery, new String[]{user_id});
                try {
                    if (cursor.moveToFirst()) {
                        userId = cursor.getInt(cursor.getColumnIndex(KEY_USER_ID));
                        db.setTransactionSuccessful();
                    }
                } finally {
                    if (cursor != null && !cursor.isClosed()) {
                        cursor.close();
                    }
                }
            } else {
                // user with this userName did not already exist, so insert new user
                // userId = db.insertOrThrow(TABLE_USERS, null, values);
                db.setTransactionSuccessful();
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to add or update user");
        } finally {
            db.endTransaction();
        }
        return String.valueOf(userId);
    }
    // Insert or update a user in the database
    // Since SQLite doesn't support "upsert" we need to fall back on an attempt to UPDATE (in case the
    // user already exists) optionally followed by an INSERT (in case the user does not already exist).
    // Unfortunately, there is a bug with the insertOnConflict method
    // (https://code.google.com/p/android/issues/detail?id=13045) so we need to fall back to the more
    // verbose option of querying for the user's primary key if we did an update.
    public long addOrUpdateUser(User user) {
        // The database connection is cached so it's not expensive to call getWriteableDatabase() multiple times.
        SQLiteDatabase db = getWritableDatabase();
        long userId = -1;

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_USER_REAL_ID, user.user_id);
            values.put(KEY_USER_FIRSTNAME, user.firstname);
            values.put(KEY_USER_LASTNAME, user.lastname);
            values.put(KEY_USER_MIDDLENAME, user.middlename);
            values.put(KEY_USER_ADDRESS, user.address);
            values.put(KEY_USER_CONTACT, user.contact);
            values.put(KEY_USER_PROFILE_URL, user.image);
            values.put(KEY_USER_BIRTHDATE, user.birthdate);

            // First try to update the user in case the user already exists in the database
            // This assumes userNames are unique
            int rows = db.update(TABLE_USERS, values, KEY_USER_REAL_ID + "= ?", new String[]{user.user_id});

            // Check if update succeeded
            if (rows == 1) {
                // Get the primary key of the user we just updated
                String usersSelectQuery = String.format("SELECT %s FROM %s WHERE %s = ?",
                        KEY_USER_ID, TABLE_USERS, KEY_USER_REAL_ID);
                Cursor cursor = db.rawQuery(usersSelectQuery, new String[]{user.user_id});
                try {
                    if (cursor.moveToFirst()) {
                        userId = cursor.getInt(cursor.getColumnIndex(KEY_USER_ID));
                        db.setTransactionSuccessful();
                    }
                } finally {
                    if (cursor != null && !cursor.isClosed()) {
                        cursor.close();
                    }
                }
            } else {
                // user with this userName did not already exist, so insert new user
                userId = db.insertOrThrow(TABLE_USERS, null, values);
                db.setTransactionSuccessful();
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to add or update user");
        } finally {
            db.endTransaction();
        }
        return userId;
    }

    public List<Report> getAllReports(String user_id) {
        List<Report> reports = new ArrayList<>();

        String REPORTS_SELECT_QUERY =
                String.format("SELECT * FROM %s WHERE %s = %s",
                        TABLE_REPORTS,
                        KEY_REPORT_USER_ID, user_id);

        // "getReadableDatabase()" and "getWriteableDatabase()" return the same object (except under low
        // disk space scenarios)
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(REPORTS_SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    Report newReport = new Report();
                    newReport.content = cursor.getString(cursor.getColumnIndex(KEY_REPORT_CONTENT));
                    newReport.checkpoint_id = cursor.getString(cursor.getColumnIndex(KEY_REPORT_CHECKPOINT));
                    newReport.image = cursor.getString(cursor.getColumnIndex(KEY_REPORT_IMAGE_URL));
                    newReport.id = cursor.getInt(0);
                    newReport.user = user_id;
                    reports.add(newReport);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get posts from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return reports;
    }

    /*public List<Report> getAllReports() {
        List<Report> reports = new ArrayList<>();

        // SELECT * FROM POSTS
        // LEFT OUTER JOIN USERS
        // ON POSTS.KEY_POST_USER_ID_FK = USERS.KEY_USER_ID
        String POSTS_SELECT_QUERY =
                String.format("SELECT * FROM %s LEFT OUTER JOIN %s ON %s.%s = %s.%s",
                        TABLE_REPORTS,
                        TABLE_USERS,
                        TABLE_REPORTS, KEY_REPORT_USER_ID,
                        TABLE_USERS, KEY_USER_ID);

        // "getReadableDatabase()" and "getWriteableDatabase()" return the same object (except under low
        // disk space scenarios)
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(POSTS_SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    User newUser = new User();
                    newUser.firstname = cursor.getString(cursor.getColumnIndex(KEY_USER_FIRSTNAME));
                    newUser.lastname = cursor.getString(cursor.getColumnIndex(KEY_USER_LASTNAME));
                    newUser.middlename = cursor.getString(cursor.getColumnIndex(KEY_USER_MIDDLENAME));
                    newUser.email = cursor.getString(cursor.getColumnIndex(KEY_USER_EMAIL));
                    newUser.address = cursor.getString(cursor.getColumnIndex(KEY_USER_ADDRESS));
                    newUser.contact = cursor.getString(cursor.getColumnIndex(KEY_USER_CONTACT));
                    newUser.birthdate = cursor.getString(cursor.getColumnIndex(KEY_USER_BIRTHDATE));
                    newUser.date_hired = cursor.getString(cursor.getColumnIndex(KEY_USER_DATEHIRED));
                    newUser.image = cursor.getString(cursor.getColumnIndex(KEY_USER_PROFILE_URL));
                    newUser.user_id = cursor.getString(cursor.getColumnIndex(KEY_USER_REAL_ID));
                    Report newPost = new Report();
                    newPost.content = cursor.getString(cursor.getColumnIndex(KEY_REPORT_CONTENT));
                    newPost.user = newUser;
                    reports.add(newPost);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get posts from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return reports;
    }*/

    // Update the user's profile picture url
    public int updateUserProfilePicture(User user) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_USER_PROFILE_URL, user.image);

        // Updating profile picture url for user with that userName
        return db.update(TABLE_USERS, values, KEY_USER_REAL_ID + " = ?",
                new String[] { String.valueOf(user.user_id) });
    }

    // Delete all posts and users in the database
    public void deleteReport(Report report) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            // Order of deletions is important when foreign key relationships exist.
            db.delete(TABLE_REPORTS, KEY_REPORT_ID + " = ?",
                    new String[] { String.valueOf(report.id) });
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to delete all posts and users");
        } finally {
            db.endTransaction();
        }
    }

}
