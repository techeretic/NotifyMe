package shetye.prathamesh.notifyme;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;

import java.util.List;

import shetye.prathamesh.notifyme.database.DatabaseHelper;
import shetye.prathamesh.notifyme.database.Notif;
import shetye.prathamesh.notifyme.ui.BaseActivity;
import shetye.prathamesh.notifyme.ui.FloatingActionButton;
import shetye.prathamesh.notifyme.ui.MyNotifRecAdapter;
import shetye.prathamesh.notifyme.ui.NotifAnimator;
import shetye.prathamesh.notifyme.ui.RecyclerItemClickListener;


public class Notifications extends BaseActivity {
    public static final String SHARED_PREF_APP_DATA = "APP_DATA";
    public static final String SHARED_PREF_KEY = "VERSION";
    private FloatingActionButton mFAddButton;
    private RecyclerView mRecyclerView;
    private Context mContext;
    private List<Notif> mNotifications;
    private MyNotifRecAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private static boolean sDoUpdate;
    private static boolean sFABClicked;
    private SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        sDoUpdate = true;
        mPrefs = getSharedPreferences(SHARED_PREF_APP_DATA, MODE_PRIVATE);
        updateVersion();
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
            //Utilities.getInstance().createNewNotifDialog(mContext, Notifications.this);
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
                    // special handler to avoid displaying half elements
                    // recyclerView.scrollTo(rec, y);
                    NotifAnimator.animateFAB(getApplicationContext(), mFAddButton, NotifAnimator.IN,
                            NotifAnimator.BOTTOM);
                } else {
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

        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (mFAddButton.getVisibility() == View.VISIBLE) {
                    NotifAnimator.animateFAB(getApplicationContext(), mFAddButton,
                            NotifAnimator.OUT, NotifAnimator.BOTTOM);
                }
                Notif note = mNotifications.get(position);
                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        Notifications.this,
                        Pair.create(view.findViewById(R.id.when_to_notify), "when_to_notify"),
                        Pair.create(view.findViewById(R.id.notify_txt), "notify_txt"),
                        Pair.create(view.findViewById(R.id.notify_title_txt), "notify_title_txt"),
                        Pair.create(view.findViewById(R.id.line_view), "line_view")
                );
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

    public void refreshNotifications() {
        mNotifications = DatabaseHelper.getInstance(mContext).getAllNotifications();
        mAdapter = new MyNotifRecAdapter(mContext, mNotifications);
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
        if (mPrefs.getString(SHARED_PREF_KEY,"1").equals(version)) {
            SharedPreferences.Editor editor = mPrefs.edit();
            editor.putString(SHARED_PREF_KEY, version);
            editor.commit();
            Utilities.getInstance().reArmAlarms(mContext);
        }
    }
}
