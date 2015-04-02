package shetye.prathamesh.notifyme.database;

import android.content.Context;

import shetye.prathamesh.notifyme.R;
import shetye.prathamesh.notifyme.Utilities;

/**
 * Created by prathamesh on 3/19/15.
 */
public class Notif {
    private int _id;
    private String notification_title;
    private String notification_content;
    private long notification_when;
    private boolean repeat;
    private boolean complete;
    private boolean ongoing;
    private String driveID;
    private Utilities.states state;

    public int get_id() {
        return _id;
    }

    public String getNotification_title() {
        return notification_title;
    }

    public String getNotification_content() {
        return notification_content;
    }

    public long getNotification_when() {
        return notification_when;
    }

    public boolean isRepeat() {
        return repeat;
    }

    public boolean isComplete() {
        return complete;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public void setDriveID(String driveID) {
        this.driveID = driveID;
    }

    public void setState(Utilities.states state) {
        this.state = state;
    }

    public String getDriveID() {

        return driveID;
    }

    public Utilities.states getState() {
        return state;
    }

    public void setNotification_title(String notification_title) {
        this.notification_title = notification_title;
    }

    public void setNotification_content(String notification_content) {
        this.notification_content = notification_content;
    }

    public void setNotification_when(long notification_when) {
        this.notification_when = notification_when;
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public boolean isOngoing() {
        return ongoing;
    }

    public void setOngoing(boolean ongoing) {
        this.ongoing = ongoing;
    }

    public Notif(int _id, String notification_title, String notification_content,
                 long notification_when, boolean repeat, boolean complete, boolean ongoing) {
        this._id = _id;
        this.notification_title = notification_title;
        this.notification_content = notification_content;
        this.notification_when = notification_when;
        this.repeat = repeat;
        this.complete = complete;
        this.ongoing = ongoing;
        this.driveID = "";
        this.state = Utilities.states.NOT_SYNCED;
    }

    public Notif() {
        this._id = 0;
        this.notification_title = "";
        this.notification_content = "";
        this.notification_when = 0;
        this.repeat = false;
        this.complete = false;
        this.ongoing = false;
        this.driveID = "";
        this.state = Utilities.states.NOT_SYNCED;
    }

    public byte[] toByteArray(Context context) {
        StringBuffer b = new StringBuffer();
        if (notification_title != null && !notification_title.isEmpty()) {
            b.append(notification_title + "\n");
        }

        b.append(notification_content + "\n\n");

        if (notification_when < System.currentTimeMillis()) {
            b.append(context.getString(R.string.notified)
                    + Utilities.getInstance().getDateFromMS(notification_when));
        } else {
            b.append(context.getString(R.string.will_notify)
                    + Utilities.getInstance().getDateFromMS(notification_when));
        }

        return b.toString().getBytes();
    }

    public void updateThisInDB(Context context) {
        DatabaseHelper.getInstance(context).updateNote(this);
    }
}
