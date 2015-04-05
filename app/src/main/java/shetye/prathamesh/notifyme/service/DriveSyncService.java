package shetye.prathamesh.notifyme.service;

import android.app.IntentService;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import shetye.prathamesh.notifyme.R;
import shetye.prathamesh.notifyme.Utilities;
import shetye.prathamesh.notifyme.database.DatabaseHelper;
import shetye.prathamesh.notifyme.database.Notif;

public class DriveSyncService extends IntentService {

    private static final String LOG_TAG = "Notif-DriveSyncService";
    private Context mContext;
    private SharedPreferences mPrefs;
    private DriveId mFolderDriveId;

    public DriveSyncService() {
        super("DriveSyncService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mContext = this;
        mPrefs = getSharedPreferences(Utilities.SHARED_PREF_APP_DATA, MODE_PRIVATE);
        if (intent != null) {
            final String action = intent.getAction();
            if (action.equals(Utilities.SYNC_SERVICE_ACTION)) {
                Utilities.getInstance().showSyncNotification(mContext);
                // Perform actual syncing
                startSync();
            }
        }
    }

    private void startSync() {
        Query query = new Query.Builder()
                .addFilter(Filters.eq(SearchableField.MIME_TYPE, "application/vnd.google-apps.folder"))
                .addFilter(Filters.eq(SearchableField.TITLE, Utilities.DRIVE_DEFAULT_FOLDER_NAME))
                .build();
        Drive.DriveApi.query(Utilities.mGoogleApiClient, query)
                .setResultCallback(folderExistsCB);
    }

    private ResultCallback<DriveApi.MetadataBufferResult> folderExistsCB =
            new ResultCallback<DriveApi.MetadataBufferResult>() {
        @Override
        public void onResult(DriveApi.MetadataBufferResult result) {
            if (!result.getStatus().isSuccess() || result.getMetadataBuffer().getCount() == 0) {
                Log.e(LOG_TAG, "Problem while retrieving results | Folder Missing");
                createDefaultFolder();
                return;
            }
            Log.d(LOG_TAG, "FOLDER PRESENT! Saving DriveID");
            mPrefs.edit().putString(Utilities.SHARED_PREF_DRIVE_FOLDERID_KEY,
                    result.getMetadataBuffer().get(0).getDriveId().encodeToString()).apply();
            mPrefs.edit().putBoolean(Utilities.SHARED_PREF_DRIVE_CONNECTED_KEY, true).apply();
            startSyncTask();
        }
    };

    private ResultCallback<DriveFolder.DriveFolderResult> createFolderCB =
            new ResultCallback<DriveFolder.DriveFolderResult>() {
        @Override
        public void onResult(DriveFolder.DriveFolderResult result) {
            if (!result.getStatus().isSuccess()) {
                Log.e(LOG_TAG, "Error while trying to create the folder");
                return;
            }
            mPrefs.edit().putString(Utilities.SHARED_PREF_DRIVE_FOLDERID_KEY,
                    result.getDriveFolder().getDriveId().encodeToString()).apply();
            mPrefs.edit().putBoolean(Utilities.SHARED_PREF_DRIVE_CONNECTED_KEY, true).apply();
            startSyncTask();
        }
    };

    private int startSavingFiles() {
        List<Notif> mNotes = DatabaseHelper.getInstance(mContext).getAllNotifications();
        String driveID = mPrefs.getString(Utilities.SHARED_PREF_DRIVE_FOLDERID_KEY,"");

        Log.e(LOG_TAG, "DRIVE ID = " + driveID);
        if (driveID.isEmpty()) {
            Log.e(LOG_TAG, "Folder doesn't exist");
            return Utilities.FAILURE;
        }

        mFolderDriveId = DriveId.decodeFromString(driveID);
        for (Notif n : mNotes) {
            Log.d(LOG_TAG, "Now processing Notif : " + n.get_id());
            if (!n.getDriveID().isEmpty() && !n.getState().needsSync()) {
                Log.d(LOG_TAG, "Notif : " + n.get_id() + " already saved");
                continue;
            }

            // New Contents
            DriveApi.DriveContentsResult driveContentsResult =
                    Drive.DriveApi.newDriveContents(Utilities.mGoogleApiClient).await();
            if (!driveContentsResult.getStatus().isSuccess()) {
                Log.e(LOG_TAG, "Failed at creating New Drive Content");
                return Utilities.FAILURE;
            }

            // Writing the file
            DriveContents originalContents = driveContentsResult.getDriveContents();
            OutputStream os = originalContents.getOutputStream();
            try {
                os.write(n.toByteArray(mContext));
            } catch (IOException e) {
                Log.e(LOG_TAG, "Failed at Writing File");
                e.printStackTrace();
                return Utilities.FAILURE;
            }

            // Creating the said file in Folder
            DriveFolder folder = Drive.DriveApi.getFolder(Utilities.mGoogleApiClient, mFolderDriveId);
            MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                    .setTitle(String.valueOf(n.get_id())
                            + "_"
                            + Utilities.getInstance().getDateFromMS(n.getNotification_when())
                            + ".txt")
                    .setMimeType("text/plain")
                    .setStarred(true).build();
            DriveFolder.DriveFileResult fileResult = folder.createFile(Utilities.mGoogleApiClient,
                    changeSet, driveContentsResult.getDriveContents()).await();

            if (!fileResult.getStatus().isSuccess()) {
                Log.e(LOG_TAG, "Failed at Writing File TO DRIVE");
                return Utilities.FAILURE;
            }
            n.setState(Utilities.states.SYNCED);
            n.setDriveID(fileResult.getDriveFile().getDriveId().encodeToString());
            n.updateThisInDB(mContext);
        }
        return Utilities.SUCCESS;
    }

    private void createDefaultFolder() {
        Log.d(LOG_TAG, "Creating New Folder");
        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle(Utilities.DRIVE_DEFAULT_FOLDER_NAME).build();
        Drive.DriveApi.getRootFolder(Utilities.mGoogleApiClient).createFolder(
                Utilities.mGoogleApiClient, changeSet).setResultCallback(createFolderCB);
    }

    private void startSyncTask() {
        new SyncTask(mContext).execute();
    }

    private class SyncTask extends AsyncTask<Void, Void, Void> {
        private Context mContext;

        SyncTask(Context context) {
            mContext = context;
        }

        @Override
        protected Void doInBackground(Void... params) {
            startSavingFiles();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Utilities.getInstance().dismissSyncNotification(mContext);
            super.onPostExecute(result);
        }
    }
}
