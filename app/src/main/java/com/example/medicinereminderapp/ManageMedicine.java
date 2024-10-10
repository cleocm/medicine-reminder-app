package com.example.medicinereminderapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.FirebaseDatabase;

public class ManageMedicine extends AppCompatActivity {

    RecyclerView recyclerView;
    ManageMedicineAdapter manageMedicineAdapter;
    Button btnAdd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manage_medicine);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(getApplicationContext(), MainMenuCaretaker.class);
                startActivity(intent);
            }
        };

        this.getOnBackPressedDispatcher().addCallback(this, callback);

        String selectedElderlyId = getIntent().getStringExtra("selectedElderlyId");

        btnAdd = findViewById(R.id.btn_add);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AddMedicine.class);
                intent.putExtra("selectedElderlyId", selectedElderlyId);
                startActivity(intent);
            }
        });

        recyclerView = findViewById(R.id.medicinerv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FirebaseRecyclerOptions<Medicine> options =
                new FirebaseRecyclerOptions.Builder<Medicine>()
                        .setQuery(FirebaseDatabase.getInstance("https://medicinereminderapp-fca68-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("user").child(selectedElderlyId).child("medicine").orderByChild("medicineTime"), Medicine.class)
                        .build();

        manageMedicineAdapter = new ManageMedicineAdapter(options, selectedElderlyId);
        recyclerView.setAdapter(manageMedicineAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        manageMedicineAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        manageMedicineAdapter.stopListening();
    }
}