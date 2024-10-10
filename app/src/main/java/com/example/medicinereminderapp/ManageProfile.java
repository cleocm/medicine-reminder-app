package com.example.medicinereminderapp;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Space;
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
import com.google.android.gms.tasks.OnSuccessListener;
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


public class ManageProfile extends AppCompatActivity {

    Button  btnEditEmailPassword, btnCopyUid, btnEditPhoneNumber, btnLinkElderly1, btnLinkElderly2, btnUnlinkElderly1, btnUnlinkElderly2, btnUpdateElderlyName1, btnUpdateElderlyName2;
    EditText etUid, etElderlyName1, etElderlyUid1, etElderlyName2, etElderlyUid2;
    Space space1,space2;

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    String uid;
    private static final String PHONE_REGEX = "^01\\d{8,9}$";

    public static boolean isValidPhoneNumber(String phoneNumber) {
        Pattern pattern = Pattern.compile(PHONE_REGEX);
        Matcher matcher = pattern.matcher(phoneNumber);
        return matcher.matches();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manage_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Get the currently authenticated user
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        uid = mAuth.getUid();

        etUid = findViewById(R.id.et_uid_manageprofile);
        etUid.setText(uid);
        btnCopyUid = findViewById(R.id.btn_copy_uid_manageprofile);
        btnCopyUid.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("uid", etUid.getText().toString());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
        });

        btnEditEmailPassword = findViewById(R.id.btn_editemailpassword_manageprofile);
        btnEditEmailPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPasswordVerificationDialog();
            }
        });

        btnEditPhoneNumber = findViewById(R.id.btn_editphonenumber_manageprofile);

        btnEditPhoneNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddOrUpdatePhoneNumberDialog();
            }
        });

        //link elderly
        btnLinkElderly1 = findViewById(R.id.btn_linkelderly1_manageprofile);
        btnLinkElderly2 = findViewById(R.id.btn_linkelderly2_manageprofile);
        btnUnlinkElderly1 = findViewById(R.id.btn_unlinkelderly1_manageprofile);
        btnUnlinkElderly2 = findViewById(R.id.btn_unlinkelderly2_manageprofile);
        btnUpdateElderlyName1 = findViewById(R.id.btn_updateelderlyname1_manageprofile);
        btnUpdateElderlyName2 = findViewById(R.id.btn_updateelderlyname2_manageprofile);
        etElderlyName1 = findViewById(R.id.et_elderlyname1_manageprofile);
        etElderlyName2 = findViewById(R.id.et_elderlyname2_manageprofile);
        etElderlyUid1 = findViewById(R.id.et_elderlyuid1_manageprofile);
        etElderlyUid2 = findViewById(R.id.et_elderlyuid2_manageprofile);
        space1 = findViewById(R.id.space_link_unlink_1);
        space2 = findViewById(R.id.space_link_unlink_2);

        DatabaseReference refLinkedElderly = FirebaseDatabase.getInstance().getReference("user").child(uid).child("linked-elderly");
        refLinkedElderly.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> elderlyUids = new ArrayList<>();
                List<String> elderlyNames = new ArrayList<>();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    elderlyUids.add(childSnapshot.getKey());
                    elderlyNames.add(childSnapshot.getValue(String.class));
                }

                if (elderlyUids.size() > 0) {
                    etElderlyUid1.setText(elderlyUids.get(0));
                    etElderlyName1.setText(elderlyNames.get(0));
                    etElderlyUid1.setInputType(0);
                    btnLinkElderly1.setVisibility(View.GONE);
                    btnUpdateElderlyName1.setVisibility(View.VISIBLE);
                    space1.setVisibility(View.VISIBLE);
                    btnUnlinkElderly1.setVisibility(View.VISIBLE);
                }else{
                    etElderlyUid1.setText("");
                    etElderlyName1.setText("");
                    etElderlyUid1.setInputType(1);
                    etElderlyUid1.setEnabled(true);
                    btnLinkElderly1.setVisibility(View.VISIBLE);
                    btnUnlinkElderly1.setVisibility(View.GONE);
                    space1.setVisibility(View.GONE);
                    btnUpdateElderlyName1.setVisibility(View.GONE);
                }
                if (elderlyUids.size() > 1) {
                    etElderlyUid2.setText(elderlyUids.get(1));
                    etElderlyName2.setText(elderlyNames.get(1));
                    etElderlyUid2.setInputType(0);
                    btnLinkElderly2.setVisibility(View.GONE);
                    btnUpdateElderlyName2.setVisibility(View.VISIBLE);
                    space2.setVisibility(View.VISIBLE);
                    btnUnlinkElderly2.setVisibility(View.VISIBLE);
                }else {
                    etElderlyUid2.setText("");
                    etElderlyName2.setText("");
                    etElderlyUid2.setInputType(InputType.TYPE_CLASS_TEXT);
                    etElderlyUid2.setEnabled(true);
                    btnLinkElderly2.setVisibility(View.VISIBLE);
                    btnUnlinkElderly2.setVisibility(View.GONE);
                    space2.setVisibility(View.GONE);
                    btnUpdateElderlyName2.setVisibility(View.GONE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
                Toast.makeText(ManageProfile.this, "Failed to fetch linked elderly data.", Toast.LENGTH_SHORT).show();
            }
        });


        btnUnlinkElderly1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unlinkElderly(etElderlyUid1.getText().toString().trim());
            }
        });
        btnUnlinkElderly2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unlinkElderly(etElderlyUid2.getText().toString().trim());
            }
        });

        btnLinkElderly1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check if user that is to be linked has the right role
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("user").child(etElderlyUid1.getText().toString());
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String role = snapshot.child("role").getValue(String.class);
                            Boolean linked = snapshot.child("linked").getValue(Boolean.class);
                            if ("elderly".equals(role)) {
                                //if role is elderly, check if user has been linked to other users.
                                if(linked){
                                    Toast.makeText(ManageProfile.this,"User may have already been linked to other caretakers.",Toast.LENGTH_SHORT).show();
                                    etElderlyUid1.setText("");
                                    etElderlyName1.setText("");
                                }else{
                                    //if not linked, then proceed to link user.
                                    linkElderly(etElderlyUid1.getText().toString(), etElderlyName1.getText().toString().trim());
                                    Toast.makeText(ManageProfile.this,"User has successfully been linked.",Toast.LENGTH_SHORT).show();
                                }} else {
                                Toast.makeText(ManageProfile.this, "This UID is not an Elderly user.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(ManageProfile.this, "Invalid UID.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ManageProfile.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        btnLinkElderly2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //check if user that is to be linked has the right role
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("user").child(etElderlyUid2.getText().toString());
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String role = snapshot.child("role").getValue(String.class);
                            Boolean linked = snapshot.child("linked").getValue(Boolean.class);
                            if ("elderly".equals(role)) {
                                //if role is elderly, check if user has been linked to other users.
                                if(linked){
                                    Toast.makeText(ManageProfile.this,"User may have already been linked to other caretakers.",Toast.LENGTH_SHORT).show();
                                    etElderlyUid2.setText("");
                                    etElderlyName2.setText("");
                                }else{
                                    //if not linked, then proceed to link user.
                                    linkElderly(etElderlyUid2.getText().toString(), etElderlyName2.getText().toString().trim());
                                    Toast.makeText(ManageProfile.this,"User has successfully been linked.",Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(ManageProfile.this, "This UID is not an Elderly user.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(ManageProfile.this, "Invalid UID.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ManageProfile.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        btnUpdateElderlyName1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateElderlyName(etElderlyUid1.getText().toString(), etElderlyName1.getText().toString());
                Toast.makeText(ManageProfile.this,"Elderly User's nickname has been updated.",Toast.LENGTH_SHORT).show();
            }
        });
        btnUpdateElderlyName2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateElderlyName(etElderlyUid2.getText().toString(), etElderlyName2.getText().toString());
                Toast.makeText(ManageProfile.this,"Elderly User's nickname has been updated.",Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void linkElderly(String elderlyUid, String elderlyName) {
        DatabaseReference refCaretaker = FirebaseDatabase.getInstance().getReference("user").child(mAuth.getUid());
        refCaretaker.child("linked-elderly").child(elderlyUid).setValue(elderlyName);
        DatabaseReference refElderlyUser = FirebaseDatabase.getInstance().getReference("user").child(elderlyUid);
        refElderlyUser.child("linked").setValue(true);
        refCaretaker.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String phoneNumber = snapshot.child("phone-number").getValue(String.class);
                    refElderlyUser.child("emergency-contacts").child("1").setValue(phoneNumber);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void updateElderlyName(String elderlyUid, String nickname) {
        DatabaseReference refLinkedElderly = FirebaseDatabase.getInstance().getReference("user").child(mAuth.getUid()).child("linked-elderly").child(elderlyUid);
        refLinkedElderly.setValue(nickname);
    }

    private void unlinkElderly(String elderlyUid) {
        DatabaseReference refLinkedElderly = FirebaseDatabase.getInstance().getReference("user").child(mAuth.getUid()).child("linked-elderly").child(elderlyUid);
        DatabaseReference refElderlyUser = FirebaseDatabase.getInstance().getReference("user").child(elderlyUid);
        refLinkedElderly.removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(ManageProfile.this, "Elderly unlinked successfully", Toast.LENGTH_SHORT).show();
                    }
                });
        refElderlyUser.child("linked").setValue(false);
        refElderlyUser.child("emergency-contacts").child("1").removeValue();
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
                    Toast.makeText(ManageProfile.this, "Password is required", Toast.LENGTH_SHORT).show();
                    return;
                }

                AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);
                user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(ManageProfile.this, "Authentication successful.", Toast.LENGTH_SHORT).show();
                            showUpdateEmailPasswordDialog();
                            dialog.dismiss();
                        } else {
                            Toast.makeText(ManageProfile.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(e -> {
                    Toast.makeText(ManageProfile.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(ManageProfile.this, "All fields are required", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!password.equals(reenteredPassword)) {
                    Toast.makeText(ManageProfile.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
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
                                            Toast.makeText(ManageProfile.this, "Email updated. Verification email sent to " + email, Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(ManageProfile.this, "Failed to update password.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            } else {
                                Toast.makeText(ManageProfile.this, "Failed to update email: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(e -> {
                        Toast.makeText(ManageProfile.this, "Update email failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        e.printStackTrace(); // Log the error
                    });
                } else {
                    Toast.makeText(ManageProfile.this, "User not authenticated", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void showPhoneNumberDialog(String phoneNumber, boolean isUpdate) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_phone_number, null);
        builder.setView(dialogView);

        EditText phoneNumberEditText = dialogView.findViewById(R.id.et_phone_number_addphonenumber);
        Button actionButton = dialogView.findViewById(R.id.btn_addupdate_addphonenumber);
        Button cancelButton = dialogView.findViewById(R.id.btn_cancel_addphonenumber);

        phoneNumberEditText.setText(phoneNumber);

        if (isUpdate) {
            actionButton.setText("Update");
        }

        AlertDialog dialog = builder.create();
        dialog.show();

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newPhoneNumber = phoneNumberEditText.getText().toString();
                if(isValidPhoneNumber(newPhoneNumber)){
                    if (isUpdate) {
                        // Update the phone number in the database
                        updatePhoneNumberInDatabase(newPhoneNumber);
                    } else {
                        // Add the new phone number to the database
                        addPhoneNumberToDatabase(newPhoneNumber);
                    }
                } else{
                    Toast.makeText(ManageProfile.this, "Incorrect phone number format.", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();

            }
        });

    }

    private void updatePhoneNumberInDatabase(String newPhoneNumber) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("user").child(uid);
        userRef.child("phone-number").setValue(newPhoneNumber)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(ManageProfile.this, "Phone number updated successfully", Toast.LENGTH_SHORT).show();
                            updateElderlyEmergencyContacts(uid, newPhoneNumber);
                        } else {
                            Toast.makeText(ManageProfile.this, "Failed to update phone number", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void addPhoneNumberToDatabase(String newPhoneNumber) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("user").child(uid);
        userRef.child("phone-number").setValue(newPhoneNumber)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(ManageProfile.this, "Phone number added successfully", Toast.LENGTH_SHORT).show();
                            updateElderlyEmergencyContacts(uid, newPhoneNumber);
                        } else {
                            Toast.makeText(ManageProfile.this, "Failed to add phone number", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void showAddOrUpdatePhoneNumberDialog() {
        // Query the database to check if a phone number exists
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("user").child(uid);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.hasChild("phone-number")&& !TextUtils.isEmpty(snapshot.child("phone-number").getValue(String.class))) {
                        // Phone number exists, set it to the EditText
                        String phoneNumber = snapshot.child("phone-number").getValue(String.class);
                        showPhoneNumberDialog(phoneNumber, true); // true indicates it's an update
                    } else {
                        // No phone number exists, show empty dialog
                        showPhoneNumberDialog("", false); // false indicates it's a new addition
                    }
                } else {
                    // User data not found
                    Toast.makeText(ManageProfile.this, "User data not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Toast.makeText(ManageProfile.this, "Failed to read user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateElderlyEmergencyContacts(String uid, String newPhoneNumber){
        DatabaseReference linkedElderlyRef = FirebaseDatabase.getInstance().getReference("user").child(uid).child("linked-elderly");
        linkedElderlyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String elderlyUid = snapshot.getKey();
                    if (elderlyUid != null) {
                        DatabaseReference elderlyRef = FirebaseDatabase.getInstance().getReference("user").child(elderlyUid);
                        elderlyRef.child("emergency-contacts").child("1").setValue(newPhoneNumber);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ManageProfile.this, "Failed to update emergency contacts", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
