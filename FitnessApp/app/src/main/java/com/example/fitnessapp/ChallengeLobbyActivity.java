package com.example.fitnessapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.GridView;

import java.util.ArrayList;

import fitnessapp_objects.Database;
import fitnessapp_objects.Participant;
import fitnessapp_objects.ParticipantModel;

public class ChallengeLobbyActivity extends AppCompatActivity implements Database.OnRoomChangeListener {


    GridView participants_view;
    ArrayList<ParticipantModel> participantModelArrayList;
    ParticipantGVAdapter adapter;
    String roomID;
    Database db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge_lobby);

        participants_view = (GridView) findViewById(R.id.participant_grid);

        participantModelArrayList = new ArrayList<>();

        adapter = new ParticipantGVAdapter(this, participantModelArrayList);
        participants_view.setAdapter(adapter);

        roomID = getIntent().getStringExtra("roomID");
        db = Database.getInstance();
        db.startChallengeRoomChangeListener(roomID, this);

    }

    @Override
    protected void onDestroy() {
        db.detachChallengeRoomListener();

        super.onDestroy();
    }

    public void addParticipant(ArrayList<Participant> participants){

        for(Participant p: participants){

            boolean found = false;
            for(ParticipantModel pm: participantModelArrayList){

                if(pm.getId().equals(p.getId())){
                    found = true;
                    break;
                }

            }

            if(!found){
                participantModelArrayList.add(new ParticipantModel(p.getName(),p.getId()));
            }

        }

        adapter.notifyDataSetChanged();


    }

    public void removeParticipant(ArrayList<Participant> participants){



    }

    public void start(View view){

//        participantModelArrayList.add(new ParticipantModel("Henry3"));
//
//        adapter.notifyDataSetChanged();


    }




}