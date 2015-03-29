package shetye.prathamesh.notifyme;

import android.content.Context;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import shetye.prathamesh.notifyme.database.DatabaseHelper;
import shetye.prathamesh.notifyme.database.Notif;
import shetye.prathamesh.notifyme.ui.BaseActivity;


public class NotificationDetail extends BaseActivity {
    private Context mContext;
    private Notif mNote;
    private EditText mNotifyTitleText;
    private EditText mNotifyText;
    private static boolean existingNotif = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        existingNotif = false;
        mNotifyTitleText = (EditText) findViewById(R.id.what_notify_title_txt);
        mNotifyText = (EditText) findViewById(R.id.what_notify_txt);

        if (getIntent().getBooleanExtra(Utilities.NOTIF_EXTRA_DONE_LATER_KEY, false)) {
            int id = getIntent().getIntExtra(Utilities.NOTIF_EXTRA_ID_KEY, 0);
            Log.d("NotifyMe","In NotificationDetail | ID received via Intent = " + id);
            Utilities.getInstance().dismissNotification(mContext, id);
            mNote = DatabaseHelper.getInstance(mContext).getNote(id);
            if (mNote != null) {
                if (mNote.getNotification_title().isEmpty()) {
                    mNotifyTitleText.setVisibility(View.INVISIBLE);
                } else {
                    mNotifyTitleText.setVisibility(View.VISIBLE);
                    mNotifyTitleText.setText(mNote.getNotification_title());
                }
                mNotifyText.setText(mNote.getNotification_content());
            }
            Utilities.getInstance().createWhenDialog(mContext, NotificationDetail.this, mNote, true);
        } else if (getIntent().getBooleanExtra(Utilities.NOTIF_EXTRA_EDIT_KEY, false)) {
            int id = getIntent().getIntExtra(Utilities.NOTIF_EXTRA_ID_KEY, 0);
            Log.d("NotifyMe","In NotificationDetail | ID received via Intent = " + id);
            Utilities.getInstance().dismissNotification(mContext, id);
            mNote = DatabaseHelper.getInstance(mContext).getNote(id);
            if (mNote != null) {
                mNotifyTitleText.setText(mNote.getNotification_title());
                mNotifyText.setText(mNote.getNotification_content());
            }
            existingNotif = true;
        }
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_notify_me;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_notify_me, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.notify_me:
                if (!mNotifyText.getText().toString().isEmpty()) {
                    saveNotif(false);
                    setResult(RESULT_OK);
                    Utilities.getInstance().createWhenDialog(mContext, NotificationDetail.this,
                            mNote, true);
                }
                return true;
            case R.id.save_me:
                if (!mNotifyText.getText().toString().isEmpty()) {
                    saveNotif(true);
                    setResult(RESULT_OK);
                    finish();
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveNotif(boolean withCurrTime) {
        if (existingNotif) {
            mNote.setNotification_title(mNotifyTitleText.getText().toString());
            mNote.setNotification_content(mNotifyText.getText().toString());
            DatabaseHelper.getInstance(mContext).updateNote(mNote);
            return;
        }
        mNote = new Notif(
                DatabaseHelper.getInstance(mContext).getNewId(),
                mNotifyTitleText.getText().toString(),
                mNotifyText.getText().toString(),
                0,
                false,
                false,
                false
        );
        if (withCurrTime) {
            mNote.setNotification_when(System.currentTimeMillis());
            DatabaseHelper.getInstance(mContext).addNotif(mNote);
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }
}
