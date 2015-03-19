package shetye.prathamesh.notifyme;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class NotifyMe extends ActionBarActivity {

    Context mContext;
    EditText mNotifyText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_notify_me);
        mNotifyText = (EditText) findViewById(R.id.what_notify_txt);
        Button notifyBtn = (Button) findViewById(R.id.notify_me_button);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(getResources().getString(R.string.app_name));
        notifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mNotifyText.getText().toString().isEmpty()) {
                    Utilities.getInstance().createZoneOutDialog(mContext,
                        NotifyMe.this, mNotifyText.getText().toString());
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_notify_me, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.notify_me) {
            if (!mNotifyText.getText().toString().isEmpty()) {
                Utilities.getInstance().createZoneOutDialog(mContext,
                    NotifyMe.this, mNotifyText.getText().toString());
            }
        }

        return super.onOptionsItemSelected(item);
    }

}
