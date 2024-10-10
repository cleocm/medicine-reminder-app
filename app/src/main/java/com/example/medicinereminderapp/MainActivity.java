package com.example.medicinereminderapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Check if the user is logged in
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Navigate to the appropriate activity
        if (currentUser == null) {
            // User is not logged in, open the login activity*/
            Intent intent = new Intent(this, Login.class);
            startActivity(intent);
        } else {
            // User is logged in, open the main activity
            FirebaseDatabase database = FirebaseDatabase.getInstance("https://medicinereminderapp-fca68-default-rtdb.asia-southeast1.firebasedatabase.app/");
            DatabaseReference usersRef = database.getReference("user");
            DatabaseReference userRef = usersRef.child(currentUser.getUid());

            //read user role
            userRef.child("role").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()){
                        // Get the role value
                        String role = snapshot.getValue(String.class);
                        //move to main menu according to their user role
                        if (role.equals("elderly")){
                            Intent intentElderlyMainMenu = new Intent(MainActivity.this, MainMenuElderly.class);
                            startActivity(intentElderlyMainMenu);
                        } else {
                            Intent intentCaretakerMainMenu = new Intent(MainActivity.this, MainMenuCaretaker.class);
                            startActivity(intentCaretakerMainMenu);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle the error

                }
            });
        }


    }
}