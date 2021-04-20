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
import androidx.work.WorkManager;

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
import com.google.firebase.Timestamp;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import fitnessapp_objects.AuthPermission;
import fitnessapp_objects.ChallengeEndDateWork;
import fitnessapp_objects.ChallengeStats;
import fitnessapp_objects.ChallengeType;
import fitnessapp_objects.Database;
import fitnessapp_objects.DistanceChallengePeriodicWork;
import fitnessapp_objects.ParticipantModel;
import fitnessapp_objects.WeightChallengePeriodicWork;
import fitnessapp_objects.WorkManagerAPI;

public class WeightChallengeActivity extends AppCompatActivity implements Database.OnLeaderBoardStatsGetCompletionHandler, Database.UIUpdateCompletionHandler {

    private final String TAG = "WeightChallActivity";
    private final int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1;

    private GoogleSignInOptionsExtension fitnessOptions;
    private GoogleSignInAccount googleSigninAccount;
    private RecordingClient recordingClient;
    private HashMap<String,String> challengeInfo;
    private long endDate;
    private TextView myWeightTV, challTypeTV;
    private ListView leaderBoardLV;
    private String roomID;
    private ArrayList<ParticipantModel> participantModels;
    private LeaderBoardParticipantLVAdapter adapter;
    private Button refreshBTN, periodBTN, endBTN;
    private Database db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge);

        myWeightTV = (TextView) findViewById(R.id.my_stats_tv);
        leaderBoardLV = (ListView) findViewById(R.id.leaderboard_lv);
        challTypeTV = (TextView) findViewById(R.id.chall_type_title_tv);
        refreshBTN = (Button) findViewById(R.id.refresh_lb_btn);
        periodBTN = (Button) findViewById(R.id.period_btn);
        endBTN = (Button) findViewById(R.id.end_btn);

        challTypeTV.setText(getString(R.string.weight));

        participantModels = new ArrayList<>();
        adapter = new LeaderBoardParticipantLVAdapter(this, participantModels);
        leaderBoardLV.setAdapter(adapter);

        myWeightTV.setText("0");

        //noinspection unchecked
        challengeInfo = (HashMap<String,String>) getIntent().getSerializableExtra("challengeInfo");
        endDate = getIntent().getLongExtra("endDate",0);
        roomID = challengeInfo.get("roomID");

        fitnessOptions = AuthPermission.getInstance().getFitnessOption();

        googleSigninAccount = GoogleSignIn.getAccountForExtension(this, fitnessOptions);

        db = Database.getInstance();


        refreshBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.getLeaderBoardStats(roomID, WeightChallengeActivity.this);
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
            db.getLeaderBoardStats(roomID, this);
        }

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

        startWeightChangeListener();
        db.getLeaderBoardStats(roomID, this);
//        startPeriodicDistanceUpdateTask();
//        startEndDateNotifyTask();
        WorkManagerAPI.getInstance().viewAllWork(WorkManager.getInstance(this), roomID);
    }

    public void startWeightChangeListener(){

        db.startStatsChangeListener(roomID, ChallengeType.WEIGHTLOSS, this);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == Activity.RESULT_OK){

            switch (requestCode){

                case GOOGLE_FIT_PERMISSIONS_REQUEST_CODE:
                    System.out.println(" Successfully granted permissions !");
                    initTask();
//                    Toast.makeText(HomeActivity.this, " Successfully granted permissions !",
//                            Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(WeightChallengeActivity.this, "unknown request code !",
                            Toast.LENGTH_SHORT).show();
            }

        }

    }

    @Override
    public void statsTransfer(ArrayList<ChallengeStats> stats) {
        participantModels.clear();

        for(ChallengeStats challengeStats: stats){

            ParticipantModel participantModel = new ParticipantModel(challengeStats.getName(), challengeStats.getId(), ChallengeType.WEIGHTLOSS, challengeStats.getWeight());
            participantModel.setInitWeight(challengeStats.getInitWeight());
            participantModels.add(participantModel);

        }

        Collections.sort(participantModels, new Comparator<ParticipantModel>() {
            @Override
            public int compare(ParticipantModel o1, ParticipantModel o2) {
                if(o1.getWeightDiff() > o2.getWeightDiff())return -1;
                return 1;
            }
        });

        adapter.notifyDataSetChanged();
    }

    @Override
    public void updateUI(boolean isSuccess, Map<String, String> data) {
        if(isSuccess)myWeightTV.setText(data.get("weight"));
    }

    public void startPeriodicDistanceUpdateTask(){

        long currentTime = new Timestamp(new Date()).toDate().getTime();

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();


//        PeriodicWorkRequest challPeriodicWorkRequest =
//                new PeriodicWorkRequest.Builder(WeightChallengePeriodicWork.class,7, TimeUnit.DAYS)
//                        .addTag(roomID)
//                        .setConstraints(constraints)
//                        .setBackoffCriteria(BackoffPolicy.LINEAR,
//                                OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
//                                TimeUnit.MILLISECONDS)
//                        .setInputData(new Data.Builder()
//                                .putString("roomID", roomID)
//                                .build())
//                        .build();
//
//
//        WorkManager
//                .getInstance(this)
//                .enqueueUniquePeriodicWork(roomID+"-periodic", ExistingPeriodicWorkPolicy.KEEP,challPeriodicWorkRequest);

                OneTimeWorkRequest challPeriodicWorkRequest =
                new OneTimeWorkRequest.Builder(WeightChallengePeriodicWork.class)
                        .addTag(roomID)
                        .setConstraints(constraints)
                        .setBackoffCriteria(BackoffPolicy.LINEAR,
                                OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                                TimeUnit.MILLISECONDS)
                        .setInputData(new Data.Builder()
                                .putString("roomID", roomID)
                                .build())
                        .build();


        WorkManager
                .getInstance(this)
                .enqueueUniqueWork(roomID+"-periodic", ExistingWorkPolicy.KEEP,challPeriodicWorkRequest);



    }

    public void startEndDateNotifyTask(){

        long currentTime = new Timestamp(new Date()).toDate().getTime();

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest challEndDateWorkRequest =
                new OneTimeWorkRequest.Builder(ChallengeEndDateWork.class)
                        .setInitialDelay(endDate - currentTime, TimeUnit.MILLISECONDS)
                        .addTag(roomID)
                        .setConstraints(constraints)
                        .setBackoffCriteria(BackoffPolicy.LINEAR,
                                OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                                TimeUnit.MILLISECONDS)
                        .setInputData(new Data.Builder()
                                .putString("roomID", roomID)
                                .build())
                        .build();

        WorkManager
                .getInstance(this)
                .enqueueUniqueWork(roomID+"-endDate", ExistingWorkPolicy.KEEP, challEndDateWorkRequest);

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, ChallengeLobbyActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}