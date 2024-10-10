package com.example.medicinereminderapp;

import static android.view.View.GONE;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ManageProfileElderly extends AppCompatActivity {

    Button btnEditEmailPassword, btnCopyUid, btnUpdateEmergencyContacts, btnDeleteEmergencyContacts;
    EditText etUid, etEmergencyContact1, etEmergencyContact2, etEmergencyContact3;

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    String uid;
    private static final String PHONE_REGEX = "^01\\d{8,9}$";

    public static boolean isValidPhoneNumber(String phoneNumber) {
        Pattern pattern = Pattern.compile(PHONE_REGEX);
        Matcher matcher = pattern.matcher(phoneNumber);
        return matcher.matches();
    }
    Boolean et2PreviouslyEmpty;
    Boolean et3PreviouslyEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manage_profile_elderly);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        uid = user.getUid();
        btnEditEmailPassword = findViewById(R.id.btn_editemailpassword_manageprofileelderly);
        btnCopyUid = findViewById(R.id.btn_copy_uid_manageprofileelderly);
        btnDeleteEmergencyContacts = findViewById(R.id.btn_deleteemergencycontacts_manageprofileelderly);
        btnUpdateEmergencyContacts = findViewById(R.id.btn_updateemergencycontacts_manageprofileelderly);
        etUid = findViewById(R.id.et_uid_manageprofileelderly);
        etEmergencyContact1 = findViewById(R.id.et_emergencycontact1_manageprofileelderly);
        etEmergencyContact2 = findViewById(R.id.et_emergencycontact2_manageprofileelderly);
        etEmergencyContact3 = findViewById(R.id.et_emergencycontact3_manageprofileelderly);


        etUid.setText(uid);
        btnEditEmailPassword.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("uid", etUid.getText().toString());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
        });
        btnEditEmailPassword.setOnClickListener(v -> {
            showPasswordVerificationDialog();
        });

        //cant change emergency contact 1 which is always going to be the caretaker's number
        etEmergencyContact1.setFocusable(false);
        etEmergencyContact1.setInputType(0);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("user").child(uid).child("emergency-contacts");
        // Fetch and display emergency contacts
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int contactCount = 0;
                    for (DataSnapshot contactSnapshot : snapshot.getChildren()) {
                        String contact = contactSnapshot.getValue(String.class);
                        contactCount++;
                        switch (contactCount) {
                            case 1:
                                etEmergencyContact1.setText(contact);
                                etEmergencyContact2.setText("");
                                et2PreviouslyEmpty = true;
                                etEmergencyContact2.setInputType(3);
                                etEmergencyContact3.setText("");
                                et3PreviouslyEmpty = true;
                                etEmergencyContact3.setInputType(3);
                                break;
                            case 2:
                                etEmergencyContact2.setText(contact);
                                etEmergencyContact2.setInputType(0);
                                et2PreviouslyEmpty = false;
                                etEmergencyContact3.setText("");
                                et3PreviouslyEmpty = true;
                                etEmergencyContact3.setInputType(3);
                                break;
                            case 3:
                                etEmergencyContact3.setText(contact);
                                etEmergencyContact3.setInputType(0);
                                et2PreviouslyEmpty = false;
                                et3PreviouslyEmpty = false;
                                break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database error
                Toast.makeText(ManageProfileElderly.this, "Failed to fetch emergency contacts", Toast.LENGTH_SHORT).show();
            }
        });

        btnDeleteEmergencyContacts.setOnClickListener(v -> {
            checkNumberOfContacts();
        });
        btnUpdateEmergencyContacts.setOnClickListener(v -> {

            if (etIsValid()) {
                String contact2 = "", contact3="";
                //valid phone number
                if (!etEmergencyContact2.getText().toString().isEmpty() && et2PreviouslyEmpty) {
                    contact2 = etEmergencyContact2.getText().toString();
                    DatabaseReference refAdd = FirebaseDatabase.getInstance().getReference("user").child(uid).child("emergency-contacts");
                    refAdd.push().setValue(contact2);
                    Toast.makeText(ManageProfileElderly.this, "Emergency contact 2 added/updated.", Toast.LENGTH_SHORT).show();
                }
                if (!etEmergencyContact3.getText().toString().isEmpty() && et3PreviouslyEmpty) {
                    contact3 = etEmergencyContact3.getText().toString();
                    DatabaseReference refAdd = FirebaseDatabase.getInstance().getReference("user").child(uid).child("emergency-contacts");
                    refAdd.push().setValue(contact3);
                    Toast.makeText(ManageProfileElderly.this, "Emergency contact 3 added/updated.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private Boolean etIsValid(){
        Boolean valid = true;
        if (!etEmergencyContact2.getText().toString().isEmpty() && !isValidPhoneNumber(etEmergencyContact2.getText().toString())) {
            etEmergencyContact2.setError("Invalid phone number");
            valid = false;
        }
        if (!etEmergencyContact3.getText().toString().isEmpty() && !isValidPhoneNumber(etEmergencyContact3.getText().toString())) {
            etEmergencyContact3.setError("Invalid phone number");
            valid = false;
        }
        return valid;
    }
    private void showPasswordVerificationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_password_verification, null);
        builder.setView(dialogView);

        final EditText currentPassword = dialogView.findViewById(R.id.et_currentpassword_passwordverificationdialog);
        Button verifyPasswordButton = dialogView.findViewById(R.id.btn_verifypassword_passwordverificationdialog);

        final AlertDialog dialog = builder.create();

        verifyPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = currentPassword.getText().toString().trim();

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(ManageProfileElderly.this, "Password is required", Toast.LENGTH_SHORT).show();
                    return;
                }

                AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);
                user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(ManageProfileElderly.this, "Authentication successful.", Toast.LENGTH_SHORT).show();
                            showUpdateEmailPasswordDialog();
                            dialog.dismiss();
                        } else {
                            Toast.makeText(ManageProfileElderly.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(e -> {
                    Toast.makeText(ManageProfileElderly.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
            }
        });
        dialog.show();
    }

    private void showUpdateEmailPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_update_email_password, null);
        builder.setView(dialogView);

        final EditText newEmail = dialogView.findViewById(R.id.et_new_email_updateemailpassword);
        final EditText newPassword = dialogView.findViewById(R.id.et_new_password_updateemailpassword);
        final EditText reenterPassword = dialogView.findViewById(R.id.et_reenter_new_password_updateemailpassword);
        Button updateEmailPasswordButton = dialogView.findViewById(R.id.btn_update_updateemailpassword);

        final AlertDialog dialog = builder.create();

        updateEmailPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = newEmail.getText().toString().trim();
                String password = newPassword.getText().toString().trim();
                String reenteredPassword = reenterPassword.getText().toString().trim();

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(reenteredPassword)) {
                    Toast.makeText(ManageProfileElderly.this, "All fields are required", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!password.equals(reenteredPassword)) {
                    Toast.makeText(ManageProfileElderly.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (user != null) {
                    user.verifyBeforeUpdateEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                user.updatePassword(password).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(ManageProfileElderly.this, "Email updated. Verification email sent to " + email, Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(ManageProfileElderly.this, "Failed to update password.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            } else {
                                Toast.makeText(ManageProfileElderly.this, "Failed to update email: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(e -> {
                        Toast.makeText(ManageProfileElderly.this, "Update email failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        e.printStackTrace(); // Log the error
                    });
                } else {
                    Toast.makeText(ManageProfileElderly.this, "User not authenticated", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void checkNumberOfContacts() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("user").child(uid).child("emergency-contacts");

        // First check if there are more than one contacts
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int contactCount = 0;
                for (DataSnapshot contactSnapshot : snapshot.getChildren()) {
                    contactCount++;
                }

                if (contactCount > 1) {
                    // If there are more than one contacts, show the dialog
                    showManageContactsDialog();
                } else {
                    // If there is only one contact, show a message
                    Toast.makeText(ManageProfileElderly.this, "No modifiable emergency contacts.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManageProfileElderly.this, "Failed to fetch emergency contacts", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showManageContactsDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_manage_emergency_contacts, null);
        builder.setView(dialogView);

        builder.setTitle("Manage Emergency Contacts");

        TextView tvContact2 = dialogView.findViewById(R.id.tvContact2);
        CheckBox cbContact2 = dialogView.findViewById(R.id.cbContact2);
        TextView tvContact3 = dialogView.findViewById(R.id.tvContact3);
        CheckBox cbContact3 = dialogView.findViewById(R.id.cbContact3);
        Button btnDeleteContacts = dialogView.findViewById(R.id.btnDeleteContacts);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("user").child(uid).child("emergency-contacts");

        List<String> contactKeys = new ArrayList<>();
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int contactCount = 0;
                    for (DataSnapshot contactSnapshot : snapshot.getChildren()) {
                        String contact = contactSnapshot.getValue(String.class);
                        contactKeys.add(contactSnapshot.getKey());
                        contactCount++;
                        switch (contactCount) {
                            case 1:
                                break;
                            case 2:
                                tvContact2.setText(contact);
                                tvContact2.setVisibility(View.VISIBLE);
                                cbContact2.setVisibility(View.VISIBLE);
                                break;
                            case 3:
                                tvContact3.setText(contact);
                                tvContact3.setVisibility(View.VISIBLE);
                                cbContact3.setVisibility(View.VISIBLE);
                                break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database error
                Toast.makeText(ManageProfileElderly.this, "Failed to fetch emergency contacts", Toast.LENGTH_SHORT).show();
            }
        });

        AlertDialog dialog = builder.create();

        btnDeleteContacts.setOnClickListener(v -> {
            int checkedCount=0;
            if (cbContact2.isChecked()) {
                // Remove contact 2
                if (!contactKeys.isEmpty() && contactKeys.size() >= 2) {
                    String key = contactKeys.get(1); // Index 1 corresponds to contact 2
                    ref.child(key).removeValue();

                }
            }
            if (cbContact3.isChecked()) {
                // Remove contact 3
                if (!contactKeys.isEmpty() && contactKeys.size() >= 3) {
                    String key = contactKeys.get(2); // Index 2 corresponds to contact 3
                    ref.child(key).removeValue();

                }
            }

            dialog.dismiss();
            Toast.makeText(ManageProfileElderly.this, "Selected contacts deleted", Toast.LENGTH_SHORT).show();
        });

        dialog.show();
    }
}