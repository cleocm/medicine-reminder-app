package com.example.medicinereminderapp;

import static com.example.medicinereminderapp.NotificationUtils.getWorkRequestId;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class MainMenuElderly extends AppCompatActivity {
    FirebaseAuth mAuth;
    DatabaseReference mDatabase;
    Button btnViewMedSchedule, btnViewMedHistory, btnEmergency, btnManageProfile, btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_menu_elderly);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;

        });
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                //do nothing
            }
        };

        this.getOnBackPressedDispatcher().addCallback(this, callback);

        mDatabase = FirebaseDatabase.getInstance("https://medicinereminderapp-fca68-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            loadMedicineReminders(currentUser.getUid());
        }

        btnViewMedSchedule = findViewById(R.id.btn_viewmedschedule_elderly);
        btnViewMedSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMenuElderly.this, ViewSchedule.class);
                startActivity(intent);
            }
        });
        btnViewMedHistory = findViewById(R.id.btn_viewmedhistory_elderly);
        btnViewMedHistory.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMenuElderly.this, MedicationHistory.class);
                startActivity(intent);
            }
        }));
        btnEmergency = findViewById(R.id.btn_emergencysos);
        btnEmergency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMenuElderly.this, EmergencySOS.class);
                startActivity(intent);
            }

        });
        btnLogout = findViewById(R.id.btn_logout_elderly);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logOut();
            }
        });
        btnManageProfile = findViewById(R.id.btn_manageprofile_mainmenuelderly);
        btnManageProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMenuElderly.this, ManageProfileElderly.class);
                startActivity(intent);
            }

        });

        // Create notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("MedicineReminderChannel",
                    "Medicine Reminder Channel", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Channel for Medicine Reminders");
            channel.setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.warning),
                    new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                            .build());
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }


    }

    public void logOut(){
        mAuth.signOut();
        Toast.makeText(this,"Logged out successfully", Toast.LENGTH_SHORT).show();
        NotificationUtils.clearAllReminders(this);
        WorkManager.getInstance(this).cancelAllWork();
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
    }

    private void loadMedicineReminders(String userId) {
        mDatabase.child("user").child(userId).child("medicine")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot medicineSnapshot : snapshot.getChildren()) {
                            String medicineName = medicineSnapshot.child("medicineName").getValue(String.class);
                            String medicineTime = medicineSnapshot.child("medicineTime").getValue(String.class);
                            String medicineImage = medicineSnapshot.child("medicineImage").getValue(String.class);

                            scheduleNotification(medicineName, medicineTime, medicineImage);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void scheduleNotification(String medicineName, String medicineTime, String medicineImage) {
        // Cancel the existing WorkRequest if it exists
        UUID existingWorkRequestId = NotificationUtils.getWorkRequestId(this, medicineName);
        if (existingWorkRequestId != null) {
            WorkManager.getInstance(this).cancelWorkById(existingWorkRequestId);
            // Remove the existing WorkRequest ID from SharedPreferences
            NotificationUtils.removeWorkRequestId(this, medicineName);
        }

        int hour = Integer.parseInt(medicineTime.substring(0,2));
        int minute = Integer.parseInt(medicineTime.substring(2));

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        long delay = calendar.getTimeInMillis() - System.currentTimeMillis();

        if (delay < 0) {
            // If time has already passed today, schedule for next day
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            delay = calendar.getTimeInMillis() - System.currentTimeMillis();
        }

        Data data = new Data.Builder()
                .putString("medicineName", medicineName)
                .putString("medicineTime", medicineTime)
                .putString("medicineImage", medicineImage)
                .build();

        OneTimeWorkRequest notificationWork = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .build();

        WorkManager.getInstance(this).enqueue(notificationWork);

        // Save the WorkRequest ID
        NotificationUtils.saveWorkRequestId(this, medicineName, notificationWork.getId());

    }

}