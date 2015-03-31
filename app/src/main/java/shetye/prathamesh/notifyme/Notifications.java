package shetye.prathamesh.notifyme;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;

import java.util.List;

import shetye.prathamesh.notifyme.database.DatabaseHelper;
import shetye.prathamesh.notifyme.database.Notif;
import shetye.prathamesh.notifyme.ui.BaseActivity;
import shetye.prathamesh.notifyme.ui.FloatingActionButton;
import shetye.prathamesh.notifyme.ui.MyNotifRecAdapter;
import shetye.prathamesh.notifyme.ui.NotifAnimator;
import shetye.prathamesh.notifyme.ui.RecyclerItemClickListener;


public class Notifications extends BaseActivity {
    private static final String LOG_TAG = "Notifications";
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

        /*mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();*/

        mNotifications = DatabaseHelper.getInstance(mContext).getAllNotifications();

        mRecyclerView = (RecyclerView) findViewById(R.id.recycleNotificions);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshNotifications();
                try {
                    //wait(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
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
            SharedPreferences.Editor editor = mPrefs.edit();
            editor.putString(Utilities.SHARED_PREF_KEY, version);
            editor.commit();
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
}
