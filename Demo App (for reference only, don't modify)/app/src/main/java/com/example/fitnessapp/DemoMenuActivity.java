package com.example.fitnessapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DemoMenuActivity extends AppCompatActivity {

    TextView greetTV;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_menu);
        mAuth = FirebaseAuth.getInstance();
        greetTV = (TextView) findViewById(R.id.greetTV);
        showGreeting();

    }

    private void showGreeting(){


        FirebaseUser currentUser = mAuth.getCurrentUser();

        if( currentUser!=null){
            greetTV.setText("HELLO " +  currentUser.getDisplayName());
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

    public void signOut(View view){

        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LobbyActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder a = new AlertDialog.Builder(this);
        a.setMessage("Do you want to sign out?");
        a.setPositiveButton("Yes, sign out.", (dialog, which) -> {
            signOut(findViewById(R.id.signOutBTN));
        });
        a.setNegativeButton("Nope      ", (dialog, which) -> {

        });
        a.show();
    }
}