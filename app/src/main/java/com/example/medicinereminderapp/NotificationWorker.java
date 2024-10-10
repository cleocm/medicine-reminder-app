package com.example.medicinereminderapp;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;


public class NotificationWorker extends Worker {
    private static final int REQUEST_POST_NOTIFICATIONS = 1001;

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        String medicineName = getInputData().getString("medicineName");
        String medicineTime = getInputData().getString("medicineTime");
        String medicineImage = getInputData().getString("medicineImage");

        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("MedicineReminder", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("notification_clicked", false);
        editor.apply();

        Uri soundUri = Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.raw.warning);

        Intent intent = new Intent(getApplicationContext(), MedicineReminder.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("medicineName", medicineName);
        intent.putExtra("medicineTime", medicineTime);
        intent.putExtra("medicineImage", medicineImage);

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "MedicineReminderChannel")
                .setSmallIcon(R.drawable.medicine_655968)
                .setContentTitle("Medicine Reminder")
                .setContentText("Time to take your medicine: " + medicineName)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSound(soundUri)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) this.getApplicationContext(),
                    new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                    REQUEST_POST_NOTIFICATIONS);
        } else {
            // Permission already granted, proceed with notifications
            notificationManager.notify(1, builder.build());
        }

        // Schedule to cancel the notification and send missed notification after 1 minute
        Intent cancelIntent = new Intent(getApplicationContext(), MissedNotificationReceiver.class);
        cancelIntent.putExtra("medicineName", medicineName);
        cancelIntent.putExtra("medicineTime", medicineTime);

        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 60000, cancelPendingIntent);
        }

        return Result.success();
    }
}
