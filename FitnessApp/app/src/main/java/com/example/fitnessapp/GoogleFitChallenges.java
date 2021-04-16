package com.example.fitnessapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptionsExtension;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessActivities;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.data.Subscription;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class GoogleFitChallenges extends AppCompatActivity {

    private final String TAG = "GooeleFitChallenges";
    private final int REQUEST_CODE = 1, SIGN_IN_REQUEST_CODE = 2;
    GoogleSignInOptionsExtension fitnessOptions;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_fit_challenges);

        fitnessOptions =
                FitnessOptions.builder()
                        .addDataType(DataType.AGGREGATE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
                        .build();


        googleFitRegister();
    }

    public void listSubs(View view){

        Fitness.getRecordingClient(this, GoogleSignIn.getAccountForExtension(this, fitnessOptions))
                .listSubscriptions()
                .addOnSuccessListener(new OnSuccessListener<List<Subscription>>() {
                    @Override
                    public void onSuccess(List<Subscription> subscriptions) {
                        for(Subscription s:subscriptions){
                                    DataType d = s.getDataType();
                                    System.out.println(d.getName());

                        }
                    }
                });



    }



    @Override
    protected void onDestroy() {

        // FOR DEMO PURPOSES ONLY: this shows how you can unsubscribe to the recording client
        Fitness.getRecordingClient(this, GoogleSignIn.getAccountForExtension(this, fitnessOptions))
                // This example shows unsubscribing from a DataType. A DataSource should be used where the
                // subscription was to a DataSource. Alternatively, a Subscription object can be used.
                .unsubscribe(DataType.AGGREGATE_DISTANCE_DELTA)
                .addOnSuccessListener(res->{
                    Toast.makeText(this, "UNSUBSCRIBED !", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> {
            Toast.makeText(this, "FAILED TO UNSUBSCRIBE !", Toast.LENGTH_SHORT).show();
        });

        super.onDestroy();
    }




    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                    recordClientReg();

                } else {
                    // Explain to the user that the feature is unavailable because
                    // the features requires a permission that the user has denied.
                    // At the same time, respect the user's decision. Don't link to
                    // system settings in an effort to convince the user to change
                    // their decision.
                    AlertDialog.Builder a = new AlertDialog.Builder(this);
                    a.setMessage("You have already registered!");
//                    a.setPositiveButton("Yes, please.", (dialog, which) -> {
//                        startActivityForResult(new Intent(Settings.ACTION_PRIVACY_SETTINGS), 0);
//                    });
                    a.setNegativeButton("Okay!", (dialog, which) -> {

                    });
                    a.show();
                }
                return;
        }

    }


    // request background collection and tracking of step data by subscribing to fitness data
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void googleFitRegister(){

        // check if user has permission
        if (oAuthPermissionsApproved()) {
            start();
        } else {

            // if no permission, then request the permission
            GoogleSignIn.requestPermissions(this, SIGN_IN_REQUEST_CODE,
                    getGoogleAccount(), fitnessOptions);
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void start(){

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                == PackageManager.PERMISSION_GRANTED) {
            // Permission granted
            recordClientReg();

        }else{

            //Permission is needed to use this app
            if(shouldShowRequestPermissionRationale(Manifest.permission.ACTIVITY_RECOGNITION)){

                Toast.makeText(this, "ACTIVITY_RECOGNITION permission is needed to use this app !", Toast.LENGTH_SHORT).show();

            }

            // Request the permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                    REQUEST_CODE);
        }

    }



    // check if the current user is authorized to use the requested google fit api and has permission enabled
    private boolean oAuthPermissionsApproved() {
        return GoogleSignIn.hasPermissions(getGoogleAccount(), fitnessOptions);
    }

    // get the current signed in google account
    private GoogleSignInAccount getGoogleAccount() {
        return GoogleSignIn.getAccountForExtension(
                this, fitnessOptions);
    }




    // create a Recording Client and subscribe to it
    public void recordClientReg(){

        Fitness.getRecordingClient(this, getGoogleAccount())
                .subscribe(DataType.AGGREGATE_DISTANCE_DELTA)
                .addOnSuccessListener(sub ->{

                    Toast.makeText(this, "SUCCESSFULLY SUBSCRIBED !", Toast.LENGTH_SHORT).show();

                }).addOnFailureListener(fail -> {
            System.out.println(fail);
            Toast.makeText(this, "FAILED TO SUBSCRIBED !", Toast.LENGTH_SHORT).show();

        });



    }

    // Show the daily steps taken by getting the History Client and read the daily total steps
    public void showDailySteps(View view){

        AtomicInteger total = new AtomicInteger();
        Fitness.getHistoryClient(this, getGoogleAccount())
                .readDailyTotal(DataType.AGGREGATE_DISTANCE_DELTA)
                .addOnSuccessListener(dataSet -> {
                    if (!dataSet.isEmpty())
                        total.set(Integer.parseInt(dataSet.getDataPoints()
                                .get(0).getValue(Field.FIELD_DISTANCE).toString()));

                    System.out.println("Total distance: "+ total.toString());

                })
                .addOnFailureListener(e -> {
                   System.out.println("there is an error reading total distance");
                });

    }

    public void distance(View view){

        Intent intent = new Intent(this, DistanceChallengeActivity.class);
        startActivity(intent);

    }
}