package com.example.medicinereminderapp;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class MedicationHistory extends AppCompatActivity {
    RecyclerView rvMedicationHistory;
    MedicineIntakeAdapter medicineIntakeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_medication_history);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        rvMedicationHistory=findViewById(R.id.rv_medication_history);
        rvMedicationHistory.setLayoutManager(new LinearLayoutManager(this));

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        String uid;
        if (getIntent().hasExtra("selectedElderlyId")) {
            // If the activity received a string extra from the previous activity
            uid = getIntent().getStringExtra("selectedElderlyId");
        } else {
            // If no string extra was received (user not a caretaker), use the user's uid (elderly)
            uid = user.getUid();
        }

        FirebaseRecyclerOptions<TakenMedicine> options =
                new FirebaseRecyclerOptions.Builder<TakenMedicine>()
                        .setQuery(FirebaseDatabase.getInstance("https://medicinereminderapp-fca68-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("user").child(uid).child("history").orderByChild("intakeDate"), TakenMedicine.class)
                        .build();

        medicineIntakeAdapter = new MedicineIntakeAdapter(options);
        rvMedicationHistory.setAdapter(medicineIntakeAdapter);
    }
    protected void onStart() {

        super.onStart();
        medicineIntakeAdapter.startListening();
    }
    protected void onStop() {
        super.onStop();
        medicineIntakeAdapter.stopListening();
    }
}