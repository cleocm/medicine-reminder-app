package com.example.medicinereminderapp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MissedNotificationReceiver extends BroadcastReceiver {

    private static final int REQUEST_POST_NOTIFICATIONS = 1001;
    String medicineName, medicineTime;

    @Override
    public void onReceive(Context context, Intent intent) {

        // Check if the notification was clicked
        SharedPreferences sharedPreferences = context.getSharedPreferences("MedicineReminder", Context.MODE_PRIVATE);
        boolean notificationClicked = sharedPreferences.getBoolean("notification_clicked", false);

        if (!notificationClicked) {
            medicineName = intent.getStringExtra("medicineName");
            medicineTime = intent.getStringExtra("medicineTime");

            // Cancel the initial notification
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.cancel(1);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "MedicineReminderChannel")
                    .setSmallIcon(R.drawable.medicine_655968)
                    .setContentTitle("Missed Medicine Reminder")
                    .setContentText("You missed your medicine: " + medicineName)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true);

            insertSkippedMedicineIntakeRecord();

            if (ContextCompat.checkSelfPermission(context,
                    android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) context,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_POST_NOTIFICATIONS);
            } else {
                // Permission already granted, proceed with notifications
                notificationManager.notify(2, builder.build());
            }
        }



    }
    private void insertSkippedMedicineIntakeRecord(){
        Map<String, Object> map = new HashMap<>();
        map.put("medicineName",medicineName);
        map.put("medicineTime",medicineTime);
        map.put("hasTaken", false);
        map.put("intakeDate", getCurrentDate());
        insertIntoDatabase(map);
    }

    private void insertIntoDatabase(Map<String, Object> map){

        String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseDatabase.getInstance("https://medicinereminderapp-fca68-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("user").child(currentUid).child("history").push()
                .setValue(map);
    }
    private String getCurrentDate(){
        // Get current time in milliseconds
        long currentTimeMillis = System.currentTimeMillis();
        // Create a Date object
        Date currentDate = new Date(currentTimeMillis);
        // Define the date format
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        // Format the date
        String formattedDate = dateFormat.format(currentDate);
        return formattedDate;
    }
}