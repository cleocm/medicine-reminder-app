package com.example.medicinereminderapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MedicineReminder extends AppCompatActivity {

    Button btnTaken, btnSkipped;
    TextView tvMedName, tvMedTime;
    ImageView ivMedImage;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_medicine_reminder);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Toast.makeText(MedicineReminder.this, "Please update the status of your medication intake.", Toast.LENGTH_SHORT).show();

            }
        };

        this.getOnBackPressedDispatcher().addCallback(this, callback);


        // Mark the notification as clicked
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("MedicineReminder", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("notification_clicked", true);
        editor.apply();


        Intent notificationIntent = getIntent();
        String medicineName = notificationIntent.getStringExtra("medicineName");
        String medicineTime = notificationIntent.getStringExtra("medicineTime");
        String medicineImage = notificationIntent.getStringExtra("medicineImage");
        TimeConverter tc = new TimeConverter();

        tvMedName = findViewById(R.id.tv_med_name);
        tvMedName.setText(medicineName);
        tvMedTime = findViewById(R.id.tv_med_time);
        tvMedTime.setText(tc.convert24HourTo12Hour(medicineTime));
        ivMedImage = findViewById(R.id.iv_med_image);
        Picasso.get().load(medicineImage).into(ivMedImage);
        btnTaken = findViewById(R.id.btn_taken);
        btnSkipped = findViewById(R.id.btn_skipped);
        mAuth = FirebaseAuth.getInstance();


        Intent intent = new Intent(MedicineReminder.this, MainMenuElderly.class);

        new CountDownTimer(10000,1000){
            public void onTick(long millisUntilFinished) {

                btnTaken.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        insertTakenMedicineIntakeRecord();
                        cancel();
                        startActivity(intent);
                    }
                });
                btnSkipped.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        insertSkippedMedicineIntakeRecord();
                        startActivity(intent);
                        cancel();
                    }
                });
            }
            public void onFinish() {
                insertSkippedMedicineIntakeRecord();
                startActivity(intent);
            }
        }.start();


    }

    private void insertTakenMedicineIntakeRecord(){
        Map<String, Object> map = new HashMap<>();
        map.put("medicineName",tvMedName.getText().toString().trim());
        TimeConverter tc = new TimeConverter();
        String time = tc.convert12HourTo24Hour(tvMedTime.getText().toString().trim());
        map.put("medicineTime",time);
        map.put("hasTaken", true);
        map.put("intakeDate", getCurrentDate());
        insertIntoDatabase(map);

    }

    private void insertSkippedMedicineIntakeRecord(){
        Map<String, Object> map = new HashMap<>();
        map.put("medicineName",tvMedName.getText().toString().trim());
        TimeConverter tc = new TimeConverter();
        String time = tc.convert12HourTo24Hour(tvMedTime.getText().toString().trim());
        map.put("medicineTime",time);
        map.put("hasTaken", false);
        map.put("intakeDate", getCurrentDate());
        insertIntoDatabase(map);

    }

    private void insertIntoDatabase(Map<String, Object> map){

        String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseDatabase.getInstance("https://medicinereminderapp-fca68-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("user").child(currentUid).child("history").push()
                .setValue(map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(MedicineReminder.this, "Medicine intake recorded successfully.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MedicineReminder.this, "Error while adding data.", Toast.LENGTH_SHORT).show();
                    }
                });
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