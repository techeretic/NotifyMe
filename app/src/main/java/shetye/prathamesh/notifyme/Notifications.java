package shetye.prathamesh.notifyme;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import shetye.prathamesh.notifyme.database.DatabaseHelper;
import shetye.prathamesh.notifyme.database.Notif;
import shetye.prathamesh.notifyme.ui.FloatingActionButton;
import shetye.prathamesh.notifyme.ui.MyNotifRecAdapter;
import shetye.prathamesh.notifyme.ui.NotifAnimator;
import shetye.prathamesh.notifyme.ui.RecyclerItemClickListener;


public class Notifications extends ActionBarActivity {

    private FloatingActionButton mFAddButton;
    private RecyclerView mRecyclerView;
    private Context mContext;
    private List<Notif> mNotifications;
    private MyNotifRecAdapter mAdapter;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        mContext = this;

        mToolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mNotifications = DatabaseHelper.getInstance(mContext).getAllNotifications();
        Log.d("NotifyMe","mNotifications.size() = " + mNotifications.size());
        mRecyclerView = (RecyclerView) findViewById(R.id.recycleNotificions);

        mAdapter = new MyNotifRecAdapter(mContext, mNotifications);
        mRecyclerView.setAdapter(mAdapter);

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

        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                
            }
        }));
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshNotifications();
    }

    public void refreshNotifications() {
        mNotifications = DatabaseHelper.getInstance(mContext).getAllNotifications();
        mAdapter = new MyNotifRecAdapter(mContext, mNotifications);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
    }
}
