package shetye.prathamesh.notifyme.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import shetye.prathamesh.notifyme.Utilities;

public class RecieveAndNotify extends BroadcastReceiver {
    public RecieveAndNotify() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("NotifyMe", "IN RecieveAndNotify - onReceive");
        int id = intent.getIntExtra(Utilities.NOTIF_EXTRA_ID_KEY,0);
        String message = intent.getStringExtra(Utilities.NOTIF_EXTRA_KEY);
        String title = intent.getStringExtra(Utilities.NOTIF_EXTRA_TITLE_KEY);
        Log.d("NotifyMe", "message = " + message);
        if (message == null && message.isEmpty()) {
            return;
        }
        Utilities.getInstance().generateNotification(context, id, title, message, true);
    }
}
