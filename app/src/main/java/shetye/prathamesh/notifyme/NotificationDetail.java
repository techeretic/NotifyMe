package shetye.prathamesh.notifyme;

import android.content.Context;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;

        mNotifyTitleText = (EditText) findViewById(R.id.what_notify_title_txt);
        mNotifyText = (EditText) findViewById(R.id.what_notify_txt);
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
                    saveNotif();
                    setResult(RESULT_OK);
                    Utilities.getInstance().createWhenDialog(mContext, NotificationDetail.this,
                            mNote, true);
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveNotif() {
        mNote = new Notif(
                DatabaseHelper.getInstance(mContext).getNewId(),
                mNotifyTitleText.getText().toString(),
                mNotifyText.getText().toString(),
                0,
                false,
                false,
                false
        );
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }
}
