package com.example.fitnessapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Map;

import fitnessapp_objects.Database;
import fitnessapp_objects.UserAccount;

public class LaunchActivity extends AppCompatActivity implements View.OnClickListener, Database.UIUpdateCompletionHandler {

    private static final String TAG = "LaunchActivity";
    private final int RC_SIGN_IN = 1;

    private GoogleSignInClient mGoogleSignInClient;
    private SignInButton googleSignInBTN;
    private FirebaseAuth mAuth;
    private UserAccount userAccount;
    private Database db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        mAuth = FirebaseAuth.getInstance();
        db = Database.getInstance();
        userAccount = UserAccount.getInstance();

        googleSignInBTN = (SignInButton) findViewById(R.id.sign_in_button);
        googleSignInBTN.setOnClickListener(this);

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(Scopes.FITNESS_LOCATION_READ_WRITE))
                .requestScopes(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

    }

    /**
     * Sign in button to go to the log in screen
     */
    public void signIn(View view) {

        Intent intent = new Intent(this, LogInActivity.class);
        startActivity(intent);

    }


    /**
     * Sign up button to go to the sign up screen
     */
    public void signUp(View view) {

        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);

    }

    /**
     * SIgn in with g-mail
     */
    private void googleSignIn() {
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
            updateUI(false, null);
        }
    }

    /**
     * Authenticate with firebase with google sign in
     *
     * @param idToken: the account token to auth with firebase
     */
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

                            if (isNew) {
                                userAccount.setName(user.getDisplayName());
                                userAccount.setEmail(user.getEmail());
                                db.updateUserAccount(LaunchActivity.this);
                            } else {
                                db.updateLocalUserAccount(user.getUid(), LaunchActivity.this);
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            updateUI(false, null);
                        }
                    }
                });
    }

    /**
     * @param isSuccess: indicate whether user account is successfully updated from firebase
     * @param data:      not used here
     */
    public void updateUI(boolean isSuccess, Map<String, String> data) {

        if (isSuccess) {
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
        } else {
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