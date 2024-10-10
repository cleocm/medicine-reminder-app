package com.example.medicinereminderapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class AddMedicine extends AppCompatActivity {

    Button btnConfirm, btnCancel;
    EditText etName, etDose,etDesc, etImage, etTime;
    String uid;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_medicine);

        uid = getIntent().getStringExtra("selectedElderlyId");

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(getApplicationContext(), ManageMedicine.class);
                intent.putExtra("selectedElderlyId", uid);
                startActivity(intent);
            }
        };

        this.getOnBackPressedDispatcher().addCallback(this, callback);

        etName = findViewById(R.id.et_name_add);
        etDesc = findViewById(R.id.et_desc_add);
        etDose = findViewById(R.id.et_dose_add);
        etImage = findViewById(R.id.et_imagelink_add);
        etTime = findViewById(R.id.et_time_add);

        btnConfirm = findViewById(R.id.btn_confirm_add);
        btnCancel = findViewById(R.id.btn_cancel_add);


        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ManageMedicine.class);
                intent.putExtra("selectedElderlyId", uid);
                startActivity(intent);
            }
        });
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean isValid = true;

                if (!isValidInput(etName)){
                    etName.setError("Please fill in the medicine name.");
                    isValid = false;
                }
                if (!isValidInput(etDose)){
                    etDose.setError("Please fill in the medicine dose.");
                    isValid = false;
                }
                if (!isValidInput(etTime)){
                    etTime.setError("Please fill in the medicine intake time.");
                    isValid = false;
                } else if (!isValidTime(etTime.getText().toString().trim())){
                    etTime.setError("Invalid time format (HHMM)");
                    isValid = false;
                }
                if (isValid){
                    insertMedicine();
                    clearAll();
                    Intent intent = new Intent(AddMedicine.this, ManageMedicine.class);
                    intent.putExtra("selectedElderlyId", uid);
                    startActivity(intent);
                }
            }
        });

    }

    private void insertMedicine(){
        Map<String,Object> map = new HashMap<>();
        map.put("medicineName",etName.getText().toString().trim());
        map.put("medicineDosage",etDose.getText().toString().trim());
        map.put("medicineDescription",etDesc.getText().toString().trim());
        map.put("medicineImage",etImage.getText().toString().trim());
        map.put("medicineTime",etTime.getText().toString().trim());

        FirebaseDatabase.getInstance("https://medicinereminderapp-fca68-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference().child("user").child(uid).child("medicine").push()
                .setValue(map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    final String message = etName.getText().toString().trim() + " added successfully.";
                    @Override
                    public void onSuccess(Void unused) {

                        Toast.makeText(AddMedicine.this, message, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddMedicine.this, "Error while adding data.", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void clearAll(){
        etName.setText("");
        etTime.setText("");
        etImage.setText("");
        etDose.setText("");
        etDesc.setText("");
    }
    private static final Pattern TIME_PATTERN = Pattern.compile("^([01]\\d|2[0-3])[0-5]\\d$");

    private boolean isValidTime(String time) {
        return !TextUtils.isEmpty(time) && TIME_PATTERN.matcher(time).matches();
    }

    private boolean isValidInput(EditText et){
        Boolean valid=true;
        if (et.getText().toString().trim().isEmpty()){
            valid = false;
        }
        return valid;
    }
}