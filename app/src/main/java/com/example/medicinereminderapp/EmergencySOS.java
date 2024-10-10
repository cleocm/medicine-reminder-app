package com.example.medicinereminderapp;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.telephony.SmsManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class EmergencySOS extends AppCompatActivity {

    TextView tvCountdown;
    Button btnCancel, btnProceed;
    private static final int SMS_PERMISSION_CODE = 100;
    FirebaseAuth mAuth;
    private List<String> emergencyContacts;
    private String uid;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_emergency_sos);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvCountdown = findViewById(R.id.tv_countdown_emergencysos);
        btnCancel = findViewById(R.id.btn_cancel_emergencysos);
        btnProceed = findViewById(R.id.btn_proceed_emergencysos);

        emergencyContacts = new ArrayList<>();

        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid();

        // Check for SMS permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE);
        }


        startCountdown();

        // Retrieve emergency contacts from Firebase
        retrieveEmergencyContacts();

        btnCancel.setOnClickListener(view -> {
            if (countDownTimer != null) {
                countDownTimer.cancel();
                Toast.makeText(EmergencySOS.this, "Emergency canceled", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(EmergencySOS.this, MainMenuElderly.class);
                startActivity(intent);
            }
        });

        btnProceed.setOnClickListener(view -> {
            if (countDownTimer != null) {
                countDownTimer.cancel();
                proceedWithEmergency();
            }
        });
    }

    private void startCountdown() {
        countDownTimer = new CountDownTimer(10000, 1000) {
            public void onTick(long millisUntilFinished) {
                tvCountdown.setText(String.valueOf(millisUntilFinished / 1000));
            }
            public void onFinish() {
                proceedWithEmergency();
            }
        }.start();
    }

    private void retrieveEmergencyContacts() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("user")
                .child(uid).child("emergency-contacts");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                emergencyContacts.clear();
                for (DataSnapshot contactSnapshot : snapshot.getChildren()) {
                    String phoneNumber = contactSnapshot.getValue(String.class);
                    if (phoneNumber != null) {
                        emergencyContacts.add(phoneNumber);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EmergencySOS.this,
                        "Failed to retrieve emergency contacts", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendSOSMessages() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            if(!emergencyContacts.isEmpty()){
                SmsManager smsManager = SmsManager.getDefault();
                String message = "SOS! I need help immediately.";

                for (String phoneNumber : emergencyContacts) {
                    smsManager.sendTextMessage(phoneNumber, null,
                            message, null, null);
                }
                Toast.makeText(this, "SOS messages sent", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this,
                        "No emergency contacts found.\nNo SOS messages were sent.",
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this,
                    "SMS permission not granted",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void proceedWithEmergency() {
        sendSOSMessages();
        simulateEmergencyCall();

        //return user to main menu 5 seconds after Emergency SOS operation is completed
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(EmergencySOS.this, MainMenuElderly.class);
                startActivity(intent);
            }
        }, 5000);

    }

    private void simulateEmergencyCall() {
        AlertDialog emergencyCallDialog = new AlertDialog.Builder(this)
                .setTitle("Emergency Call")
                .setMessage("Simulating call to emergency services (999)...")
                .setPositiveButton("OK", (dialogInterface, which) ->
                        Toast.makeText(EmergencySOS.this, "Emergency call simulated", Toast.LENGTH_SHORT).show())
                .setCancelable(false) // Prevent the dialog from being dismissed by user
                .create();

        emergencyCallDialog.show();

        // Automatically dismiss the dialog after 5 seconds
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (emergencyCallDialog.isShowing()) {
                    emergencyCallDialog.dismiss();
                    Toast.makeText(EmergencySOS.this, "Emergency call simulated", Toast.LENGTH_SHORT).show();
                }
            }
        }, 4000); // 1000 milliseconds = 1 second
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SMS permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}