package com.example.fitnessapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

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

    TextView roomNameTV;
    GridView participants_view;
    ArrayList<ParticipantModel> participantModelArrayList;
    ParticipantGVAdapter adapter;
    String roomID;
    HashMap<String,String> challengeInfo;
    Database db;
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

        Intent intent = new Intent(this, GoogleFitChallenges.class);
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


}