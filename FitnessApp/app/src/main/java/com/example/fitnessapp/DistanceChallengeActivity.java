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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptionsExtension;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.RecordingClient;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import fitnessapp_objects.AuthPermission;
import fitnessapp_objects.ChallengeEndDateWork;
import fitnessapp_objects.DistanceChallengePeriodicWork;
import fitnessapp_objects.ChallengeStats;
import fitnessapp_objects.ChallengeType;
import fitnessapp_objects.Database;
import fitnessapp_objects.ParticipantModel;
import fitnessapp_objects.WorkManagerAPI;


public class DistanceChallengeActivity extends AppCompatActivity implements Database.OnLeaderBoardStatsGetCompletionHandler, Database.UIUpdateCompletionHandler {

    final String TAG = "DistanceChallActivity";
    final int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1;
    GoogleSignInOptionsExtension fitnessOptions;
    GoogleSignInAccount googleSigninAccount;
    RecordingClient recordingClient;
    HashMap<String,String> challengeInfo;
    long endDate;
    TextView myDistanceTV, challTypeTV;
    ListView leaderBoardLV;
    ArrayList<ParticipantModel> participantModels;
    LeaderBoardParticipantLVAdapter adapter;
    OnDataPointListener listener;
    Database db;
    Button periodBTN, endBTN;

    Button refreshBTN;
    @SuppressLint("LongLogTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge);

        //myDistanceTV shows the total distance by the user
        myDistanceTV = (TextView) findViewById(R.id.my_stats_tv);
        //current leaderboard of the distance challenge
        leaderBoardLV = (ListView) findViewById(R.id.leaderboard_lv);
        //showing challengeType - this case , it will be Distance
        challTypeTV = (TextView) findViewById(R.id.chall_type_title_tv);
        challTypeTV.setText(getString(R.string.distance));


        //leaderboard components need participant models objects
        participantModels = new ArrayList<>();
        adapter = new LeaderBoardParticipantLVAdapter(this, participantModels);
        leaderBoardLV.setAdapter(adapter);

        //set myDistance as 0
        myDistanceTV.setText("0");

        challengeInfo = (HashMap<String,String>) getIntent().getSerializableExtra("challengeInfo");
        endDate = getIntent().getLongExtra("endDate",0);

        fitnessOptions = AuthPermission.getInstance().getFitnessOption();

        googleSigninAccount = GoogleSignIn.getAccountForExtension(this, fitnessOptions);

        db = Database.getInstance();
        refreshBTN = (Button) findViewById(R.id.refresh_lb_btn);

        refreshBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.getLeaderBoardStats(challengeInfo.get("roomID"), DistanceChallengeActivity.this);
            }
        });

        if (!GoogleSignIn.hasPermissions(googleSigninAccount, fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    this, // your activity
                    GOOGLE_FIT_PERMISSIONS_REQUEST_CODE, // e.g. 1
                    googleSigninAccount,
                    fitnessOptions);
        } else {
            //permission granted
            initTask();
        }



        ////////////////

        periodBTN = (Button) findViewById(R.id.period_btn);
        endBTN = (Button) findViewById(R.id.end_btn);

        periodBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPeriodicDistanceUpdateTask();
            }
        });

        endBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startEndDateNotifyTask();
            }
        });

    }

    @Override
    protected void onDestroy() {
        db.removeStatsChangeListener();
        super.onDestroy();
    }

    public void initTask(){

        recordingClientSub();
        startDistanceChangeListener();
        db.getLeaderBoardStats(challengeInfo.get("roomID"), this);
//        startPeriodicDistanceUpdateTask();
//        startEndDateNotifyTask();
        WorkManagerAPI.getInstance().viewAllWork(WorkManager.getInstance(this), challengeInfo.get("roomID"));
        startSensorClientListener();
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

    public void startDistanceChangeListener(){
        db.startStatsChangeListener(challengeInfo.get("roomID"), ChallengeType.DISTANCE, this);
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
                    initTask();
//                    Toast.makeText(HomeActivity.this, " Successfully granted permissions !",
//                            Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(DistanceChallengeActivity.this, " idk where this from!",
                            Toast.LENGTH_SHORT).show();
            }

        }

    }


    public void startPeriodicDistanceUpdateTask(){

        long currentTime = new Timestamp(new Date()).toDate().getTime();

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

//
//        PeriodicWorkRequest challPeriodicWorkRequest =
//                new PeriodicWorkRequest.Builder(DistanceChallengePeriodicWork.class,PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS, TimeUnit.MILLISECONDS)
//                        .addTag(challengeInfo.get("roomID"))
//                        .setConstraints(constraints)
//                        .setBackoffCriteria(BackoffPolicy.LINEAR,
//                                OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
//                                TimeUnit.MILLISECONDS)
//                        .setInputData(new Data.Builder()
//                                .putString("roomID", challengeInfo.get("roomID"))
//                                .putLong("startDate", currentTime)
//                                .build())
//                        .build();
//
//
//        WorkManager
//                .getInstance(this)
//                .enqueueUniquePeriodicWork(challengeInfo.get("roomID")+"-periodic", ExistingPeriodicWorkPolicy.KEEP,challPeriodicWorkRequest);

        OneTimeWorkRequest challPeriodicWorkRequest =
                new  OneTimeWorkRequest.Builder(DistanceChallengePeriodicWork.class)
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
                .enqueueUniqueWork(challengeInfo.get("roomID")+"-periodic", ExistingWorkPolicy.KEEP,challPeriodicWorkRequest);

    }

    public void startEndDateNotifyTask(){

        long currentTime = new Timestamp(new Date()).toDate().getTime();

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest challEndDateWorkRequest =
                new OneTimeWorkRequest.Builder(ChallengeEndDateWork.class)
//                        .setInitialDelay(endDate - currentTime, TimeUnit.MILLISECONDS)
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

    @Override
    public void statsTransfer(ArrayList<ChallengeStats> stats) {
        participantModels.clear();

        for(ChallengeStats challengeStats: stats){

            participantModels.add(new ParticipantModel(challengeStats.getName(), challengeStats.getId(), challengeStats.getDistance(), ChallengeType.DISTANCE));

        }

        //sort participant models in order  for the leaderboard
        Collections.sort(participantModels, new Comparator<ParticipantModel>() {
            @Override
            public int compare(ParticipantModel o1, ParticipantModel o2) {
                if(o1.getDistance() > o2.getDistance())return -1;
                return 1;
            }
        });

        adapter.notifyDataSetChanged();
    }

    @Override
    public void updateUI(boolean isSuccess, Map<String, String> data) {
        if(isSuccess){
            myDistanceTV.setText(data.get("distance"));
            db.getLeaderBoardStats(challengeInfo.get("roomID"), DistanceChallengeActivity.this);
        }
    }
}