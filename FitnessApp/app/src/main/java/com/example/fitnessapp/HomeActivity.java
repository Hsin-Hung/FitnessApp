package com.example.fitnessapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

import fitnessapp_objects.UserAccount;

public class HomeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    Button settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();

        System.out.println(UserAccount.getInstance().getEmail());
        System.out.println(UserAccount.getInstance().getName());

        settings = (Button) findViewById(R.id.btn_settings);

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
            }
        });
    }

    public void createChallenge(View view){

        Intent intent = new Intent(this, CreateChallengePresetActivity.class);
        startActivity(intent);

    }

    public void joinChallenge(View view){

        Intent intent = new Intent(this, JoinChallengeActivity.class);
        startActivity(intent);

    }

    public void myChallenges(View view){

        Intent intent = new Intent(this, MyChallengesActivity.class);
        startActivity(intent);

    }



    public void signOut(View view){

        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LaunchActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

    }

    @Override
    public void onBackPressed() {
        //do nothing
    }
}