package shetye.prathamesh.notifyme;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.text.format.Time;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import shetye.prathamesh.notifyme.database.DatabaseHelper;
import shetye.prathamesh.notifyme.database.Notif;
import shetye.prathamesh.notifyme.receiver.RecieveAndNotify;

/**
 * Created by p.shetye on 3/19/15.
 */
public class Utilities {
    public static final String NOTIF_EXTRA_KEY = "NOTIF_MESSAGE";
    public static final String NOTIF_EXTRA_ID_KEY = "NOTIF_ID";
    public static final String NOTIF_EXTRA_TITLE_KEY = "NOTIF_TITLE";
    public static final String NOTIF_EXTRA_DONE_LATER_KEY = "NOTIF_DONE_OR_LATER";
    public static final String NOTIF_SERVICE_ACTION = "shetye.prathamesh.GENERATE_NOTIFICATION";
    public static final String NOTIF_SERVICE_DONE_ACTION = "shetye.prathamesh.DONE_NOTIFICATION";
    public static final int UPDATED = 7;
    private static Utilities instance;
    private Dialog mDialog;
    private Button mTimeBtn;
    private Button mDateBtn;
    private Button mDialogOKBtn;
    private Button mDialogCancelBtn;
    private int mSelectedDay;
    private int mSelectedMonth;
    private int mSelectedYear;
    private int mSelectedHours;
    private int mSelectedMinutes;
    private boolean mPastDateSelected;

    public static Utilities getInstance() {
        if (instance == null) {
            instance = new Utilities();
        }
        return instance;
    }

    public void createWhenDialog(final Context context, final Activity parentActivity, final Notif note, final boolean back) {
        mDialog = new Dialog(context);
        mDialog.setContentView(R.layout.datetimepicker_dialog);
        mDialog.setTitle("When to Notify??");
        mDialogOKBtn = (Button) mDialog.findViewById(R.id.btn_ok);
        mDialogOKBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                note.setNotification_when(getSelectedTime());
                DatabaseHelper.getInstance(context).addNotif(note);
                setZoneInTimer(context, note);
                mDialog.dismiss();
                if (parentActivity != null) {
                    if (back) {
                        parentActivity.finishAfterTransition();
                    } else {
                        ((Notifications) parentActivity).refreshNotifications();
                    }
                }
            }
        });
        mDialogOKBtn.setEnabled(false);
        mDialogCancelBtn = (Button) mDialog.findViewById(R.id.btn_cancel);
        mDialogCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
                if (parentActivity != null) {
                    parentActivity.finishAfterTransition();
                }
            }
        });
        mTimeBtn = (Button) mDialog.findViewById(R.id.btntimeset);
        mDateBtn = (Button) mDialog.findViewById(R.id.btndateset);
        final Time dtNow = new Time();
        dtNow.setToNow();
        mSelectedHours = dtNow.hour;
        mSelectedMinutes = dtNow.minute;
        mSelectedYear = dtNow.year;
        mSelectedMonth = dtNow.month;
        mSelectedDay = dtNow.monthDay;
        updateBtnText(dtNow, false);
        Time selectedTime = new Time();
        selectedTime.set(0, mSelectedMinutes, mSelectedHours, mSelectedDay, mSelectedMonth, mSelectedYear);
        mPastDateSelected = true;
        mTimeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        Time selectedTime = new Time();
                        mSelectedHours = hourOfDay;
                        mSelectedMinutes = minute;
                        selectedTime.set(0, mSelectedMinutes, mSelectedHours, mSelectedDay, mSelectedMonth, mSelectedYear);
                        if (Time.compare(selectedTime, dtNow) <= 0) {
                            mPastDateSelected = true;
                        } else {
                            mPastDateSelected = false;
                        }
                        updateBtnText(selectedTime, true);
                    }
                }, mSelectedHours, mSelectedMinutes, false);
                timePickerDialog.show();
            }
        });
        mDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(context,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int yy, int mm, int dd) {
                                Time selectedTime = new Time();
                                mSelectedYear = yy;
                                mSelectedMonth = mm;
                                mSelectedDay = dd;
                                selectedTime.set(0, mSelectedMinutes, mSelectedHours,
                                        mSelectedDay, mSelectedMonth, mSelectedYear);
                                if (Time.compare(selectedTime, dtNow) <= 0) {
                                    mPastDateSelected = true;
                                } else {
                                    mPastDateSelected = false;
                                }
                                updateBtnText(selectedTime, false);
                            }
                        }, mSelectedYear, mSelectedMonth, mSelectedDay);
                datePickerDialog.show();
            }
        });
        mDialog.show();
    }

    private void updateBtnText(Time time, boolean setTime) {
        if (setTime) {
            if (mTimeBtn != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
                mTimeBtn.setText(sdf.format(new Date(time.toMillis(false))));
                if (mPastDateSelected) {
                    mTimeBtn.setTextColor(Color.RED);
                } else {
                    mTimeBtn.setTextColor(Color.GRAY);
                    mDateBtn.setTextColor(Color.GRAY);
                }
            }
        } else {
            if (mDateBtn != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("cccc, MMMM dd");
                mDateBtn.setText(sdf.format(new Date(time.toMillis(false))));
                if (mPastDateSelected) {
                    mDateBtn.setTextColor(Color.RED);
                } else {
                    mDateBtn.setTextColor(Color.GRAY);
                    mTimeBtn.setTextColor(Color.GRAY);
                }
            }
        }
        if (!mPastDateSelected) {
            mDialogOKBtn.setEnabled(true);
        } else {
            mDialogOKBtn.setEnabled(false);
        }
    }

    private long getSelectedTime() {
        Calendar cl = new GregorianCalendar(mSelectedYear, mSelectedMonth, mSelectedDay,
                mSelectedHours, mSelectedMinutes, 0);
        return cl.getTimeInMillis();
    }

    private void setZoneInTimer(Context context, Notif note) {
        Intent intent = new Intent(context, RecieveAndNotify.class);
        intent.setData(Uri.parse(Integer.toString(note.get_id())));
        intent.setAction(NOTIF_SERVICE_ACTION);
        intent.putExtra(NOTIF_EXTRA_TITLE_KEY, note.getNotification_title());
        intent.putExtra(NOTIF_EXTRA_KEY, note.getNotification_content());
        intent.putExtra(NOTIF_EXTRA_ID_KEY, note.get_id());
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, note.getNotification_when(),
                PendingIntent.getBroadcast(context, 0, intent,
                        PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_CANCEL_CURRENT));
    }

    public void generateNotification(Context context, int ID, String title, String message, boolean isOngoing) {
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        Intent notificationIntent = new Intent(context, NotifyMe.class);
        notificationIntent.setData(Uri.parse(Integer.toString(ID)));
        notificationIntent.putExtra(Utilities.NOTIF_EXTRA_ID_KEY, ID);
        notificationIntent.putExtra(Utilities.NOTIF_EXTRA_KEY,message);
        notificationIntent.putExtra(Utilities.NOTIF_EXTRA_TITLE_KEY,title);

        Intent laterIntent = new Intent(context, NotificationDetail.class);
        laterIntent.setData(Uri.parse(Integer.toString(ID)));
        laterIntent.putExtra(Utilities.NOTIF_EXTRA_ID_KEY, ID);
        laterIntent.putExtra(Utilities.NOTIF_EXTRA_DONE_LATER_KEY,true);

        Intent doneIntent = new Intent(context, RecieveAndNotify.class);
        doneIntent.setData(Uri.parse(Integer.toString(ID)));
        doneIntent.setAction(NOTIF_SERVICE_DONE_ACTION);
        doneIntent.putExtra(Utilities.NOTIF_EXTRA_ID_KEY, ID);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(
                context,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        PendingIntent remindLaterIntent = PendingIntent.getActivity(
                context,
                0,
                laterIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        PendingIntent completedIntent = PendingIntent.getBroadcast(
                context,
                0,
                doneIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        String newTitle;
        if (title == null || title.isEmpty())
            newTitle = context.getResources().getString(R.string.app_name);
        else
            newTitle = title;
        inboxStyle.setBigContentTitle(newTitle);
        inboxStyle.addLine(message);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_dashboard_light)
                        .setContentTitle(newTitle)
                        .setContentText(message)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setContentIntent(resultPendingIntent)
                        .addAction(R.drawable.ic_action_edit, "Later", remindLaterIntent)
                        .addAction(R.drawable.ic_action_discard, "Done", completedIntent)
                        .setStyle(inboxStyle);
        Log.d("NotifyMe", "Notifying for ID : " + ID);
        notificationManager.notify(ID, mBuilder.build());
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(context, notification);
            Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

            // Vibrate for 300 milliseconds
            v.vibrate(200);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getDateFromMS(long timeInMS) {
        Date d = new Date(timeInMS);
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a cccc, MMMM dd");
        return sdf.format(d);
    }

    public void dismissNotification(Context context, int ID) {
        NotificationManager notificationManager = (NotificationManager) context
            .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(ID);
        DatabaseHelper.getInstance(context).markComplete(ID);
    }

    public void reArmAlarms(Context context) {
        List<Notif> notifs = DatabaseHelper.getInstance(context).getAllNotifications();
        for (Notif n : notifs) {
            if (n.getNotification_when() > System.currentTimeMillis()) {
                setZoneInTimer(context, n);
            }
        }
    }
}
