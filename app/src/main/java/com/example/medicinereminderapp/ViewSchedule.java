package com.example.medicinereminderapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

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

public class ViewSchedule extends AppCompatActivity {

    RecyclerView recyclerView;
    MedicineAdapter medicineAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_schedule);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        recyclerView = findViewById(R.id.medicinerv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        String uid = user.getUid();

        FirebaseRecyclerOptions<Medicine> options =
                new FirebaseRecyclerOptions.Builder<Medicine>()
                        .setQuery(FirebaseDatabase.getInstance("https://medicinereminderapp-fca68-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("user").child(uid).child("medicine").orderByChild("medicineTime"), Medicine.class)
                        .build();

        medicineAdapter = new MedicineAdapter(options);
        recyclerView.setAdapter(medicineAdapter);

    }
    protected void onStart() {
        super.onStart();
        medicineAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        medicineAdapter.stopListening();
    }

}