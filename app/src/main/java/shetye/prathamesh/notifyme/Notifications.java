package shetye.prathamesh.notifyme;

import android.content.Context;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;

import java.util.List;

import io.realm.internal.Util;
import shetye.prathamesh.notifyme.shetye.prathamesh.notifyme.realm.Notif;
import shetye.prathamesh.notifyme.shetye.prathamesh.notifyme.ui.FloatingActionButton;
import shetye.prathamesh.notifyme.shetye.prathamesh.notifyme.ui.NotifAnimator;


public class Notifications extends ActionBarActivity {

    private FloatingActionButton mFAddButton;
    private RecyclerView mRecyclerView;
    private Context mContext;
    private List<Notif> mNotifications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        mContext = this;
        Utilities.getInstance().initializeRealm(mContext);

        mNotifications = Utilities.getInstance().getMyNotifs();
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
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
    }
}
