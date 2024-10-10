package com.example.medicinereminderapp;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Login extends AppCompatActivity {

    Button registerAcc,login;
    TextView resetPass;
    FirebaseAuth mAuth;
    EditText email, password;
    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                //do nothing
            }
        };

        this.getOnBackPressedDispatcher().addCallback(this, callback);



        checkAndRequestPermissions();

        mAuth = FirebaseAuth.getInstance();

        registerAcc = findViewById(R.id.btn_signup);
        email =findViewById(R.id.et_email_login);
        password =findViewById(R.id.et_password_login);
        login = findViewById(R.id.btn_login);

        registerAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Login.this, SignUp.class);
                startActivity(intent);
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean isValid = isValid();
                if(isValid){
                    mAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = task.getResult().getUser();
                                    if (user != null) {
                                        if (!user.isEmailVerified()) {
                                            showResendVerificationDialog(user);
                                            mAuth.signOut();
                                        } else {
                                            navigateToMainMenu(user.getUid());
                                        }
                                    } else {
                                        Exception loginError = task.getException();
                                        if (loginError != null) {
                                            Log.e("Login Error", "Login failed: " + loginError.getMessage());
                                            Toast.makeText(getApplicationContext(), "Login failed: " + loginError.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                } else {
                                    Exception loginError = task.getException();
                                    if (loginError != null) {
                                        Log.e("Login Error", "Login failed: " + loginError.getMessage());
                                        Toast.makeText(getApplicationContext(), "Login failed: " + "Invalid email and/or password", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });


                } else {
                    Toast.makeText(Login.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                }
            }
        });

        resetPass = findViewById(R.id.tv_resetpass);
        resetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showResetPasswordDialog();
            }
        });

    }
    private Boolean isValid(){
        Boolean result = true;
        if(email.getText().toString().isEmpty()){
            email.setError("Email is required");
            result = false;
        }
        if(password.getText().toString().isEmpty()){
            password.setError("Password is required");
            result = false;
        }
        return result;
    }

    private void showResetPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_reset_password, null);
        builder.setView(dialogView);

        final EditText emailEditText = dialogView.findViewById(R.id.et_reset_email_resetpassword);
        Button resetPasswordButton = dialogView.findViewById(R.id.btn_reset_password_resetpassword);

        final AlertDialog dialog = builder.setTitle("Reset Password").create();

        resetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(Login.this, "Email is required", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(Login.this, "If this email is registered, a password reset email will be sent.", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(Login.this, "Failed to send reset email", Toast.LENGTH_LONG).show();
                                }
                                dialog.dismiss();
                            }
                        });
            }
        });

        dialog.show();
    }

    private void navigateToMainMenu(String userId){

        DatabaseReference userRoleRef = FirebaseDatabase.getInstance("https://medicinereminderapp-fca68-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference().child("user").child(userId).child("role");

        userRoleRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String role = snapshot.getValue(String.class);
                    switch (role) {
                        case "caretaker":
                            startActivity(new Intent(Login.this, MainMenuCaretaker.class));
                            break;
                        case "elderly":
                            startActivity(new Intent(Login.this, MainMenuElderly.class));
                            break;
                        default:
                            Toast.makeText(Login.this, "Invalid user role", Toast.LENGTH_SHORT).show();
                            break;
                    }
                } else {
                    Toast.makeText(Login.this, "User role not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Login.this, "Failed to retrieve user role", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showResendVerificationDialog(FirebaseUser user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_resend_verification_email, null);
        builder.setView(dialogView);

        Button resendEmailButton = dialogView.findViewById(R.id.resend_email_button);
        Button cancelButton = dialogView.findViewById(R.id.cancel_button);

        AlertDialog dialog = builder.setTitle("Please verify your email.").create();

        resendEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(Login.this, "Verification email sent", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(Login.this, "Failed to send verification email", Toast.LENGTH_SHORT).show();
                        }
                        dialog.dismiss();
                    }
                });
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }
    private void checkAndRequestPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(android.Manifest.permission.POST_NOTIFICATIONS);
        }
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(android.Manifest.permission.SEND_SMS);
        }

        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissionsNeeded.toArray(new String[0]),
                    PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allPermissionsGranted = true;

            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }

            if (allPermissionsGranted) {
                //proceeed as usual
            } else {
                showPermissionDeniedDialog();
            }
        }
    }

    private void showPermissionDeniedDialog() {
        String titleText = "Permissions Denied";
        SpannableString spannableTitle = new SpannableString(titleText);
        spannableTitle.setSpan(new StyleSpan(Typeface.BOLD), 0, titleText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        new AlertDialog.Builder(this)
                .setTitle(spannableTitle)
                .setMessage("Some of the core functions of the app might not work as intended without the requested permissions. \nPlease grant the permissions for the best experience.")
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

}