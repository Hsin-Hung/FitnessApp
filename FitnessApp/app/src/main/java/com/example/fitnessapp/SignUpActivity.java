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
import com.google.firebase.firestore.auth.User;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import fitnessapp_objects.Database;
import fitnessapp_objects.FirestoreCompletionHandler;
import fitnessapp_objects.UserAccount;

public class SignUpActivity extends AppCompatActivity implements FirestoreCompletionHandler , View.OnClickListener {

    private final String TAG = "SignUpActivity";
    private GoogleSignInClient mGoogleSignInClient;
    private SignInButton googleSignInBTN;
    private final int RC_SIGN_IN = 1;
    private FirebaseAuth mAuth;
    private EditText usernameET, emailET, passwordET, confirmPasswordET;
    private TextView infoTV;
    private UserAccount userAccount;
    private Database db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        googleSignInBTN = (SignInButton) findViewById(R.id.sign_in_button);
        googleSignInBTN.setOnClickListener(this);

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
               .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


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

    private void googleSignIn(){
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
    startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
    // The Task returned from this call is always completed, no need to attach
    // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
           GoogleSignInAccount account = completedTask.getResult(ApiException.class);
           Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
            firebaseAuthWithGoogle(account.getIdToken());
    // Signed in successfully, show authenticated UI.
        } catch (ApiException e) {
    // The ApiException status code indicates the detailed failure reason.
    // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "Google sign in failed", e);
            updateUI(false);
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                       if (task.isSuccessful()) {
    // Sign in success, update UI with the signed-in user's information
                           Log.d(TAG, "signInWithCredential:success");

                            FirebaseUser user = mAuth.getCurrentUser();
                            boolean isNew = task.getResult().getAdditionalUserInfo().isNewUser();

                            if(isNew){
                                userAccount.setName(user.getDisplayName());
                                userAccount.setEmail(user.getEmail());
                                db.updateUserAccount(SignUpActivity.this);
                            }else{
                                db.updateLocalUserAccount(user.getUid(), SignUpActivity.this);
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            updateUI(false);
                        }
                    }
                });
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
                            updateUI(false);
                        }
                    }
                });


    }


    public void updateUI(boolean isSuccess){

        if(isSuccess){
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
        }else{
            Toast.makeText(this, "SIGN IN FAIL !", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                googleSignIn();
                break;
        }
    }

}