package shetye.prathamesh.notifyme;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import shetye.prathamesh.notifyme.database.DatabaseHelper;
import shetye.prathamesh.notifyme.database.Notif;
import shetye.prathamesh.notifyme.ui.BaseActivity;

public class NotifyMe extends BaseActivity {

    TextView mNotifTitle;
    TextView mNotifText;
    TextView mNotifDate;
    Context mContext;
    Notif mNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        mNotifTitle = (TextView) findViewById(R.id.notify_title_txt);
        mNotifText = (TextView) findViewById(R.id.notify_txt);
        mNotifDate = (TextView) findViewById(R.id.when_to_notify);

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                String title = intent.getStringExtra(Intent.EXTRA_SUBJECT);
                if (title == null)
                    title = "";
                mNote = new Notif(
                        DatabaseHelper.getInstance(mContext).getNewId(),
                        title,
                        intent.getStringExtra(Intent.EXTRA_TEXT),
                        0,
                        false,
                        false,
                        false
                );
                mNotifTitle.setText(mNote.getNotification_title());
                mNotifText.setText(mNote.getNotification_content());
                mNotifDate.setText(Utilities.getInstance().getDateFromMS(
                        mNote.getNotification_when()
                ));
                Utilities.getInstance().createWhenDialog(mContext, NotifyMe.this, mNote, true);
            }
        } else {
            int ID = getIntent().getIntExtra(Utilities.NOTIF_EXTRA_ID_KEY, 0);
            Utilities.getInstance().dismissNotification(this, ID);
            mNote = DatabaseHelper.getInstance(this).getNote(ID);
            mNotifTitle.setText(mNote.getNotification_title());
            mNotifText.setText(mNote.getNotification_content());
            mNotifText.setMovementMethod(new ScrollingMovementMethod());
            String date_txt;
            if (mNote.getNotification_when() > System.currentTimeMillis()) {
                date_txt = "Will Notify at " + Utilities.getInstance().getDateFromMS(
                        mNote.getNotification_when()
                );
                mNotifDate.setBackgroundColor(getResources().getColor(R.color.pending_notif));
            } else {
                date_txt = "Notified at " + Utilities.getInstance().getDateFromMS(
                        mNote.getNotification_when()
                );
                mNotifDate.setBackgroundColor(getResources().getColor(R.color.completed_notif));
            }
            mNotifDate.setText(date_txt);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_notification_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.reNotify:
                Utilities.getInstance().createWhenDialog(mContext, NotifyMe.this, mNote, true);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_notification_detail;
    }

    @Override
    protected void onResume() {
        super.onResume();

        mNotifTitle.setMovementMethod(LinkMovementMethod.getInstance());
        mNotifText.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public static void launchNotifyMe(BaseActivity activity, View transitionView, Notif note) {
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity,
                transitionView, "temp_view");
        Intent intent = new Intent(activity, NotifyMe.class);
        intent.putExtra(Utilities.NOTIF_EXTRA_ID_KEY, note.get_id());
        ActivityCompat.startActivity(activity, intent, options.toBundle());
    }

}
