package com.example.fitnessapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import fitnessapp_objects.ChallengeRoom;
import fitnessapp_objects.ChallengeRoomModel;
import fitnessapp_objects.Database;

/**
 * this class represents the screen for views all the participated challenges
 */
public class MyChallengesActivity extends AppCompatActivity implements Database.OnRoomGetCompletionHandler, AdapterView.OnItemClickListener {

    private ListView myChallengesLV;
    private ArrayList<ChallengeRoomModel> challengeRoomModelArrayList;
    private ChallengeRoomLVAdapter adapter;
    private Database db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_challenges);

        myChallengesLV = (ListView) findViewById(R.id.my_chall_lv);

        challengeRoomModelArrayList = new ArrayList<>();
        adapter = new ChallengeRoomLVAdapter(this, challengeRoomModelArrayList);
        myChallengesLV.setAdapter(adapter);

        myChallengesLV.setOnItemClickListener(this);
        db = Database.getInstance();
        db.getMyChallenges(this);

    }

    /**
     * reload my challenges
     */
    public void reload(View view) {

        db.getMyChallenges(this);

    }

    @Override
    public void challengeRoomsTransfer(Map<String, ChallengeRoom> rooms) {

        challengeRoomModelArrayList.clear();

        for (Map.Entry<String, ChallengeRoom> entry : rooms.entrySet()) {

            ChallengeRoom room = entry.getValue();

            ChallengeRoomModel model = new ChallengeRoomModel(entry.getKey(), room.getDescription(), room.getName(), room.getType(), room.isBet(), room.getBetAmount(), room.getEndDate(), room.getPassword(), room.isStarted());
            challengeRoomModelArrayList.add(model);

        }

        adapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        ChallengeRoomModel challengeRoomModel = challengeRoomModelArrayList.get(position);
        Intent intent;
        if (!challengeRoomModel.isStarted()) {
            intent = new Intent(this, ChallengeEndActivity.class);
        } else {
            intent = new Intent(this, ChallengeLobbyActivity.class);
        }
        intent.putExtra("endDate", challengeRoomModel.getEndDate().toDate().getTime());
        intent.putExtra("challengeInfo", challengeRoomModel.getChallengeInfoMap());
        startActivity(intent);
    }
}