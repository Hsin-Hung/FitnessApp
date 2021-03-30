package com.example.fitnessapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;

import java.util.concurrent.atomic.AtomicInteger;

public class GoogleFitActivity extends AppCompatActivity {

    private final int  REQUEST_CODE = 1;
    private static final int SIGN_IN_REQUEST_CODE = 2;
    private TextView testingTV;
    private FitnessOptions fitnessOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        testingTV = (TextView) findViewById(R.id.testingTV);

        fitnessOptions =
                FitnessOptions.builder()
                        .addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                        .build();

    }

    @Override
    protected void onDestroy() {
        Fitness.getRecordingClient(this, GoogleSignIn.getAccountForExtension(this, fitnessOptions))
                // This example shows unsubscribing from a DataType. A DataSource should be used where the
                // subscription was to a DataSource. Alternatively, a Subscription object can be used.
                .unsubscribe(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener(res->{
                    Toast.makeText(this, "UNSUBSCRIBED !", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> {
                    Toast.makeText(this, "FAILED TO UNSUBSCRIBE !", Toast.LENGTH_SHORT).show();
                });

        super.onDestroy();
    }




    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                    recordClientReg();

                }  else {
                    // Explain to the user that the feature is unavailable because
                    // the features requires a permission that the user has denied.
                    // At the same time, respect the user's decision. Don't link to
                    // system settings in an effort to convince the user to change
                    // their decision.

                    Toast.makeText(this, "ACTIVITY_RECOGNITION permission is not granted !", Toast.LENGTH_SHORT).show();
                }
                return;
        }

    }


    // request background collection of by subscribing to fitness data
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void googleFitRegister(View view){

        if (oAuthPermissionsApproved()) {
            start();
        } else {
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

            if(shouldShowRequestPermissionRationale(Manifest.permission.ACTIVITY_RECOGNITION)){

                Toast.makeText(this, "ACTIVITY_RECOGNITION permission is needed to use this app !", Toast.LENGTH_SHORT).show();

            }

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                    REQUEST_CODE);
        }

    }




    private boolean oAuthPermissionsApproved() {
        return GoogleSignIn.hasPermissions(getGoogleAccount(), fitnessOptions);
    }

    private GoogleSignInAccount getGoogleAccount() {
        return GoogleSignIn.getAccountForExtension(
                this, fitnessOptions);
    }





    public void recordClientReg(){

        Fitness.getRecordingClient(this, getGoogleAccount())
                .subscribe(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                .addOnSuccessListener(sub ->{

                    Toast.makeText(this, "SUCCESSFULLY SUBSCRIBED !", Toast.LENGTH_SHORT).show();

                }).addOnFailureListener(fail -> {

            Toast.makeText(this, "FAILED TO SUBSCRIBED !", Toast.LENGTH_SHORT).show();

                 });



    }

    public void showDailySteps(View view){

        AtomicInteger total = new AtomicInteger();
        Fitness.getHistoryClient(this, getGoogleAccount())
                .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener(dataSet -> {
                    if (!dataSet.isEmpty())
                        total.set(Integer.parseInt(dataSet.getDataPoints()
                                .get(0).getValue(Field.FIELD_STEPS).toString()));

                    testingTV.setText("Total steps: "+ total.toString());

                })
                .addOnFailureListener(e -> {
                    testingTV.setText("There was a problem getting the step count.");
                });

    }

}