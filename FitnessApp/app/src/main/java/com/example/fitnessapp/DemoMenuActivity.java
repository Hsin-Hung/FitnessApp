package com.example.fitnessapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

public class DemoMenuActivity extends AppCompatActivity {

    TextView greetTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_menu);

        greetTV = (TextView) findViewById(R.id.greetTV);
        showGreeting();

    }

    private void showGreeting(){

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        if(acct!=null){
            greetTV.setText("HELLO " + acct.getDisplayName());
        }

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