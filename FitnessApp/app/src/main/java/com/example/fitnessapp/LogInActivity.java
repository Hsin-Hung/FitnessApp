package com.example.fitnessapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import fitnessapp_objects.Database;

public class LogInActivity extends AppCompatActivity{

    private static final String TAG = "MyActivity";
    private FirebaseAuth mAuth;
    EditText emailET, passET;
    Database db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        emailET = (EditText) findViewById(R.id.email_signin_et);
        passET = (EditText) findViewById(R.id.pass_signin_et);

        db = Database.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    public void updateUI(FirebaseUser user){

        if(user != null){
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
        }else{
            Toast.makeText(this, "SIGN IN FAIL !", Toast.LENGTH_SHORT).show();
        }

    }

    public void signInWithEP(View view){

        String email = emailET.getText().toString(), password = passET.getText().toString();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            db.updateLocalUserAccount(user.getUid()); //TODO - need to deal with async call back before updating UI
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LogInActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });

    }
}