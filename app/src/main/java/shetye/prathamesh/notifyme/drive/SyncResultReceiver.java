package shetye.prathamesh.notifyme.drive;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

/**
 * Created by prathamesh on 4/5/15.
 */
public class SyncResultReceiver extends ResultReceiver {
    private Receiver mReceiver;

    public SyncResultReceiver(Handler handler) {
        super(handler);
    }

    public interface Receiver {
        void onReceiveSyncResult(int resultCode, Bundle resultData);
    }

    public void setReceiver(Receiver receiver) {
        mReceiver = receiver;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {

        if (mReceiver != null) {
            mReceiver.onReceiveSyncResult(resultCode, resultData);
        }
    }

}
