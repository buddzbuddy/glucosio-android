package org.deabee.android.tools;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import org.deabee.android.activity.RemindersActivity;
import org.deabee.android.db.DatabaseHandler;
import org.deabee.android.db.Reminder;
import org.deabee.android.receivers.GlucosioBroadcastReceiver;
import org.joda.time.DateTime;

import java.util.Calendar;
import java.util.List;

public class GlucosioAlarmManager {
    private Context context;
    private AlarmManager alarmMgr;
    private DatabaseHandler db;
    private RemindersActivity activity;

    public GlucosioAlarmManager(Context context) {
        this.context = context;
        this.db = new DatabaseHandler(context);
        this.alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        this.activity = activity;
    }

    public GlucosioAlarmManager(Context context, RemindersActivity activity) {
        this.context = context;
        this.db = new DatabaseHandler(context);
        this.alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        this.activity = activity;
    }

    public void setAlarms() {
        List<Reminder> reminders = db.getReminders();
        int activeRemindersCount = 0;

        // Set an alarm for each date
        for (int i = 0; i < reminders.size(); i++) {
            Reminder reminder = reminders.get(i);

            Intent intent = new Intent(context, GlucosioBroadcastReceiver.class);
            intent.putExtra("metric", reminder.getMetric());
            intent.putExtra("glucosio_reminder", true);
            intent.putExtra("reminder_label", reminder.getLabel());

            PendingIntent alarmIntent = PendingIntent.getBroadcast(context, (int) reminder.getId(), intent, 0);

            if (reminder.isActive()) {
                activeRemindersCount++;
                Calendar calNow = Calendar.getInstance();
                Calendar calAlarm = Calendar.getInstance();
                calAlarm.setTime(reminder.getAlarmTime());
                calAlarm.set(Calendar.SECOND, 0);

                DateTime now = new DateTime(calNow.getTime());
                DateTime reminderDate = new DateTime(calAlarm);

                if (reminderDate.isBefore(now)) {
                    calAlarm.set(Calendar.DATE, calNow.get(Calendar.DATE));
                    calAlarm.add(Calendar.DATE, 1);
                    //Toast.makeText(context, "isBefore", Toast.LENGTH_LONG);
                    if(activity != null){
                        activity.showAlarmInfo("IS BEFORE");
                    }
                }
                else {
                    if(activity != null) {
                        activity.showAlarmInfo("IS AFTER");
                    }
                }

                Log.d("Glucosio", "Added reminder on " + calAlarm.get(Calendar.DAY_OF_MONTH) + " at " + calAlarm.get(Calendar.HOUR) + ":" + calAlarm.get(Calendar.MINUTE));
                //Toast.makeText(this.context, "Added reminder on " + calAlarm.get(Calendar.DAY_OF_MONTH) + " at " + calAlarm.get(Calendar.HOUR) + ":" + calAlarm.get(Calendar.MINUTE), Toast.LENGTH_LONG);

                activity.showAlarmInfo("Added reminder on " + calAlarm.get(Calendar.DAY_OF_MONTH) + " at " + calAlarm.get(Calendar.HOUR) + ":" + calAlarm.get(Calendar.MINUTE));
                alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calAlarm.getTimeInMillis(),
                        AlarmManager.INTERVAL_DAY, alarmIntent);
            } else {
                alarmMgr.cancel(alarmIntent);
            }
        }

        enableBootReceiver(activeRemindersCount > 0);
    }

    private void enableBootReceiver(boolean value) {
        ComponentName receiver = new ComponentName(context, GlucosioBroadcastReceiver.class);
        PackageManager pm = context.getPackageManager();

        int componentState = value ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;

        pm.setComponentEnabledSetting(receiver,
                componentState,
                PackageManager.DONT_KILL_APP);
    }
}
