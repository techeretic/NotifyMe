package shetye.prathamesh.notifyme;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import shetye.prathamesh.notifyme.database.DatabaseHelper;
import shetye.prathamesh.notifyme.database.Notif;


public class NotifyMe extends Activity {

    TextView notifTitle;
    TextView notifText;
    TextView notifDate;
    Button doneBtn;
    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_detail);
        mContext = this;
        int ID = getIntent().getIntExtra(Utilities.NOTIF_EXTRA_ID_KEY, 0);
        Utilities.getInstance().dismissNotification(this, ID);

        notifTitle = (TextView) findViewById(R.id.notify_title_txt);
        notifText = (TextView) findViewById(R.id.notify_txt);
        notifDate = (TextView) findViewById(R.id.when_to_notify);
        doneBtn = (Button) findViewById(R.id.done_button);

        Notif n = DatabaseHelper.getInstance(this).getNote(ID);


        notifTitle.setText(n.getNotification_title());
        notifText.setText(n.getNotification_content());
        notifDate.setText(Utilities.getInstance().getDateFromMS(
            n.getNotification_when()
        ));
        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

}
