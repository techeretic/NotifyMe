
package shetye.prathamesh.notifyme.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import shetye.prathamesh.notifyme.Utilities;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String LOG_TAG = "DatabaseHelper";

    private static final int DATABASE_VERSION = 2;

    // Database Name
    private static final String DATABASE_NAME = "MyNotifs";

    // Contacts table name
    private static final String MYNOTIF = "MyNotif";

    // Contacts Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_TITLE = "notifTitle";
    private static final String KEY_CONTENT = "notifContent";
    private static final String KEY_DATE = "notifWhen";
    private static final String KEY_REPEAT = "notifDoRepeat";
    private static final String KEY_COMPLETE = "notifIsComplete";
    private static final String KEY_ONGOING = "notifIsOngoing";
    private static final String KEY_DRIVEID = "notifDriveID";
    private static final String KEY_STATUS = "notifState";

    private static int notes;

    private static DatabaseHelper mInstance = null;

    public static DatabaseHelper getInstance(Context ctx) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (mInstance == null) {
            Log.d(LOG_TAG, "Creating new Instance");
            mInstance = new DatabaseHelper(ctx.getApplicationContext());
        }
        return mInstance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public DatabaseHelper(Context context, String name, CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_NOTIFICATIONS_TABLE =
            "CREATE TABLE " + MYNOTIF + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_TITLE + " TEXT, "
                + KEY_CONTENT + " TEXT, "
                + KEY_DATE + " INTEGER, "
                + KEY_REPEAT + " INTEGER, "
                + KEY_COMPLETE + " INTEGER, "
                + KEY_ONGOING + " INTEGER, "
                + KEY_DRIVEID + " TEXT, "
                + KEY_STATUS + " INTEGER"
                + ")";
        db.execSQL(CREATE_NOTIFICATIONS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(LOG_TAG, "In onUpgrade -> oldVersion = " + oldVersion + " newVersion = " + newVersion);
        if (oldVersion < newVersion) {
            if (newVersion == 2) {
                db.execSQL("ALTER TABLE " + MYNOTIF + " ADD COLUMN " + KEY_DRIVEID + " TEXT");
                db.execSQL("ALTER TABLE " + MYNOTIF + " ADD COLUMN " + KEY_STATUS + " INTEGER");
                ContentValues values = new ContentValues();
                values.put(KEY_DRIVEID,"");
                values.put(KEY_STATUS,1);
                db.update(MYNOTIF, values, "", null);
            }
        }
    }

    // Adding new contact
    public void addNotif(Notif note) {
        SQLiteDatabase db = this.getWritableDatabase();

        if(ifExists(note.get_id())) {
            updateNote(note);
            return;
        }

        ContentValues values = new ContentValues();
        values.put(KEY_CONTENT, note.getNotification_content()); // Note
        values.put(KEY_TITLE, note.getNotification_title()); // Title
        values.put(KEY_ID, note.get_id()); // ID
        values.put(KEY_DATE, note.getNotification_when()); // Date
        values.put(KEY_REPEAT, note.isRepeat()); // Is Repeating
        values.put(KEY_COMPLETE, note.isComplete()); // Is Completed
        values.put(KEY_ONGOING, note.isOngoing()); // Is Ongoing
        values.put(KEY_DRIVEID, note.getDriveID()); // DriveID
        values.put(KEY_STATUS, note.getState().getValue()); // DriveID

        // Inserting Row
        db.insert(MYNOTIF, null, values);
        db.close(); // Closing database connection
    }

    // Getting single note
    public Notif getNote(long _id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(MYNOTIF, new String[]{
            KEY_ID,
            KEY_TITLE,
            KEY_CONTENT,
            KEY_DATE,
            KEY_REPEAT,
            KEY_COMPLETE,
            KEY_ONGOING,
            KEY_DRIVEID,
            KEY_STATUS
        }, KEY_ID + "=?", new String[]{
                String.valueOf(_id)
        }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        Notif note = new Notif(
            Integer.parseInt(cursor.getString(0)),
            cursor.getString(1),
            cursor.getString(2),
            Long.parseLong(cursor.getString(3)),
            Integer.parseInt(cursor.getString(4)) == 1,
            Integer.parseInt(cursor.getString(5)) == 1,
            Integer.parseInt(cursor.getString(6)) == 1
        );
        note.setDriveID(cursor.getString(7));
        note.setState(Utilities.states.getState(Integer.parseInt(cursor.getString(8))));
        cursor.close();
        return note;
    }

    // Getting All Notifications
    public List<Notif> getAllNotifications() {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Notif> noteList = new ArrayList<Notif>();
        String selectQuery = "SELECT  * FROM " + MYNOTIF
                //+ " WHERE " + KEY_COMPLETE + " = 0"
                + " ORDER BY " + KEY_DATE + " DESC";

        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Notif note = new Notif(
                        Integer.parseInt(cursor.getString(0)),
                        cursor.getString(1),
                        cursor.getString(2),
                        Long.parseLong(cursor.getString(3)),
                        Integer.parseInt(cursor.getString(4)) == 1,
                        Integer.parseInt(cursor.getString(5)) == 1,
                        Integer.parseInt(cursor.getString(6)) == 1
                );
                note.setDriveID(cursor.getString(7));
                note.setState(Utilities.states.getState(Integer.parseInt(cursor.getString(8))));
                // Adding contact to list
                noteList.add(note);
            } while (cursor.moveToNext());
        }

        // return contact list
        return noteList;
    }

    // Getting contacts Count
    public int getNotesCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + MYNOTIF;

        Cursor cursor = db.rawQuery(selectQuery, null);

        return cursor.getCount();
    }

    // Updating single contact
    public int updateNote(Notif note) {
        SQLiteDatabase db = this.getWritableDatabase();

        Log.d("NotifyMe","IN UpdateNotif = " + note.isComplete());

        ContentValues values = new ContentValues();
        values.put(KEY_CONTENT, note.getNotification_content()); // Note
        values.put(KEY_TITLE, note.getNotification_title()); // Title
        values.put(KEY_ID, note.get_id()); // ID
        values.put(KEY_DATE, note.getNotification_when()); // Date
        values.put(KEY_REPEAT, note.isRepeat()); // Is Repeating
        values.put(KEY_COMPLETE, note.isComplete()); // Is Completed
        values.put(KEY_ONGOING, note.isOngoing()); // Is Ongoing
        values.put(KEY_DRIVEID, note.getDriveID()); // DriveID
        values.put(KEY_STATUS, note.getState().getValue()); // State

        // updating row
        return db.update(MYNOTIF, values, KEY_ID + " = ?", new String[]{
                String.valueOf(note.get_id())
        });
    }

    // Deleting single contact
    public void deleteNote(Notif note) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(MYNOTIF, KEY_ID + " = ?", new String[]{
                String.valueOf(note.get_id())
        });
        if (note.get_id() != 0 && note.get_id() < notes) {

        }
        db.close();
    }

    public int getNewId() {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT  * FROM " + MYNOTIF + " ORDER BY " + KEY_DATE + " DESC";

        Cursor cursor = db.rawQuery(selectQuery, null);

        return cursor.getCount() + 1;
    }

    public boolean ifExists(int ID) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + MYNOTIF + " WHERE " + KEY_ID + " = " + Integer.toString(ID);

        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.getCount() > 0)
            return true;
        else
            return false;
    }

    public int markComplete(int ID) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_COMPLETE, true); // Is Completed

        // updating row
        return db.update(MYNOTIF, values, KEY_ID + " = ?", new String[]{
            String.valueOf(ID)
        });
    }

    public int markIncomplete(int ID) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_COMPLETE, false); // Is Completed

        // updating row
        return db.update(MYNOTIF, values, KEY_ID + " = ?", new String[]{
                String.valueOf(ID)
        });
    }

    //Searching notes based on note content
    public List<Notif> searchNotes(String query) {
        String q = query.replaceAll("'", "''");
        SQLiteDatabase db = this.getReadableDatabase();
        List<Notif> noteList = new ArrayList<Notif>();
        String selectQuery = "SELECT  "
                + KEY_ID + ","
                + KEY_TITLE + ","
                + KEY_CONTENT + ","
                + KEY_DATE + ","
                + KEY_REPEAT + ","
                + KEY_COMPLETE + ","
                + KEY_ONGOING + ","
                + KEY_DRIVEID + ","
                + KEY_STATUS
                + " FROM " + MYNOTIF
                + " WHERE UPPER(" + KEY_CONTENT + ") LIKE UPPER('%" + q + "%') "
                + " OR UPPER(" + KEY_TITLE + ") LIKE UPPER('%" + q + "%') "
                + "ORDER BY " + KEY_DATE + " DESC";

        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Notif note = new Notif(
                        Integer.parseInt(cursor.getString(0)),
                        cursor.getString(1),
                        cursor.getString(2),
                        Long.parseLong(cursor.getString(3)),
                        Integer.parseInt(cursor.getString(4)) == 1,
                        Integer.parseInt(cursor.getString(5)) == 1,
                        Integer.parseInt(cursor.getString(6)) == 1
                );
                note.setDriveID(cursor.getString(7));
                note.setState(Utilities.states.getState(Integer.parseInt(cursor.getString(8))));
                // Adding contact to list
                noteList.add(note);
            } while (cursor.moveToNext());
        }

        Log.d(LOG_TAG, "noteList,SIZE = " + noteList.size());
        // return contact list
        return noteList;
    }

    // Getting All Notifications
    public HashMap<Integer, Notif> getAllNotificationsInHash() {
        SQLiteDatabase db = this.getReadableDatabase();
        HashMap<Integer, Notif> notifs = new HashMap<>();
        String selectQuery = "SELECT  * FROM " + MYNOTIF
                //+ " WHERE " + KEY_COMPLETE + " = 0"
                + " ORDER BY " + KEY_DATE + " DESC";

        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Notif note = new Notif(
                        Integer.parseInt(cursor.getString(0)),
                        cursor.getString(1),
                        cursor.getString(2),
                        Long.parseLong(cursor.getString(3)),
                        Integer.parseInt(cursor.getString(4)) == 1,
                        Integer.parseInt(cursor.getString(5)) == 1,
                        Integer.parseInt(cursor.getString(6)) == 1
                );
                note.setDriveID(cursor.getString(7));
                note.setState(Utilities.states.getState(Integer.parseInt(cursor.getString(8))));
                // Adding contact to list
                notifs.put(note.get_id(),note);
            } while (cursor.moveToNext());
        }

        // return contact list
        return notifs;
    }

    public void handleNote(Notif note, HashMap<Integer, Notif> notifs) {
        SQLiteDatabase db = this.getReadableDatabase();
        String title = note.getNotification_title().replaceAll("'","''");
        String content = note.getNotification_content().replaceAll("'","''");
        List<Notif> noteList = new ArrayList<Notif>();
        String selectQuery = "SELECT  "
                + KEY_ID
                + " FROM " + MYNOTIF
                + " WHERE " + KEY_CONTENT + " = '" + content + "' "
                + " AND " + KEY_TITLE + " = '" + title + "'"
                + " ORDER BY " + KEY_ID + " DESC";

        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.getCount() == 0) {
            note.set_id(getNewId());
            note.setState(Utilities.states.SYNCED);
            addNotif(note);
            return;
        }
        if (cursor.moveToFirst()) {
            do {
                notifs.remove(Integer.parseInt(cursor.getString(0)));
            } while (cursor.moveToNext());
        }
    }
}
