package com.example.fitnessapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class LaunchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
    }

    public void signIn(View view){

        Intent intent = new Intent(this, LogInActivity.class);
        startActivity(intent);

    }

    public void signUp(View view){

        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);

    }
}