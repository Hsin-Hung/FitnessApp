package com.example.fitnessapp;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptionsExtension;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.RecordingClient;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.Timestamp;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import fitnessapp_objects.AuthPermission;
import fitnessapp_objects.ChallengeEndDateWork;
import fitnessapp_objects.ChallengePeriodicWork;


public class DistanceChallengeActivity extends AppCompatActivity {

    final String TAG = "DistanceChallActivity";
    final int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1;
    GoogleSignInOptionsExtension fitnessOptions;
    GoogleSignInAccount googleSigninAccount;
    RecordingClient recordingClient;
    HashMap<String,String> challengeInfo;
    long endDate;
    TextView myDistanceTV;
    ListView leaderBoardLV;
    OnDataPointListener listener;

    @SuppressLint("LongLogTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_distance_challenge);

        myDistanceTV = (TextView) findViewById(R.id.my_distance_tv);
        leaderBoardLV = (ListView) findViewById(R.id.leaderboard_lv);

        myDistanceTV.setText("0");

        challengeInfo = (HashMap<String,String>) getIntent().getSerializableExtra("challengeInfo");
        endDate = getIntent().getLongExtra("endDate",0);

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
            recordingClientSub();
            startSensorClientListener();
        }
    }

    @Override
    protected void onDestroy() {
        removeSensorClientListener();
        super.onDestroy();
    }

    public void recordingClientSub(){

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

    }

    public void startSensorClientListener(){

        listener = dataPoint -> {

            float totalDistance = 0;
            for (Field field : dataPoint.getDataType().getFields()) {
                Value value = dataPoint.getValue(field);
                Log.i(TAG, "Detected DataPoint field: " + field.getName());
                Log.i(TAG, "Detected DataPoint value: " + value);

                totalDistance += value.asFloat();
            }
            myDistanceTV.setText(String.valueOf(totalDistance));

        };
        Fitness.getSensorsClient(this, GoogleSignIn.getAccountForExtension(this, fitnessOptions))
                .add(
                        new SensorRequest.Builder()
                                // for custom data sets.
                                .setDataType(DataType.AGGREGATE_DISTANCE_DELTA) // Can't be omitted.
                                .setSamplingRate(10, TimeUnit.SECONDS)
                                .build(),
                        listener
                )
                .addOnSuccessListener(unused ->
                        Log.i(TAG, "Listener registered!"))
                .addOnFailureListener(task ->
                        Log.e(TAG, "Listener not registered.", task.getCause()));
    }



    public void removeSensorClientListener(){

        if(listener==null)return;
        Fitness.getSensorsClient(this, GoogleSignIn.getAccountForExtension(this, fitnessOptions))
                .remove(listener)
                .addOnSuccessListener(unused ->
                        Log.i(TAG, "Listener was removed!"))
                .addOnFailureListener(e ->
                        Log.i(TAG, "Listener was not removed."));

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
                    recordingClientSub();
                    startSensorClientListener();
//                    Toast.makeText(HomeActivity.this, " Successfully granted permissions !",
//                            Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(DistanceChallengeActivity.this, " idk where this from!",
                            Toast.LENGTH_SHORT).show();
            }

        }

    }


    public void startPeriodicDistanceUpdateTask(View view){

        long currentTime = new Timestamp(new Date()).toDate().getTime();

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();


        PeriodicWorkRequest challPeriodicWorkRequest =
                new PeriodicWorkRequest.Builder(ChallengePeriodicWork.class,PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS, TimeUnit.MILLISECONDS)
                        .addTag(challengeInfo.get("roomID"))
                        .setConstraints(constraints)
                        .setBackoffCriteria(BackoffPolicy.LINEAR,
                                OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                                TimeUnit.MILLISECONDS)
                        .setInputData(new Data.Builder()
                                .putString("roomID", challengeInfo.get("roomID"))
                                .putLong("startDate", currentTime)
                                .build())
                        .build();


        WorkManager
                .getInstance(this)
                .enqueueUniquePeriodicWork(challengeInfo.get("roomID")+"-periodic", ExistingPeriodicWorkPolicy.KEEP,challPeriodicWorkRequest);


    }

    public void startEndDateNotifyTask(View view){

        long currentTime = new Timestamp(new Date()).toDate().getTime();

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest challEndDateWorkRequest =
                new OneTimeWorkRequest.Builder(ChallengeEndDateWork.class)
                        .setInitialDelay(endDate - currentTime, TimeUnit.MILLISECONDS)
                        .addTag(challengeInfo.get("roomID"))
                        .setConstraints(constraints)
                        .setBackoffCriteria(BackoffPolicy.LINEAR,
                                OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                                TimeUnit.MILLISECONDS)
                        .setInputData(new Data.Builder()
                                .putString("roomID", challengeInfo.get("roomID"))
                                .build())
                        .build();

        WorkManager
                .getInstance(this)
                .enqueueUniqueWork(challengeInfo.get("roomID")+"-endDate", ExistingWorkPolicy.KEEP, challEndDateWorkRequest);




    }

    public void cancelAllTask(View view){

        WorkManager
                .getInstance(this).cancelAllWorkByTag(challengeInfo.get("roomID"));


    }

    public void viewAllWork(View view){

        ListenableFuture<List<WorkInfo>> listListenableFuture = WorkManager
                .getInstance(this).getWorkInfosByTag(challengeInfo.get("roomID"));

        try {
            for(WorkInfo w: listListenableFuture.get()){

                Log.i(TAG, w.toString());

            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

}