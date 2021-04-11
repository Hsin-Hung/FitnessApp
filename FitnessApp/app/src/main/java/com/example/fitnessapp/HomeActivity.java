package com.example.fitnessapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;

import fitnessapp_objects.UserAccount;

public class HomeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();

        System.out.println(UserAccount.getInstance().getEmail());
        System.out.println(UserAccount.getInstance().getName());
    }

    public void createChallenge(View view){

        Intent intent = new Intent(this, CreateChallengePresetActivity.class);
        startActivity(intent);

    }

    public void joinChallenge(View view){

        Intent intent = new Intent(this, JoinChallengeActivity.class);
        startActivity(intent);

    }

    public void yourChallenges(View view){



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