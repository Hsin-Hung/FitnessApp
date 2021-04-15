package com.example.fitnessapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Map;

import fitnessapp_objects.Database;
import fitnessapp_objects.UserAccount;

public class SignUpActivity extends AppCompatActivity implements Database.UIUpdateCompletionHandler {

    private final String TAG = "SignUpActivity";
    private FirebaseAuth mAuth;
    private EditText usernameET, emailET, passwordET, confirmPasswordET;
    private TextView infoTV;
    private UserAccount userAccount;
    private Database db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();

        usernameET = (EditText) findViewById(R.id.username_et);
        emailET = (EditText) findViewById(R.id.email_et);
        passwordET = (EditText) findViewById(R.id.pass_et);
        confirmPasswordET = (EditText) findViewById(R.id.confirm_pass_et);
        infoTV = (TextView) findViewById(R.id.error_tv);
        userAccount = UserAccount.getInstance();
        db = Database.getInstance();
        infoTV.setText("");


    }





    public void signUp(View view){

        infoTV.setText("");
        String email = emailET.getText().toString(),
                password = passwordET.getText().toString(),
                confirmPassword = confirmPasswordET.getText().toString(),
                username = usernameET.getText().toString();

        if(username.isEmpty()){
            infoTV.setText("Username mustn't be empty !");
            return;
        }

        if(!password.equals(confirmPassword)){
            infoTV.setText("Passwords don't match !");
            return;
        }


        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            userAccount.setUserID(user.getUid());
                            userAccount.setName(username);
                            userAccount.setEmail(email);
                            db.updateUserAccount(SignUpActivity.this);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            infoTV.setText(task.getException().toString());
                            Toast.makeText(SignUpActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(false, null);
                        }
                    }
                });


    }


    public void updateUI(boolean isSuccess, Map<String,String> data){

        if(isSuccess){
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
        }else{
            Toast.makeText(this, "SIGN IN FAIL !", Toast.LENGTH_SHORT).show();
        }

    }

}