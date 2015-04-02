package shetye.prathamesh.notifyme;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
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

import shetye.prathamesh.notifyme.database.DatabaseHelper;
import shetye.prathamesh.notifyme.database.Notif;
import shetye.prathamesh.notifyme.ui.BaseActivity;
import shetye.prathamesh.notifyme.ui.FloatingActionButton;
import shetye.prathamesh.notifyme.ui.MyNotifRecAdapter;
import shetye.prathamesh.notifyme.ui.NotifAnimator;
import shetye.prathamesh.notifyme.ui.RecyclerItemClickListener;


public class Notifications extends BaseActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private static final String LOG_TAG = "Notifications";
    private static int REQUEST_CODE_RESOLUTION = 10602966;
    private FloatingActionButton mFAddButton;
    private RecyclerView mRecyclerView;
    private Context mContext;
    private List<Notif> mNotifications;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private static boolean sDoUpdate;
    private static boolean sFABClicked;
    private SharedPreferences mPrefs;
    private MenuItem mSearchItem;

    private GoogleApiClient mGoogleApiClient;
    private boolean mIsDefaultFolderPresent = false;
    private boolean mIsConnected = false;

    private static View sView;
    private static int sPosition;

    private void resetView(int position, View view) {
        if (position != sPosition && sView != null) {
            LinearLayout content = (LinearLayout) sView.findViewById(R.id.item_content);
            LinearLayout controller = (LinearLayout) sView.findViewById(R.id.item_controllers);
            Utilities.getInstance().hideView(controller);
            Utilities.getInstance().showView(content);
            sPosition = position;
            sView = view;
        }
    }

    private void showView(int position, View view) {
        LinearLayout content = (LinearLayout) view.findViewById(R.id.item_content);
        LinearLayout controller = (LinearLayout) view.findViewById(R.id.item_controllers);

        Log.d("NotifyMe","Content Height = " + content.getHeight());
        Log.d("NotifyMe","controller Height = " + controller.getHeight());

        if (content.getVisibility() == View.INVISIBLE) {
            Utilities.getInstance().hideView(controller);
            Utilities.getInstance().showView(content);
            content.setClickable(true);
            sPosition = -1;
            sView = null;
        } else {
            sPosition = position;
            sView = view;
            Utilities.getInstance().hideView(content);
            Utilities.getInstance().showView(controller);
            content.setClickable(false);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        sDoUpdate = true;
        mPrefs = getSharedPreferences(Utilities.SHARED_PREF_APP_DATA, MODE_PRIVATE);
        updateVersion();

        mNotifications = DatabaseHelper.getInstance(mContext).getAllNotifications();

        mRecyclerView = (RecyclerView) findViewById(R.id.recycleNotificions);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                resetView(-1, null);
                refreshNotifications();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        mFAddButton = new FloatingActionButton.Builder(this)
                .withDrawable(getResources().getDrawable(R.drawable.ic_action_new))
                .withButtonColor(getResources().getColor(R.color.accent))
                .withGravity(Gravity.BOTTOM | Gravity.END).withMargins(0, 0, 15, 15).create();

        mFAddButton.setTransitionName("new_notif");

        NotifAnimator.animateFAB(getApplicationContext(), mFAddButton, NotifAnimator.IN,
                NotifAnimator.BOTTOM);

        mFAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            sFABClicked = true;
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    Notifications.this,
                    mFAddButton, "new_notif");
            Intent intent = new Intent(Notifications.this, NotificationDetail.class);
            ActivityCompat.startActivityForResult(Notifications.this, intent,
                    Utilities.UPDATED, options.toBundle());
            }
        });

        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    NotifAnimator.animateFAB(getApplicationContext(), mFAddButton, NotifAnimator.IN,
                            NotifAnimator.BOTTOM);
                } else {
                    resetView(-1, null);
                    NotifAnimator.animateFAB(getApplicationContext(), mFAddButton, NotifAnimator.OUT,
                            NotifAnimator.BOTTOM);
                }
                recyclerView.animate();
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                recyclerView.animate();
            }
        });

        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, mRecyclerView,
                new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemLongClick(View view, int position) {
                resetView(position, view);
                showView(position, view);
            }

            @Override
            public void onItemClick(View view, int position) {
                LinearLayout content = (LinearLayout) view.findViewById(R.id.item_content);
                if (content.getVisibility()==View.INVISIBLE) {
                    return;
                }
                if (mFAddButton.getVisibility() == View.VISIBLE) {
                    NotifAnimator.animateFAB(getApplicationContext(), mFAddButton,
                            NotifAnimator.OUT, NotifAnimator.BOTTOM);
                }
                Notif note = mNotifications.get(position);
                ActivityOptionsCompat options;
                TextView titleV = (TextView) view.findViewById(R.id.notify_title_txt);
                if (titleV.getVisibility()==View.GONE) {
                    options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                            Notifications.this,
                            Pair.create(view.findViewById(R.id.when_to_notify), "when_to_notify"),
                            Pair.create(view.findViewById(R.id.notify_txt), "notify_txt"),
                            Pair.create(view.findViewById(R.id.line_view), "line_view")
                    );
                } else {
                    options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                            Notifications.this,
                            Pair.create(view.findViewById(R.id.when_to_notify), "when_to_notify"),
                            Pair.create(view.findViewById(R.id.notify_txt), "notify_txt"),
                            Pair.create(view.findViewById(R.id.notify_title_txt), "notify_title_txt"),
                            Pair.create(view.findViewById(R.id.line_view), "line_view")
                    );
                }
                Intent intent = new Intent(Notifications.this, NotifyMe.class);
                intent.putExtra(Utilities.NOTIF_EXTRA_ID_KEY, note.get_id());
                ActivityCompat.startActivityForResult(Notifications.this, intent,
                        Utilities.UPDATED, options.toBundle());
            }
        }));

        mToolbar.setNavigationIcon(R.mipmap.ic_launcher);
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_notifications;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPrefs.getBoolean(Utilities.SHARED_PREF_SEARCH_KEY, false)) {
            mPrefs.edit().putBoolean(Utilities.SHARED_PREF_SEARCH_KEY,false).commit();
            if (mSearchItem != null) {
                mSearchItem.collapseActionView();
            }
        }
        if (sDoUpdate)
            refreshNotifications();
        if (!sFABClicked) {
            NotifAnimator.animateFAB(getApplicationContext(), mFAddButton,
                    NotifAnimator.IN, NotifAnimator.BOTTOM);
        } else {
            sFABClicked = false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Check which request we're responding to
        if (requestCode == Utilities.UPDATED) {
            if (resultCode == RESULT_OK) {
                sDoUpdate = true;
            } else {
                sDoUpdate = false;
            }
        } else
        if (requestCode == REQUEST_CODE_RESOLUTION && resultCode == RESULT_OK) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        resetView(-1, null);
        sView = null;
        sPosition = -1;
    }

    public void refreshNotifications() {
        mNotifications = DatabaseHelper.getInstance(mContext).getAllNotifications();
        MyNotifRecAdapter mAdapter = new MyNotifRecAdapter(mContext, mNotifications);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
    }

    private void updateVersion() {
        String version;
        try {
            version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (Exception e) {
            e.printStackTrace();
            version = "1";
        }
        if (mPrefs.getString(Utilities.SHARED_PREF_KEY,"1").equals(version)) {
            mPrefs.edit().putString(Utilities.SHARED_PREF_KEY, version).commit();
            Utilities.getInstance().reArmAlarms(mContext);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_base, menu);
        // Associate searchable configuration with the SearchView
        mSearchItem = menu.findItem(R.id.search);
        if (mSearchItem != null) {
            Log.d(LOG_TAG, "searchItem is NOT null");
        } else {
            Log.d(LOG_TAG, "searchItem is null");
        }
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(mSearchItem);
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            LinearLayout linearLayout1 = (LinearLayout) searchView.getChildAt(0);
            LinearLayout linearLayout2 = (LinearLayout) linearLayout1.getChildAt(2);
            LinearLayout linearLayout3 = (LinearLayout) linearLayout2.getChildAt(1);
            AutoCompleteTextView autoComplete = (AutoCompleteTextView) linearLayout3.getChildAt(0);
            autoComplete.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        NotifAnimator.animateFAB(getApplicationContext(), mFAddButton,
                                NotifAnimator.OUT, NotifAnimator.BOTTOM);
                    } else {
                        NotifAnimator.animateFAB(getApplicationContext(), mFAddButton,
                                NotifAnimator.IN, NotifAnimator.BOTTOM);
                    }
                }
            });
        } else {
            Log.d(LOG_TAG, "searchView is null");
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.info:
                Utilities.getInstance().showLegalNotice(mContext);
                return true;
            case R.id.drive:
                if (mIsConnected) {
                    syncFilesToDrive();
                } else {
                    startGoogleDriveSetup();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (sView != null) {
            resetView(-1, null);
        } else {
            super.onBackPressed();
        }
    }

    private void startGoogleDriveSetup() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mIsConnected = true;
        syncFilesToDrive();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {

        mIsConnected = false;
        // Called whenever the API client fails to connect.
        Log.i(LOG_TAG, "GoogleApiClient connection failed: " + result.toString());
        if (!result.hasResolution()) {
            // show the localized error dialog.
            Log.i(LOG_TAG, "NO RESOLUTION!! FML!");
            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this, 0).show();
            return;
        }
        // The failure has a resolution. Resolve it.
        // Called typically when the app is not yet authorized, and an
        // authorization
        // dialog is displayed to the user.
        try {
            Log.i(LOG_TAG, "NO RESOLUTION!! FML!");
            result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
        } catch (IntentSender.SendIntentException e) {
            Log.e(LOG_TAG, "Exception while starting resolution activity", e);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mIsConnected = false;
        Log.d(LOG_TAG, "GoogleApiClient connection suspended");
    }

    private GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    private void syncFilesToDrive() {
        createDefaultFolder();
        if (mPrefs.getBoolean(Utilities.SHARED_PREF_DRIVE_CONNECTED_KEY, false)) {
            Log.d(LOG_TAG, "Executing SyncTask");
            new SyncTask(mContext).execute();
        }
    }

    private void createDefaultFolder() {
        isFolderPresent();
        if (!mIsDefaultFolderPresent) {
            Log.d(LOG_TAG, "Creating New Folder");
            MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                    .setTitle(Utilities.DRIVE_DEFAULT_FOLDER_NAME).build();
            Drive.DriveApi.getRootFolder(getGoogleApiClient()).createFolder(
                    getGoogleApiClient(), changeSet).setResultCallback(
                        new ResultCallback<DriveFolder.DriveFolderResult>() {
                @Override
                public void onResult(DriveFolder.DriveFolderResult result) {
                    if (!result.getStatus().isSuccess()) {
                        Log.e(LOG_TAG, "Error while trying to create the folder");
                        return;
                    }
                    mPrefs.edit().putString(Utilities.SHARED_PREF_DRIVE_KEY,
                            result.getDriveFolder().getDriveId().encodeToString()).commit();
                    mPrefs.edit().putBoolean(Utilities.SHARED_PREF_DRIVE_CONNECTED_KEY, true).commit();
                }
            });
        }
    }

    private void isFolderPresent() {

        Query query = new Query.Builder()
                .addFilter(Filters.eq(SearchableField.MIME_TYPE, "application/vnd.google-apps.folder"))
                .addFilter(Filters.eq(SearchableField.TITLE, Utilities.DRIVE_DEFAULT_FOLDER_NAME))
                .build();
        Drive.DriveApi.query(getGoogleApiClient(), query)
            .setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
                   @Override
                   public void onResult(DriveApi.MetadataBufferResult result) {
                       if (!result.getStatus().isSuccess()) {
                           Log.e(LOG_TAG, "Problem while retrieving results | Folder Missing");
                           mIsDefaultFolderPresent = false;
                           return;
                       }
                       Log.d(LOG_TAG, "FOLDER PRESENT! Setting mIsDefaultFolderPresent = true");
                       mIsDefaultFolderPresent = true;
                       mPrefs.edit().putBoolean(Utilities.SHARED_PREF_DRIVE_CONNECTED_KEY, true).commit();
                   }
               }
            );
    }

    public class SyncTask extends AsyncTask<Void, Void, String> {
        private ProgressDialog mProgress;
        private Context mContext;
        private List<Notif> mNotes;
        private DriveId mFolderDriveId;

        SyncTask(Context context) {
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
            mNotes = DatabaseHelper.getInstance(mContext).getAllNotifications();
            mProgress = new ProgressDialog(mContext);
            mProgress.setMessage(mContext.getString(R.string.syncing));
            mProgress.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            String driveID = mPrefs.getString(Utilities.SHARED_PREF_DRIVE_KEY,"");
            if (driveID.isEmpty()) {
                Log.e(LOG_TAG, "Folder doesn't exist");
                return null;
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
                        Drive.DriveApi.newDriveContents(getGoogleApiClient()).await();
                if (!driveContentsResult.getStatus().isSuccess()) {
                    Log.e(LOG_TAG, "Failed at creating New Drive Content");
                    return null;
                }

                // Writing the file
                DriveContents originalContents = driveContentsResult.getDriveContents();
                OutputStream os = originalContents.getOutputStream();
                try {
                    os.write(n.toByteArray(mContext));
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Failed at Writing File");
                    e.printStackTrace();
                    return null;
                }

                // Creating the said file in Folder
                DriveFolder folder = Drive.DriveApi.getFolder(getGoogleApiClient(), mFolderDriveId);
                MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                        .setTitle(String.valueOf(n.get_id()) + ".txt")
                        .setMimeType("text/plain")
                        .setStarred(true).build();
                DriveFolder.DriveFileResult fileResult = folder.createFile(mGoogleApiClient,
                        changeSet, driveContentsResult.getDriveContents()).await();

                if (!fileResult.getStatus().isSuccess()) {
                    Log.e(LOG_TAG, "Failed at Writing File TO DRIVE");
                    return null;
                }
                n.setState(Utilities.states.SYNCED);
                n.setDriveID(fileResult.getDriveFile().getDriveId().encodeToString());
                n.updateThisInDB(mContext);
            }
            return "DONE";
        }

        @Override
        protected void onPostExecute(String result) {
            mProgress.dismiss();
            if (result == null) {
                Utilities.getInstance().showErrorDialog(mContext, "Drive Sync Failed");
            }
            super.onPostExecute(result);
        }
    }
}
