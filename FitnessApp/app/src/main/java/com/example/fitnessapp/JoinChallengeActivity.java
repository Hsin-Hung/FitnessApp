package com.example.fitnessapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SearchView;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Date;

import fitnessapp_objects.ChallengeRoom;
import fitnessapp_objects.ChallengeRoomModel;
import fitnessapp_objects.ChallengeType;
import fitnessapp_objects.Database;
import fitnessapp_objects.FirestoreCompletionHandler;

public class JoinChallengeActivity extends AppCompatActivity implements FirestoreCompletionHandler {

    SearchView challengeSV;
    ListView challengesLV;
    ArrayList<ChallengeRoomModel> challengeRoomModelArrayList;
    ChallengeRoomLVAdapter adapter;
    Database db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_challenge);

        challengeSV = (SearchView) findViewById(R.id.chall_room_searchView);
        challengesLV = (ListView) findViewById(R.id.rooms_view);

        challengeRoomModelArrayList = new ArrayList<>();

        adapter = new ChallengeRoomLVAdapter(this, challengeRoomModelArrayList);

        challengesLV.setAdapter(adapter);

        db = Database.getInstance();


    }

    public void randomChallenges(View view){

        db.getPendingChallengeRooms(this);

    }

    @Override
    public void challengeRoomsTransfer(ArrayList<ChallengeRoom> rooms) {

        for(ChallengeRoom room : rooms){

            ChallengeRoomModel model = new ChallengeRoomModel(room.getName(),room.getType(),room.isBet(), room.getBetAmount(),room.getEndDate());
            challengeRoomModelArrayList.add(model);

        }

        adapter.notifyDataSetChanged();

    }
}