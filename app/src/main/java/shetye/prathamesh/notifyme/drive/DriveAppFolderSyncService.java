package shetye.prathamesh.notifyme.drive;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.ResultReceiver;
import android.util.Log;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import shetye.prathamesh.notifyme.Utilities;
import shetye.prathamesh.notifyme.database.DatabaseHelper;
import shetye.prathamesh.notifyme.database.Notif;

public class DriveAppFolderSyncService extends IntentService {

    private static final String LOG_TAG = "AppFolderSyncService";
    private Context mContext;
    private SharedPreferences mPrefs;
    private HashMap<Integer, Notif> mNotifs;
    private MetadataBuffer mAppDataBuffer;
    private DriveFolder mFolder;
    private ResultReceiver mReceiver;

    public DriveAppFolderSyncService() {
        super("DriveAppFolderSyncService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mContext = this;
        if (intent != null) {
            final String action = intent.getAction();
            mReceiver = intent.getParcelableExtra(Utilities.NOTIF_SYNC_SERVICE_RECEIVER_KEY);
            if (action.equals(Utilities.APPDATA_SYNC_SERVICE_ACTION)) {
                mNotifs = DatabaseHelper.getInstance(mContext).getAllNotificationsInHash();
                // Sync!!
                startAppDataSync();
            }
        }
    }

    private void startAppDataSync() {
        Drive.DriveApi.newDriveContents(Utilities.mGoogleApiClient)
                .setResultCallback(driveContentsCallback);
    }

    final private ResultCallback<DriveApi.DriveContentsResult> driveContentsCallback =
            new ResultCallback<DriveApi.DriveContentsResult>() {
                @Override
                public void onResult(DriveApi.DriveContentsResult result) {
                    if (!result.getStatus().isSuccess()) {
                        Log.e(LOG_TAG, "Error while trying to create new file contents");
                        return;
                    }

                    mFolder = Drive.DriveApi.getAppFolder(Utilities.mGoogleApiClient);
                    if (mFolder != null) {
                        mFolder.listChildren(Utilities.mGoogleApiClient)
                                .setResultCallback(metadataResult);
                    } else {
                        Log.d(LOG_TAG, "mFolder is NULL!!!");
                    }
                }
            };

    final private ResultCallback<DriveApi.MetadataBufferResult> metadataResult = new
            ResultCallback<DriveApi.MetadataBufferResult>() {
                @Override
                public void onResult(DriveApi.MetadataBufferResult result) {
                    if (!result.getStatus().isSuccess() || result.getMetadataBuffer().getCount() == 0) {
                        Log.e(LOG_TAG, "Problem while retrieving files");
                        result.getMetadataBuffer().release();
                        return;
                    }
                    mAppDataBuffer = result.getMetadataBuffer();
                    result.getMetadataBuffer().release();
                    new SyncTask().execute();
                }
            };

    private class SyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            for (int i = 0; i < mAppDataBuffer.getCount(); i++) {
                processServerNotif(mAppDataBuffer.get(i).getDriveId());
            }
            for(Map.Entry<Integer, Notif> n : mNotifs.entrySet()) {
                syncNotifs(n.getValue());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Utilities.getInstance().dismissSyncNotification(mContext);
            if (mReceiver != null) {
                // Sending 0 & null as Receiver is DUMB
                mReceiver.send(0, null);
            }
            super.onPostExecute(aVoid);
        }
    }

    private void processServerNotif(DriveId dID) {
        String contents = null;
        DriveFile file = Drive.DriveApi.getFile(Utilities.mGoogleApiClient, dID);
        DriveApi.DriveContentsResult driveContentsResult =
                file.open(Utilities.mGoogleApiClient, DriveFile.MODE_READ_ONLY, null).await();
        if (!driveContentsResult.getStatus().isSuccess()) {
            return;
        }
        DriveContents driveContents = driveContentsResult.getDriveContents();
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(driveContents.getInputStream()));
        StringBuilder builder = new StringBuilder();
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            contents = builder.toString();
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException while reading from the stream", e);
        }
        Notif existingNote = Notif.fromJSONString(contents);

        DatabaseHelper.getInstance(mContext).handleNote(existingNote, mNotifs);
    }

    private void syncNotifs(Notif n) {
        if (mFolder == null) {
            mFolder = Drive.DriveApi.getAppFolder(Utilities.mGoogleApiClient);
        }

        // New Contents
        DriveApi.DriveContentsResult driveContentsResult =
                Drive.DriveApi.newDriveContents(Utilities.mGoogleApiClient).await();
        if (!driveContentsResult.getStatus().isSuccess()) {
            Log.e(LOG_TAG, "Failed at creating New Drive Content");
            return;
        }

        // Writing the file
        DriveContents originalContents = driveContentsResult.getDriveContents();
        OutputStream os = originalContents.getOutputStream();
        try {
            os.write(n.toJSONByteArray());
        } catch (IOException e) {
            Log.e(LOG_TAG, "Failed at Writing File");
            e.printStackTrace();
            return;
        }

        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle(String.valueOf(n.get_id()))
                .setMimeType("text/plain")
                .setStarred(true).build();
        DriveFolder.DriveFileResult fileResult = mFolder.createFile(Utilities.mGoogleApiClient,
                changeSet, driveContentsResult.getDriveContents()).await();

        if (!fileResult.getStatus().isSuccess()) {
            Log.e(LOG_TAG, "Failed at Writing File TO DRIVE");
            return;
        }
        Log.d(LOG_TAG, "Wrote the File " + n.get_id() + " TO DRIVE");
        n.setState(Utilities.states.SYNCED);
        n.setDriveID(fileResult.getDriveFile().getDriveId().encodeToString());
        n.updateThisInDB(mContext);
    }

}
