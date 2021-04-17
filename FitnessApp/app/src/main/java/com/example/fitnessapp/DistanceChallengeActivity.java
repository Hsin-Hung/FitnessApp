package com.example.fitnessapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptionsExtension;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessActivities;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.RecordingClient;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Session;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import fitnessapp_objects.AuthPermission;


public class DistanceChallengeActivity extends AppCompatActivity {

    final String TAG = "DistanceChallActivity";
    final int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1;
    GoogleSignInOptionsExtension fitnessOptions;
    GoogleSignInAccount googleSigninAccount;
    RecordingClient recordingClient;
    HashMap<String,String> challengeInfo;


    @SuppressLint("LongLogTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_distance_challenge);


        challengeInfo = (HashMap<String,String>) getIntent().getSerializableExtra("challengeInfo");

        fitnessOptions = AuthPermission.getInstance().getFitnessOption();

        googleSigninAccount = GoogleSignIn.getAccountForExtension(this, fitnessOptions);

        if (!GoogleSignIn.hasPermissions(googleSigninAccount, fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    this, // your activity
                    GOOGLE_FIT_PERMISSIONS_REQUEST_CODE, // e.g. 1
                    googleSigninAccount,
                    fitnessOptions);
        } else {
            //permission granted
            recordingClient = Fitness.getRecordingClient(this, googleSigninAccount);

            recordingClient
                    .subscribe(DataType.TYPE_DISTANCE_DELTA)
                    .addOnSuccessListener(unused ->
                            Log.i(TAG, "TYPE_DISTANCE_DELTA Successfully subscribed!"))
                    .addOnFailureListener( e ->
                            Log.w(TAG, "There was a problem subscribing to TYPE_DISTANCE_DELTA.", e));

            recordingClient.subscribe(DataType.AGGREGATE_DISTANCE_DELTA)
                    .addOnSuccessListener(unused ->
                            Log.i(TAG, "AGGREGATE_DISTANCE_DELTA Successfully subscribed!"))
                    .addOnFailureListener( e ->
                            Log.w(TAG, "There was a problem subscribing to AGGREGATE_DISTANCE_DELTA.", e));
            createChallengeSession();
        }
    }

    public void createChallengeSession(){

// 1. Subscribe to fitness data
// 2. Create a session object
// (provide a name, identifier, description, activity and start time)
        Session session = new Session.Builder()
                .setName(challengeInfo.get("name"))
                .setIdentifier(challengeInfo.get("roomID"))
                .setDescription(challengeInfo.get("description"))
                .setActivity(challengeInfo.get("type"))
                .setStartTime(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .build();

// 3. Use the Sessions client to start a session:
        Fitness.getSessionsClient(this, googleSigninAccount)
                .startSession(session)
                .addOnSuccessListener(unused ->
                        Log.i(TAG, "Session started successfully!"))
                .addOnFailureListener(e ->
                        Log.w(TAG, "There was an error starting the session", e));


    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("request code: " + requestCode + "result code: " + resultCode);

        if(requestCode== Activity.RESULT_OK){

            switch (requestCode){

                case GOOGLE_FIT_PERMISSIONS_REQUEST_CODE:
                    System.out.println(" Successfully granted permissions !");
//                    Toast.makeText(HomeActivity.this, " Successfully granted permissions !",
//                            Toast.LENGTH_SHORT).show();
                    createChallengeSession();
                    break;
                default:
                    Toast.makeText(DistanceChallengeActivity.this, " idk where this from!",
                            Toast.LENGTH_SHORT).show();
            }

        }

    }
}