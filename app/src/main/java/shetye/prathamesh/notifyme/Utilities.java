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
import android.support.v4.app.NotificationCompat;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by p.shetye on 3/19/15.
 */
public class Utilities {
    public static final String NOTIF_EXTRA_KEY = "NOTIF_MESSAGE";
    public static final String NOTIF_SERVICE_ACTION = "shetye.prathamesh.GENERATE_NOTIFICATION";
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

    public void createZoneOutDialog(final Context context, final Activity parentActivity,
                                    final String message) {
        mDialog = new Dialog(context);
        mDialog.setContentView(R.layout.datetimepicker_dialog);
        mDialog.setTitle("Zone Out until??");
        mDialogOKBtn = (Button) mDialog.findViewById(R.id.btn_ok);
        mDialogOKBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Actually Hibernate the apps
                setZoneInTimer(context, message, getTime());
                mDialog.dismiss();
                parentActivity.finish();
            }
        });
        mDialogOKBtn.setEnabled(false);
        mDialogCancelBtn = (Button) mDialog.findViewById(R.id.btn_cancel);
        mDialogCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
                parentActivity.finish();
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
                }
            }
        }
        if (!mPastDateSelected) {
            mDialogOKBtn.setEnabled(true);
        } else {
            mDialogOKBtn.setEnabled(false);
        }
    }

    private long getTime() {
        Calendar cl = new GregorianCalendar(mSelectedYear, mSelectedMonth, mSelectedDay,
                mSelectedHours, mSelectedMinutes, 0);
        return cl.getTimeInMillis();
    }

    private void setZoneInTimer(Context context, String message, long tillWhen) {
        Intent intent = new Intent(context, RecieveAndNotify.class);
        intent.setAction(NOTIF_SERVICE_ACTION);
        intent.putExtra(NOTIF_EXTRA_KEY, message);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, tillWhen,
                PendingIntent.getBroadcast(context, 0, intent,
                        PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_CANCEL_CURRENT));
    }

    public void generateNotification(Context context, int ID, String message, boolean isOngoing) {
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        Intent notificationIntent = new Intent(context, NotifyMe.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(
                context,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        PendingIntent remindLaterIntent = PendingIntent.getActivity(
                context,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        PendingIntent completedIntent = PendingIntent.getActivity(
                context,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        // Sets a title for the Inbox in expanded layout
        inboxStyle.setBigContentTitle(context.getString(R.string.app_name));
        inboxStyle.addLine(message);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(android.R.drawable.ic_menu_set_as)
                        .setContentTitle(context.getString(R.string.app_name))
                        .setContentText(message)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setContentIntent(resultPendingIntent)
                        .addAction(android.R.drawable.ic_popup_reminder, "Later", remindLaterIntent)
                        .addAction(android.R.drawable.ic_menu_agenda, "Done", completedIntent)
                        .setStyle(inboxStyle);
        notificationManager.notify(ID, mBuilder.build());
    }
}
