package com.example.medicinereminderapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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

public class MainMenuCaretaker extends AppCompatActivity {

    DatabaseReference mDatabase;
    Button btnManageMedication, btnManageProfile, btnViewMedicationHistory, btnLogout;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_menu_caretaker);
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

        mAuth = FirebaseAuth.getInstance();
        String currentUid = mAuth.getUid();
        mDatabase = FirebaseDatabase.getInstance("https://medicinereminderapp-fca68-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();
        DatabaseReference linkedElderlyRef = mDatabase.child("user").child(currentUid).child("linked-elderly");

        this.getOnBackPressedDispatcher().addCallback(this, callback);

        //logout button
        btnLogout = findViewById(R.id.btn_logout_caretaker);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logOut();
            }
        });

        //manage medication
        btnManageMedication = findViewById(R.id.btn_managemed);
        btnManageMedication.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                linkedElderlyRef.addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<String> linkedElderlyUids = new ArrayList<>();
                        List<String> linkedElderlyNames = new ArrayList<>();
                        for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                            linkedElderlyUids.add(childSnapshot.getKey());
                            linkedElderlyNames.add(childSnapshot.getValue(String.class));
                        }
                        showLinkedElderlyDialog_ManageMedicine(linkedElderlyNames,linkedElderlyUids);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
        });

        btnViewMedicationHistory = findViewById(R.id.btn_viewmedhistory_caretaker);
        btnViewMedicationHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                linkedElderlyRef.addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<String> linkedElderlyUids = new ArrayList<>();
                        List<String> linkedElderlyNames = new ArrayList<>();
                        for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                            linkedElderlyUids.add(childSnapshot.getKey());
                            linkedElderlyNames.add(childSnapshot.getValue(String.class));
                        }
                        showLinkedElderlyDialog_ViewMedicationHistory(linkedElderlyNames,linkedElderlyUids);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        btnManageProfile = findViewById(R.id.btn_manageprofile);
        btnManageProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMenuCaretaker.this, ManageProfile.class);
                startActivity(intent);
            }
        });

    }
    private int selectedUserIndex = -1;
    private void showLinkedElderlyDialog_ManageMedicine(List<String> linkedElderlyNames, List<String> linkedElderlyUids){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Linked Elderly");

        if (linkedElderlyNames.size() > 0) {
            selectedUserIndex = 0;
        }
        builder.setSingleChoiceItems(new ArrayAdapter<>(this, android.R.layout.select_dialog_singlechoice, linkedElderlyNames), selectedUserIndex, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedUserIndex = which;
            }
        });
        builder.setPositiveButton("Select", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (selectedUserIndex != -1) {
                    String selectedElderlyId = linkedElderlyUids.get(selectedUserIndex);
                    Intent intent = new Intent(MainMenuCaretaker.this, ManageMedicine.class);
                    intent.putExtra("selectedElderlyId", selectedElderlyId);
                    startActivity(intent);
                }
            }
        });
        builder.setNegativeButton("Cancel", null);

        builder.show();
    }
    private void showLinkedElderlyDialog_ViewMedicationHistory(List<String> linkedElderlyNames, List<String> linkedElderlyUids){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Linked Elderly");

        if (linkedElderlyNames.size() > 0) {
            selectedUserIndex = 0;
        }
        builder.setSingleChoiceItems(new ArrayAdapter<>(this, android.R.layout.select_dialog_singlechoice, linkedElderlyNames), selectedUserIndex, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedUserIndex = which;
            }
        });
        builder.setPositiveButton("Select", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (selectedUserIndex != -1) {
                    String selectedElderlyId = linkedElderlyUids.get(selectedUserIndex);
                    Intent intent = new Intent(MainMenuCaretaker.this, MedicationHistory.class);
                    intent.putExtra("selectedElderlyId", selectedElderlyId);
                    startActivity(intent);
                }
            }
        });
        builder.setNegativeButton("Cancel", null);

        builder.show();
    }


    public void logOut(){
        mAuth.signOut();
        Toast.makeText(this,"Logged out successfully", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
    }

}