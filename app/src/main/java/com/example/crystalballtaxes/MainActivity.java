package com.example.crystalballtaxes;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHelper(this);
        Button logoutBtn = findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
            finish();
        });

        Button startBtn = findViewById(R.id.startBtn);
        try {
            //ensure the user id is passed from each activity from the start
            startBtn.setOnClickListener(view -> {
                Log.d("MainActivity", "Starting FilingStatusActivity");
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null && currentUser.getEmail() != null) {
                    long userId = db.getUserIdFromEmail(currentUser.getEmail());
                    if (userId != -1) {
                        db.initializeTaxRecord(userId);
                        Intent i = new Intent(this, FilingStatusActivity.class);
                        i.putExtra("USER_ID", userId);
                        startActivity(i);
                    } else {
                        Toast.makeText(this, "Error: User not found in database",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Error: Please log in again",
                            Toast.LENGTH_SHORT).show();
                    //redirect to login
                    Intent i = new Intent(this, LoginActivity.class);
                    startActivity(i);
                    finish();
                }
            });
        }catch (Exception e){
            Log.e("MainActivity", "Error starting FilingStatusActivity", e);
        }

    }

}