package com.example.medicinereminderapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignUp extends AppCompatActivity {
    RadioButton rbElderly, rbCaretaker;
    RadioGroup rbgroup;

    EditText email, password, passwordcheck, elderlyuid1, elderlyuid2, phonenumber;

    Button btnSignUp;
    FirebaseAuth mAuth;
    private static final String PHONE_REGEX = "^01\\d{8,9}$";
    LinearLayout llCaretakerExtras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();

        email = findViewById(R.id.et_email_signup);
        password = findViewById(R.id.et_password_signup);
        passwordcheck = findViewById(R.id.et_passwordcheck_signup);
        btnSignUp = findViewById(R.id.btn_next);
        rbElderly = findViewById(R.id.rb_elderly);
        rbCaretaker = findViewById(R.id.rb_caretaker);
        rbgroup = findViewById(R.id.rbgroup);
        llCaretakerExtras = findViewById(R.id.ll_caretaker_extras);
        elderlyuid1 = findViewById(R.id.et_elderly_uid_1);
        elderlyuid2 = findViewById(R.id.et_elderly_uid_2);
        phonenumber = findViewById(R.id.et_phone_number_signup);

        llCaretakerExtras.setVisibility(View.GONE);

        rbgroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                Boolean isCaretaker = radioGroup.getCheckedRadioButtonId() == rbCaretaker.getId();
                if (isCaretaker) {
                    llCaretakerExtras.setVisibility(View.VISIBLE);
                } else {
                    llCaretakerExtras.setVisibility(View.GONE);
                }
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean isValid = checkValid();
                if (isValid) {
                    mAuth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        mAuth.signInWithEmailAndPassword(email.getText().toString(),password.getText().toString());
                                        FirebaseUser user = task.getResult().getUser();

                                        try{
                                            if (rbElderly.isChecked()) {
                                                insertIntoDatabase(user.getUid(), "elderly");
                                            }
                                            if (rbCaretaker.isChecked()) {
                                                insertIntoDatabase(user.getUid(), "caretaker");
                                            }
                                        }catch (Exception e){
                                            Toast.makeText(SignUp.this, "Database error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }

                                        Toast.makeText(getApplicationContext(),
                                                        "Registration was successful!",
                                                        Toast.LENGTH_SHORT)
                                                .show();

                                        user.sendEmailVerification();
                                        mAuth.signOut();
                                        showCompleteRegistrationDialog();


                                    } else {
                                        Exception regError = task.getException();
                                        if (regError != null) {
                                            Toast.makeText(getApplicationContext(),
                                                            "Registration failed: " + regError.getMessage(),
                                                            Toast.LENGTH_SHORT)
                                                    .show();
                                        }
                                    }
                                }
                            });
                }
            }
        });

    }
    private void showCompleteRegistrationDialog() {
        String titleText = "Registration Successful!";
        SpannableString spannableTitle = new SpannableString(titleText);
        spannableTitle.setSpan(new StyleSpan(Typeface.BOLD), 0, titleText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        String messageText = "Account has successfully been registered.\n\nA verification link has been sent to your email.";
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(spannableTitle)
                .setMessage(messageText)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                        mAuth.signOut();

                        Intent intent
                                = new Intent(SignUp.this,
                                Login.class);
                        startActivity(intent);
                    }
                });
        AlertDialog alert = builder.create();
        alert.setCanceledOnTouchOutside(false);
        alert.setCancelable(false);
        alert.show();
    }

    private void insertIntoDatabase(String userId, String role) {
        DatabaseReference reference = FirebaseDatabase.getInstance("https://medicinereminderapp-fca68-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("user").child(userId);
        reference.child("role").setValue(role);
        reference.child("verified").setValue(false);

        if (role.equals("elderly")) {
            reference.child("linked").setValue(false);
        }

        if (role.equals("caretaker")) {
            reference.child("phone-number").setValue(phonenumber.getText().toString().trim());
            String uidElderly1 = elderlyuid1.getText().toString().trim();
            if (!uidElderly1.matches("")) {
                linkElderly(userId, uidElderly1,1);

            }
            String uidElderly2 = elderlyuid2.getText().toString().trim();
            if (!uidElderly2.matches("")) {
                linkElderly(userId, uidElderly2,2);
            }
        }
    }

    Boolean elderlyUIDValidity=false;

    private void checkElderlyUid(EditText elderlyUid) {
        //check if user that is to be linked has the right role
        String uidElderly = elderlyUid.getText().toString().trim();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("user").child(uidElderly);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String role = snapshot.child("role").getValue(String.class);
                    Boolean linked = snapshot.child("linked").getValue(Boolean.class);
                    if ("elderly".equals(role)) {
                        //if role is elderly, check if user has been linked to other users.
                        if (linked) {
                            elderlyUIDValidity=false;
                            elderlyUid.setError("User already linked.");
                        } else {
                            //good to go
                            elderlyUIDValidity=true;
                        }
                    } else {
                        elderlyUIDValidity=false;
                        elderlyUid.setError("UID does not belong to an elderly user.");
                    }
                } else {
                    elderlyUIDValidity=false;
                    elderlyUid.setError("Invalid UID.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SignUp.this, "Database error: " + error.getMessage() + ". Elderly UID info not read.", Toast.LENGTH_SHORT).show();
                elderlyUIDValidity=false;
                elderlyUid.setError("Try Again.");
            }
        });
    }

    private void linkElderly(String uid, String elderlyUid, int editTextIndex) {
        DatabaseReference refCaretaker = FirebaseDatabase.getInstance("https://medicinereminderapp-fca68-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("user").child(uid).child("linked-elderly").child(elderlyUid);
        switch(editTextIndex) {
            case 1:
                refCaretaker.setValue("Linked Elderly 1");
                break;
            case 2:
                refCaretaker.setValue("Linked Elderly 2");
                break;
        }
        DatabaseReference refElderlyUser = FirebaseDatabase.getInstance("https://medicinereminderapp-fca68-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("user").child(elderlyUid);
        refElderlyUser.child("linked").setValue(true);
        refElderlyUser.child("emergency-contacts").child("1").setValue(phonenumber.getText().toString().trim());
    }

    private Boolean checkValid() {
        Boolean isValid = true;
        if (email.getText().toString().trim().isEmpty()) {
            email.setError("Email is required");
            isValid = false;
        }
        if (password.getText().toString().trim().isEmpty()) {
            password.setError("Password is required");
            isValid = false;
        }
        if (passwordcheck.getText().toString().trim().isEmpty()) {
            passwordcheck.setError("Please reenter password.");
            isValid = false;
        }
        if (!password.getText().toString().equals(passwordcheck.getText().toString())) {
            passwordcheck.setError("Password does not match.");
            isValid = false;
        }

        if (rbgroup.getCheckedRadioButtonId() == -1) {
            rbCaretaker.setError("Please select a role.");
            rbElderly.setError("Please select a role.");
            isValid = false;
        }
        if (rbCaretaker.isChecked()) {
            if (phonenumber.getText().toString().trim().isEmpty()||phonenumber.getText().toString().trim()=="") {
                phonenumber.setError("Phone number is required.");
                isValid = false;
            }else if (!isValidPhoneNumber(phonenumber.getText().toString().trim())) {
                phonenumber.setError("Incorrect phone number format.");
                isValid = false;
            }
            if (!elderlyuid1.getText().toString().trim().isEmpty()) {
                checkElderlyUid(elderlyuid1);
                if (!elderlyUIDValidity) {
                    isValid = false;
                }
            }
            if (!elderlyuid2.getText().toString().trim().isEmpty()) {
                checkElderlyUid(elderlyuid2);
                if (!elderlyUIDValidity) {
                    isValid = false;
                }
            }
        }
        return isValid;
    }

    public static boolean isValidPhoneNumber(String phoneNumber) {
        Pattern pattern = Pattern.compile(PHONE_REGEX);
        Matcher matcher = pattern.matcher(phoneNumber);
        return matcher.matches();
    }
}