package com.example.fitnessapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class DemoMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_menu);
    }



    public void goToGoogleFitDemo(View view){

            Intent intent = new Intent(this, GoogleFitActivity.class);
            startActivity(intent);

    }

    public void goToStripeDemo(View view){

        Intent intent = new Intent(this, StripeActivity.class);
        startActivity(intent);

    }
}