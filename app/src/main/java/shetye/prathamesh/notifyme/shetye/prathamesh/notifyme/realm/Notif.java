package shetye.prathamesh.notifyme.shetye.prathamesh.notifyme.realm;

import io.realm.RealmObject;

/**
 * Created by prathamesh on 3/19/15.
 */
public class Notif extends RealmObject {
    private int _id;
    private String notification_title;
    private String notification_content;
    private long notification_when;
    private boolean repeat;
    private boolean complete;

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

    public Notif(int _id, String notification_title, String notification_content, long notification_when, boolean repeat, boolean complete) {
        this._id = _id;
        this.notification_title = notification_title;
        this.notification_content = notification_content;
        this.notification_when = notification_when;
        this.repeat = repeat;
        this.complete = complete;
    }
}
