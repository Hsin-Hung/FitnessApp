package com.example.fitnessapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TableLayout;

import java.util.ArrayList;

import fitnessapp_objects.ParticipantModel;

public class ChallengeLobbyActivity extends AppCompatActivity {


    GridView participants_view;
    ArrayList<ParticipantModel> participantModelArrayList;
    ParticipantGVAdapter adapter;
    String roomID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge_lobby);

        participants_view = (GridView) findViewById(R.id.participant_grid);

        participantModelArrayList = new ArrayList<>();
        participantModelArrayList.add(new ParticipantModel("Henry"));
        participantModelArrayList.add(new ParticipantModel("Henry2"));
        participantModelArrayList.add(new ParticipantModel("Henry3"));

        adapter = new ParticipantGVAdapter(this, participantModelArrayList);
        participants_view.setAdapter(adapter);

        roomID = getIntent().getStringExtra("roomID");

    }

    public void start(View view){

        participantModelArrayList.add(new ParticipantModel("Henry3"));

        adapter.notifyDataSetChanged();


    }




}