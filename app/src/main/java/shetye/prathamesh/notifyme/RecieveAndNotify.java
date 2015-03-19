package shetye.prathamesh.notifyme;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class RecieveAndNotify extends BroadcastReceiver {
    public RecieveAndNotify() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("NotifyMe", "IN RecieveAndNotify - onReceive");
        String message = intent.getStringExtra(Utilities.NOTIF_EXTRA_KEY);
        Log.d("NotifyMe", "message = " + message);
        if (message == null && message.isEmpty()) {
            return;
        }
        Utilities.getInstance().generateNotification(context, 001, message, true);
    }
}
