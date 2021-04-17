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
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInOptionsExtension;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataType;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;

import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import fitnessapp_objects.ChallengeRoom;
import fitnessapp_objects.ChallengeType;
import fitnessapp_objects.Database;
import fitnessapp_objects.GoogleFitAPI;
import fitnessapp_objects.Participant;
import fitnessapp_objects.ParticipantModel;

public class ChallengeLobbyActivity extends AppCompatActivity implements Database.OnRoomChangeListener, Database.UIUpdateCompletionHandler{

    final int MY_PERMISSIONS_REQUEST_ACTIVITY = 2;
    TextView roomNameTV;
    GridView participants_view;
    ArrayList<ParticipantModel> participantModelArrayList;
    ParticipantGVAdapter adapter;
    String roomID;
    Timestamp endDate;
    HashMap<String,String> challengeInfo;
    Database db;
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge_lobby);

        roomNameTV = (TextView) findViewById(R.id.room_name_info_tv);
        participants_view = (GridView) findViewById(R.id.participant_grid);

        participantModelArrayList = new ArrayList<>();

        adapter = new ParticipantGVAdapter(this, participantModelArrayList);
        participants_view.setAdapter(adapter);
        challengeInfo = (HashMap<String,String>) getIntent().getSerializableExtra("challengeInfo");

        roomNameTV.setText(challengeInfo.get("name"));

        roomID = challengeInfo.get("roomID");
        db = Database.getInstance();
        db.startChallengeRoomChangeListener(roomID, this);
        checkSensitiveDataAccessPermission();

    }

    @Override
    protected void onDestroy() {
        db.detachChallengeRoomListener();

        super.onDestroy();
    }

    public void modifyParticipant(ArrayList<Participant> participants){

        participantModelArrayList.clear();

        for(Participant p: participants){

           participantModelArrayList.add(new ParticipantModel(p.getName(), p.getId()));

        }

        adapter.notifyDataSetChanged();


    }

    public void viewStats(View view){

        Intent intent = new Intent(this, DistanceChallengeActivity.class);
        intent.putExtra("challengeInfo", challengeInfo);
        startActivity(intent);

    }

    public void challengeInfo(View view){

        Intent intent = new Intent(this, ChallengeInfoActivity.class);
        intent.putExtra("challengeInfo", challengeInfo);
        startActivity(intent);

    }



    public void quit(View view){

        db.quitChallenge(roomID, this);

    }


    @Override
    public void updateUI(boolean isSuccess, Map<String, String> data) {

        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }


    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void checkSensitiveDataAccessPermission(){

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACTIVITY_RECOGNITION, Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACTIVITY);
        }else{
            Toast.makeText(ChallengeLobbyActivity.this, " Successfully granted permissions for ACTIVITY_RECOGNITION and ACCESS_FINE_LOCATION!",
                    Toast.LENGTH_SHORT).show();

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACTIVITY:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                    Toast.makeText(ChallengeLobbyActivity.this, " Successfully granted permissions for ACTIVITY_RECOGNITION and ACCESS_FINE_LOCATION!",
                            Toast.LENGTH_SHORT).show();

                } else {
                    // Explain to the user that the feature is unavailable because
                    // the features requires a permission that the user has denied.
                    // At the same time, respect the user's decision. Don't link to
                    // system settings in an effort to convince the user to change
                    // their decision.
                    AlertDialog.Builder a = new AlertDialog.Builder(this);
                    a.setMessage("You won't be able to do Challenges without given permission!");
//                    a.setPositiveButton("Yes, please.", (dialog, which) -> {
//                        startActivityForResult(new Intent(Settings.ACTION_PRIVACY_SETTINGS), 0);
//                    });
                    a.setNegativeButton("Okay!", (dialog, which) -> {
                        onBackPressed();
                    });
                    a.show();

                }
                return;
        }

    }


}