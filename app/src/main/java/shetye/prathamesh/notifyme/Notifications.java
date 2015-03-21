package shetye.prathamesh.notifyme;

import android.content.Context;
import android.os.Bundle;
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

    private FloatingActionButton mFAddButton;
    private RecyclerView mRecyclerView;
    private Context mContext;
    private List<Notif> mNotifications;
    private MyNotifRecAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;

        mNotifications = DatabaseHelper.getInstance(mContext).getAllNotifications();

        mRecyclerView = (RecyclerView) findViewById(R.id.recycleNotificions);

        mFAddButton = new FloatingActionButton.Builder(this)
                .withDrawable(getResources().getDrawable(R.drawable.ic_action_new))
                .withButtonColor(getResources().getColor(android.R.color.holo_red_light))
                .withGravity(Gravity.BOTTOM | Gravity.END).withMargins(0, 0, 15, 15).create();

        NotifAnimator.animateFAB(getApplicationContext(), mFAddButton, NotifAnimator.IN,
                NotifAnimator.BOTTOM);

        mFAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utilities.getInstance().createNewNotifDialog(mContext, Notifications.this);
            }
        });

        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                // TODO Auto-generated method stub
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
                // TODO Auto-generated method stub
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
                NotifyMe.launchNotifyMe(Notifications.this,
                        view.findViewById(R.id.transition_view), note);
            }
        }));
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_notifications;
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshNotifications();
        NotifAnimator.animateFAB(getApplicationContext(), mFAddButton,
                    NotifAnimator.IN, NotifAnimator.BOTTOM);
    }

    public void refreshNotifications() {
        mNotifications = DatabaseHelper.getInstance(mContext).getAllNotifications();
        mAdapter = new MyNotifRecAdapter(mContext, mNotifications);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
    }
}
