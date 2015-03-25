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
        Log.d("NotifyMe","In RecieveAndNotify | ID received via Intent = " + id);
        String action = intent.getAction();
        switch (action) {
            case Utilities.NOTIF_SERVICE_ACTION:
                Log.d("NotifyMe", "Utilities.NOTIF_SERVICE_ACTION = " + Utilities.NOTIF_SERVICE_ACTION);
                String message = intent.getStringExtra(Utilities.NOTIF_EXTRA_KEY);
                String title = intent.getStringExtra(Utilities.NOTIF_EXTRA_TITLE_KEY);
                if (message == null || message.isEmpty()) {
                    return;
                }
                Utilities.getInstance().generateNotification(context, id, title, message, true);
                break;
            case Utilities.NOTIF_SERVICE_DONE_ACTION:
                Log.d("NotifyMe", "Utilities.NOTIF_SERVICE_DONE_ACTION = " + Utilities.NOTIF_SERVICE_DONE_ACTION);
                Utilities.getInstance().dismissNotification(context, id);
                break;
            case Utilities.BOOT_COMPLETE_INTENT:
                Log.d("NotifyMe", "Received BOOT COMPLETE");
                Utilities.getInstance().reArmAlarms(context);
                break;
        }
    }
}
